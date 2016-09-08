package com.ecertic.signWallet.ui.base;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerQrActivity extends BaseActivity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView mScannerView;
    private boolean twoPaneMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_scanner_qr);
        setupToolbar();


    }

    private boolean isTwoPaneLayoutUsed() {
        return findViewById(R.id.article_detail_container) != null;
    }

    public void QrScanner(View view){
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();         // Start camera
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
        // show the scanner result into dialog box.<br />
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        builder.setMessage(rawResult.getText()+"\n\n Redireccionando...");
        final AlertDialog alert1 = builder.create();
        alert1.show();

        // If you would like to resume scanning, call this method below:<br />
        // mScannerView.resumeCameraPreview(this);<br />
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent finishIntent = new Intent(getApplicationContext(), ListActivity.class);
                Bundle b = new Bundle();
                b.putString("urlScan",rawResult.getText());
                finishIntent.putExtras(b);
                startActivity(finishIntent);
                alert1.dismiss();
                finish();
            }
        }, 2500);

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
