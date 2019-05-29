package com.owm.component.register.bean;

/**
 * gradle 配置信息缓存
 * Created by "ouweiming" on 2019/5/17.
 */
class ConfigCache {

    List<String> destList = new ArrayList<>()

    String configString

    @Override String toString() {
        return "ConfigInfo{" +
            "destList=" + destList +
            ", configString='" + configString + '\'' +
            '}'
    }
}
