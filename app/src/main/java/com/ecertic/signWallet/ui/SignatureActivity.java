package com.ecertic.signWallet.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ecertic.signWallet.R;
import com.ecertic.signWallet.ui.base.BaseActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

import android.widget.Button;
import com.github.gcacace.signaturepad.views.SignaturePad;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;


/**
 * Created by Javier on 14/06/2016.
 */
public class SignatureActivity extends BaseActivity {

    private SignaturePad mSignaturePad;
    private Button mClearButton;
    private Button mSaveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature_pad);
        ButterKnife.bind(this);

        mSignaturePad = (SignaturePad) findViewById(R.id.signature_pad);
        mSignaturePad.setMinWidth(1);
        mSignaturePad.setMaxWidth(3);
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                Toast.makeText(SignatureActivity.this, "OnStartSigning", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSigned() {
                mSaveButton.setEnabled(true);
                mClearButton.setEnabled(true);
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
                if(addJpgSignatureToGallery(signatureBitmap)) {
                    Toast.makeText(SignatureActivity.this, "Signature saved into the Gallery", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignatureActivity.this, "Unable to store the signature", Toast.LENGTH_SHORT).show();
                }
                if(addSvgSignatureToGallery(mSignaturePad.getSignatureSvg())) {
                    Toast.makeText(SignatureActivity.this, "SVG Signature saved into the Gallery", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignatureActivity.this, "Unable to store the SVG signature", Toast.LENGTH_SHORT).show();
                }
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
            OutputStreamWriter writer  = new OutputStreamWriter(stream);
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
}
