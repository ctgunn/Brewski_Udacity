package gunn.brewski.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import gunn.brewski.app.main.BrewskiApplication;
import gunn.brewski.app.R;
import gunn.brewski.app.data.BrewskiContentProvider;
import gunn.brewski.app.data.BrewskiContract;

/**
 * Created by SESA300553 on 4/2/2015.
 */
public class BrewskiSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String CALLING_LIST = "callingList";
    public final String LOG_TAG = BrewskiSyncAdapter.class.getSimpleName();
    public static final int SYNC_INTERVAL = 1000 * 60 * 60 * 24;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = SYNC_INTERVAL;

    public BrewskiSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        String callingList = extras.getString(CALLING_LIST);

        Intent intent = new Intent();
        intent.setAction("moreBeersLoaded");

        if("beer".equals(callingList)) {
            performBeerSync();
            intent.setAction("moreBeersLoaded");
        }
        else if("brewery".equals(callingList)) {
            performBrewerySync();
            intent.setAction("moreBreweriesLoaded");
        }
        else if("style".equals(callingList)) {
            performStyleSync();
            intent.setAction("moreStylesLoaded");
        }
        else {
            performBeerSync();
        }

        getContext().sendBroadcast(intent);

        return;
    }

    private void performBeerSync() {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection beerUrlConnection = null;
        BufferedReader beerReader = null;

        if(null == BrewskiApplication.getCurrentBeerPage()) {
            BrewskiApplication.setCurrentBeerPage(1);
        }

        // Will contain the raw JSON response as a string.
        String beerJsonStr = null;

        String format = "json";
        String api_key = "1be59a6cc44af64d5c7d6aafad061f23";
        String endpoint = "beers";
        String page = String.valueOf(BrewskiApplication.getCurrentBeerPage());
        String withBreweries = "Y";

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String BREWERY_DB_BASE_URL =
                    "http://api.brewerydb.com/v2/" + endpoint + "?";
            final String FORMAT_PARAM = "format";
            final String KEY_PARAM = "key";
            final String PAGE_PARAM = "p";
            final String WITH_BREWERIES = "withBreweries";

            Uri builtBeerUri = Uri.parse(BREWERY_DB_BASE_URL).buildUpon()
                    .appendQueryParameter(PAGE_PARAM, page)
                    .appendQueryParameter(WITH_BREWERIES, withBreweries)
                    .appendQueryParameter(KEY_PARAM, api_key)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .build();

            URL beerUrl = new URL(builtBeerUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            beerUrlConnection = (HttpURLConnection) beerUrl.openConnection();
            beerUrlConnection.setRequestMethod("GET");
            beerUrlConnection.connect();

            // Read the input stream into a String
            InputStream beerInputStream = beerUrlConnection.getInputStream();
            StringBuffer beerBuffer = new StringBuffer();
            if (beerInputStream == null) {
                // Nothing to do.
                return;
            }
            beerReader = new BufferedReader(new InputStreamReader(beerInputStream));

            String line;
            while ((line = beerReader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                beerBuffer.append(line + "\n");
            }

            if (beerBuffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }

            beerJsonStr = beerBuffer.toString();

            BrewskiApplication.setCurrentBeerPage(BrewskiApplication.getCurrentBeerPage() + 1);

            getBeerDataFromJson(beerJsonStr);
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            e.printStackTrace();
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        }
        catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        finally {
            if (beerUrlConnection != null) {
                beerUrlConnection.disconnect();
            }
            if (beerReader != null) {
                try {
                    beerReader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                    e.printStackTrace();
                }
            }
        }
    }

    private void performBrewerySync() {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection breweryUrlConnection = null;
        BufferedReader breweryReader = null;

        if(null == BrewskiApplication.getCurrentBreweryPage()) {
            BrewskiApplication.setCurrentBreweryPage(1);
        }

        // Will contain the raw JSON response as a string.
        String breweryJsonStr = null;

        String format = "json";
        String api_key = "1be59a6cc44af64d5c7d6aafad061f23";
        String endpoint = "breweries";
        String page = String.valueOf(BrewskiApplication.getCurrentBreweryPage());
        String withGuilds = "Y";
        String withLocations = "Y";
        String withAlternateNames = "Y";

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String BREWERY_DB_BASE_URL =
                    "http://api.brewerydb.com/v2/" + endpoint + "?";
            final String FORMAT_PARAM = "format";
            final String KEY_PARAM = "key";
            final String PAGE_PARAM = "p";
            final String WITH_GUILDS = "withGuilds";
            final String WITH_LOCATIONS = "withLocations";
            final String WITH_ALTERNATE_NAMES = "withAlternateNames";

            Uri builtBreweryUri = Uri.parse(BREWERY_DB_BASE_URL).buildUpon()
                    .appendQueryParameter(PAGE_PARAM, page)
                    .appendQueryParameter(KEY_PARAM, api_key)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(WITH_GUILDS, withGuilds)
                    .appendQueryParameter(WITH_LOCATIONS, withLocations)
                    .appendQueryParameter(WITH_ALTERNATE_NAMES, withAlternateNames)
                    .build();

            URL breweryUrl = new URL(builtBreweryUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            breweryUrlConnection = (HttpURLConnection) breweryUrl.openConnection();
            breweryUrlConnection.setRequestMethod("GET");
            breweryUrlConnection.connect();

            // Read the input stream into a String
            InputStream breweryInputStream = breweryUrlConnection.getInputStream();
            StringBuffer breweryBuffer = new StringBuffer();
            if (breweryInputStream == null) {
                // Nothing to do.
                return;
            }
            breweryReader = new BufferedReader(new InputStreamReader(breweryInputStream));

            String line;
            while ((line = breweryReader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                breweryBuffer.append(line + "\n");
            }

            if (breweryBuffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }

            BrewskiApplication.setCurrentBreweryPage(BrewskiApplication.getCurrentBreweryPage() + 1);

            breweryJsonStr = breweryBuffer.toString();

            getBreweryDataFromJson(breweryJsonStr);
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        }
        catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        finally {
            if (breweryUrlConnection != null) {
                breweryUrlConnection.disconnect();
            }
            if (breweryReader != null) {
                try {
                    breweryReader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    private void performStyleSync() {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection styleUrlConnection = null;
        BufferedReader styleReader = null;

        if(null == BrewskiApplication.getCurrentStylePage()) {
            BrewskiApplication.setCurrentStylePage(1);
        }

        // Will contain the raw JSON response as a string.
        String styleJsonStr = null;

        String format = "json";
        String api_key = "1be59a6cc44af64d5c7d6aafad061f23";
        String endpoint = "styles";

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String BREWERY_DB_BASE_URL =
                    "http://api.brewerydb.com/v2/" + endpoint + "?";
            final String FORMAT_PARAM = "format";
            final String KEY_PARAM = "key";

            Uri builtStyleUri = Uri.parse(BREWERY_DB_BASE_URL).buildUpon()
                    .appendQueryParameter(KEY_PARAM, api_key)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .build();

            URL styleUrl = new URL(builtStyleUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            styleUrlConnection = (HttpURLConnection) styleUrl.openConnection();
            styleUrlConnection.setRequestMethod("GET");
            styleUrlConnection.connect();

            // Read the input stream into a String
            InputStream styleInputStream = styleUrlConnection.getInputStream();
            StringBuffer styleBuffer = new StringBuffer();
            if (styleInputStream == null) {
                // Nothing to do.
                return;
            }
            styleReader = new BufferedReader(new InputStreamReader(styleInputStream));

            String line;
            while ((line = styleReader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                styleBuffer.append(line + "\n");
            }

            if (styleBuffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }

            styleJsonStr = styleBuffer.toString();

            getStyleDataFromJson(styleJsonStr);
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        }
        catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        finally {
            if (styleUrlConnection != null) {
                styleUrlConnection.disconnect();
            }
            if (styleReader != null) {
                try {
                    styleReader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    /**
     * Take the String representing the complete beer results in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getBeerDataFromJson(String beerJsonStr) throws JSONException {

        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        // These are the names of the JSON objects that need to be extracted.
        // Beer Information
        final String BDB_CURRENT_PAGE = "currentPage";
        final String BDB_NUMBER_OF_PAGES = "numberOfPages";
        final String BDB_BEER_ID = "id";
        final String BDB_BEER_NAME = "name";
        final String BDB_BEER_DESCRIPTION = "description";
        final String BDB_BEER_BREWERIES = "breweries";
        final String BDB_BREWERY_ID = "id";
        final String BDB_BREWERY_NAME = "name";
        final String BDB_BREWERY_DESCRIPTION = "description";
        final String BDB_BREWERY_WEBSITE = "website";
        final String BDB_BREWERY_ESTABLISHED = "established";
        final String BDB_BREWERY_IMAGES = "images";
        final String BDB_BREWERY_IMAGE_LARGE = "large";
        final String BDB_BREWERY_IMAGE_MEDIUM = "medium";
        final String BDB_BREWERY_IMAGE_ICON = "icon";
        final String BDB_BEER_STYLE = "style";
        final String BDB_STYLE_ID = "id";
        final String BDB_STYLE_NAME = "name";
        final String BDB_STYLE_SHORT_NAME = "shortName";
        final String BDB_STYLE_DESCRIPTION = "description";
        final String BDB_BEER_LABELS = "labels";
        final String BDB_BEER_LABEL_ICON = "icon";
        final String BDB_BEER_LABEL_MEDIUM = "medium";
        final String BDB_BEER_LABEL_LARGE = "large";

        // Beer information.  Each beer's info is an element of the "list" array.
        final String BDB_DATA = "data";

        ArrayList<String> beerIdList = new ArrayList<>();
        ArrayList<String> breweryIdList = new ArrayList<>();
        ArrayList<String> styleIdList = new ArrayList<>();

        try {
            JSONObject beerJson = new JSONObject(beerJsonStr);
            String numberOfPages = beerJson.getString(BDB_NUMBER_OF_PAGES);
            JSONArray beerArray = beerJson.getJSONArray(BDB_DATA);

            BrewskiApplication.setNumberOfBeerPages(Integer.parseInt(numberOfPages));

            // Insert the new beer information into the database
            Vector<ContentValues> beerContentValuesVector = new Vector<>(beerArray.length());
            Vector<ContentValues> breweryContentValuesVector = new Vector<>(beerArray.length());
            Vector<ContentValues> styleContentValuesVector = new Vector<>(beerArray.length());

            for(int i = 0; i < beerArray.length(); i++) {
                // These are the values that will be collected.
                String beerId;
                String beerName;
                String beerDescription;
                String beerBreweryId;
                String beerStyleId;
                String beerLabelIcon;
                String beerLabelMedium;
                String beerLabelLarge;

                String breweryId;
                String breweryName;
                String breweryDescription;
                String breweryWebsite;
                String breweryEstablished;
                String breweryImageLarge;
                String breweryImageMedium;
                String breweryImageIcon;

                String styleId;
                String styleName;
                String styleShortName;
                String styleDescription;

                // Get the JSON object representing the day
                JSONObject beerInfo = beerArray.getJSONObject(i);

                // Cheating to convert this to UTC time, which is what we want anyhow

                beerId = beerInfo.getString(BDB_BEER_ID);
                beerName = beerInfo.getString(BDB_BEER_NAME);
                if(beerInfo.has(BDB_BEER_DESCRIPTION)) {
                    beerDescription = beerInfo.getString(BDB_BEER_DESCRIPTION);
                }
                else {
                    beerDescription = null;
                }

                if(beerInfo.has(BDB_BEER_LABELS)) {
                    JSONObject beerLabels = beerInfo.getJSONObject(BDB_BEER_LABELS);
                    beerLabelIcon = beerLabels.getString(BDB_BEER_LABEL_ICON);
                    beerLabelMedium = beerLabels.getString(BDB_BEER_LABEL_MEDIUM);
                    beerLabelLarge = beerLabels.getString(BDB_BEER_LABEL_LARGE);
                }
                else {
                    beerLabelIcon = null;
                    beerLabelMedium = null;
                    beerLabelLarge = null;
                }

                if(beerInfo.has(BDB_BEER_STYLE)) {
                    JSONObject styleInfo = beerInfo.getJSONObject(BDB_BEER_STYLE);
                    beerStyleId = styleInfo.getString(BDB_STYLE_ID);
                    styleId = styleInfo.getString(BDB_STYLE_ID);
                    styleName = styleInfo.getString(BDB_STYLE_NAME);
                    styleShortName = styleInfo.getString(BDB_STYLE_SHORT_NAME);
                    styleDescription = styleInfo.getString(BDB_STYLE_DESCRIPTION);
                }
                else {
                    beerStyleId = null;
                    styleId = null;
                    styleName = null;
                    styleShortName = null;
                    styleDescription = null;
                }

                JSONArray breweriesArray = beerInfo.getJSONArray(BDB_BEER_BREWERIES);
                JSONObject breweryInfo = breweriesArray.getJSONObject(0);
                beerBreweryId = breweryInfo.getString(BDB_BREWERY_ID);
                breweryId = breweryInfo.getString(BDB_BREWERY_ID);
                breweryName = breweryInfo.getString(BDB_BREWERY_NAME);

                if(breweryInfo.has(BDB_BREWERY_DESCRIPTION)) {
                    breweryDescription = breweryInfo.getString(BDB_BREWERY_DESCRIPTION);
                }
                else {
                    breweryDescription = null;
                }

                if(breweryInfo.has(BDB_BREWERY_WEBSITE)) {
                    breweryWebsite = breweryInfo.getString(BDB_BREWERY_WEBSITE);
                }
                else {
                    breweryWebsite = null;
                }

                if(breweryInfo.has(BDB_BREWERY_ESTABLISHED)) {
                    breweryEstablished = breweryInfo.getString(BDB_BREWERY_ESTABLISHED);
                }
                else {
                    breweryEstablished = null;
                }

                if(breweryInfo.has(BDB_BREWERY_IMAGES)) {
                    JSONObject breweryImages = breweryInfo.getJSONObject(BDB_BREWERY_IMAGES);
                    breweryImageLarge = breweryImages.getString(BDB_BREWERY_IMAGE_LARGE);
                    breweryImageMedium = breweryImages.getString(BDB_BREWERY_IMAGE_MEDIUM);
                    breweryImageIcon = breweryImages.getString(BDB_BREWERY_IMAGE_ICON);
                }
                else {
                    breweryImageIcon = null;
                    breweryImageMedium = null;
                    breweryImageLarge = null;
                }

                ContentValues beerValues = new ContentValues();
                ContentValues breweryValues = new ContentValues();
                ContentValues categoryValues = new ContentValues();
                ContentValues styleValues = new ContentValues();

                beerValues.put(BrewskiContract.BeerEntry.COLUMN_BEER_ID, beerId);
                beerValues.put(BrewskiContract.BeerEntry.COLUMN_BEER_NAME, beerName);
                beerValues.put(BrewskiContract.BeerEntry.COLUMN_BEER_DESCRIPTION, beerDescription);
                beerValues.put(BrewskiContract.BeerEntry.COLUMN_BREWERY_ID, beerBreweryId);
                beerValues.put(BrewskiContract.BeerEntry.COLUMN_STYLE_ID, beerStyleId);
                beerValues.put(BrewskiContract.BeerEntry.COLUMN_LABEL_ICON, beerLabelIcon);
                beerValues.put(BrewskiContract.BeerEntry.COLUMN_LABEL_MEDIUM, beerLabelMedium);
                beerValues.put(BrewskiContract.BeerEntry.COLUMN_LABEL_LARGE, beerLabelLarge);

                breweryValues.put(BrewskiContract.BreweryEntry.COLUMN_BREWERY_ID, breweryId);
                breweryValues.put(BrewskiContract.BreweryEntry.COLUMN_BREWERY_NAME, breweryName);
                breweryValues.put(BrewskiContract.BreweryEntry.COLUMN_BREWERY_DESCRIPTION, breweryDescription);
                breweryValues.put(BrewskiContract.BreweryEntry.COLUMN_BREWERY_WEBSITE, breweryWebsite);
                breweryValues.put(BrewskiContract.BreweryEntry.COLUMN_ESTABLISHED, breweryEstablished);
                breweryValues.put(BrewskiContract.BreweryEntry.COLUMN_IMAGE_LARGE, breweryImageLarge);
                breweryValues.put(BrewskiContract.BreweryEntry.COLUMN_IMAGE_MEDIUM, breweryImageMedium);
                breweryValues.put(BrewskiContract.BreweryEntry.COLUMN_IMAGE_ICON, breweryImageIcon);

                styleValues.put(BrewskiContract.StyleEntry.COLUMN_STYLE_ID, styleId);
                styleValues.put(BrewskiContract.StyleEntry.COLUMN_STYLE_NAME, styleName);
                styleValues.put(BrewskiContract.StyleEntry.COLUMN_STYLE_SHORT_NAME, styleShortName);
                styleValues.put(BrewskiContract.StyleEntry.COLUMN_STYLE_DESCRIPTION, styleDescription);

                if(!existsInDb("beer", beerId) && !alreadyAddedToTheList(beerIdList, beerId)) {
                    beerIdList.add(beerId);
                    beerContentValuesVector.add(beerValues);
                }

                if(!existsInDb("brewery", breweryId) && !alreadyAddedToTheList(breweryIdList, breweryId)) {
                    breweryIdList.add(breweryId);
                    breweryContentValuesVector.add(breweryValues);
                }

                if(!existsInDb("style", styleId) && !alreadyAddedToTheList(styleIdList, styleId)) {
                    styleIdList.add(styleId);
                    styleContentValuesVector.add(styleValues);
                }
            }

            // add to database
            if(beerContentValuesVector.size() > 0) {
                ContentValues[] beerContentValuesArray = new ContentValues[beerContentValuesVector.size()];
                beerContentValuesVector.toArray(beerContentValuesArray);
                getContext().getContentResolver().bulkInsert(BrewskiContract.BeerEntry.BEER_CONTENT_URI, beerContentValuesArray);
            }

            Log.d(LOG_TAG, "Sync Complete. " + beerContentValuesVector.size() + " Beers Inserted");

            if(breweryContentValuesVector.size() > 0) {
                ContentValues[] breweryContentValuesArray = new ContentValues[breweryContentValuesVector.size()];
                breweryContentValuesVector.toArray(breweryContentValuesArray);
                getContext().getContentResolver().bulkInsert(BrewskiContract.BreweryEntry.BREWERY_CONTENT_URI, breweryContentValuesArray);
            }

            Log.d(LOG_TAG, "Sync Complete. " + breweryContentValuesVector.size() + " Breweries Inserted");

            if(styleContentValuesVector.size() > 0) {
                ContentValues[] styleContentValuesArray = new ContentValues[styleContentValuesVector.size()];
                styleContentValuesVector.toArray(styleContentValuesArray);
                getContext().getContentResolver().bulkInsert(BrewskiContract.StyleEntry.STYLE_CONTENT_URI, styleContentValuesArray);
            }

            Log.d(LOG_TAG, "Sync Complete. " + styleContentValuesVector.size() + " Styles Inserted");
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        catch(SQLiteConstraintException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void getBreweryDataFromJson(String breweryJsonStr) throws JSONException {
        // Brewery Information
        final String BDB_CURRENT_PAGE = "currentPage";
        final String BDB_NUMBER_OF_PAGES = "numberOfPages";
        final String BDB_BREWERY_ID = "id";
        final String BDB_BREWERY_NAME = "name";
        final String BDB_BREWERY_DESCRIPTION = "description";
        final String BDB_BREWERY_WEBSITE = "website";
        final String BDB_BREWERY_ESTABLISHED = "established";
        final String BDB_BREWERY_IMAGES = "images";
        final String BDB_BREWERY_IMAGE_ICON = "icon";
        final String BDB_BREWERY_IMAGE_MEDIUM = "medium";
        final String BDB_BREWERY_IMAGE_LARGE = "large";

        // Beer information.  Each beer's info is an element of the "list" array.
        final String BDB_DATA = "data";

        ArrayList<String> breweryIdList = new ArrayList<>();

        try {
            JSONObject breweryJson = new JSONObject(breweryJsonStr);
            String numberOfPages = breweryJson.getString(BDB_NUMBER_OF_PAGES);
            JSONArray breweryArray = breweryJson.getJSONArray(BDB_DATA);

            BrewskiApplication.setNumberOfBreweryPages(Integer.parseInt(numberOfPages));

            // Insert the new beer information into the database
            Vector<ContentValues> breweryContentValuesVector = new Vector<>(breweryArray.length());

            for(int i = 0; i < breweryArray.length(); i++) {
                // These are the values that will be collected.
                String breweryId;
                String breweryName;
                String breweryDescription;
                String breweryWebsite;
                String breweryEstablished;
                String breweryImageLarge;
                String breweryImageMedium;
                String breweryImageIcon;

                // Get the JSON object representing the day
                JSONObject breweryInfo = breweryArray.getJSONObject(i);

                breweryId = breweryInfo.getString(BDB_BREWERY_ID);
                breweryName = breweryInfo.getString(BDB_BREWERY_NAME);

                if(breweryInfo.has(BDB_BREWERY_DESCRIPTION)) {
                    breweryDescription = breweryInfo.getString(BDB_BREWERY_DESCRIPTION);
                }
                else {
                    breweryDescription = null;
                }

                if(breweryInfo.has(BDB_BREWERY_WEBSITE)) {
                    breweryWebsite = breweryInfo.getString(BDB_BREWERY_WEBSITE);
                }
                else {
                    breweryWebsite = null;
                }

                if(breweryInfo.has(BDB_BREWERY_ESTABLISHED)) {
                    breweryEstablished = breweryInfo.getString(BDB_BREWERY_ESTABLISHED);
                }
                else {
                    breweryEstablished = null;
                }

                if(breweryInfo.has(BDB_BREWERY_IMAGES)) {
                    JSONObject breweryImages = breweryInfo.getJSONObject(BDB_BREWERY_IMAGES);
                    breweryImageLarge = breweryImages.getString(BDB_BREWERY_IMAGE_LARGE);
                    breweryImageMedium = breweryImages.getString(BDB_BREWERY_IMAGE_MEDIUM);
                    breweryImageIcon = breweryImages.getString(BDB_BREWERY_IMAGE_ICON);
                }
                else {
                    breweryImageIcon = null;
                    breweryImageMedium = null;
                    breweryImageLarge = null;
                }

                ContentValues breweryValues = new ContentValues();

                breweryValues.put(BrewskiContract.BreweryEntry.COLUMN_BREWERY_ID, breweryId);
                breweryValues.put(BrewskiContract.BreweryEntry.COLUMN_BREWERY_NAME, breweryName);
                breweryValues.put(BrewskiContract.BreweryEntry.COLUMN_BREWERY_DESCRIPTION, breweryDescription);
                breweryValues.put(BrewskiContract.BreweryEntry.COLUMN_BREWERY_WEBSITE, breweryWebsite);
                breweryValues.put(BrewskiContract.BreweryEntry.COLUMN_ESTABLISHED, breweryEstablished);
                breweryValues.put(BrewskiContract.BreweryEntry.COLUMN_IMAGE_LARGE, breweryImageLarge);
                breweryValues.put(BrewskiContract.BreweryEntry.COLUMN_IMAGE_MEDIUM, breweryImageMedium);
                breweryValues.put(BrewskiContract.BreweryEntry.COLUMN_IMAGE_ICON, breweryImageIcon);

                if(!existsInDb("brewery", breweryId) && !alreadyAddedToTheList(breweryIdList, breweryId)) {
                    breweryIdList.add(breweryId);
                    breweryContentValuesVector.add(breweryValues);
                }
            }

            // add to database
            if (breweryContentValuesVector.size() > 0) {
                ContentValues[] breweryContentValuesArray = new ContentValues[breweryContentValuesVector.size()];
                breweryContentValuesVector.toArray(breweryContentValuesArray);
                getContext().getContentResolver().bulkInsert(BrewskiContract.BreweryEntry.BREWERY_CONTENT_URI, breweryContentValuesArray);
            }

            Log.d(LOG_TAG, "Sync Complete. " + breweryContentValuesVector.size() + " Breweries Inserted");
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void getStyleDataFromJson(String styleJsonStr) throws JSONException {
        // Style Information
        final String BDB_STYLE_ID = "id";
        final String BDB_STYLE_NAME = "name";
        final String BDB_STYLE_SHORT_NAME = "shortName";
        final String BDB_STYLE_DESCRIPTION = "description";
        ArrayList<String> styleIdList = new ArrayList<>();

        // Beer information.  Each beer's info is an element of the "list" array.
        final String BDB_DATA = "data";

        try {
            JSONObject styleJson = new JSONObject(styleJsonStr);
            JSONArray styleArray = styleJson.getJSONArray(BDB_DATA);

            // Insert the new beer information into the database
            Vector<ContentValues> styleContentValuesVector = new Vector<>(styleArray.length());

            for(int i = 0; i < styleArray.length(); i++) {
                // These are the values that will be collected.
                String styleId;
                String styleName;
                String styleShortName;
                String styleDescription;

                // Get the JSON object representing the day
                JSONObject styleInfo = styleArray.getJSONObject(i);

                styleId = styleInfo.getString(BDB_STYLE_ID);
                styleName = styleInfo.getString(BDB_STYLE_NAME);

                if(styleInfo.has(BDB_STYLE_SHORT_NAME)) {
                    styleShortName = styleInfo.getString(BDB_STYLE_SHORT_NAME);
                }
                else {
                    styleShortName = null;
                }

                if(styleInfo.has(BDB_STYLE_DESCRIPTION)) {
                    styleDescription = styleInfo.getString(BDB_STYLE_DESCRIPTION);
                }
                else {
                    styleDescription = null;
                }

                ContentValues styleValues = new ContentValues();

                styleValues.put(BrewskiContract.StyleEntry.COLUMN_STYLE_ID, styleId);
                styleValues.put(BrewskiContract.StyleEntry.COLUMN_STYLE_NAME, styleName);
                styleValues.put(BrewskiContract.StyleEntry.COLUMN_STYLE_SHORT_NAME, styleShortName);
                styleValues.put(BrewskiContract.StyleEntry.COLUMN_STYLE_DESCRIPTION, styleDescription);

                if(!existsInDb("style", styleId) && !alreadyAddedToTheList(styleIdList, styleId)) {
                    styleIdList.add(styleId);
                    styleContentValuesVector.add(styleValues);
                }
            }

            // add to database
            if(styleContentValuesVector.size() > 0) {
                ContentValues[] styleContentValuesArray = new ContentValues[styleContentValuesVector.size()];
                styleContentValuesVector.toArray(styleContentValuesArray);
                getContext().getContentResolver().bulkInsert(BrewskiContract.StyleEntry.STYLE_CONTENT_URI, styleContentValuesArray);
            }

            Log.d(LOG_TAG, "Sync Complete. " + styleContentValuesVector.size() + " Styles Inserted");
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context, String listName) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putString(CALLING_LIST, listName);

        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);
        }

        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        BrewskiSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context, "default");
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    private boolean existsInDb(String type, String id) {
        Cursor cursor = null;

        try {
            cursor = BrewskiContentProvider.mOpenHelper.getReadableDatabase().rawQuery("SELECT " + type + "_id FROM " + type + " WHERE " + type + "_id=\"" + id + "\"", null);

            int count = cursor.getCount();

            if (cursor.getCount() > 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return false;
    }

    private boolean alreadyAddedToTheList(ArrayList<String> list, String id) {
        return list.contains(id);
    }
}