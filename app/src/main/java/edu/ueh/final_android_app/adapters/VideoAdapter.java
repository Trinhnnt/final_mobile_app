package edu.ueh.final_android_app.adapters;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

import edu.ueh.final_android_app.AccountFragment;
import edu.ueh.final_android_app.MainActivity;
import edu.ueh.final_android_app.R;
import edu.ueh.final_android_app.models.Video;
import edu.ueh.final_android_app.util.CommonUtil;
import edu.ueh.final_android_app.util.FireStorageUtil;
import edu.ueh.final_android_app.util.FirebaseUtil;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private final List<Video> videoList; // chứa id video trong res/raw
    private static Activity activity = null;

    public VideoAdapter(List<Video> videoList, Activity activity) {
        this.videoList = videoList;
        VideoAdapter.activity = activity;
    }

    private static void downloadAndCacheVideo(Context context, String url, FireStorageUtil.UploadListener listener) {
        File cacheDir = context.getExternalFilesDir("videos");
        if (cacheDir == null) {
            listener.onError(new Exception("Cache dir null"));
            return;
        }

        if (!cacheDir.exists()) cacheDir.mkdirs();
        String rawName = Uri.parse(url).getLastPathSegment();
        String safeName = rawName.replaceAll("[^a-zA-Z0-9._-]", "_");
        File outFile = new File(cacheDir, safeName);

        // Nếu đã có trong cache → trả về luôn
        if (outFile.exists()) {
            listener.onSuccess(outFile.getAbsolutePath());
            return;
        }

        FireStorageUtil fireStorageUtil = new FireStorageUtil(context);

        fireStorageUtil.downloadFile(url, outFile, new FireStorageUtil.UploadListener() {
            @Override
            public void onSuccess(String filePath) {
                listener.onSuccess(outFile.getAbsolutePath());
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
    }

    public int removeItem(Video item){
        int index = videoList.indexOf(item);
        if (index == -1) return -1;

        videoList.remove(index);
        notifyItemRemoved(index);
        return index;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        holder.bind(videoList.get(position));
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        private final TextView videoAuthor;
        private final TextView videoCaption, videoLikeCount;
        private final ImageButton likeButton, btnDelete;
        private final ImageView avatar;
        public VideoView videoView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.videoView);
            videoAuthor = itemView.findViewById(R.id.videoAuthor);
            videoCaption = itemView.findViewById(R.id.videoCaption);
            videoLikeCount = itemView.findViewById(R.id.tvLikeCount);
            likeButton = itemView.findViewById(R.id.btnLike);
            avatar = itemView.findViewById(R.id.avatar);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(Video videoRes) {
            downloadAndCacheVideo(itemView.getContext(), videoRes.getVideoUrl(), new FireStorageUtil.UploadListener() {
                @Override
                public void onSuccess(String filePath) {
                    Uri uri = Uri.fromFile(new File(filePath));
                    videoView.setVideoURI(uri);
                }

                @Override
                public void onError(Exception e) {

                }
            });
            videoAuthor.setText(videoRes.getAuthorName());
            videoCaption.setText(videoRes.getCaption());
            videoLikeCount.setText(String.valueOf(videoRes.getTotalLike()));

            videoView.setOnPreparedListener(MediaPlayer::start);
            videoView.setOnClickListener(v -> {
                if (videoView.isPlaying()) videoView.pause();
                else videoView.start();
            });

            if (!CommonUtil.isInternetAvailable(itemView.getContext())) {
                return;
            }

            boolean isLiked = videoRes.isLiked(CommonUtil.currentUser.getId());
            likeButton.setImageResource(getLikeButtonIconId(isLiked));
            likeButton.setOnClickListener(v -> {
                videoRes.toggleLike(CommonUtil.currentUser.getId());
                FirebaseUtil firebaseUtil = new FirebaseUtil();
                firebaseUtil.updateVideo(videoRes, unused -> {
                    videoLikeCount.setText(String.valueOf(videoRes.getTotalLike()));
                    likeButton.setImageResource(getLikeButtonIconId(videoRes.isLiked(CommonUtil.currentUser.getId())));
                }, e -> {
                    Toast.makeText(itemView.getContext(), "Lỗi khi like video", Toast.LENGTH_SHORT).show();
                });
            });

            if (videoRes.getAuthorAvatarUrl() != null) {
                CommonUtil.downloadAndCacheAvatar(itemView.getContext(), videoRes.getAuthorAvatarUrl(), new FireStorageUtil.UploadListener() {
                    @Override
                    public void onSuccess(String filePath) {
                        avatar.setImageURI(Uri.fromFile(new File(filePath)));
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }

            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) activity).replaceFragment(new AccountFragment(videoRes.getAuthorId()));
                }
            });

            if (!CommonUtil.currentUser.getUsername().equals("superadmin")) {
                return;
            }
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> {
                FireStorageUtil fireStorageUtil = new FireStorageUtil(itemView.getContext());

                fireStorageUtil.deleteFileByUrl(videoRes.getVideoUrl(), new FireStorageUtil.UploadListener() {
                    @Override
                    public void onSuccess(String fileUrl) {
                        deleteInCache(fileUrl);
                        FirebaseUtil firebaseUtil = new FirebaseUtil();
                        firebaseUtil.deleteVideo(videoRes, unused -> {
                            if(getBindingAdapter() != null){
                                ((VideoAdapter)getBindingAdapter()).removeItem(videoRes);
                            }
                        }, e -> {});
                    }

                    @Override
                    public void onError(Exception e) {
                        deleteInCache(videoRes.getVideoUrl());
                    }
                });
            });
        }

        private void deleteInCache(String fileId) {
            File cacheDir = itemView.getContext().getExternalFilesDir("videos");
            if (cacheDir != null && cacheDir.exists()) {
                File file = new File(cacheDir, fileId + ".mp4");
                boolean delete = file.delete();
                if (!delete) {
                    Log.e("VideoAdapter", "Error deleting file: " + file.getAbsolutePath());
                }
            }
        }

        private int getLikeButtonIconId(boolean isLiked) {
            if (isLiked) {
                return R.drawable.heart_fill;
            } else {
                return R.drawable.heart_outline;
            }
        }
    }
}

