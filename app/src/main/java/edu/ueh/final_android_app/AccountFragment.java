package edu.ueh.final_android_app;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.ueh.final_android_app.adapters.VideoThumbAdapter;
import edu.ueh.final_android_app.models.Account;
import edu.ueh.final_android_app.models.Video;
import edu.ueh.final_android_app.util.CommonUtil;
import edu.ueh.final_android_app.util.FireStorageUtil;
import edu.ueh.final_android_app.util.FirebaseUtil;

public class AccountFragment extends Fragment {
    private final List<Video> allVideo = new ArrayList<>();
    private final List<Video> videoList = new ArrayList<>();
    private TextView tvFullname;
    private TextView tvUsername;
    private TextView tvLikes;
    private TextView btnBanUnban;
    private ImageView btnLogout, avatar;
    private ActivityResultLauncher<String> pickImageLauncher;
    private File selectedAvatarFile;
    private File currentAvatarFile;
    private LinearLayout banBtnContainer;
    private RecyclerView recyclerVideos;
    private VideoThumbAdapter adapter;
    private TextView tabMy, tabLiked;
    private Account viewedUser;
    private String accountId;

    public AccountFragment() {
        // Required empty public constructor
    }

    public AccountFragment(String id) {
        accountId = id;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account, container, false);
        tvFullname = root.findViewById(R.id.fullname);
        tvUsername = root.findViewById(R.id.username);
        tvLikes = root.findViewById(R.id.likes);
        btnLogout = root.findViewById(R.id.btnLogout);
        btnBanUnban = root.findViewById(R.id.btnBan);
        avatar = root.findViewById(R.id.avatar);

        recyclerVideos = root.findViewById(R.id.recyclerVideos);
        recyclerVideos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        tabMy = root.findViewById(R.id.tabMyVideos);
        tabLiked = root.findViewById(R.id.tabLikedVideos);

        banBtnContainer = root.findViewById(R.id.banContainer);

        tvFullname.setText(CommonUtil.currentUser.getFullName());
        tvUsername.setText(String.format("@%s", CommonUtil.currentUser.getUsername()));

        addLogoutListen();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new VideoThumbAdapter(requireContext(), requireActivity(), videoList, new VideoThumbAdapter.OnVideoClickListener() {
            @Override
            public void onClick(Video v) {
                ((MainActivity) requireActivity()).replaceFragment(new HomeFragment(v.getId()));
            }
        });
        recyclerVideos.setAdapter(adapter);

        loadVideoList();

        if (accountId == null) loadCurrentUser();
        else loadOtherUser();

        tabMy.setOnClickListener(v -> {
            List<Video> myVideos = allVideo.stream().filter(video -> video.getAuthorId().equals(viewedUser.getId())).collect(Collectors.toList());

            showList(myVideos);
            setActiveTab(tabMy);
        });

        tabLiked.setOnClickListener(v -> {
            List<Video> likedVideos = allVideo.stream().filter(video -> video.isLiked(CommonUtil.currentUser.getId())).collect(Collectors.toList());

            showList(likedVideos);
            setActiveTab(tabLiked);
        });
    }

    private void loadOtherUser() {
        FirebaseUtil firebaseUtil = new FirebaseUtil();

        firebaseUtil.getUserById(accountId, new FirebaseUtil.OnUserGetListener() {
            @Override
            public void onSuccess(Account account) {
                viewedUser = account;
                // Xem profile người khác
                btnLogout.setVisibility(View.GONE);
                avatar.setOnClickListener(null); // Không cho đổi avatar
                avatar.setClickable(false);

                btnBanUnban.setVisibility(View.VISIBLE);
                setupBanUnban();

                // Lấy user theo id bạn tự implement
                tvFullname.setText(viewedUser.getFullName());
                tvUsername.setText("@" + viewedUser.getUsername());

                loadAvatar(viewedUser.getAvatarUrl());
            }

            @Override
            public void onNotFound() {
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    private void loadCurrentUser() {
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                try {
                    selectedAvatarFile = CommonUtil.uriToFile(uri, requireContext());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                uploadAvatar(); // GỌI UPDATE AVATAR Ở ĐÂY
            }
        });
        // Xem profile của chính mình
        viewedUser = CommonUtil.currentUser;

        avatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnLogout.setVisibility(View.VISIBLE);
        btnBanUnban.setVisibility(View.GONE);

        loadAvatar(viewedUser.getAvatarUrl());
    }

    private void loadAvatar(String avatarId) {
        if (avatarId == null) return;
        CommonUtil.downloadAndCacheAvatar(requireContext(), avatarId, new FireStorageUtil.UploadListener() {
            @Override
            public void onSuccess(String filePath) {
                avatar.setImageURI(Uri.fromFile(new File(filePath)));
            }

            @Override
            public void onError(Exception e) {
            }
        });
    }

    private void logoutLocal() {
        ((MainActivity) requireActivity()).onLogoutSuccess();
    }

    private void addLogoutListen() {
        btnLogout.setOnClickListener(v -> {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

            googleSignInClient.signOut().addOnCompleteListener(task -> {
                // Chuyển về màn hình login
                logoutLocal();
            });
        });
    }

    private void updateBanBtnState(boolean isBanned) {
        btnBanUnban.setBackgroundColor(getResources().getColor(isBanned ? R.color.tiktok1 : R.color.tiktok2));
        btnBanUnban.setText(isBanned ? "Open" : "Ban");
    }

    private void setupBanUnban() {
        if (!CommonUtil.currentUser.getUsername().equals("superadmin") || viewedUser == null)
            return;
        banBtnContainer.setVisibility(View.VISIBLE);
        updateBanBtnState(viewedUser.isBanned());

        btnBanUnban.setOnClickListener(v -> {
            boolean newState = !viewedUser.isBanned();
            viewedUser.setIsBanned(newState);

            FirebaseUtil firebase = new FirebaseUtil();
            firebase.updateUser(viewedUser, unused -> {
                updateBanBtnState(newState);
            }, e -> Log.e("BAN", "Failed", e));
        });
    }


    private void uploadAvatar() {
        if (selectedAvatarFile == null) return;

        FireStorageUtil fireStorageUtil = new FireStorageUtil(requireContext());
        fireStorageUtil.uploadImage(selectedAvatarFile, new FireStorageUtil.UploadListener() {
            @Override
            public void onSuccess(String fileId) {
                CommonUtil.currentUser.setAvatarUrl(fileId);
                FirebaseUtil firebaseUtil = new FirebaseUtil();
                firebaseUtil.updateUser(CommonUtil.currentUser, unused -> {
                    CommonUtil.downloadAndCacheAvatar(requireContext(), fileId, new FireStorageUtil.UploadListener() {
                        @Override
                        public void onSuccess(String filePath) {
                            currentAvatarFile = new File(filePath);
                            avatar.setImageURI(Uri.fromFile(currentAvatarFile));
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                }, e -> Log.e("Avatar", "Update failed", e));
            }

            @Override
            public void onError(Exception e) {
                Log.e("Avatar", "Upload failed", e);
            }
        });
    }

    private void loadVideoList() {
        FirebaseUtil firebaseUtil = new FirebaseUtil();
        firebaseUtil.getAllVideos(new FirebaseUtil.OnVideosLoadListener() {
            @Override
            public void onSuccess(List<Video> videos) {
                allVideo.addAll(videos);
                showList(allVideo.stream().filter(video -> video.getAuthorId().equals(viewedUser.getId())).collect(Collectors.toList()));
                int likeCount = videos.stream().reduce(0, (a, b) ->
                        CommonUtil.currentUser.getId().equals(b.getAuthorId()) ?
                                a + b.getLikes().size() :
                        0, Integer::sum);
                tvLikes.setText(String.valueOf(likeCount) + " Likes");
            }

            @Override
            public void onError(Exception e) {
                recyclerVideos.setAdapter(adapter);
            }
        });
    }

    private void showList(List<Video> list) {
        videoList.clear();
        videoList.addAll(list);
        adapter.notifyDataSetChanged();
    }

    private void setActiveTab(TextView tab) {
        tabMy.setBackgroundResource(R.drawable.tab_inactive);
        tabLiked.setBackgroundResource(R.drawable.tab_inactive);

        tab.setBackgroundResource(R.drawable.tab_active);
    }
}