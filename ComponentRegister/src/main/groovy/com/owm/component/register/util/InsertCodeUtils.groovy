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
        def result = ["state": false, "message":"component insert cant insert"]
        def classPathCache = []
        LogUtils.i("InsertCodeUtils config = ${config.toAllString()}")

        // 实例类池
        ClassPool classPool = new ClassPool()
        classPool.appendSystemPath()

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
            // 记录组件注入情况，用于日志输出
            def componentInsertSuccessList = []
            def errorComponent = config.componentRegisterList.find { component ->
                LogUtils.i("component = ${component}")
                if (component.enable) {
                    String instanceCode = component.singleton ? "${component.instanceClass}.getInstance()" : "new ${component.instanceClass}()"
                    insertCode = """${config.componentContainer}.put("${component.componentName}", ${instanceCode});"""
                    LogUtils.i("insertCode = ${insertCode}")
                    try {
                        ctMethod.insertBefore(insertCode)
                        componentInsertSuccessList.add(component.componentName)
                        return false
                    } catch (Exception e) {
                        if (LogUtils.logEnable) { e.printStackTrace() }
                        result = ["state": false, "message":"""insert "${insertCode}" error : ${e.getMessage()}"""]
                        return true
                    }
                }
            }
            LogUtils.i("errorComponent = ${errorComponent}")
            if (errorComponent == null) {
                ctClass.writeFile(config.mainClassPath)
                result = ["state": true, "message": "component register ${componentInsertSuccessList}"]
            }
        } catch (Exception e) {
            LogUtils.r("""error : ${e.getMessage()}""")
            if (LogUtils.logEnable) { e.printStackTrace() }
        } finally {
            // 需要释放资源，否则会io占用
            if (ctClass != null) {
                ctClass.detach()
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
        }  catch (Exception e) {
            LogUtils.r("""error : ${e.getMessage()}""")
            if (LogUtils.logEnable) { e.printStackTrace() }
        }
        return result
    }

    // 检测classPath是否包含任意一个classList类
    static scanImportClass(String classPath, ComponentRegisterConfig config) {
        ClassPool classPool = null
        def classPathCache = null
        try {
            classPool = new ClassPool()
            classPathCache = classPool.appendClassPath(classPath)
            def clazz = config.classNameList.find {
//                LogUtils.i("className = ${it}")
                classPool.getOrNull(it) != null
            }
            LogUtils.i("clazz = ${clazz}")
            if (clazz != null) {
                config.classPathList.add(classPath)
            }
            if (clazz == config.componentMain) {
                config.mainClassPath = classPath
            }
        }  catch (Exception e) {
            LogUtils.r("""error : ${e.getMessage()}""")
            if (LogUtils.logEnable) { e.printStackTrace() }
        } finally {
            if (classPool != null && classPathCache != null) classPool.removeClassPath(classPathCache)
        }
    }

}
