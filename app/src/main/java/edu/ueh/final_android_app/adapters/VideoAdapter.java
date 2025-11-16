package edu.ueh.final_android_app.adapters;

import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.ueh.final_android_app.R;
import edu.ueh.final_android_app.models.Video;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private final List<Video> videoList; // chứa id video trong res/raw

    public VideoAdapter(List<Video> videoList) {
        this.videoList = videoList;
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
        public VideoView videoView;
        private TextView videoAuthor;
        private TextView videoCaption;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.videoView);
            videoAuthor = itemView.findViewById(R.id.videoAuthor);
            videoCaption = itemView.findViewById(R.id.videoCaption);
        }

        void bind(Video videoRes) {
            Uri uri = Uri.parse("android.resource://" + itemView.getContext().getPackageName() + "/" + videoRes.getVideoUrl());
            videoView.setVideoURI(uri);
            videoAuthor.setText(videoRes.getAuthorName());
            videoCaption.setText(videoRes.getCaption());

            videoView.setOnPreparedListener(MediaPlayer::start);

            videoView.setOnClickListener(v -> {
                if (videoView.isPlaying()) videoView.pause();
                else videoView.start();
            });
        }
    }
}

