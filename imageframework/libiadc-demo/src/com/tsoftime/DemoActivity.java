package com.tsoftime;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;
import com.tsoftime.libjsonet.JSONNet;
import com.tsoftime.libjsonet.OnExecuteDoneCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 示例Activity
 */
public class DemoActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // 初始化ImageManager。
        ImageManager.init(getApplicationContext());
        ImageManager.instance().setDownloadThreadNumber(2);

        listView = (PullToRefreshListView) findViewById(R.id.listview);
        adaper = new ImageListViewAdaper(DemoActivity.this, listView);

        pd = new ProgressDialog(this);
        pd.setIndeterminate(true);
        pd.show();

        JSONNet net = new JSONNet();

        // callback and callback parameters
        MyCallback callback = new MyCallback();
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("pd", pd);

        // request parameters
        String strParams = "";
        JSONObject json = new JSONObject();
        try {
            json.put("type", 2);
            json.put("start", 0);
            json.put("limit", 120);
            strParams = json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // post the request.
        net.post("42.121.132.22", 3000, "/travel/get_travel_list", strParams, callback, params);
    }

    private class LoginTask extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... voids)
        {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPostExecute(Void params)
        {
            listView.onRefreshComplete();
        }
    }


    private class MyCallback implements OnExecuteDoneCallback
    {

        @Override
        public void onExecuteDone(int status, JSONObject content, HashMap<String, Object> params)
        {
            if (status == HttpURLConnection.HTTP_OK) {
                final ArrayList<String> urls = new ArrayList<String>();
                JSONArray travels = null;
                try {
                    travels = content.getJSONObject("data").getJSONArray("travels");
                    for (int i = 0; i < travels.length(); ++i) {
                        JSONObject travel = travels.getJSONObject(i);
                        if (travel == null) continue;
                        Log.d("DemoActivity", travel.toString(4));
                        String cover = travel.getString("origin_cover");
                        if (cover == null) continue;
                        adaper.addURL(cover);
                        urls.add(cover);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(DemoActivity.class.getSimpleName(), String.format("url count %d", urls.size()));

                listView.setAdapter(adaper);
                listView.setFastScrollEnabled(true);
                listView.setOnScrollListener(new AbsListView.OnScrollListener()
                {
                    private int firstVisibleItem;
                    private int visibleItemCount;

                    @Override
                    public void onScrollStateChanged(AbsListView absListView, int scrollState)
                    {
                        ImageManager imageManager = ImageManager.instance();
                        switch (scrollState) {
                            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                                //Log.d(DemoActivity.class.getSimpleName(), "Scroll view idle.");
                                imageManager.removeAllTasks();
                                for (int i = 0, j = firstVisibleItem - 1; i < visibleItemCount; ++i, ++j) {
                                    Log.d(DemoActivity.class.getSimpleName(), String.format("downloading ... %d.jpg", j));
                                    adaper.downloadImage(j);
                                }
                                break;
                            default:
                                break;
                        }
                    }

                    @Override
                    public void onScroll(AbsListView absListView, int firstVisibleItem
                        , int visibleItemCount, int totalItemCount)
                    {
                        this.firstVisibleItem = firstVisibleItem;
                        this.visibleItemCount = visibleItemCount;
                    }
                });
                listView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener()
                {
                    @Override
                    public void onRefresh()
                    {
                        new LoginTask().execute();
                    }
                });

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                    {
                        Intent intent = new Intent(DemoActivity.this, ImageViewActivity.class);
                        intent.putExtra("urls", urls);
                        intent.putExtra("url", (String)adaper.getItem(i - 1));
                        startActivity(intent);
                        overridePendingTransition(R.anim.zoomout, R.anim.zoomin);
                    }
                });
                ProgressDialog pd = (ProgressDialog) params.get("pd");
                pd.dismiss();
            }
        }
    }

    private ProgressDialog pd;
    private PullToRefreshListView listView;
    private ImageListViewAdaper adaper;

}
