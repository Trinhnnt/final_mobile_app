package edu.ueh.final_android_app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import edu.ueh.final_android_app.models.Account;

public class CommonUtil {
    public static Account currentUser = null;

    public static boolean isRequired(String value) {
        return value != null && !value.isEmpty();
    }

    public static long getNow() {
        LocalDateTime now = LocalDateTime.now();
        String sb = String.valueOf(now.getYear()) +
                now.getMonthValue() +
                now.getDayOfMonth() +
                now.getHour() +
                now.getMinute() +
                now.getSecond();
        return Long.parseLong(sb);
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        return false;
    }

    public static File uriToFile(Uri uri, Context context) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("avatar_", ".jpg", context.getCacheDir());
        OutputStream out = new FileOutputStream(tempFile);

        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }

        out.close();
        inputStream.close();
        return tempFile;
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
    public static void downloadAndCacheAvatar(Context context, String url, FireStorageUtil.UploadListener listener) {
        File cacheDir = context.getExternalFilesDir("images");
        if (cacheDir == null) {
            listener.onError(new Exception("Cache dir null"));
            return;
        }

        if (!cacheDir.exists()) cacheDir.mkdirs();

        String rawName = Uri.parse(url).getLastPathSegment();
        String safeName = rawName.replaceAll("[^a-zA-Z0-9._-]", "_");

        File outFile = new File(cacheDir, safeName);

        if (outFile.exists()) {
            listener.onSuccess(outFile.getAbsolutePath());
            return;
        }

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(url);

        ref.getFile(outFile)
                .addOnSuccessListener(task -> listener.onSuccess(outFile.getAbsolutePath()))
                .addOnFailureListener(listener::onError);
    }
    // helper parse path từ public URL
    public static String extractPath(String url) {
        String encoded = url.substring(url.indexOf("/o/") + 3, url.indexOf("?"));
        return java.net.URLDecoder.decode(encoded, StandardCharsets.UTF_8);
    }
    public interface ThumbnailListener {
        void onThumbnailReady(Bitmap bmp);
        void onError(Exception e);
    }
    public static void getVideoThumbnail(Context context, String url, ThumbnailListener listener) {

        downloadAndCacheVideo(context, url, new FireStorageUtil.UploadListener() {
            @Override
            public void onSuccess(String filePath) {

                try {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(filePath);
                    Bitmap bmp = retriever.getFrameAtTime(0);
                    retriever.release();

                    listener.onThumbnailReady(bmp);

                } catch (Exception e) {
                    listener.onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
    }


}
