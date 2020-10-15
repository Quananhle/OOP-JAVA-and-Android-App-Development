package com.example.stockwatch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Quan Le
 */

public class MainActivity extends AppCompatActivity
        implements View.OnLongClickListener, View.OnLongClickListener{
    private final List<Stock> stockList = new ArrayList<>();
    private RecyclerView recyclerView;
    private static final String TAG = "MainActivity";
    private StockAdapter stockAdapter;
    private SwipeRefreshLayout swipeRefresh;

    private static final int ADD_CODE = 1;
    private static final int UPDATE_CODE = 2;

    private DatabaseHandler databaseHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "onRefresh: ");
                doRefresh();
            }
        });

        stockAdapter = new StockAdapter(stockList, this);
        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseHandler = new DatabaseHandler(this);
        //Load the data
        StockLoaderRunnable stockLoaderRunnable = new StockLoaderRunnable(this);
        new Thread(stockLoaderRunnable).start();
    }
//    public void execRunnable(View v){
//        String stockData = editText.getText().toString();
//        StockDataGetter dataGetter = new StockDataGetter(this, stockData);
//        new Thread(dataGetter).start();
//        Log.d(TAG, "run: Pretending");
//    }
    @Override
    public void onClick(View v){
        int pos = recyclerView.getChildLayoutPosition(v);
        Stock s = stockList.get(pos);
        Intent intent = new Intent(MainActivity.this, StockDetailActivity.class);
        intent.putExtra(Stock.class.getName(), s);
        startActivityForResult(intent, UPDATE_CODE);
    }
    @Override
    public boolean onLongClick(View v){
        final int pos = recyclerView.getChildLayoutPosition(v);
        Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
        return true;
    }
    @Override
    public void onBackPressed(){

    }
    @Override
    public void onResume(){
        databaseHandler.dumpDbToLog();
        ArrayList<Stock> watchList = databaseHandler.loadStocks();
        stockList.clear();
        stockList.addAll(watchList);
        Log.d(TAG, "onResume: " + watchList);
        stockAdapter.notifyDataSetChanged();
        super.onResume();
    }
    public void onDestroy(){
        databaseHandler.shutDown();
        super.onDestroy();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        if (requestCode != ADD_CODE && requestCode != UPDATE_CODE){
            Log.d(TAG, "onActivityResult: Unknown Request Code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK){
            Toast.makeText(this, "ERROR ADDING NEW STOCK", Toast.LENGTH_SHORT).show();
            return;
        }
        Stock stock = null;
        if (data.hasExtra("COMPANY")){
            stock = (Stock) data.getSerializableExtra("COMPANY");
        }
        if (stock == null){
            Toast.makeText(this, "COMPANY DATA NOT FOUND", Toast.LENGTH_SHORT).show();
            return;
        }
        //Make database request
        switch (requestCode){
            case ADD_CODE:
                databaseHandler.addStock(stock);
                break;
            case UPDATE_CODE:
                databaseHandler.updateList(stock);
                break;
        }
    }
    //========================HELPER===================================\\
    public void doAdd(View v){
        Intent intent = new Intent(this, dialog.class);
        startActivityForResult(int, ADD_CODE);
    }

    public void updateStockData(ArrayList<Stock> stockList){
        this.stockList.addAll(stockList);
        this.stockAdapter.notifyDataSetChanged();
    }
    public void downloadFailed(){
        this.stockList.clear();
        this.stockAdapter.notifyDataSetChanged();
    }
    public void doRefresh(){
        Log.d(TAG, "onRefresh: ");
        new Thread(new ThreadedClass(this)).start();
        stockAdapter.notifyDataSetChanged();
        swipeRefresh.setRefreshing(false);
    }
    public void doneRefresh(){
        swipeRefresh.setRefreshing(false);
    }
}