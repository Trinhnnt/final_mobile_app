package edu.ueh.final_android_app.util;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ueh.final_android_app.models.Account;
import edu.ueh.final_android_app.models.Video;

public class FirebaseUtil {
    public void saveUserToFirestore(Account account, OnSuccessListener<DocumentReference> onSuccess, OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("firstName", account.getFirstName());
        data.put("lastName", account.getLastName());
        data.put("username", account.getUsername());
        data.put("email", account.getEmail());
        data.put("password", account.getPassword());
        data.put("isBanned", account.isBanned());

        db.collection("accounts").add(data).addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    public void updateUser(Account account, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("firstName", account.getFirstName());
        data.put("lastName", account.getLastName());
        data.put("username", account.getUsername());
        data.put("email", account.getEmail());
        data.put("password", account.getPassword());
        data.put("avatarUrl", account.getAvatarUrl());
        data.put("isBanned", account.isBanned());

        db.collection("accounts").document(account.getId()).update(data).addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    public void getUserByEmail(String email, OnUserGetListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("accounts").whereEqualTo("email", email).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);

                Account account = new Account(doc.getId(), doc.getString("firstName"), doc.getString("lastName"), doc.getString("username"), doc.getString("password"), doc.getString("avatarUrl"), Boolean.TRUE.equals(doc.getBoolean("isBanned")));
                account.setEmail(doc.getString("email"));

                listener.onSuccess(account);
            } else {
                listener.onNotFound();
            }
        }).addOnFailureListener(listener::onError);
    }
    public void getUserById(String id, OnUserGetListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("accounts")
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Account acc = doc.toObject(Account.class);
                        if (acc != null) acc.setId(doc.getId()); // GÁN ID
                        listener.onSuccess(acc);
                    } else {
                        listener.onNotFound();
                    }
                })
                .addOnFailureListener(listener::onError);
    }


    public void getAllUsers(OnSuccessListener<List<Account>> onSuccess,
                            OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("accounts")
                .get()
                .addOnSuccessListener(query -> {
                    List<Account> accounts = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Account acc = new Account();

                        acc.setId(doc.getId());
                        acc.setFirstName(doc.getString("firstName"));
                        acc.setLastName(doc.getString("lastName"));
                        acc.setUsername(doc.getString("username"));
                        acc.setEmail(doc.getString("email"));
                        acc.setPassword(doc.getString("password"));
                        acc.setAvatarUrl(doc.getString("avatarUrl"));
                        acc.setIsBanned(Boolean.TRUE.equals(doc.getBoolean("isBanned")));

                        accounts.add(acc);
                    }

                    onSuccess.onSuccess(accounts);
                })
                .addOnFailureListener(onFailure);
    }


    public void login(String username, String password, OnUserGetListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Task<QuerySnapshot> q1 = db.collection("accounts").whereEqualTo("username", username).whereEqualTo("password", password).get();

        Task<QuerySnapshot> q2 = db.collection("accounts").whereEqualTo("email", username).whereEqualTo("password", password).get();

        Tasks.whenAllComplete(q1, q2).addOnCompleteListener(all -> {
            boolean hasUsername = false, hasEmail = false;
            Account acc = null;

            if (q1.isSuccessful() && q1.getResult() != null && !q1.getResult().isEmpty()) {
                DocumentSnapshot doc = q1.getResult().getDocuments().get(0);
                acc = doc.toObject(Account.class);
                if (acc != null) acc.setId(doc.getId());    // GÁN ID
                hasUsername = true;
            }

            if (!hasUsername && q2.isSuccessful() && q2.getResult() != null && !q2.getResult().isEmpty()) {
                DocumentSnapshot doc = q2.getResult().getDocuments().get(0);
                acc = doc.toObject(Account.class);
                if (acc != null) acc.setId(doc.getId());    // GÁN ID
                hasEmail = true;
            }


            if (hasUsername || hasEmail) {
                listener.onSuccess(acc);
            } else {
                listener.onNotFound();
            }
        });

    }

    public void saveVideoToFirestore(Video video, OnSuccessListener<DocumentReference> onSuccess, OnFailureListener onFailure) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("caption", video.getCaption());
        data.put("videoUrl", video.getVideoUrl());
        data.put("authorId", video.getAuthorId());
        data.put("authorName", video.getAuthorName());
        data.put("authorAvatarUrl", video.getAuthorAvatarUrl());
        data.put("createdAt", video.getCreatedAt());
        data.put("likes", video.getLikes());

        db.collection("videos").add(data).addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    public void updateVideo(Video video, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("caption", video.getCaption());
        data.put("videoUrl", video.getVideoUrl());
        data.put("authorId", video.getAuthorId());
        data.put("authorName", video.getAuthorName());
        data.put("likes", video.getLikes());

        db.collection("videos").document(video.getId()).update(data).addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    public void deleteVideo(Video video, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("videos").document(video.getId()).delete().addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    public void getAllVideos(OnVideosLoadListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("videos").get().addOnSuccessListener(query -> {
            List<Video> result = new ArrayList<>();

            for (DocumentSnapshot doc : query.getDocuments()) {
                Map<String, Object> data = doc.getData();
                if (data == null) continue;

                String id = doc.getId();
                String caption = (String) data.get("caption");
                String fileId = (String) data.get("videoUrl");
                String authorId = (String) data.get("authorId");
                String authorName = (String) data.get("authorName");
                String authorAvatarId = (String) data.get("authorAvatarUrl");
                Long createdAt = (Long) data.get("createdAt");

                List<String> likes = (List<String>) data.get("likes");

                Video video = new Video(id, caption, fileId, authorId, authorName, createdAt, likes, authorAvatarId);

                result.add(video);
            }

            listener.onSuccess(result);
        }).addOnFailureListener(listener::onError);
    }

    public interface OnVideosLoadListener {
        void onSuccess(List<Video> videos);

        void onError(Exception e);
    }

    public interface OnUserGetListener {
        void onSuccess(Account account);

        void onNotFound();

        void onError(Exception e);
    }
}
