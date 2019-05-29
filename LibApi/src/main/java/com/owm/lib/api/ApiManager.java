package com.owm.lib.api;

/**
 * Created by "ouweiming" on 2019/5/24.
 */
public class ApiManager {

    private static volatile ApiManager instance;

    private LoginInterface loginInterface;

    private ApiManager() {}

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

}
