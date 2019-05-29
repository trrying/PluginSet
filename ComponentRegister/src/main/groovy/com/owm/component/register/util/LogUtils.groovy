package com.owm.component.register.util

/**
 * Created by "ouweiming" on 2019/5/15.
 */
class LogUtils {

    static boolean logEnable = false

    static void i(String msg) {
        if (logEnable) println "${msg}\n"
    }

    static void r(String msg) {
        println "${msg}"
    }

}
