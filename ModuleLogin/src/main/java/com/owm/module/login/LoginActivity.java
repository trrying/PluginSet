package com.owm.module.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Created by "ouweiming" on 2019/5/27.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";

    private Button btn_login_success;
    private Button btn_login_fail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_login_success = findViewById(R.id.btn_login_success);
        btn_login_fail = findViewById(R.id.btn_login_fail);

        btn_login_success.setOnClickListener(this);
        btn_login_fail.setOnClickListener(this);

        String dataJson = getIntent().getStringExtra("dataJson");
        Log.i(TAG, "onCreate: dataJson = " + dataJson);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btn_login_success.getId()) {
            setResult(RESULT_OK);
            finish();
        } else if (v.getId() == btn_login_fail.getId()) {
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}
