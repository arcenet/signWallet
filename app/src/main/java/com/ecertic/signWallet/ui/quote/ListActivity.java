package com.ecertic.signWallet.ui.quote;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.util.Log;

import com.ecertic.signWallet.R;
import com.ecertic.signWallet.dummy.DummyContent;
import com.ecertic.signWallet.ui.base.BaseActivity;
import com.ecertic.signWallet.util.JSONParser;
import com.ecertic.signWallet.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Lists all available quotes. This Activity supports a single pane (= smartphones) and a two pane mode (= large screens with >= 600dp width).
 *
 * Created by Andreas Schrade on 14.12.2015.
 */
public class ListActivity extends BaseActivity implements ArticleListFragment.Callback {
    /**
     * Whether or not the activity is running on a device with a large screen
     */
    private boolean twoPaneMode;
    JSONObject json = new JSONObject();
    private static String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);


        setupToolbar();

        if (isTwoPaneLayoutUsed()) {
            twoPaneMode = true;
            LogUtil.logD("TEST", "TWO POANE TASDFES");
            enableActiveItemState();
        }

        if (savedInstanceState == null && twoPaneMode) {
            setupDetailFragment();
        }


        /*Petición HTTP a la URL obtenida*/


        Bundle b = getIntent().getExtras();
        if (b != null) {
            url = b.getString("urlScan");
            new retrieveJson().execute();

        }

        if (getIntent().getData() != null) {
            try {
                url = getIntent().getDataString();
                Log.d("URL", url);
                new retrieveJson().execute();

            } catch (Exception e) {
                Log.d("Error", e.toString());
            }
        }

    }

    /**
     * Called when an item has been selected
     *
     * @param id the selected quote ID
     */
    @Override
    public void onItemSelected(String id) {
        if (twoPaneMode) {
            // Show the quote detail information by replacing the DetailFragment via transaction.
            ArticleDetailFragment fragment = ArticleDetailFragment.newInstance(id);
            getFragmentManager().beginTransaction().replace(R.id.article_detail_container, fragment).commit();
        } else {
            // Start the detail activity in single pane mode.
            Intent detailIntent = new Intent(this, ArticleDetailActivity.class);
            detailIntent.putExtra(ArticleDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    private void setupToolbar() {
        final ActionBar ab = getActionBarToolbar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setupDetailFragment() {
        ArticleDetailFragment fragment = ArticleDetailFragment.newInstance(DummyContent.ITEMS.get(0).id);
        getFragmentManager().beginTransaction().replace(R.id.article_detail_container, fragment).commit();
    }

    /**
     * Enables the functionality that selected items are automatically highlighted.
     */
    private void enableActiveItemState() {
        ArticleListFragment fragmentById = (ArticleListFragment) getFragmentManager().findFragmentById(R.id.article_list);
        fragmentById.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    /**
     * Is the container present? If so, we are using the two-pane layout.
     *
     * @return true if the two pane layout is used.
     */
    private boolean isTwoPaneLayoutUsed() {
        return findViewById(R.id.article_detail_container) != null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_quotes;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }

    private class retrieveJson extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            pDialog = new ProgressDialog(ListActivity.this);
            pDialog.setMessage("Getting Data ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            JSONParser jParser = new JSONParser();

            // Getting JSON from URL
            JSONObject json = null;
            try {
                json = jParser.getJSONFromUrl(url);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json;

        }

        @Override
        protected void onPostExecute(JSONObject json) {
            pDialog.dismiss();

            if (json != null) {
                Log.d("JSON Content:", json.toString());
            }
        }

    }
}


