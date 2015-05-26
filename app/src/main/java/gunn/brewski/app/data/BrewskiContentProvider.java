package gunn.brewski.app.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import gunn.brewski.app.BrewskiApplication;
import gunn.brewski.app.MainActivity;

public class BrewskiContentProvider extends ContentProvider {
    public final String LOG_TAG = BrewskiContentProvider.class.getSimpleName();

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    public static BrewskiDbHelper mOpenHelper;

    static final int BEER = 100;
    static final int INDIVIDUAL_BEER = 101;
    static final int BREWERY = 200;
    static final int INDIVIDUAL_BREWERY = 201;
    static final int STYLE = 400;
    static final int INDIVIDUAL_STYLE = 401;

    private static final SQLiteQueryBuilder sBrewskiBeerQueryBuilder;
    private static final SQLiteQueryBuilder sBrewskiBreweryQueryBuilder;
    private static final SQLiteQueryBuilder sBrewskiStyleQueryBuilder;

    static {
        sBrewskiBeerQueryBuilder = new SQLiteQueryBuilder();
        sBrewskiBreweryQueryBuilder = new SQLiteQueryBuilder();
        sBrewskiStyleQueryBuilder = new SQLiteQueryBuilder();

        sBrewskiBeerQueryBuilder.setTables(BrewskiContract.BeerEntry.TABLE_NAME);
        sBrewskiBreweryQueryBuilder.setTables(BrewskiContract.BreweryEntry.TABLE_NAME);
        sBrewskiStyleQueryBuilder.setTables(BrewskiContract.StyleEntry.TABLE_NAME);
    }

    private static final String sIndividualBeer =
            BrewskiContract.BeerEntry.TABLE_NAME + "." +
                BrewskiContract.BeerEntry.COLUMN_BEER_ID + " = ? ";

    private static final String sIndividualBrewery =
            BrewskiContract.BreweryEntry.TABLE_NAME + "." +
                BrewskiContract.BreweryEntry.COLUMN_BREWERY_ID + " = ?";

    private static final String sIndividualStyle =
            BrewskiContract.StyleEntry.TABLE_NAME + "." +
                BrewskiContract.StyleEntry.COLUMN_STYLE_ID + " = ?";

    private Cursor getIndividualBeer(Uri uri, String[] projection, String sortOrder) {
        String beerId = BrewskiApplication.getCurrentBeerId();

        String[] selectionArgs;
        String selection;

        selectionArgs = new String[]{beerId};
        selection = sIndividualBeer;

        return sBrewskiBeerQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getIndividualBrewery(Uri uri, String[] projection, String sortOrder) {
        String breweryId = BrewskiApplication.getCurrentBreweryId();

        String[] selectionArgs;
        String selection;

        selectionArgs = new String[]{breweryId};
        selection = sIndividualBrewery;

        return sBrewskiBreweryQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getIndividualStyle(Uri uri, String[] projection, String sortOrder) {
        String styleId = BrewskiApplication.getCurrentStyleId();

        String[] selectionArgs;
        String selection;

        selectionArgs = new String[]{styleId};
        selection = sIndividualStyle;

        return sBrewskiStyleQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = BrewskiContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, BrewskiContract.PATH_BEER, BEER);
        matcher.addURI(authority, BrewskiContract.PATH_BEER + "/beerId/*", INDIVIDUAL_BEER);

        matcher.addURI(authority, BrewskiContract.PATH_BREWERY, BREWERY);
        matcher.addURI(authority, BrewskiContract.PATH_BREWERY + "/breweryId/*", INDIVIDUAL_BREWERY);

        matcher.addURI(authority, BrewskiContract.PATH_STYLE, STYLE);
        matcher.addURI(authority, BrewskiContract.PATH_STYLE + "/styleId/*", INDIVIDUAL_STYLE);

        return matcher;
    }

    /*
        Students: We've coded this for you.  We just create a new WeatherDbHelper for later use
        here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new BrewskiDbHelper(getContext());
        return true;
    }

    /*
        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.

     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case BEER:
                return BrewskiContract.BeerEntry.BEER_CONTENT_TYPE;
            case INDIVIDUAL_BEER:
                return BrewskiContract.BeerEntry.BEER_CONTENT_ITEM_TYPE;
            case BREWERY:
                return BrewskiContract.BreweryEntry.BREWERY_CONTENT_TYPE;
            case INDIVIDUAL_BREWERY:
                return BrewskiContract.BreweryEntry.BREWERY_CONTENT_ITEM_TYPE;
            case STYLE:
                return BrewskiContract.StyleEntry.STYLE_CONTENT_TYPE;
            case INDIVIDUAL_STYLE:
                return BrewskiContract.StyleEntry.STYLE_CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "beer"
            case BEER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        BrewskiContract.BeerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case INDIVIDUAL_BEER: {
                retCursor = getIndividualBeer(uri, projection, sortOrder);
                break;
            }
            // "brewery"
            case BREWERY: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        BrewskiContract.BreweryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case INDIVIDUAL_BREWERY: {
                retCursor = getIndividualBrewery(uri, projection, sortOrder);
                break;
            }
            // "style"
            case STYLE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        BrewskiContract.StyleEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case INDIVIDUAL_STYLE: {
                retCursor = getIndividualStyle(uri, projection, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case BEER: {
                long _id = db.insert(BrewskiContract.BeerEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = BrewskiContract.BeerEntry.buildBeerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case BREWERY: {
                long _id = db.insert(BrewskiContract.BreweryEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = BrewskiContract.BreweryEntry.buildBreweryUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case STYLE: {
                long _id = db.insert(BrewskiContract.StyleEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = BrewskiContract.StyleEntry.buildStyleUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case BEER:
                rowsDeleted = db.delete(
                        BrewskiContract.BeerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BREWERY:
                rowsDeleted = db.delete(
                        BrewskiContract.BreweryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case STYLE:
                rowsDeleted = db.delete(
                        BrewskiContract.StyleEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case BEER:
                rowsUpdated = db.update(BrewskiContract.BeerEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case BREWERY:
                rowsUpdated = db.update(BrewskiContract.BreweryEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case STYLE:
                rowsUpdated = db.update(BrewskiContract.StyleEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;

        switch (match) {
            case BEER:
                db.beginTransaction();

                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(BrewskiContract.BeerEntry.TABLE_NAME, null, value);

                        if (_id != -1) {
                            returnCount++;
                        }
                    }

                    db.setTransactionSuccessful();
                }
                catch(SQLiteConstraintException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
                finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);

                return returnCount;
            case BREWERY:
                db.beginTransaction();

                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(BrewskiContract.BreweryEntry.TABLE_NAME, null, value);

                        if (_id != -1) {
                            returnCount++;
                        }
                    }

                    db.setTransactionSuccessful();
                }
                catch(SQLiteConstraintException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
                finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);

                return returnCount;
            case STYLE:
                db.beginTransaction();

                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(BrewskiContract.StyleEntry.TABLE_NAME, null, value);

                        if (_id != -1) {
                            returnCount++;
                        }
                    }

                    db.setTransactionSuccessful();
                }
                catch(SQLiteConstraintException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
                finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);

                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(16)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}