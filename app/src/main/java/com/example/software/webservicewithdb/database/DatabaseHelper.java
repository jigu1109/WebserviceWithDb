package com.example.software.webservicewithdb.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.software.webservicewithdb.model.Movie;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "moveis_db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // create notes table
        db.execSQL(Movie.CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + Movie.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public long insertNote(int ids,String release_date,String title, String overview,  double vote_avg) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(Movie.COLUMN_ID, ids);
        values.put(Movie.COLUMN_RELEASE_DATE, release_date);
        values.put(Movie.COLUMN_TITLE, title);
        values.put(Movie.COLUMN_OVERVIEW, overview);
        values.put(Movie.COLUMN_VOTE_AVG, vote_avg);

        // insert row
        long id = db.insert(Movie.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public List<Movie> getAllNotes() {
        List<Movie> movies = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + Movie.TABLE_NAME ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Movie movie = new Movie();
                movie.setId(cursor.getInt(cursor.getColumnIndex(Movie.COLUMN_ID)));
                movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(Movie.COLUMN_RELEASE_DATE)));
                movie.setTitle(cursor.getString(cursor.getColumnIndex(Movie.COLUMN_TITLE)));
                movie.setOverview(cursor.getString(cursor.getColumnIndex(Movie.COLUMN_OVERVIEW)));
                movie.setVoteAverage(cursor.getDouble(cursor.getColumnIndex(Movie.COLUMN_VOTE_AVG)));

                movies.add(movie);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return movies;
    }

    public void deleteNote(Movie movie) {
        Log.d("Movie", String.valueOf(movie));
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Movie.TABLE_NAME, Movie.COLUMN_ID + " = ?",
                new String[]{String.valueOf(movie.getId())});
        db.close();
    }

    public Movie getMovie(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Movie.TABLE_NAME,
                new String[]{Movie.COLUMN_ID, Movie.COLUMN_RELEASE_DATE, Movie.COLUMN_TITLE, Movie.COLUMN_OVERVIEW, Movie.COLUMN_VOTE_AVG},
                Movie.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare note object
        Movie movie= new Movie(
                cursor.getInt(cursor.getColumnIndex(Movie.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(Movie.COLUMN_RELEASE_DATE)),
                cursor.getString(cursor.getColumnIndex(Movie.COLUMN_TITLE)),
                cursor.getString(cursor.getColumnIndex(Movie.COLUMN_OVERVIEW)),
                cursor.getDouble(cursor.getColumnIndex(Movie.COLUMN_VOTE_AVG))
                );

        // close the db connection
        cursor.close();

        return movie;
    }
}
