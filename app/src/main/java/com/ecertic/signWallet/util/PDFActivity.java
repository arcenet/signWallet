package com.ecertic.signWallet.util;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ecertic.signWallet.R;
import com.ecertic.signWallet.ui.SignatureActivity;
import com.joanzapata.pdfview.PDFView;

import java.io.File;


public class PDFActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Documento PDF a firmar");
        setSupportActionBar(toolbar);

        File pdf = (File) getIntent().getExtras().get("file");


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                  //      .setAction("Action", null).show();
                Intent signIntent = new Intent(PDFActivity.this, SignatureActivity.class);
                signIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

                Bundle b = new Bundle();

                b.putString("oId", getIntent().getExtras().getString("oId"));
                b.putString("dummyID", getIntent().getExtras().getString("dummyID"));

                signIntent.putExtras(b);
                startActivity(signIntent);
                finish();
            }
        });



        PDFView pdfView = (PDFView) findViewById(R.id.pdfview);

        pdfView.fromFile(pdf)

                .defaultPage(1)
                .showMinimap(false)
                .enableSwipe(true)
                .load();
    }

}
