package com.ecertic.signWallet.ui.quote;

import android.app.ProgressDialog;
import android.content.Context;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
    private JSONObject jsonR = new JSONObject();
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

        //Actualiza la lista de contratos
        updateList();

        /*Launch de aplicación a través de un código QR válido*/

        Bundle b = getIntent().getExtras();
        if (b != null) {
            url = b.getString("urlScan");
            if (url != null) {
                Log.d("URL:Scan", url);
                //postURL = "https://api.rubricae.es/api/operation/" + url.substring(29);
                new retrieveJson().execute();
            }

        }

        /*Launch de aplicación a través de un applink válido*/

        if (getIntent().getData() != null) {
            try {
                url = getIntent().getDataString();
                Log.d("URL:AppLink", url);
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
            Bundle b = new Bundle();
            b.putString("id", id);

            Intent detailIntent = new Intent(this, ArticleDetailActivity.class);
            detailIntent.putExtra(ArticleDetailFragment.ARG_ITEM_ID, id);
            detailIntent.putExtras(b);
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


    //Actualiza la lista de contratos: un contrato por archivo JSON en el Internal Storage
    public void updateList(){
        int i= 5;
        File dir = getFilesDir();
        File[] subFiles = dir.listFiles();


        if (subFiles != null)
        {
            for (File filet : subFiles)
            {
                Log.d("Files: ",filet.getName());
                if (!filet.getName().equals("instant-run")) {
                    DummyContent.addItem(new DummyContent.DummyItem(String.valueOf(i), R.drawable.p5, "Contrato Galp", "Empresa X", filet.getName()));
                    i++;
                }
            }
        }

    }



    private class retrieveJson extends AsyncTask<String, String, String> {

        private ProgressDialog pDialog;
        HttpURLConnection urlConnection;

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
        protected String doInBackground(String... args) {
            JSONParser jParser = new JSONParser();
            StringBuilder result = new StringBuilder();

            /* Getting JSON from URL
            JSONObject json = null;
            try {
                json = jParser.getJSONFromUrl(url);
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
            try {
                URL postURL = new URL(url);
                urlConnection = (HttpURLConnection) postURL.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            }catch( Exception e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }


            return result.toString();
        }


        @Override
        protected void onPostExecute(String json) {
            pDialog.dismiss();
            FileOutputStream outputStream;
            try {
                if (json != null) {
                    jsonR = new JSONObject(json);
                    Log.d("JSON Content:", json.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Guardar archivo con contenido del JSON en un archivo con nombre igual al id de operacion
            try {
                File file = new File(getFilesDir(),jsonR.optString("oId").toString());



                if (!file.exists()) {

                    outputStream = openFileOutput(jsonR.optString("oId").toString(), Context.MODE_PRIVATE);

                    outputStream.write(jsonR.toString().getBytes());
                    outputStream.close();
                    Log.d("Message:", "File created");
                }
                else{
                    Log.d("Message:", "File alerady exists");
                }



                Log.d("Directory:", jsonR.optString("oId").toString() + "Filelist: " + getFilesDir());

                //Imprime la lista de archivos guardados en ese momento
                File dir = getFilesDir();
                File[] subFiles = dir.listFiles();
                if (subFiles != null)
                {
                    for (File filet : subFiles)
                    {
                        Log.d("Files: ",filet.getName());
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}


