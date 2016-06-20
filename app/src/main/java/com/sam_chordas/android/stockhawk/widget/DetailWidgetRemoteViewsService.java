package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by Ankit on 6/18/2016.
 */
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = "Query Result Stock";
    public static final String[] STOCK_COLUMNS = {QuoteColumns._ID,
            QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
            QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE,
            QuoteColumns.ISUP};

    /**
     * To be implemented by the derived service to generate appropriate factories for
     * the data.
     *
     * @param intent
     */
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            Cursor data = null;

            /**
             * Called when your factory is first constructed. The same factory may be shared across
             * multiple RemoteViewAdapters depending on the intent passed.
             */
            @Override
            public void onCreate() {

            }

            /**
             * Called when notifyDataSetChanged() is triggered on the remote adapter. This allows a
             * RemoteViewsFactory to respond to data changes by updating any internal references.
             * <p/>
             * Note: expensive tasks can be safely performed synchronously within this method. In the
             * interim, the old data will be displayed within the widget.
             *
             * @see AppWidgetManager#notifyAppWidgetViewDataChanged(int[], int)
             */
            @Override
            public void onDataSetChanged() {
                //Refresh The Cursor
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                        STOCK_COLUMNS,
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        null);
                /*String stockSymbol = "";
                if (data != null) {
                    if (data.moveToFirst()) {
                        stockSymbol = data.getString(data.getColumnIndex(QuoteColumns.SYMBOL));
                    }
                }
                Log.e(LOG_TAG, stockSymbol);*/
                Binder.restoreCallingIdentity(identityToken);
            }

            /**
             * Called when the last RemoteViewsAdapter that is associated with this factory is
             * unbound.
             */
            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            /**
             * See
             *
             * @return Count of items.
             */
            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            /**
             * See
             * <p/>
             * Note: expensive tasks can be safely performed synchronously within this method, and a
             * loading view will be displayed in the interim. See {@link #getLoadingView()}.
             *
             * @param position The position of the item within the Factory's data set of the item whose
             *                 view we want.
             * @return A RemoteViews object corresponding to the data at the specified position.
             */
            @Override
            public RemoteViews getViewAt(int position) {
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);
                if (position == AdapterView.INVALID_POSITION
                        || data == null || !data.moveToPosition(position)) {
                    return null;
                }
                String stockSymbol = data.getString(data.getColumnIndex(QuoteColumns.SYMBOL));
                String price = data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE));
                String percentChange = data.getString(data.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
                String valueChange = data.getString(data.getColumnIndex(QuoteColumns.CHANGE));
                views.setTextViewText(R.id.widget_stock_symbol, stockSymbol);
                views.setTextViewText(R.id.widget_bid_price, price);

                Intent intent1 = new Intent();
                //Symbol
                intent1.putExtra("StockName", stockSymbol);
                //Stock Price
                intent1.putExtra("StockValue", price);
                //Stock Percentage Change
                intent1.putExtra("StockPercent", percentChange);
                //Stock Value Change
                intent1.putExtra("StockValueChange", valueChange);
                views.setOnClickFillInIntent(R.id.widget_list_item, intent1);
                return views;
            }


            /**
             * This allows for the use of a custom loading view which appears between the time that
             * {@link #getViewAt(int)} is called and returns. If null is returned, a default loading
             * view will be used.
             *
             * @return The RemoteViews representing the desired loading view.
             */
            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            /**
             *
             *
             * @return The number of types of Views that will be returned by this factory.
             */
            @Override
            public int getViewTypeCount() {
                return 1;
            }

            /**
             * See
             *
             * @param position The position of the item within the data set whose row id we want.
             * @return The id of the item at the specified position.
             */
            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(0);
                return position;
            }

            /**
             * See
             *
             * @return True if the same id always refers to the same object.
             */
            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
