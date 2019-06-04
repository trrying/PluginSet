package com.owm.component.register.transform

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.owm.component.register.bean.ComponentRegisterConfig
import com.owm.component.register.bean.ConfigCache
import com.owm.component.register.util.CacheUtils
import com.owm.component.register.util.InsertCodeUtils
import com.owm.component.register.util.LogUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 * Created by "ouweiming" on 2019/4/28.
 */
class BaseComponentRegisterTransform extends Transform {

    // 组件注册配置
    protected ComponentRegisterConfig config

    // Project
    protected Project project

    // Transform 显示名字，只是部分，真实显示还有前缀和后缀
    protected String name = this.class.simpleName

    BaseComponentRegisterTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return name
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        // 处理的类型，这里是要处理class文件
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        // 处理范围，这里是整个项目所有资源，library只能处理本模块
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        // 是否支持增量
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        LogUtils.i("${this.class.name} start ")

        if (!config.componentRegisterEnable) {
            LogUtils.r("componentRegisterEnable = false")
            return
        }

        // 缓存信息，决解UpTpDate缓存无法控制问题
        ConfigCache configInfo = new ConfigCache()
        configInfo.configString = config.toString()

        // 遍历输入文件
        transformInvocation.getInputs().each { TransformInput input ->
            // 遍历jar
            input.jarInputs.each { JarInput jarInput ->
                File dest = transformInvocation.outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                // 复制jar到目标目录
                FileUtils.copyFile(jarInput.file, dest)
                // 查看是否需要导包，是则加入导包列表
                InsertCodeUtils.scanImportClass(dest.toString(), config)
            }

            // 遍历源码目录文件
            input.directoryInputs.each { DirectoryInput directoryInput ->
                // 获得输出的目录
                File dest = transformInvocation.outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                // 复制文件夹到目标目录
                FileUtils.copyDirectory(directoryInput.file, dest)
                // 查看是否需要导包，是则加入导包列表
                InsertCodeUtils.scanImportClass(dest.toString(), config)
            }
        }

        // 代码注入
        def result = InsertCodeUtils.insertCode(config)
        LogUtils.i("insertCode result = ${result}")
        LogUtils.r("${result.message}")
        if (!result.state) {
            // 插入代码异常，终止编译打包
            throw new Exception(result.message)
        }
        // 缓存-记录路径
        configInfo.destList.add(config.directoryInputPath)

        // 保存缓存文件
        CacheUtils.saveConfigInfo(project, configInfo)
    }

    ComponentRegisterConfig getConfig() {
        return config
    }

    void setConfig(ComponentRegisterConfig config) {
        this.config = config
    }

}
