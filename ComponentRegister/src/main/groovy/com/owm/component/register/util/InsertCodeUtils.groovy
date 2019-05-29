package com.owm.component.register.util

import com.owm.component.register.bean.ComponentRegisterConfig
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod

import java.lang.reflect.Modifier

/**
 * Created by "ouweiming" on 2019/5/14.
 */
class InsertCodeUtils {

    /**
     * 注入组件实例代码
     * @param config 组件注入配置
     * @return 注入状态["state":true/false]
     */
    static insertCode(ComponentRegisterConfig config) {
        def result = ["state": true]
        def classPathCache = []
        LogUtils.i("InsertCodeUtils config = ${config.toAllString()}")

        // 实例类池
        ClassPool classPool = new ClassPool()
        classPool.appendSystemPath()

        // 添加根目录
        appendClassPath(classPool, classPathCache, config.directoryInputPath)

        // 添加类路径
        config.classPathList.each { jarPath ->
            appendClassPath(classPool, classPathCache, jarPath)
        }

        CtClass ctClass = null
        try {
            // 获取注入注册代码的类
            ctClass = classPool.getCtClass(config.componentMain)
            LogUtils.i("ctClass ${ctClass}")

            if (ctClass.isFrozen()) {
                // 如果冻结就解冻
                ctClass.deFrost()
            }

            // 获取注入方法
            CtMethod ctMethod = ctClass.getDeclaredMethod(config.componentMethod)
            LogUtils.i("ctMethod = $ctMethod")

            // 判断是否有组件容器
            boolean hasComponentContainer = false
            ctClass.fields.each { field ->
                if (field.name == config.componentContainer) {
                    hasComponentContainer = true
                }
            }
            if (!hasComponentContainer) {
                CtField componentContainerField = new CtField(classPool.get("java.util.HashMap"), config.componentContainer, ctClass)
                componentContainerField.setModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)
                ctClass.addField(componentContainerField, "new java.util.HashMap();")
            }

            // 注入组件实例代码
            String insertCode = ""
            config.componentRegisterMap.each { component ->
                if (component.enable) {
                    String instanceCode = component.singleton ? "${component.instanceClass}.getInstance()" : "new ${component.instanceClass}()"
                    insertCode = """${config.componentContainer}.put("${component.componentName}", ${instanceCode});"""
                    LogUtils.i("insertCode = ${insertCode}")
                    try {
                        ctMethod.insertBefore(insertCode)
                    } catch (Exception e) {
                        if (LogUtils.logEnable) { e.printStackTrace() }
                        result = ["state": false, "message":"""insert "${insertCode}" error : ${e.getMessage()}"""]
                        return
                    }
                }
            }
            ctClass.writeFile(config.directoryInputPath)
        } catch (Exception e) {
            LogUtils.r("""error : ${e.getMessage()}""")
            if (LogUtils.logEnable) { e.printStackTrace() }
        } finally {
            // 需要释放资源，否则会io占用
            if (ctClass != null) {
                ctClass.detach()
                ctClass = null
            }
            if (classPool != null) {
                classPathCache.each { classPool.removeClassPath(it) }
                classPool = null
            }
        }
        return result
    }

    /**
     * 缓存添加类路径
     * @param classPool 类池
     * @param classPathCache 类路径缓存
     * @param classPath 类路径
     */
    static void appendClassPath(ClassPool classPool, classPathCache, classPath) {
        classPathCache.add(classPool.appendClassPath(classPath))
    }

    // 检测classPath是否包含任意一个classList类
    static boolean classPathContainClass(String classPath, List<String> classList) {
        boolean result = false
        try {
            ClassPool classPool = new ClassPool()
            def classPathCache = classPool.appendClassPath(classPath)
            for (int i = 0; i < classList.size(); i++) {
                try {
                    if (classPool.getOrNull(classList.get(i)) != null) {
                        result = true
                        break
                    }
                } catch (Exception ignored) {}
            }
            classPool.removeClassPath(classPathCache)
        } catch (Exception ignored) {}
        return result
    }

}
