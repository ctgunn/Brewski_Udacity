package gunn.brewski.app.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Created by SESA300553 on 4/2/2015.
 */
public class BrewskiContract {
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "gunn.brewski.app";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.

    public static final String PATH_BEER = "beer";
    public static final String PATH_BREWERY = "brewery";
    public static final String PATH_STYLE = "style";

    public static final class BeerEntry implements BaseColumns {
        public static final Uri BEER_CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BEER).build();

        public static final String BEER_CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BEER;
        public static final String BEER_CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BEER;

        // Table name
        public static final String TABLE_NAME = "beer";

        // Foreign Beer ID that is received from BreweryDB.
        public static final String COLUMN_BEER_ID = "beer_id";
        // Beer name that is received from BreweryDB.
        public static final String COLUMN_BEER_NAME = "beer_name";
        // Beer description that is received from BreweryDB.
        public static final String COLUMN_BEER_DESCRIPTION = "beer_description";
        // Beer's Brewery ID that is received from BreweryDB.
        public static final String COLUMN_BREWERY_ID = "brewery_id";
        // Style ID that is received from BreweryDB.
        public static final String COLUMN_STYLE_ID = "style_id";
        // URL that links to the large label image for the corresponding beer,
        // which is received from BreweryDB.
        public static final String COLUMN_LABEL_LARGE = "label_large";
        // URL that links to the medium label image for the corresponding beer,
        // which is received from BreweryDB.
        public static final String COLUMN_LABEL_MEDIUM = "label_medium";
        // URL that links to the icon label image for the corresponding beer,
        // which is received from BreweryDB.
        public static final String COLUMN_LABEL_ICON = "label_icon";

        public static Uri buildBeerUri(long id) {
            return ContentUris.withAppendedId(BEER_CONTENT_URI, id);
        }

        public static Uri buildBeerUriWithBeerId(String beerId) {
            return BEER_CONTENT_URI.buildUpon().appendPath("beerId").appendPath(beerId).build();
        }
    }

    public static final class BreweryEntry implements BaseColumns {

        public static final Uri BREWERY_CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BREWERY).build();

        public static final String BREWERY_CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BREWERY;
        public static final String BREWERY_CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BREWERY;

        // Table name
        public static final String TABLE_NAME = "brewery";

        // Foreign Brewery ID that is received from BreweryDB.
        public static final String COLUMN_BREWERY_ID = "brewery_id";
        // Brewery name that is received from BreweryDB.
        public static final String COLUMN_BREWERY_NAME = "brewery_name";
        // Brewery description that is received from BreweryDB.
        public static final String COLUMN_BREWERY_DESCRIPTION = "brewery_description";
        // Brewery description that is received from BreweryDB.
        public static final String COLUMN_BREWERY_WEBSITE = "brewery_website";
        // The year that the brewery was established according to BreweryDB.
        public static final String COLUMN_ESTABLISHED = "established";
        // URL that links to the large image for the corresponding brewery,
        // which is received from BreweryDB.
        public static final String COLUMN_IMAGE_LARGE = "image_large";
        // URL that links to the medium image for the corresponding brewery,
        // which is received from BreweryDB.
        public static final String COLUMN_IMAGE_MEDIUM = "image_medium";
        // URL that links to the icon image for the corresponding brewery,
        // which is received from BreweryDB.
        public static final String COLUMN_IMAGE_ICON = "image_icon";

        public static Uri buildBreweryUri(long id) {
            return ContentUris.withAppendedId(BREWERY_CONTENT_URI, id);
        }

        public static Uri buildBreweryUriWithBreweryId(String breweryId) {
            return BREWERY_CONTENT_URI.buildUpon().appendPath("breweryId").appendPath(breweryId).build();
        }
    }

    public static final class StyleEntry implements BaseColumns {
        public static final Uri STYLE_CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STYLE).build();

        public static final String STYLE_CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STYLE;
        public static final String STYLE_CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STYLE;

        // Table name
        public static final String TABLE_NAME = "style";

        // Foreign Style ID that is received from BreweryDB.
        public static final String COLUMN_STYLE_ID = "style_id";
        // Style name that is received from BreweryDB.
        public static final String COLUMN_STYLE_NAME = "style_name";
        // Style name that is received from BreweryDB.
        public static final String COLUMN_STYLE_SHORT_NAME = "style_short_name";
        // Style description that is received from BreweryDB.
        public static final String COLUMN_STYLE_DESCRIPTION = "style_description";

        public static Uri buildStyleUri(long id) {
            return ContentUris.withAppendedId(STYLE_CONTENT_URI, id);
        }

        public static Uri buildStyleUriWithStyleId(String styleId) {
            return STYLE_CONTENT_URI.buildUpon().appendPath("styleId").appendPath(styleId).build();
        }
    }
}
