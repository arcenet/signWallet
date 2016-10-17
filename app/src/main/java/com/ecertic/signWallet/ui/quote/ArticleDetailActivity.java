package com.ecertic.signWallet.ui.quote;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.ecertic.signWallet.ui.SignatureActivity;
import com.ecertic.signWallet.ui.base.BaseActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.ecertic.signWallet.R;

import static com.ecertic.signWallet.util.LogUtil.logD;
import static com.ecertic.signWallet.util.LogUtil.makeLogTag;


/**
 * Simple wrapper for {@link ArticleDetailFragment}
 * This wrapper is only used in single pan mode (= on smartphones)
 * Created by Andreas Schrade on 14.12.2015.
 */
public class ArticleDetailActivity extends BaseActivity {

    private static final String TAG = makeLogTag(BaseActivity.class);


    /*@Bind(R.id.main_content)
    CoordinatorLayout layoutRoot;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //ButterKnife.bind(this);

        // Show the Up button in the action bar.
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ArticleDetailFragment fragment =  ArticleDetailFragment.newInstance(getIntent().getStringExtra(ArticleDetailFragment.ARG_ITEM_ID));
        getFragmentManager().beginTransaction().replace(R.id.article_detail_container, fragment).commit();
    }

   /* @OnClick(R.id.sign)
    public void submit(View view) {
        Snackbar.make(layoutRoot, "Hey, I'm SnackBar!", Snackbar.LENGTH_SHORT)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(ArticleDetailActivity.this, "Undo!", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }*/

    @Override
    public boolean providesActivityToolbar() {
        return false;
    }
}
