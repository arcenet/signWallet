package com.ecertic.signWallet.ui.quote;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.util.Log;
import android.widget.Spinner;
import android.widget.Toast;

import com.ecertic.signWallet.R;
import com.ecertic.signWallet.dummy.DummyContent;
import com.ecertic.signWallet.ui.base.BaseActivity;
import com.ecertic.signWallet.util.AlarmReceiver;
import com.ecertic.signWallet.util.JSONParser;
import com.ecertic.signWallet.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.ecertic.signWallet.ui.quote.ArticleListFragment.*;

/**
 * Lists all available quotes. This Activity supports a single pane (= smartphones) and a two pane mode (= large screens with >= 600dp width).
 *
 * Created by Andreas Schrade on 14.12.2015.
 */
public class ListActivity extends BaseActivity implements Callback {
    /**
     * Whether or not the activity is running on a device with a large screen
     */
    private boolean twoPaneMode;
    private JSONObject jsonR = new JSONObject();
    private static String url;
    private String lastId;
    private int totalCont;
    private int pendCont = 0;
    private int finCont = 0;
    private int errCont = 0;
    AlertDialog  alert;
    Spinner spinner;
    private List<DummyContent.DummyItem> clone = new ArrayList<DummyContent.DummyItem>();


    @Override
    protected void onRestart(){
        super.onRestart();
        Log.e("Dummies1", DummyContent.ITEMS.toString());
        updateList(false);

    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.e("Dummies2", DummyContent.ITEMS.toString());
        //updateList(false);
    }

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
        updateList(true);


        countCont();
        /*Launch de aplicación a través de un código QR válido*/

        Bundle b = getIntent().getExtras();
        if (b != null) {
            url = b.getString("urlScan");
            if (url != null) {
                Log.d("URL:Scan", url);
                //postURL = "https://api.rubricae.es/api/operation/" + url.substring(29);
                new retrieveJson().execute();
                //updateList();
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

        setNotification();



    }

    private void countCont() {
        totalCont = DummyContent.ITEMS.size();
        for (int i= 0; i < DummyContent.ITEMS.size(); i++){
            if (DummyContent.ITEMS.get(i).status == DummyContent.DummyItem.LISTO){
                pendCont++;
            }
            if (DummyContent.ITEMS.get(i).status == DummyContent.DummyItem.ERROR){
                errCont++;
            }
            if (DummyContent.ITEMS.get(i).status == DummyContent.DummyItem.FINALIZADO){
                finCont++;
            }
        }
    }

    private void setNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(ListActivity.this, AlarmReceiver.class); // AlarmReceiver1 = broadcast receiver

        int count = 0;
        for (int i = 0; i < DummyContent.ITEMS.size();i++) {
            if (DummyContent.ITEMS.get(i).status == DummyContent.DummyItem.LISTO){
                count++;
            }
        }
        alarmIntent.putExtra("Pend",count);

        PendingIntent pendingIntent = PendingIntent.getBroadcast( ListActivity.this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmIntent.setData((Uri.parse("custom://"+System.currentTimeMillis())));



        Log.e("Count", String.valueOf(count));



        Calendar alarmStartTime = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        alarmStartTime.set(Calendar.HOUR_OF_DAY, 1);
        alarmStartTime.set(Calendar.MINUTE, 44);
        alarmStartTime.set(Calendar.SECOND, 0);
        if (now.after(alarmStartTime)) {
            Log.d("Hey","Added a day");
            alarmStartTime.add(Calendar.DATE, 1);
        }

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

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
            Log.d("IDIDIDID",id );

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

    public void errorMessage(){
        Toast.makeText(this, "Error en la operación, operación caduca o inexistente", Toast.LENGTH_LONG).show();
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
        getMenuInflater().inflate(R.menu.list_menu, menu);
        MenuItem item = menu.findItem(R.id.spinner);
        spinner = (Spinner) MenuItemCompat.getActionView(item);
        String[] items = new String[]{"Todos " + "(" + String.valueOf(totalCont) + ")",
                "Pendientes " + "(" + String.valueOf(pendCont) + ")",
                "Finalizados " + "(" + String.valueOf(finCont) + ")",
                "Error " + "(" + String.valueOf(errCont) + ")"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);

        spinner.setAdapter(adapter); // set the adapter to provide layout of rows and content
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                int pos = spinner.getSelectedItemPosition();
                switch (pos){
                    case 0: ArticleListFragment.hiddenPositions.clear();
                            ArticleListFragment fragmentById = (ArticleListFragment) getFragmentManager().findFragmentById(R.id.article_list);
                            fragmentById.update();
                            break;
                    case 1: filterPend();
                            break;
                    case 2: filterFin();
                            break;
                    case 3: filterErr();
                            break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        return true;
    }

    private void filterPend() {
        ArticleListFragment.hiddenPositions.clear();

        for (int i = 0; i <= DummyContent.ITEMS.size()-1;i++){
            if (!((DummyContent.ITEMS.get(i).status == DummyContent.DummyItem.PENDIENTE_DE_ENVIO)|| DummyContent.ITEMS.get(i).status == DummyContent.DummyItem.LISTO)){
                ArticleListFragment.hiddenPositions.add(i);
            }
        }
        ArticleListFragment fragmentById = (ArticleListFragment) getFragmentManager().findFragmentById(R.id.article_list);
        fragmentById.update();
    }
    private void filterFin()  {
        ArticleListFragment.hiddenPositions.clear();

        for (int i = 0; i <= DummyContent.ITEMS.size()-1;i++){
            if (!(DummyContent.ITEMS.get(i).status == DummyContent.DummyItem.FINALIZADO)){
                ArticleListFragment.hiddenPositions.add(i);
            }
        }
        ArticleListFragment fragmentById = (ArticleListFragment) getFragmentManager().findFragmentById(R.id.article_list);
        fragmentById.update();

    }
    private void filterErr()  {

        ArticleListFragment.hiddenPositions.clear();

        for (int i = 0; i <= DummyContent.ITEMS.size()-1;i++){
            Log.e("Status",String.valueOf(DummyContent.ITEMS.get(i).status));
            if (!(DummyContent.ITEMS.get(i).status == DummyContent.DummyItem.ERROR)){
                ArticleListFragment.hiddenPositions.add(i);
            }
        }
        ArticleListFragment fragmentById = (ArticleListFragment) getFragmentManager().findFragmentById(R.id.article_list);
        fragmentById.update();

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
    public void updateList(boolean first){
        int i= DummyContent.ITEMS.size();
        Log.d("Dummy Size ",String.valueOf(i));
        Boolean fileExists = false;
        File dir = getFilesDir();
        File[] subFiles = dir.listFiles();


        if (subFiles != null)
        {
            for (File filet : subFiles)
            {
                Log.d("Files: ",filet.getName());

                //Vericar que para cada archivo JSON, exista un Dummy Item, si no crearlo.

                if (filet.getName().contains(".json")) {

                    for (int f = 0; f <= DummyContent.ITEMS.size() - 1; f++) {

                        if (filet.getName().equals(DummyContent.ITEMS.get(f).content + ".json")){

                            //Cargar al dummy item, el status guardado su archivo ccorrespondiente (cargar datos al iniciar la aplicación)
                            loadDummyStatus(filet,DummyContent.ITEMS.get(f));


                            fileExists = true;
                            Log.d("Names: ", "File Exists");
                            break;

                        }

                    }

                    if (!fileExists) {

                        DummyContent.DummyItem fa = new DummyContent.DummyItem(String.valueOf(i), R.drawable.p5, "Contrato Galp", filet.getName(), filet.getName().substring(0,filet.getName().lastIndexOf('.')));
                        DummyContent.addItem(fa);

                        if (first){
                            loadDummyStatus(filet,fa);
                        }
                        else {
                            fa.status = 0;
                            i++;
                        }

                    }
                }

                fileExists = false;
            }
        }
        lastId = String.valueOf(i-1);
        ArticleListFragment fragmentById = (ArticleListFragment) getFragmentManager().findFragmentById(R.id.article_list);
        fragmentById.update();


    }


    public void loadDummyStatus(File file, DummyContent.DummyItem dummyItem){

        FileInputStream fis;
        String content = "";
        try {
            fis = openFileInput(file.getName());
            byte[] input = new byte[fis.available()];
            while (fis.read(input) != -1) {
            }
            content += new String(input);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            JSONObject json = new JSONObject(content);
            Log.d("JSON",json.toString());
            if (json.has("status")) {
                Log.d("JSONN Status",String.valueOf(json.getInt("status")));
                dummyItem.status = json.getInt("status");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private class retrieveJson extends AsyncTask<String, String, String> {

        private ProgressDialog pDialog;
        HttpURLConnection urlConnection;
        boolean flag = false;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            pDialog = new ProgressDialog(ListActivity.this);
            pDialog.setMessage("Getting Data ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

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
                try {

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                }
                    finally{
                        urlConnection.disconnect();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    flag = true;

                }




            return result.toString();
        }


        @Override
        protected void onPostExecute(String json) {

            if (flag) {
                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                Toast.makeText(ListActivity.this, "Error de conexión a Internet ", Toast.LENGTH_SHORT).show();
            }

            else {
                Boolean error = false;
                FileOutputStream outputStream;
                try {
                    if (json.isEmpty()) {
                        error = true;
                    } else {

                        jsonR = new JSONObject(json);
                        jsonR.put("status", DummyContent.DummyItem.LISTO);
                        Log.d("JSON Content:", json);

                        String pdf64 = jsonR.getJSONObject("file").getString("content");

                        File pdf = new File(getFilesDir(), jsonR.optString("oId") + ".pdf");

                        FileOutputStream fos = new FileOutputStream(pdf);
                        fos.write(Base64.decode(pdf64, Base64.NO_WRAP));
                        fos.close();


                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }

                //Verifica si existe un error en el contenido
                if (!error) {

                    //Guardar archivo con contenido del JSON en un archivo con nombre igual al id de operacion
                    try {
                        File file = new File(getFilesDir(), jsonR.optString("oId") + ".json");


                        if (!file.exists()) {

                            outputStream = openFileOutput(jsonR.optString("oId") + ".json", Context.MODE_PRIVATE);

                            outputStream.write(jsonR.toString().getBytes());
                            outputStream.close();
                            Log.d("Message:", "File saved");
                        } else {
                            Log.d("Message:", "File alerady exists");
                        }


                        Log.d("Directory:", jsonR.optString("oId") + "Filelist: " + getFilesDir());

                        //Imprime la lista de archivos guardados en ese momento
                        File dir = getFilesDir();
                        File[] subFiles = dir.listFiles();
                        if (subFiles != null) {
                            for (File filet : subFiles) {
                                Log.d("Files: ", filet.getName());
                            }
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else {
                    errorMessage();


                }

                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss();
                }

                updateList(false);
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ListActivity.this);

                alertBuilder.setTitle("Aviso");
                alertBuilder.setMessage("Se ha agregado una nueva operación al wallet, ¿deseas proceder con la firma del documento o verlo mas tarde?");
                alertBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        Bundle b = new Bundle();
                        b.putString("id", lastId);

                        Log.d("IDIDIDID", lastId);
                        Intent detailIntent = new Intent(ListActivity.this, ArticleDetailActivity.class);
                        detailIntent.putExtra(ArticleDetailFragment.ARG_ITEM_ID, lastId);
                        detailIntent.putExtras(b);
                        startActivity(detailIntent);

                    }
                });
                alertBuilder.setNegativeButton("Verlo mas tarde", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        alert.dismiss();
                    }
                });


                alert = alertBuilder.create();
                alert.show();

            }

        }
    }
}


