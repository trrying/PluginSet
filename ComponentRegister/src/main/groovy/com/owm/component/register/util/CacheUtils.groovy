package com.owm.component.register.util

import com.android.builder.model.AndroidProject
import com.google.gson.Gson
import com.owm.component.register.bean.ComponentRegisterConfig
import com.owm.component.register.bean.ConfigCache
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

import java.nio.charset.Charset

/**
 * Created by "ouweiming" on 2019/5/17.
 */
class CacheUtils {

    final static String CACHE_INFO_DIR = "component_register"

    final static String CACHE_CONFIG_FILE_NAME = "config.txt"

    static void saveConfigInfo(Project project, ConfigCache configInfo) {
        saveConfigCache(project, new Gson().toJson(configInfo))
    }

    static void saveConfigCache(Project project, String config) {
        LogUtils.i("HelperUtils-saveConfigCache :" + " project = " + project + " config = " + config)
        try {
            FileUtils.writeStringToFile(getRegisterInfoCacheFile(project), config, Charset.defaultCharset())
        } catch (Exception e) {
            LogUtils.i("saveConfigCache error ${e.message}")
        }
    }

    static String readConfigCache(Project project) {
        try {
            return FileUtils.readFileToString(getRegisterInfoCacheFile(project), Charset.defaultCharset())
        } catch (Exception e) {
            LogUtils.i("readConfigCache error ${e.message}")
        }
        return ""
    }

    /**
     * 缓存自动注册配置的文件
     * @param project
     * @return file
     */
    static File getRegisterInfoCacheFile(Project project) {
        File baseFile = new File(getCacheFileDir(project))
        if (baseFile.exists() || baseFile.mkdirs()) {
            File cacheFile = new File(baseFile, CACHE_CONFIG_FILE_NAME)
            if (!cacheFile.exists()) cacheFile.createNewFile()
            return cacheFile
        } else {
            throw new FileNotFoundException("Not found  path:" + baseFile)
        }
    }

    static String getCacheFileDir(Project project) {
        return project.getBuildDir().absolutePath + File.separator + AndroidProject.FD_INTERMEDIATES + File.separator + CACHE_INFO_DIR
    }

    static boolean handleUpToDate(Project project, ComponentRegisterConfig config) {
        LogUtils.i("HelperUtils-handleUpToDate :" + " project = " + project + " config = " + config)
        Gson gson = new Gson()
        String configInfoText = getRegisterInfoCacheFile(project).text
        LogUtils.i("configInfoText = ${configInfoText}")
        ConfigCache configInfo = gson.fromJson(configInfoText, ConfigCache.class)
        LogUtils.i("configInfo = ${configInfo}")
        if (configInfo != null && configInfo.configString != config.toString()) {
            configInfo.destList.each {
                LogUtils.i("delete ${it}")
                File handleFile = new File(it)
                if (handleFile.isDirectory()) {
                    FileUtils.deleteDirectory(handleFile)
                } else {
                    handleFile.delete()
                }
            }
        }
    }

}
