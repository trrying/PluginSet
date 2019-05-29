package com.owm.pluginset;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.owm.lib.api.ApiManager;
import com.owm.lib.api.LoginInterface;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btn_login.getId()) {
            ApiManager.getInstance().loginInterface().login(this, "{form:MainActivity}");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == LoginInterface.REQUEST_CODE_LOGIN) {
            Toast.makeText(this, resultCode == RESULT_OK? "success" : "fail", Toast.LENGTH_SHORT).show();
        }
    }
}
