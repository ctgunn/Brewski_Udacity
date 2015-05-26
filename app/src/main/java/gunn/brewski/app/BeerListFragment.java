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
import android.widget.Toast;


import gunn.brewski.app.data.BrewskiContract;
import gunn.brewski.app.sync.BrewskiSyncAdapter;

public class BeerListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = BeerListFragment.class.getSimpleName();
    private BeerListAdapter mBeerListAdapter;

    private static final String BEERS_SHARE_HASHTAG = " #BrewskiBeers";

    private ListView mBeerListView;
    private int mPosition = ListView.INVALID_POSITION;
    private boolean mUseTodayLayout;
    private boolean loadingMore;
    private IntentFilter beerFilter;

    private static final String SELECTED_KEY = "selected_position";

    private BroadcastReceiver beerReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            loadingMore = false;
            getLoaderManager().restartLoader(BEER_LIST_LOADER, null, BeerListFragment.this);
        }
    };

    private static final int BEER_LIST_LOADER = 0;

    private static final String[] BEER_COLUMNS = {
        BrewskiContract.BeerEntry.TABLE_NAME + "." + BrewskiContract.BeerEntry._ID,
        BrewskiContract.BeerEntry.COLUMN_BEER_ID,
        BrewskiContract.BeerEntry.COLUMN_BEER_NAME,
        BrewskiContract.BeerEntry.COLUMN_BEER_DESCRIPTION,
        BrewskiContract.BeerEntry.COLUMN_LABEL_MEDIUM
    };

    static final int COL_BEER_ID = 1;
    static final int COL_BEER_NAME = 2;
    static final int COL_BEER_DESCRIPTION = 3;
    static final int COL_LABEL_MEDIUM = 4;

    public interface Callback {
        /**
         * BeerDetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    public BeerListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);

        beerFilter = new IntentFilter("moreBeersLoaded");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_beer_list_fragment, menu);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBeerListAdapter = new BeerListAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_beer_list, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mBeerListView = (ListView) rootView.findViewById(R.id.listview_beer);
        mBeerListView.setAdapter(mBeerListAdapter);
        mBeerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            // CursorAdapter returns a cursor at the correct position for getItem(), or null
            // if it cannot seek to that position.
            Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
            BrewskiApplication.setCurrentBeerId(cursor.getString(COL_BEER_ID));
            if (cursor != null) {
                ((Callback) getActivity()).onItemSelected(
                    BrewskiContract.BeerEntry.buildBeerUriWithBeerId(cursor.getString(COL_BEER_ID))
                );
            }

            mPosition = position;
            }
        });

        mBeerListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                int lastInScreen = firstVisibleItem + visibleItemCount;

                if ((lastInScreen >= (totalItemCount - 25)) && !(loadingMore)) {
                    loadingMore = true;
                    syncBeer();
                }

                mPosition = ListView.INVALID_POSITION;
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        mBeerListAdapter.setUseTodayLayout(mUseTodayLayout);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(BEER_LIST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void syncBeer() {
        BrewskiSyncAdapter.syncImmediately(getActivity(), "beer");
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

        // To only show current and future dates, filter the query to return weather only for
        // dates after or including today.

        // Sort order:  Ascending, by name.
        String sortOrder = BrewskiContract.BeerEntry.COLUMN_BEER_NAME + " ASC";

        Uri beerUri = BrewskiContract.BeerEntry.BEER_CONTENT_URI;

        return new CursorLoader(
            getActivity(),
            beerUri,
            BEER_COLUMNS,
            null,
            null,
            sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mBeerListAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mBeerListView.smoothScrollToPosition(mPosition);
        }

        loadingMore = false;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mBeerListAdapter.swapCursor(null);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mBeerListAdapter != null) {
            mBeerListAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPosition = ListView.INVALID_POSITION;
        getActivity().registerReceiver(beerReceiver, beerFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(beerReceiver);
    }
}
