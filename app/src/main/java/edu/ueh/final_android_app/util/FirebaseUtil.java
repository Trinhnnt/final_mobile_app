package edu.ueh.final_android_app.util;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import edu.ueh.final_android_app.models.Account;

public class FirebaseUtil {
    public void saveUserToFirestore(Account account, OnSuccessListener<DocumentReference> onSuccess, OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("firstName", account.getFirstName());
        data.put("lastName", account.getLastName());
        data.put("username", account.getUsername());
        data.put("email", account.getEmail());
        data.put("password", account.getPassword());

        db.collection("accounts").add(data).addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    public void getUserByEmail(String email, OnUserGetListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("accounts").whereEqualTo("email", email).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);

                Account account = new Account(doc.getId(), doc.getString("firstName"), doc.getString("lastName"), doc.getString("username"), doc.getString("password"));
                account.setEmail(doc.getString("email"));

                listener.onSuccess(account);
            } else {
                listener.onNotFound();
            }
        }).addOnFailureListener(listener::onError);
    }

    public void login(String username, String password, OnUserGetListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Task<QuerySnapshot> q1 = db.collection("accounts").whereEqualTo("username", username).whereEqualTo("password", password).get();

        Task<QuerySnapshot> q2 = db.collection("accounts").whereEqualTo("email", username).whereEqualTo("password", password).get();

        Tasks.whenAllComplete(q1, q2).addOnCompleteListener(all -> {
            boolean hasUsername = false, hasEmail = false;
            Account acc = null;

            if (q1.isSuccessful() && q1.getResult() != null && !q1.getResult().isEmpty()) {
                acc = q1.getResult().getDocuments().get(0).toObject(Account.class);
                hasUsername = true;
            }

            if (!hasUsername && q2.isSuccessful() && q2.getResult() != null && !q2.getResult().isEmpty()) {
                acc = q2.getResult().getDocuments().get(0).toObject(Account.class);
                hasEmail = true;
            }

            if (hasUsername || hasEmail) {
                listener.onSuccess(acc);
            } else {
                listener.onNotFound();
            }
        });

    }

    public interface OnUserGetListener {
        void onSuccess(Account account);

        void onNotFound();

        void onError(Exception e);
    }
}
