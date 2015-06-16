package gunn.brewski.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import gunn.brewski.app.data.BrewskiContract.BeerEntry;
import gunn.brewski.app.data.BrewskiContract.BreweryEntry;
import gunn.brewski.app.data.BrewskiContract.StyleEntry;

/**
 * Created by SESA300553 on 4/2/2015.
 */
public class BrewskiDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "brewski.db";

    public BrewskiDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_BEER_TABLE = "CREATE TABLE " + BeerEntry.TABLE_NAME + " (" +
                BeerEntry._ID + " INTEGER PRIMARY KEY," +
                BeerEntry.COLUMN_BEER_ID + " TEXT, " +
                BeerEntry.COLUMN_BEER_NAME + " TEXT, " +
                BeerEntry.COLUMN_BEER_DESCRIPTION + " TEXT, " +
                BeerEntry.COLUMN_BREWERY_ID + " TEXT, " +
                BeerEntry.COLUMN_STYLE_ID + " TEXT, " +
                BeerEntry.COLUMN_LABEL_LARGE + " TEXT, " +
                BeerEntry.COLUMN_LABEL_MEDIUM + " TEXT, " +
                BeerEntry.COLUMN_LABEL_ICON + " TEXT " +
                " );";

        final String SQL_CREATE_BREWERY_TABLE = "CREATE TABLE " + BreweryEntry.TABLE_NAME + " (" +
                BreweryEntry._ID + " INTEGER PRIMARY KEY," +
                BreweryEntry.COLUMN_BREWERY_ID + " TEXT, " +
                BreweryEntry.COLUMN_BREWERY_NAME + " TEXT, " +
                BreweryEntry.COLUMN_BREWERY_DESCRIPTION + " TEXT, " +
                BreweryEntry.COLUMN_BREWERY_WEBSITE + " TEXT, " +
                BreweryEntry.COLUMN_ESTABLISHED + " TEXT, " +
                BreweryEntry.COLUMN_IMAGE_LARGE + " TEXT, " +
                BreweryEntry.COLUMN_IMAGE_MEDIUM + " TEXT, " +
                BreweryEntry.COLUMN_IMAGE_ICON + " TEXT " +
                " );";

        final String SQL_CREATE_STYLE_TABLE = "CREATE TABLE " + StyleEntry.TABLE_NAME + " (" +
                StyleEntry._ID + " INTEGER PRIMARY KEY," +
                StyleEntry.COLUMN_STYLE_ID + " TEXT, " +
                StyleEntry.COLUMN_STYLE_NAME + " TEXT, " +
                StyleEntry.COLUMN_STYLE_SHORT_NAME + " TEXT, " +
                StyleEntry.COLUMN_STYLE_DESCRIPTION + " TEXT " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_BEER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_BREWERY_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_STYLE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BeerEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BreweryEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + StyleEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
