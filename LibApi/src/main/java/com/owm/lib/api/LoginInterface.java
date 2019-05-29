package com.owm.lib.api;

import android.app.Activity;

/**
 * Created by "ouweiming" on 2019/5/27.
 */
public interface LoginInterface {

    int REQUEST_CODE_LOGIN = 0x101;

    void login(Activity activity, String dataJson);

}
