package gunn.brewski.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;


import gunn.brewski.app.data.BrewskiContract;
import gunn.brewski.app.sync.BrewskiSyncAdapter;

public class BreweryListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = BreweryListFragment.class.getSimpleName();
    private BreweryListAdapter mBreweryListAdapter;

    private static final String BREWERIES_SHARE_HASHTAG = " #BrewskiBreweries";

    private ListView mBreweryListView;
    private int mPosition = ListView.INVALID_POSITION;
    private boolean mUseTodayLayout;
    private boolean loadingMore;
    private IntentFilter breweryFilter;

    private static final String SELECTED_KEY = "selected_position";

    private BroadcastReceiver breweryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            loadingMore = false;
            getLoaderManager().restartLoader(BREWERY_LIST_LOADER, null, BreweryListFragment.this);
        }
    };

    private static final int BREWERY_LIST_LOADER = 1;

    private static final String[] BREWERY_COLUMNS = {
        BrewskiContract.BreweryEntry.TABLE_NAME + "." + BrewskiContract.BreweryEntry._ID,
        BrewskiContract.BreweryEntry.COLUMN_BREWERY_ID,
        BrewskiContract.BreweryEntry.COLUMN_BREWERY_NAME,
        BrewskiContract.BreweryEntry.COLUMN_BREWERY_DESCRIPTION,
        BrewskiContract.BreweryEntry.COLUMN_IMAGE_MEDIUM
    };

    // These indices are tied to BREWERY_COLUMNS.  If BREWERY_COLUMNS changes, these
    // must change.
    static final int COL_BREWERY_ID = 1;
    static final int COL_BREWERY_NAME = 2;
    static final int COL_BREWERY_DESCRIPTION = 3;
    static final int COL_IMAGE_MEDIUM = 4;

    public interface Callback {
        /**
         * BreweryDetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    public BreweryListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);

        breweryFilter = new IntentFilter("moreBreweriesLoaded");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_beer_list_fragment, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBreweryListAdapter = new BreweryListAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_brewery_list, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mBreweryListView = (ListView) rootView.findViewById(R.id.listview_brewery);
        mBreweryListView.setAdapter(mBreweryListAdapter);

        mBreweryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                BrewskiApplication.setCurrentBreweryId(cursor.getString(COL_BREWERY_ID));
                if (cursor != null) {
                    ((Callback) getActivity()).onItemSelected(
                        BrewskiContract.BreweryEntry.buildBreweryUriWithBreweryId(cursor.getString(COL_BREWERY_ID))
                    );
                }

                mPosition = position;
            }
        });

        mBreweryListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                int lastInScreen = firstVisibleItem + visibleItemCount;

                if ((lastInScreen >= (totalItemCount - 25)) && !(loadingMore)) {
                    loadingMore = true;
                    syncBrewery();
                }

                mPosition = ListView.INVALID_POSITION;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        mBreweryListAdapter.setUseTodayLayout(mUseTodayLayout);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(BREWERY_LIST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void syncBrewery() {
        BrewskiSyncAdapter.syncImmediately(getActivity(), "brewery");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // Sort order:  Ascending, by brewery name.
        String sortOrder = BrewskiContract.BreweryEntry.COLUMN_BREWERY_NAME + " ASC";

        Uri breweryUri = BrewskiContract.BreweryEntry.BREWERY_CONTENT_URI;

        return new CursorLoader(getActivity(),
                breweryUri,
                BREWERY_COLUMNS,
                BrewskiContract.BreweryEntry.TABLE_NAME + "." +
                BrewskiContract.BreweryEntry.COLUMN_BREWERY_NAME + " IS NOT NULL",
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mBreweryListAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mBreweryListView.smoothScrollToPosition(mPosition);
        }

        loadingMore = false;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mBreweryListAdapter.swapCursor(null);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mBreweryListAdapter != null) {
            mBreweryListAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPosition = ListView.INVALID_POSITION;
        getActivity().registerReceiver(breweryReceiver, breweryFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(breweryReceiver);
    }
}
