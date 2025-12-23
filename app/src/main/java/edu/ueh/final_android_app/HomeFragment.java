package edu.ueh.final_android_app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.ueh.final_android_app.adapters.AccountSearchAdapter;
import edu.ueh.final_android_app.adapters.VideoAdapter;
import edu.ueh.final_android_app.dao.VideoDao;
import edu.ueh.final_android_app.models.Account;
import edu.ueh.final_android_app.models.Video;
import edu.ueh.final_android_app.util.CommonUtil;
import edu.ueh.final_android_app.util.FirebaseUtil;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private final List<Video> currentVideos = new ArrayList<>();
    private final List<Account> matchedUsers = new ArrayList<>();
    private VideoAdapter adapter;
    private ViewPager2 viewPager;
    private String priorityVideoId;
    private ImageButton btnToggleSearch, btnDoSearch, btnCloseSearch;
    private EditText etSearch;
    private LinearLayout searchContainer;
    private VideoDao videoDao;
    private RecyclerView rvUserSearch;
    private AccountSearchAdapter userAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    public HomeFragment(String videoId) {
        priorityVideoId = videoId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        videoDao = new VideoDao(requireContext());
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        viewPager = view.findViewById(R.id.videoPage);
        btnCloseSearch = view.findViewById(R.id.btnCloseSearch);
        btnToggleSearch = view.findViewById(R.id.btnToggleSearch);
        btnDoSearch = view.findViewById(R.id.btnDoSearch);
        searchContainer = view.findViewById(R.id.searchContainer);
        etSearch = view.findViewById(R.id.etSearch);

        // tạo adapter rỗng trước
        adapter = new VideoAdapter(currentVideos, requireActivity());
        viewPager.setAdapter(adapter);

        // load dữ liệu
        getVideos();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            private int currentPage = -1;

            @Override
            public void onPageSelected(int position) {
                if (position >= currentVideos.size()) return;
                super.onPageSelected(position);

                // pause old
                RecyclerView rv = (RecyclerView) viewPager.getChildAt(0);
                if (rv != null && currentPage >= 0 && currentPage < currentVideos.size()) {
                    RecyclerView.ViewHolder oldHolder =
                            rv.findViewHolderForAdapterPosition(currentPage);

                    if (oldHolder instanceof VideoAdapter.VideoViewHolder)
                        ((VideoAdapter.VideoViewHolder) oldHolder).videoView.pause();
                }

                currentPage = position;
                videoDao.addVideo(currentVideos.get(position));
            }
        });

        if (priorityVideoId != null) {
            viewPager.setCurrentItem(currentVideos.indexOf(currentVideos.stream().filter(video -> video.getId().equals(priorityVideoId)).findFirst().orElse(null)));
        }

        btnToggleSearch.setOnClickListener(v -> {
            searchContainer.setVisibility(View.VISIBLE);
            btnToggleSearch.setVisibility(View.GONE);
        });

        btnCloseSearch.setOnClickListener(v -> {
            searchContainer.setVisibility(View.GONE);
            btnToggleSearch.setVisibility(View.VISIBLE);
            etSearch.setText("");
        });

        btnDoSearch.setOnClickListener(v -> {
            if (etSearch.getText() == null || etSearch.getText().toString().trim().isEmpty())
                return;
            String query = etSearch.getText().toString();

            if (query.startsWith("@")) {
                String username = query.substring(1).trim();
                if (username.isEmpty()) {
                    rvUserSearch.setVisibility(View.GONE);
                    return;
                }

                loadMatchedUsers(username, users -> {
                    matchedUsers.clear();
                    matchedUsers.addAll(users);
                    userAdapter.notifyDataSetChanged();
                    rvUserSearch.setVisibility(View.VISIBLE);
                });

            } else {
                rvUserSearch.setVisibility(View.GONE);
                Video searchedVideo = currentVideos.stream().filter(video -> video.getCaption().contains(query)).findFirst().orElse(null);

                if (searchedVideo != null) {
                    viewPager.setCurrentItem(currentVideos.indexOf(searchedVideo));
                }
            }
        });

        rvUserSearch = view.findViewById(R.id.rvUserSearch);
        rvUserSearch.setLayoutManager(new LinearLayoutManager(requireContext()));
        userAdapter = new AccountSearchAdapter(matchedUsers, user -> {
            rvUserSearch.setVisibility(View.GONE);
            ((MainActivity) requireActivity()).replaceFragment(new AccountFragment(user.getId()));
        });
        rvUserSearch.setAdapter(userAdapter);
    }

    private void loadMatchedUsers(String username, OnUsersLoaded callback) {
        FirebaseUtil fb = new FirebaseUtil();
        fb.getAllUsers(users -> {
            List<Account> result = users.stream().filter(u -> u.getUsername() != null && u.getUsername().toLowerCase().contains(username.toLowerCase())).collect(Collectors.toList());
            callback.onLoaded(result);
        }, e -> {
            Log.e("HomeFragment", "Error getting users", e);
        });
    }

    private void getVideos() {
        if (CommonUtil.isInternetAvailable(requireContext())) {

            FirebaseUtil firebaseUtil = new FirebaseUtil();

            firebaseUtil.getAllVideos(new FirebaseUtil.OnVideosLoadListener() {
                @Override
                public void onSuccess(List<Video> videos) {
                    // update UI
                    currentVideos.clear();
                    currentVideos.addAll(videos);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(Exception e) {
                    Log.e("HomeFragment", "Error getting videos", e);
                }
            });

        } else {
            // OFFLINE MODE
            List<Video> cached = videoDao.getAll();

            currentVideos.clear();
            currentVideos.addAll(cached);
            adapter.notifyDataSetChanged();
        }
    }


    interface OnUsersLoaded {
        void onLoaded(List<Account> users);
    }

}
