package gunn.brewski.app.main.brewery;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import gunn.brewski.app.R;

/**
 * Created by SESA300553 on 4/2/2015.
 */
public class BreweryListAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_BREWERY = 0;
    private static final int VIEW_TYPE_SELECTED_BREWERY = 1;

    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseTodayLayout = true;

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView largeImageView;
        public final TextView breweryNameView;
        public final TextView breweryDescriptionView;

        public ViewHolder(View view) {
            largeImageView = (ImageView) view.findViewById(R.id.list_item_icon);
            breweryNameView = (TextView) view.findViewById(R.id.list_item_brewery_name_textview);
            breweryDescriptionView = (TextView) view.findViewById(R.id.list_item_brewery_description_textview);
        }
    }

    public BreweryListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (0) {
            case VIEW_TYPE_BREWERY: {
                layoutId = R.layout.list_item_brewery;
                break;
            }
            case VIEW_TYPE_SELECTED_BREWERY: {
                layoutId = R.layout.list_item_brewery_selected;
                break;
            }
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        if (null != cursor.getString(BreweryListFragment.COL_IMAGE_MEDIUM)) {
            ImageLoader.getInstance().displayImage(cursor.getString(BreweryListFragment.COL_IMAGE_MEDIUM), viewHolder.largeImageView);
        }
        else {
            viewHolder.largeImageView.setImageResource(R.drawable.brewski_default);
        }

        // Read date from cursor
        String breweryName = cursor.getString(BreweryListFragment.COL_BREWERY_NAME);
        viewHolder.breweryNameView.setText(breweryName);

        String breweryDescription = "";

        if(null != cursor.getString(BreweryListFragment.COL_BREWERY_DESCRIPTION)) {
            breweryDescription = cursor.getString(BreweryListFragment.COL_BREWERY_DESCRIPTION);
        }

        if(!TextUtils.isEmpty(breweryDescription) && breweryDescription.length() > 100) {
            breweryDescription = breweryDescription.substring(0, 100) + "...";
        }

        viewHolder.breweryDescriptionView.setText(breweryDescription);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_BREWERY : VIEW_TYPE_SELECTED_BREWERY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}
