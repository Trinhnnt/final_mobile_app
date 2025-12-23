package edu.ueh.final_android_app.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import edu.ueh.final_android_app.helper.VideoDatabaseHelper;
import edu.ueh.final_android_app.models.Video;

public class VideoDao {
    private final SQLiteDatabase db;

    public VideoDao(Context context) {
        VideoDatabaseHelper helper = new VideoDatabaseHelper(context);
        db = helper.getWritableDatabase();
    }

    public void saveVideos(List<Video> videos) {
        db.beginTransaction();
        try {
            db.delete("videos", null, null); // clear old
            for (Video v : videos) {
                ContentValues cv = new ContentValues();
                cv.put("id", v.getId());
                cv.put("caption", v.getCaption());
                cv.put("videoUrl", v.getVideoUrl());
                cv.put("authorId", v.getAuthorId());
                cv.put("authorName", v.getAuthorName());
                cv.put("authorAvatarId", v.getAuthorAvatarUrl());
                cv.put("createdAt", v.getCreatedAt());
                db.insert("videos", null, cv);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void addVideo(Video video){
        ContentValues cv = new ContentValues();
        cv.put("id", video.getId());
        cv.put("caption", video.getCaption());
        cv.put("videoUrl", video.getVideoUrl());
        cv.put("authorId", video.getAuthorId());
        cv.put("authorName", video.getAuthorName());
        cv.put("authorAvatarUrl", video.getAuthorAvatarUrl());
        cv.put("createdAt", video.getCreatedAt());

        db.insert("videos", null, cv);
    }


    public List<Video> getAll() {
        List<Video> result = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM videos ORDER BY createdAt DESC", null);

        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            String caption = cursor.getString(1);
            String videoUrl = cursor.getString(2);
            String authorId = cursor.getString(3);
            String authorName = cursor.getString(4);
            String authorAvatarUrl = cursor.getString(5);
            long createdAt = cursor.getLong(6);

            result.add(new Video(
                    id, caption, videoUrl,
                    authorId, authorName, createdAt,
                    new ArrayList<>(), // likes không lưu để đơn giản
                    authorAvatarUrl
            ));
        }
        cursor.close();
        return result;
    }
}

