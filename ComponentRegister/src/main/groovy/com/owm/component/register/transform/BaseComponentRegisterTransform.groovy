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

        boolean leftSlash = File.separator == '/'

        // 组件注册代码插入类
        String componentMain = "${config.componentMain.replace(".", "/")}.class"

        // 缓存需要操作的类
        def classNameList = [config.componentMain]
        config.componentRegisterMap.each { component ->
            classNameList.add(component.instanceClass)
        }

        // 遍历输入文件
        transformInvocation.getInputs().each { TransformInput input ->
            // 遍历jar
            input.jarInputs.each { JarInput jarInput ->
                File dest = transformInvocation.outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                // 暂不修改jar，原样输出
                FileUtils.copyFile(jarInput.file, dest)
                // 查看是否需要导包，是则加入导包列表
                if (InsertCodeUtils.classPathContainClass(dest.toString(), classNameList)) { config.classPathList.add(dest.toString()) }
            }

            // 遍历源码
            input.directoryInputs.each { DirectoryInput directoryInput ->
                LogUtils.i("directoryInput = $directoryInput")
                // 获得输出的目录
                File dest = transformInvocation.outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                LogUtils.i("dest = $dest")
                // 根目录
                String root = directoryInput.file.absolutePath
                if (!root.endsWith(File.separator)) {
                    root += File.separator
                }
                LogUtils.i("root = $root")
                //遍历目录下的每个文件
                directoryInput.file.eachFileRecurse { File file ->
                    LogUtils.i("file = $file")
                    // 去掉根路径，以类包名路径操作
                    def path = file.absolutePath.replace(root, '')
                    if (file.isFile()) {
                        def entryName = path
                        if (!leftSlash) {
                            entryName = entryName.replaceAll("\\\\", "/")
                        }
                        if (componentMain == entryName) {
                            // 类路径
                            config.directoryInputPath = directoryInput.file.absolutePath
                            def result = InsertCodeUtils.insertCode(config)
                            LogUtils.i("insertCode result = ${result}")
                            LogUtils.r("component register ${result.state ? "completed" : "error"}")
                            if (!result.state) {
                                // 插入代码异常，终止编译打包
                                throw new Exception(result.message)
                            }
                            // 缓存-记录路径
                            configInfo.destList.add(file.absolutePath.replace(root, "${dest.absolutePath}${File.separator}"))
                        }
                    }
                }
                // 处理完后拷到目标文件
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }
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
