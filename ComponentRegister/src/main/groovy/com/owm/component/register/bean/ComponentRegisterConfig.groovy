package com.owm.component.register.bean

/**
 * Created by "ouweiming" on 2019/4/28.
 */
class ComponentRegisterConfig {

    // 是否启动组件注册
    public boolean componentRegisterEnable = false
    // 组件注册代码注入类
    public String componentMain = ""
    // 注册代码注入类的方法
    public String componentMethod = ""
    // 注册组件容器 HashMap，如果没有该字段则创建一个 public static final HashMap<String, Object> componentMap = new HashMap<>();
    public String componentContainer = ""
    /**
     * componentRegisterList = [
     *         [
     *             "componentName": "LoginInterface", // 组件名，componentContainer容器的key
     *             "instanceClass": "com.owm.module.login.LoginManager", // 需要实例的组件
     *             "enable"       : loginModuleEnable, // 默认为：true
     *             "singleton"    : false, // 默认为：false，是否单例实现，为true调用Xxx.getInstance()，否则调用new Xxx();
     *         ],
     *     ]
     */
    public def componentRegisterList
    // 是否开启debug模式，输出详细日志
    public boolean isDebug = false


    // 插入注册代码的类根路径
    public String mainClassPath

    // 在jar注入代码，避免文件占用，将真实路径文件在此，mainClassPath为临时文件
    public String mainJarFilePath

    // 操作需要导包的类路径
    public List<String> classPathList = new ArrayList<>()

    // 需要操作或者导入的类全名称
    public List<String> classNameList = new ArrayList<>()

    // 设置未配置项的默认值
    void setDefaultValue() {
        // 给每个组件配置项设置默认值
        if (componentRegisterList != null) {
            componentRegisterList.each { component ->
                if (component.enable == null) {
                    component.enable = true
                }
                if (component.singleton == null) {
                    component.singleton = false
                }
            }
        }

        // 缓存需要操作或者导入的类全名称
        classNameList.clear()
        classNameList.add(componentMain)
        if (componentRegisterList != null) {
            componentRegisterList.each { component ->
                classNameList.add(component.instanceClass)
            }
        }
    }

    @Override String toString() {
        return "ComponentRegisterConfig{" +
            "componentRegisterEnable=" + componentRegisterEnable +
            ", componentMain='" + componentMain + '\'' +
            ", componentMethod='" + componentMethod + '\'' +
            ", componentContainer='" + componentContainer + '\'' +
            ", componentRegisterList=" + componentRegisterList +
            ", isDebug=" + isDebug +
            ", mainClassPath='" + mainClassPath + '\'' +
            ", mainJarFilePath='" + mainJarFilePath + '\'' +
            ", classPathList=" + classPathList +
            ", classNameList=" + classNameList +
            '}'
    }

    String configString() {
        return "ComponentRegisterConfig{" +
            "componentRegisterEnable=" + componentRegisterEnable +
            ", componentMain=" + componentMain +
            ", componentMethod=" + componentMethod +
            ", componentContainer=" + componentContainer +
            "componentRegisterList=" + componentRegisterList +
            ", isDebug=" + isDebug +
            '}'
    }
}
