package com.owm.lib.api;

import android.app.Activity;
import android.util.Log;

/**
 * Created by "ouweiming" on 2019/5/28.
 */
public class LoginDefaultImpl implements LoginInterface {

    private static final String TAG = "LoginImpl";

    @Override
    public void login(Activity activity, String dataJson) {
        Log.i(TAG,"LoginDefaultImpl-login :" + " activity = " + activity + " dataJson = " + dataJson);
    }
}
