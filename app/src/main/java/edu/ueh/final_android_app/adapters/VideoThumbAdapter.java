package edu.ueh.final_android_app.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.ueh.final_android_app.HomeFragment;
import edu.ueh.final_android_app.MainActivity;
import edu.ueh.final_android_app.R;
import edu.ueh.final_android_app.models.Video;
import edu.ueh.final_android_app.util.CommonUtil;

public class VideoThumbAdapter extends RecyclerView.Adapter<VideoThumbAdapter.VideoViewHolder> {

    private List<Video> videos;
    private Context context;
    private OnVideoClickListener listener;
    private Activity activity;

    public interface OnVideoClickListener {
        void onClick(Video v);
    }

    public VideoThumbAdapter(Context context, Activity activity, List<Video> videos, OnVideoClickListener listener) {
        this.context = context;
        this.activity = activity;
        this.videos = videos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_thumb_item, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Video v = videos.get(position);
        holder.tvLikeCount.setText(String.valueOf(v.getTotalLike()));

        CommonUtil.getVideoThumbnail(context, v.getVideoUrl(), new CommonUtil.ThumbnailListener() {
            @Override
            public void onThumbnailReady(Bitmap bmp) {
                holder.imgThumb.setImageBitmap(bmp);
            }

            @Override
            public void onError(Exception e) {

            }
        });

        holder.itemView.setOnClickListener(view -> {
            if (listener != null) listener.onClick(v);
        });

        holder.itemView.setOnClickListener(view -> {
            ((MainActivity) activity).replaceFragment(new HomeFragment(v.getId()));
        });
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvLikeCount;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.imgThumb);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
        }
    }
}
