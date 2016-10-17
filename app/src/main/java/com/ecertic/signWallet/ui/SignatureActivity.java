package com.ecertic.signWallet.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.ecertic.signWallet.R;
import com.ecertic.signWallet.ui.base.BaseActivity;
import com.ecertic.signWallet.util.JSONParser;
import com.ecertic.signWallet.util.SignaturePad;
import com.github.gcacace.signaturepad.utils.TimedPoint;

import butterknife.ButterKnife;
import butterknife.OnClick;

import android.widget.Button;
//import com.github.gcacace.signaturepad.views.SignaturePad;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class SignatureActivity extends BaseActivity {

    private Context mContext;
    private SignaturePad mSignaturePad;
    private Button mClearButton;
    private Button mSaveButton;
    public boolean isSigning = false;
    public JSONObject json;
    private String ipAddress;
    private double longitude = 0;
    private double latitude = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature_pad);
        ButterKnife.bind(this);

        getJSON();

        Log.d("JSON", json.toString());

        mSignaturePad = (SignaturePad) findViewById(R.id.signature_pad);
        mSignaturePad.setMinWidth(1);
        mSignaturePad.setMaxWidth(3);

        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                Toast.makeText(SignatureActivity.this, "OnStartSigning", Toast.LENGTH_SHORT).show();
                isSigning = true;

            }

            @Override
            public void onSigned() {
                mSaveButton.setEnabled(true);
                mClearButton.setEnabled(true);
                List<TimedPoint> points = mSignaturePad.getxPoints();
                List<Float> pressurePoints = mSignaturePad.pressurePoints;
                for (int i = 0; i < points.size(); i++) {
                    Log.d("Points:", points.get(i).x + "," + points.get(i).y);
                    Log.d("Points:", String.valueOf(points.size()));
                    Log.d("Tilt:", String.valueOf(mSignaturePad.pressurePoints.size()));
                }

            }

            @Override
            public void onClear() {
                mSaveButton.setEnabled(false);
                mClearButton.setEnabled(false);
            }


        });

        mClearButton = (Button) findViewById(R.id.clear_button);
        mSaveButton = (Button) findViewById(R.id.save_button);

        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSignaturePad.clear();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                if (addJpgSignatureToGallery(signatureBitmap)) {
                    Toast.makeText(SignatureActivity.this, "Signature saved into the Gallery", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignatureActivity.this, "Unable to store the signature", Toast.LENGTH_SHORT).show();
                }
                if (addPngSignatureToGallery(signatureBitmap)) {
                    Toast.makeText(SignatureActivity.this, "Signature PNG saved into the Gallery", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignatureActivity.this, "Unable to store the signature", Toast.LENGTH_SHORT).show();
                }
                if (addSvgSignatureToGallery(mSignaturePad.getSignatureSvg())) {
                    Toast.makeText(SignatureActivity.this, "SVG Signature saved into the Gallery", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignatureActivity.this, "Unable to store the SVG signature", Toast.LENGTH_SHORT).show();
                }
                buildJSON(json);


                AlertDialog.Builder builder = new AlertDialog.Builder(SignatureActivity.this);
                builder.setMessage("Firma guardada")
                        .setTitle("Aviso");
                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SignatureActivity.this.finish();
                    }
                });
                AlertDialog dialog = builder.create();

                dialog.show();

                //Log.d("IP",ipAddress);
            }
        });

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
        //return R.id.nav_samples;
        return 0;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }

    //////

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("SignaturePad", "Directory not created");
        }
        return file;
    }

    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }



    public boolean addJpgSignatureToGallery(Bitmap signature) {
        boolean result = false;
        try {
            File photo = new File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.jpg", System.currentTimeMillis()));
            saveBitmapToJPG(signature, photo);
            scanMediaFile(photo);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void saveBitmapToPNG(Bitmap bitmap, File photo) throws IOException {
        //Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        OutputStream stream = new FileOutputStream(photo);

        bitmap.compress(Bitmap.CompressFormat.PNG, 80, stream);

        stream.close();

        //Get Byte Array from bitmap PNG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
        byte [] b = baos.toByteArray();
        baos.close();

        //Get base64 content of saved image
        String base64 = Base64.encodeToString(b,Base64.DEFAULT);
        Log.d("Length",String.valueOf(base64.length()));
        int maxLogSize = 1000;
        for(int i = 0; i <= base64.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i+1) * maxLogSize;
            end = end > base64.length() ? base64.length() : end;
            Log.v("TAG", base64.substring(start, end));
        }
        try {
            json.getJSONArray("signers").getJSONObject(0).getJSONObject("signature").put("content",base64);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean addPngSignatureToGallery(Bitmap signature) {
        boolean result = false;
        try {
            File photo = new File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.png", System.currentTimeMillis()));
            saveBitmapToPNG(signature, photo);
            scanMediaFile(photo);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void scanMediaFile(File photo) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(photo);
        mediaScanIntent.setData(contentUri);
        SignatureActivity.this.sendBroadcast(mediaScanIntent);
    }

    public boolean addSvgSignatureToGallery(String signatureSvg) {
        boolean result = false;
        try {
            File svgFile = new File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.svg", System.currentTimeMillis()));
            OutputStream stream = new FileOutputStream(svgFile);
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            writer.write(signatureSvg);
            writer.close();
            stream.flush();
            stream.close();
            scanMediaFile(svgFile);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void getJSON() {
        String id = getIntent().getExtras().getString("oId");
        File file = new File(getFilesDir(), id);

        FileInputStream fis;
        String content = "";
        try {
            fis = openFileInput(file.getName());
            byte[] input = new byte[fis.available()];
            while (fis.read(input) != -1) {
            }
            content += new String(input);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            json = new JSONObject(content);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void buildJSON(JSONObject json) {

        new getSystemInfo().execute();

        try {

            JSONArray signers = json.getJSONArray("signers");
            JSONObject signer = signers.getJSONObject(0);


            JSONObject signature = signer.getJSONObject("signature");
            signature.put("imageType", "image/png");
            signature.put("eventType", "Motion Event");
            signature.put("createdAt", System.currentTimeMillis());

            /*Tomar el arreglo que incluye los puntos de la firma*/

            Log.d("Content", signature.toString());
            JSONArray points = signature.getJSONArray("points");


            /*Construir el JSONObject para cada punto*/

            for (int i = 0; i < mSignaturePad.getxPoints().size(); i++) {
                JSONObject point = new JSONObject();

                point.put("tiltY", 0);
                point.put("tiltX", 0);
                point.put("x", mSignaturePad.getxPoints().get(i).x);
                point.put("y", mSignaturePad.getxPoints().get(i).y);
                point.put("time", 0);
                point.put("pointerType", "touch");
                point.put("radiusX", 0);
                point.put("radiusY", 0);
                point.put("pressure", mSignaturePad.pressurePoints.get(i).floatValue());

                points.put(point);

            }

            /*Construir JSONObject system con los datos del sistema*/


            JSONObject system = new JSONObject();

            system.put("camouflage", 0);
            system.put("latitude", latitude);
            system.put("longitude", longitude);
            system.put("address", 0);
            system.put("device", Build.MODEL);
            system.put("useragent", "signWallet");
            system.put("os", Build.VERSION.RELEASE);
            system.put("browser", "");


            signer.put("system", system);

        } catch (JSONException e) {
            e.printStackTrace();



        }
    }


    private class getSystemInfo extends AsyncTask<String, String, String> {

        private ProgressDialog pDialog;


        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            pDialog = new ProgressDialog(SignatureActivity.this);
            pDialog.setMessage("Saving Data ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                ipAddress = InetAddress.getLocalHost().getHostAddress();

                Log.d("FINISH ASYNC","Ready");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            /*LocationManager lm = (LocationManager) getSystemService(mContext.LOCATION_SERVICE);

            try {
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }
            catch(SecurityException e){

            }*/
            return "Done";
        }


        @Override
        protected void onPostExecute(String i) {
            try {
                json.getJSONArray("signers").getJSONObject(0).getJSONObject("system").put("ip",ipAddress);
                Log.d("JSON", json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();



        }

    }

}
