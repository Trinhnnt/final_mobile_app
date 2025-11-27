package edu.ueh.final_android_app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.ueh.final_android_app.adapters.VideoAdapter;
import edu.ueh.final_android_app.models.Video;
import edu.ueh.final_android_app.util.CommonUtil;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private List<Video> videos;

    private void getVideos(){
        videos = List.of(
                new Video(1, "this is first video", R.raw.sample_video, CommonUtil.currentUser, null),
                new Video(2, "this is second video", R.raw.sample_video, CommonUtil.currentUser, null)
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ViewPager2 viewPager = view.findViewById(R.id.videoPage);
        getVideos();
        viewPager.setAdapter(new VideoAdapter(videos));
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            private int currentPage = -1;

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (currentPage != -1) {
                    RecyclerView.ViewHolder oldHolder = ((RecyclerView) viewPager.getChildAt(0)).findViewHolderForAdapterPosition(currentPage);
                    if (oldHolder instanceof VideoAdapter.VideoViewHolder) {
                        ((VideoAdapter.VideoViewHolder) oldHolder).videoView.pause();
                    }
                }
                currentPage = position;
            }
        });

        return view;
    }
}