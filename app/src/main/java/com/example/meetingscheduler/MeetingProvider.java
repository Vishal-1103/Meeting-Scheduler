package com.example.meetingscheduler;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.content.Context;

public class MeetingProvider extends ContentProvider {

    // Authority and Content URI
    public static final String AUTHORITY = "com.example.meetingscheduler";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/meetings");

    // MIME types
    public static final String MIME_TYPE_DIR = "vnd.android.cursor.dir/vnd.com.example.meetingscheduler.meeting";
    public static final String MIME_TYPE_ITEM = "vnd.android.cursor.item/vnd.com.example.meetingscheduler.meeting";

    // Table and Column names
    public static final String TABLE_NAME = "meetings";
    public static final String KEY_ID = "_id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_PARTICIPANTS = "participants";
    public static final String KEY_DATE = "date";
    public static final String KEY_TIME = "time";

    // UriMatcher
    private static final int URI_MATCHER_MEETINGS = 1;
    private static final int URI_MATCHER_MEETING_ID = 2;
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY, "meetings", URI_MATCHER_MEETINGS);
        URI_MATCHER.addURI(AUTHORITY, "meetings/#", URI_MATCHER_MEETING_ID);
    }

    private SQLiteOpenHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        switch (URI_MATCHER.match(uri)) {
            case URI_MATCHER_MEETINGS:
                cursor = db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case URI_MATCHER_MEETING_ID:
                long id = ContentUris.parseId(uri);
                cursor = db.query(TABLE_NAME, projection, KEY_ID + "=" + id, null, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long id = db.insert(TABLE_NAME, null, values);
        if (id > 0) {
            Uri insertedUri = ContentUris.withAppendedId(CONTENT_URI, id);
            getContext().getContentResolver().notifyChange(insertedUri, null);
            return insertedUri;
        } else {
            throw new IllegalArgumentException("Failed to insert row into " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected;

        switch (URI_MATCHER.match(uri)) {
            case URI_MATCHER_MEETINGS:
                rowsAffected =db.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            case URI_MATCHER_MEETING_ID:
                long id = ContentUris.parseId(uri);
                rowsAffected = db.update(TABLE_NAME, values, KEY_ID + "=" + id
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (rowsAffected > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected;

        switch (URI_MATCHER.match(uri)) {
            case URI_MATCHER_MEETINGS:
                rowsAffected = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
            case URI_MATCHER_MEETING_ID:
                long id = ContentUris.parseId(uri);
                rowsAffected = db.delete(TABLE_NAME, KEY_ID + "=" + id
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (rowsAffected > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case URI_MATCHER_MEETINGS:
                return MIME_TYPE_DIR;
            case URI_MATCHER_MEETING_ID:
                return MIME_TYPE_ITEM;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "meeting_scheduler.db";
        private static final int DATABASE_VERSION = 1;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_TITLE + " TEXT, " +
                    KEY_PARTICIPANTS + " TEXT, " +
                    KEY_DATE + " TEXT, " +
                    KEY_TIME + " TEXT)";
            db.execSQL(createTableQuery);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
