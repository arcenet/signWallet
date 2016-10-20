package com.ecertic.signWallet.ui.base;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ecertic.signWallet.R;
import com.ecertic.signWallet.ui.quote.ListActivity;
import com.ecertic.signWallet.util.LogUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.Collection;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerQrActivity extends BaseActivity implements ZXingScannerView.ResultHandler{

    public ZXingScannerView mScannerView;
    private boolean twoPaneMode;
    public boolean finished = false;
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 0;
    private boolean camera = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkCamera();

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);


        Log.d("Camera permission",String.valueOf(permissionCheck));


        //QrScanner();
        /*setContentView(R.layout.activity_scanner_qr);
        setupToolbar();*/


    }

    private boolean isTwoPaneLayoutUsed() {
        return findViewById(R.id.article_detail_container) != null;
    }

    public void checkCamera(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        else{
            QrScanner();
        }
    }

    public void QrScanner(){
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view

        //Limitar al Scanner a solamente detectar "QR_CODE"
        Collection<BarcodeFormat> barcodeFormatList = mScannerView.getFormats();
        barcodeFormatList.clear();
        barcodeFormatList.add(BarcodeFormat.QR_CODE);
        mScannerView.setFormats((List<BarcodeFormat>) barcodeFormatList);


        setContentView(mScannerView);
        Log.e("formats:", mScannerView.getFormats().toString());
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();// Start camera

    }

    public void resumeScan(){

        mScannerView.resumeCameraPreview(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    QrScanner();



                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(ScannerQrActivity.this);
                    builder.setMessage("Se necesitan los permisos de la camara para poder escanear")
                            .setTitle("Error");
                    builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ScannerQrActivity.this.finish();
                        }
                    });
                    AlertDialog dialog = builder.create();

                    dialog.show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mScannerView != null) {
            mScannerView.stopCamera();   // Stop camera on pause
        }
    }

    @Override
    public void handleResult(final Result rawResult) {
        // Do something with the result here</p>
        Log.e("handler", rawResult.getText()); // Prints scan results<br />
        Log.e("handler", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode)</p>

        if (rawResult.getText().contains("https://applinks.rubricae.es/")) { //Valida que el resultado del scanner contenga un URL valido

            // show the scanner result into dialog box.<br />
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Scan Result");
            builder.setMessage(rawResult.getText() + "\n\n Espere...");
            final AlertDialog alert1 = builder.create();
            alert1.show();


            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent finishIntent = new Intent(getApplicationContext(), ListActivity.class);
                    Bundle b = new Bundle();
                    b.putString("urlScan", rawResult.getText());
                    finishIntent.putExtras(b);
                    startActivity(finishIntent);
                    alert1.dismiss();
                    finish();
                }
            }, 2500);


        }
        else{
            // show the scanner result into dialog box.<br />
            mScannerView.stopCameraPreview();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("QR NO VÁLIDO");



            builder.setMessage("URL: " + rawResult.getText() + "\n\nEl código QR no es compatible con la aplicación." +
                    " Verifique el código o vuelva a intentar");

            final AlertDialog alert1 = builder.create();
            alert1.setCancelable(true);

            alert1.setOnCancelListener(new DialogInterface.OnCancelListener() {


                @Override
                public void onCancel(DialogInterface dialog) {
                    alert1.dismiss();
                    resumeScan();
                }
            });
            alert1.show();



            Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    alert1.dismiss();

                    resumeScan();

                }


            }, 5000);


            // If you would like to resume scanning, call this method below:

            if (finished) {
                mScannerView.resumeCameraPreview(this);
            }

        }



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


    private void setupToolbar() {
        final ActionBar ab = getActionBarToolbar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        Intent backIntent = new Intent(getApplicationContext(), ListActivity.class);
        startActivity(backIntent);
        finish();
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_scan;
    }

    @Override
    public boolean providesActivityToolbar() { return true;  }
}
