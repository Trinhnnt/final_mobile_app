package edu.ueh.final_android_app.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VideoDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "videos.db";
    private static final int DB_VERSION = 1;

    public VideoDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE videos (" +
                        "id TEXT PRIMARY KEY, " +
                        "caption TEXT, " +
                        "videoUrl TEXT, " +
                        "authorId TEXT, " +
                        "authorName TEXT, " +
                        "authorAvatarUrl TEXT, " +
                        "createdAt INTEGER)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS videos");
        onCreate(db);
    }
}

