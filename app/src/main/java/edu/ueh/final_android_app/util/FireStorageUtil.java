package edu.ueh.final_android_app.util;

import android.content.Context;
import android.net.Uri;

import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class FireStorageUtil {

    private final StorageReference folderRef;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public FireStorageUtil(Context context) {
        FirebaseApp.initializeApp(context);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        folderRef = storage.getReference().child("videos");
    }

    public void checkFileExists(String fileName, Consumer<Boolean> callback) {
        executor.execute(() -> {
            folderRef.child(fileName).getMetadata().addOnSuccessListener(m -> callback.accept(true)).addOnFailureListener(e -> callback.accept(false));
        });
    }

    public void uploadVideo(File file, UploadListener listener) {
        executor.execute(() -> {
            Uri uri = Uri.fromFile(file);
            StorageReference ref = folderRef.child(file.getName());
            UploadTask task = ref.putFile(uri);

            task.addOnSuccessListener(t -> ref.getDownloadUrl().addOnSuccessListener(url -> listener.onSuccess(url.toString())).addOnFailureListener(listener::onError)).addOnFailureListener(listener::onError);
        });
    }

    public void uploadImage(File file, UploadListener listener) {
        executor.execute(() -> {
            Uri uri = Uri.fromFile(file);
            StorageReference imgRef = FirebaseStorage.getInstance().getReference().child("images").child(file.getName());

            UploadTask task = imgRef.putFile(uri);

            task.addOnSuccessListener(t -> imgRef.getDownloadUrl().addOnSuccessListener(url -> listener.onSuccess(url.toString())).addOnFailureListener(listener::onError)).addOnFailureListener(listener::onError);
        });
    }

    public void deleteFileByUrl(String url, UploadListener listener) {
        executor.execute(() -> {

            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(url);

            ref.delete().addOnSuccessListener(unused -> listener.onSuccess(url)).addOnFailureListener(listener::onError);
        });
    }

    public void listVideos(UploadListener listener) {
        executor.execute(() -> {
            folderRef.listAll().addOnSuccessListener(result -> {
                StringBuilder sb = new StringBuilder();
                for (StorageReference ref : result.getItems()) {
                    sb.append(ref.getName()).append("\n");
                }
                listener.onSuccess(sb.toString());
            }).addOnFailureListener(listener::onError);
        });
    }

    public void downloadFile(String url, File outFile, UploadListener listener) {

        executor.execute(() -> {
            try {
                String storagePath = CommonUtil.extractPath(url);

                StorageReference ref = FirebaseStorage.getInstance().getReference().child(storagePath);

                ref.getFile(outFile).addOnSuccessListener(task -> listener.onSuccess(outFile.getAbsolutePath())).addOnFailureListener(listener::onError);

            } catch (Exception ex) {
                listener.onError(ex);
            }
        });
    }


    public interface UploadListener {
        void onSuccess(String fileId);

        void onError(Exception e);
    }
}
