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
    // 缓存文件夹，在构建目录下
    final static String CACHE_INFO_DIR = "component_register"
    // 缓存文件
    final static String CACHE_CONFIG_FILE_NAME = "config.txt"

    /**
     * 保存配置信息
     * @param project project
     * @param configInfo 配置信息
     */
    static void saveConfigInfo(Project project, ConfigCache configInfo) {
        saveConfigCache(project, new Gson().toJson(configInfo))
    }

    /**
     * 保存配置信息
     * @param project project
     * @param config 配置信息
     */
    static void saveConfigCache(Project project, String config) {
        LogUtils.i("HelperUtils-saveConfigCache :" + " project = " + project + " config = " + config)
        try {
            FileUtils.writeStringToFile(getRegisterInfoCacheFile(project), config, Charset.defaultCharset())
        } catch (Exception e) {
            LogUtils.i("saveConfigCache error ${e.message}")
        }
    }

    /**
     * 读取配置缓存信息
     * @param project project
     * @return 配置信息
     */
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

    /**
     * 获取缓存文件夹路径
     * @param project project
     * @return 缓存文件夹路径
     */
    static String getCacheFileDir(Project project) {
        return project.getBuildDir().absolutePath + File.separator + AndroidProject.FD_INTERMEDIATES + File.separator + CACHE_INFO_DIR
    }

    /**
     * 判断是否需要强制执行Task
     * @param project project
     * @param config 配置信息
     * @return true：强制执行
     */
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
