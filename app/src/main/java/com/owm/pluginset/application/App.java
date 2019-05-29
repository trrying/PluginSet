package com.owm.pluginset.application;

import android.app.Application;

import com.owm.lib.api.ApiManager;
import com.owm.lib.api.LoginInterface;

import java.util.HashMap;

/**
 * Created by "ouweiming" on 2019/5/28.
 */
public class App extends Application {

    public static final HashMap<String, Object> componentMap = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        instanceModule();
    }

    public void instanceModule() {
        if (componentMap.containsKey("LoginInterface")) {
            ApiManager.getInstance().setLoginInterface((LoginInterface) componentMap.get("LoginInterface"));
        }
    }

}
