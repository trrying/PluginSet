package com.owm.component.register.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.owm.component.register.bean.ComponentRegisterConfig
import com.owm.component.register.transform.ComponentRegisterAppTransform
import com.owm.component.register.transform.ComponentRegisterLibTransform
import com.owm.component.register.util.CacheUtils
import com.owm.component.register.util.LogUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by "ouweiming" on 2019/4/28.
 */
class RegisterPlugin implements Plugin<Project> {

    // 定义gradle配置名称
    static final String EXT_CONFIG_NAME = 'componentRegister'

    @Override
    void apply(Project project) {
        LogUtils.i("RegisterPlugin 1.1.0 $project.name")

        // 注册Transform
        def transform = registerTransform(project)

        // 创建配置项
        project.extensions.create(EXT_CONFIG_NAME, ComponentRegisterConfig)

        project.afterEvaluate {
            // 获取配置项
            ComponentRegisterConfig config = project.extensions.findByName(EXT_CONFIG_NAME)
            // 配置项设置设置默认值
            config.setDefaultValue()

            LogUtils.logEnable = config.isDebug
            LogUtils.i("RegisterPlugin apply config = ${config}")

            transform.setConfig(config)

            // 保存配置缓存，判断改动设置UpToDate状态
            CacheUtils.handleUpToDate(project, config)
        }
    }

    // 注册Transform
    static registerTransform(Project project) {
        LogUtils.i("RegisterPlugin-registerTransform :" + " project = " + project)

        // 初始化Transform
        def extension = null, transform = null
        if (project.plugins.hasPlugin(AppPlugin)) {
            extension = project.extensions.getByType(AppExtension)
            transform = new ComponentRegisterAppTransform(project)
        } else if (project.plugins.hasPlugin(LibraryPlugin)) {
            extension = project.extensions.getByType(LibraryExtension)
            transform = new ComponentRegisterLibTransform(project)
        }

        LogUtils.i("extension = ${extension} \ntransform = $transform")

        if (extension != null && transform != null) {
            // 注册Transform
            extension.registerTransform(transform)
            LogUtils.i("register transform")
        } else {
            throw new RuntimeException("can not register transform")
        }
        return transform
    }

}
