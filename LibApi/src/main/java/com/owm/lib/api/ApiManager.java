package com.owm.lib.api;

import java.util.HashMap;

/**
 * Created by "ouweiming" on 2019/5/24.
 */
public class ApiManager {

    private static volatile ApiManager instance;

    public static final HashMap<String, Object> componentMap = new HashMap<>();

    private LoginInterface loginInterface;

    private ApiManager() {
        instanceModule();
    }

    public static ApiManager getInstance() {
        if (instance == null) {
            synchronized (ApiManager.class) {
                if (instance == null) {
                    instance = new ApiManager();
                }
            }
        }
        return instance;
    }

    public LoginInterface loginInterface() {
        if (loginInterface == null) {
            loginInterface = new LoginDefaultImpl();
        }
        return loginInterface;
    }

    public void setLoginInterface(LoginInterface loginInterface) {
        this.loginInterface = loginInterface;
    }

    private void instanceModule() {
        if (componentMap.containsKey("LoginInterface")) {
            setLoginInterface((LoginInterface) componentMap.get("LoginInterface"));
        }
    }

}
