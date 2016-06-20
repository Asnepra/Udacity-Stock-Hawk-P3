package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class DetailActivity extends AppCompatActivity {
    ArrayList<String> graphValue;
    ArrayList<String> graphYear;
    ArrayList<Entry> stockEntries;
    RequestQueue requestQueue;
    LineChart lineChart;
    String stockSymbol, stockPrice, valueChange, percentChange;
    TextView detailStockSymbol, detailStockValue, detailChangePrice, detailChangePercentage;
    int color;
    String todayDate, startDate, date = "";

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

    }

    @Override
    protected void onStart() {
        super.onStart();
        initialize();
        Intent intent = getIntent();
        stockSymbol = intent.getStringExtra(getString(R.string.stock_name_key_intent));
        stockPrice = intent.getStringExtra(getString(R.string.stock_value_key_intent));
        percentChange = intent.getStringExtra(getString(R.string.stock_percent_key_intent));
        valueChange = intent.getStringExtra(getString(R.string.stock_value_change_key_intent));
        populate();
    }

    private void initialize() {
        detailStockSymbol = (TextView) findViewById(R.id.detail_stock_symbol);
        detailStockValue = (TextView) findViewById(R.id.detail_stock_value);
        detailChangePrice = (TextView) findViewById(R.id.detail_change_price);
        detailChangePercentage = (TextView) findViewById(R.id.detail_change_percentage);
        //barChart = (BarChart) findViewById(R.id.chart);
        lineChart = (LineChart) findViewById(R.id.chart);

    }

    public void populate() {
        //Date Checking
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        todayDate = simpleDateFormat.format(calendar.getTime());
        Calendar calendar1 = Calendar.getInstance();
        calendar1.add(Calendar.YEAR, -1);
        startDate = simpleDateFormat.format(calendar1.getTime());
        //Toast.makeText(DetailActivity.this, "Date \n" + todayDate + "initial date \n" + startDate, Toast.LENGTH_LONG).show();

        //set Stock Symbol
        detailStockSymbol.setText(stockSymbol + " " + getText(R.string.stock_last_year));
        //Check color
        char c = valueChange.charAt(0);
        if (c == '+')
            color = Color.GREEN;
        else if (c == '-')
            color = Color.RED;
        else
            color = Color.YELLOW;
        //Toast.makeText(DetailActivity.this, "Sign is " + c, Toast.LENGTH_SHORT).show();
        //detailStockValue.setBackgroundColor(color);
        //Set Stock Price
        detailStockValue.setText(stockPrice);
        //Set Stock Value Change
        detailChangePrice.setText(valueChange + getText(R.string.dollar_sign));
        detailChangePrice.setTextColor(color);
        //Set Stock Percent Change
        detailChangePercentage.setText(percentChange);
        detailChangePercentage.setTextColor(color);
        getHistoricalData(stockSymbol);
    }

    public void getHistoricalData(String symbol) {
        String endDate = todayDate;
        stockEntries = new ArrayList<>();
        graphYear = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(DetailActivity.this);
        graphValue = new ArrayList<>();
        String q = getString(R.string.query_initial_string) +
                symbol +
                "%22%20and%20" +
                "startDate%20%3D%20%22" +
                startDate +
                "%22%20and%20" +
                "endDate%20%3D%20%22" +
                endDate +
                "%22%0A&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=  ";
        String baseUrl = getString(R.string.query_base) + q;
        String tryUrl = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22GOOG%22%20and%20startDate%20%3D%20%222015-06-14%22%20and%20endDate%20%3D%20%222016-06-14%22%0A&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(baseUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject queryObject = response.getJSONObject("query");
                            JSONObject resultObject = queryObject.getJSONObject("results");
                            JSONArray data = resultObject.getJSONArray("quote");
                            JSONObject stockObject;
                            for (int i = 0; i < data.length(); i++) {
                                stockObject = data.getJSONObject(i);
                                double closeValue = stockObject.getDouble("Close");
                                date = stockObject.getString("Date");
                                //Toast.makeText(DetailActivity.this, "Value Returned " + closeValue, Toast.LENGTH_LONG).show();
                                graphValue.add(String.valueOf(closeValue));
                                graphYear.add(date);
                                Entry entry = new Entry((float) closeValue, i);
                                stockEntries.add(entry);
                            }
                            //Toast.makeText(DetailActivity.this, "Historical Data Fetch Sucessfull ", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            //Log.e("Fetch Error", e.toString());
                            Toast.makeText(DetailActivity.this, getString(R.string.fetch_error) + e.toString(), Toast.LENGTH_LONG).show();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    //Populate Graph
                                    fillGraph();
                                } catch (Exception e) {
                                    Toast.makeText(DetailActivity.this, getString(R.string.graph_filling_error) + e.toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(DetailActivity.this, getString(R.string.error_parsing_json) + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES
                , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);
    }


    public void fillGraph() {
        //Collections.reverse(stockEntries);
        //Collections.reverse(graphYear);
        LineDataSet dataset = new LineDataSet(stockEntries, "Stock Values Over Last Year");
        //LineData data = new LineData(labels, dataset);
        LineData data = new LineData(graphYear, dataset);
        int color = getResources().getColor(R.color.text);
        data.setValueTextColor(color);
        //data.setColors(Color.WHITE);
        lineChart.setData(data); // set the data and list of lables into chart
        lineChart.setDescription("Description ");
        lineChart.animateX(2000);
    }
}

