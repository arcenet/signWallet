package com.ecertic.signWallet.ui.quote;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ecertic.signWallet.ui.SignatureActivity;
import com.ecertic.signWallet.ui.base.BaseFragment;
import com.bumptech.glide.Glide;

import butterknife.Bind;
import butterknife.OnClick;

import com.ecertic.signWallet.R;
import com.ecertic.signWallet.dummy.DummyContent;
import com.ecertic.signWallet.ui.base.BaseActivity;
import com.ecertic.signWallet.util.JSONParser;
import com.ecertic.signWallet.util.PDFActivity;

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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.Intent.getIntent;

/**
 * Shows the quote detail page.
 *
 * Created by Andreas Schrade on 14.12.2015.
 */
public class ArticleDetailFragment extends BaseFragment {

    public JSONObject json;
    public JSONObject jsonR;

    /**
     * The argument represents the dummy item ID of this fragment.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content of this fragment.
     */
    private DummyContent.DummyItem dummyItem;

    @Bind(R.id.main_content)
    CoordinatorLayout layoutRoot;

    @Bind(R.id.quote)
    TextView quote;

    @Bind(R.id.author)
    TextView author;

    @Bind(R.id.backdrop)
    ImageView backdropImg;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // load dummy item by using the passed item ID.
            dummyItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }

        setHasOptionsMenu(true);

        getJSON();

        /*try {
            Log.d("JSON:",json.getString("oId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }*/


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflateAndBind(inflater, container, R.layout.fragment_article_detail);

        if (!((BaseActivity) getActivity()).providesActivityToolbar()) {
            // No Toolbar present. Set include_toolbar:
            ((BaseActivity) getActivity()).setToolbar((Toolbar) rootView.findViewById(R.id.toolbar));
        }

        if (dummyItem != null) {
            loadBackdrop();
            collapsingToolbar.setTitle(dummyItem.title);
            author.setText(dummyItem.author);
            quote.setText(dummyItem.content);
        }

        //CoordinatorLayout signBtnCL = (CoordinatorLayout) rootView.findViewById(R.id.signLayout);

        CoordinatorLayout sendBtnCL = (CoordinatorLayout) rootView.findViewById(R.id.sendLayout);
        sendBtnCL.setVisibility(View.GONE);

        //Código para deshabilitar/desaparecer el botón de envio

        if (dummyItem != null && dummyItem.status == DummyContent.DummyItem.PENDIENTE_DE_ENVIO) {

            sendBtnCL.setVisibility(View.VISIBLE);
        }

        /*if (dummyItem.status == DummyContent.DummyItem.FINALIZADO){
            signBtnCL.setVisibility(View.GONE);
        }*/

        /**FloatingActionButton sendButton =  (FloatingActionButton) rootView.findViewById(R.id.send);
        CoordinatorLayout sendBtnCL = (CoordinatorLayout) rootView.findViewById(R.id.sendLayout);
        sendBtnCL.setVisibility(View.GONE);
        sendButton.setEnabled(false);

        sendButton.clearAnimation();
        sendButton.setVisibility(View.GONE);**/



        return rootView;
    }

    private void loadBackdrop() {
        Glide.with(this).load(dummyItem.photoId).centerCrop().into(backdropImg);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sample_actions, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // your logic
                // Aqui van las opciones del menu de arriba a la derecha...
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*@OnClick(R.id.sign)
    public void onSignClick(View view) {
        Intent signIntent = new Intent(getActivity().getApplicationContext(), SignatureActivity.class);
        Bundle b = new Bundle();

        b.putString("oId", dummyItem.content);
        b.putString("dummyID", dummyItem.id);

        signIntent.putExtras(b);
        startActivityForResult(signIntent,1);
    }*/

    @OnClick(R.id.pdf)
    public void onPdfClick(View view) {
        String id = dummyItem.content;
        Intent pdfIntent = new Intent(getActivity(), PDFActivity.class);

        File pdfFile = new File(getActivity().getFilesDir(), id + ".pdf");;

        pdfIntent.putExtra("file",pdfFile);
        pdfIntent.putExtra("oId", dummyItem.content);
        pdfIntent.putExtra("dummyID", dummyItem.id);

        //Pasar true a actividad PDF si el status de la operación es: FIRMADO

        if (dummyItem.status == DummyContent.DummyItem.FINALIZADO){
            pdfIntent.putExtra("firmado", true);
        }

        startActivityForResult(pdfIntent,1);

    }

    @OnClick(R.id.send)
    public void onSendClick(View view) {
        new sendJson().execute();



    }

    public void signPad(View view) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.e("Request Code", String.valueOf(requestCode));

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                Toast.makeText(getActivity(),"Finished",Toast.LENGTH_LONG);
                //String result=data.getStringExtra("result");
                getJSON();
                //FloatingActionButton signButton =  (FloatingActionButton) getActivity().findViewById(R.id.sign);
                //signButton.setEnabled(false);
                dummyItem.status = DummyContent.DummyItem.PENDIENTE_DE_ENVIO;
                updateFileStatus();

                //CoordinatorLayout sendBtnCL = (CoordinatorLayout) getView().findViewById(R.id.sendLayout);
                //sendBtnCL.setVisibility(View.VISIBLE);

                //Enviar firma a rubricae
                new sendJson().execute();

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getActivity(),"Canceled",Toast.LENGTH_LONG);
                //Write your code if there's no result
            }
        }
    }//onActivityResult

    public void formatJSON() throws JSONException {
        jsonR = new JSONObject();
        jsonR.put("oId",json.get("oId"));
        JSONArray signers = json.getJSONArray("signers");
        jsonR.put("sId",signers.getJSONObject(0).get("sId"));
        jsonR.put("profile",signers.getJSONObject(0).getJSONObject("profile"));
        jsonR.put("signature",signers.getJSONObject(0).getJSONObject("signature"));
        //signers.getJSONObject(0).getJSONObject("system").p("ip","127.0.0.1");
        jsonR.put("system",signers.getJSONObject(0).getJSONObject("system"));
        jsonR.getJSONObject("system").put("ip","127.0.0.1");

        Log.d("SYSTEM JSON:", jsonR.getJSONObject("system").toString());
        Log.d("FORMATTED JSON:", jsonR.toString());
    }

    public void updateFileStatus()  {
        try {
            json.put("status",dummyItem.status);


            saveJSONFile();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Guardar Cambios en el archivo

    public void saveJSONFile(){
        try {
            FileOutputStream outputStream;

            File file = new File(getActivity().getFilesDir(), json.optString("oId"));




                outputStream = getActivity().openFileOutput(json.optString("oId"), Context.MODE_PRIVATE);

                outputStream.write(json.toString().getBytes());
                outputStream.close();
                Log.d("Message:", "File Overwritten");



            Log.d("Directory:", json.optString("oId") + "Filelist: " + getActivity().getFilesDir());

            //Imprime la lista de archivos guardados en ese momento
            File dir = getActivity().getFilesDir();
            File[] subFiles = dir.listFiles();
            if (subFiles != null) {
                for (File filet : subFiles) {
                    Log.d("Files2: ", filet.getName());
                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static ArticleDetailFragment newInstance(String itemID) {
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        Bundle args = new Bundle();
        args.putString(ArticleDetailFragment.ARG_ITEM_ID, itemID);
        fragment.setArguments(args);
        return fragment;
    }

    public ArticleDetailFragment() {}



    public void getJSON() {
        if (dummyItem == null){
            return;
        }
        String id = dummyItem.content;
        File file = new File(getActivity().getFilesDir(), id);

        FileInputStream fis;
        String content = "";
        try {
            fis = getActivity().openFileInput(file.getName());
            byte[] input = new byte[fis.available()];
            while (fis.read(input) != -1) {
            }
            content += new String(input);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            json = new JSONObject(content);
            Log.d("JSON",json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        try {
            formatJSON();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class sendJson extends AsyncTask<String, String, String> {

        private ProgressDialog pDialog;

        HttpURLConnection urlConnection;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Sending Data ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
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
                URL postURL = new URL("https://testapi.rubricae.es/sendSign/sign");
                urlConnection = (HttpURLConnection) postURL.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                OutputStream wr = urlConnection.getOutputStream();



                wr.write(jsonR.toString().getBytes());

                wr.flush();
                wr.close();

                //display what returns the POST request

                final StringBuilder sb = new StringBuilder();
                int HttpResult = urlConnection.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), sb.toString() + "Operación Firmada", Toast.LENGTH_SHORT).show();
                            dummyItem.status = DummyContent.DummyItem.FINALIZADO;
                            updateFileStatus();
                            CoordinatorLayout sendBtnCL = (CoordinatorLayout) getView().findViewById(R.id.sendLayout);
                            //CoordinatorLayout signBtnCL = (CoordinatorLayout) getView().findViewById(R.id.signLayout);
                            //signBtnCL.setVisibility(View.GONE);
                            sendBtnCL.setVisibility(View.GONE);



                        }
                    });

                } else {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(urlConnection.getErrorStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();

                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {

                            //Toast.makeText(getActivity(),urlConnection.getResponseMessage(),Toast.LENGTH_SHORT).show();
                            Toast.makeText(getActivity(), "Error en la operación: " + sb.toString(), Toast.LENGTH_LONG).show();
                            dummyItem.status = DummyContent.DummyItem.ERROR;
                            updateFileStatus();
                        }
                    });

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

            //Toast.makeText(getActivity(), "Firmado", Toast.LENGTH_SHORT).show();

            //Actualizar estado de la operación
            
            pDialog.dismiss();
        }

    }



}
