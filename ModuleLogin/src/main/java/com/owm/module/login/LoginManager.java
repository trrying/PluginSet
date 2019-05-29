package com.owm.module.login;

import android.app.Activity;
import android.content.Intent;

import com.owm.lib.api.LoginInterface;

/**
 * Created by "ouweiming" on 2019/5/27.
 */
public class LoginManager implements LoginInterface {

    @Override
    public void login(Activity activity, String dataJson) {
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.putExtra("dataJson", dataJson);
        activity.startActivityForResult(intent, LoginInterface.REQUEST_CODE_LOGIN);
    }

}
