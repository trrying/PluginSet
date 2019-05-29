package com.owm.component.register.bean

/**
 * Created by "ouweiming" on 2019/4/28.
 */
class ComponentRegisterConfig {

    // 是否启动组件注册
    public boolean componentRegisterEnable = true
    // 组件注册代码注入类
    public String componentMain = ""
    // 注册代码注入类的方法
    public String componentMethod = ""
    // 注册组件容器 HashMap，如果没有该字段则创建一个 public static final HashMap<String, Object> componentMap = new HashMap<>();
    public String componentContainer = ""
    /**
     * componentRegisterMap = [
     *         [
     *             "componentName": "LoginInterface", // 组件名，componentContainer容器的key
     *             "instanceClass": "com.owm.module.login.LoginManager", // 需要实例的组件
     *             "enable"       : loginModuleEnable, // 默认为：true
     *             "singleton"    : false, // 默认为：false，是否单例实现，为true调用Xxx.getInstance()，否则调用new Xxx();
     *         ],
     *     ]
     */
    public def componentRegisterMap

    // 是否开启debug模式，输出详细日志
    public boolean isDebug = false
    // 插入注册代码的类根路径
    public String directoryInputPath

    // 操作需要导包的类路径
    public List<String> classPathList = new ArrayList<>()

    // 设置未配置项的默认值
    void setDefaultValue() {
        if (componentRegisterMap != null) {
            componentRegisterMap.each { component ->
                if (component.enable == null) {
                    component.enable = true
                }
                if (component.singleton == null) {
                    component.singleton = false
                }
            }
        }
    }

    @Override String toString() {
        return "ComponentRegisterConfig{" +
                ", componentRegisterEnable=" + componentRegisterEnable +
                ", componentMain=" + componentMain +
                ", componentMethod=" + componentMethod +
                ", componentContainer=" + componentContainer +
                "componentRegisterMap=" + componentRegisterMap +
                '}'
    }

    String toAllString() {
        return "ComponentRegisterConfig{" +
            "componentRegisterEnable=" + componentRegisterEnable +
            ", componentMain='" + componentMain + '\'' +
            ", componentMethod='" + componentMethod + '\'' +
            ", componentContainer='" + componentContainer + '\'' +
            ", componentRegisterMap=" + componentRegisterMap +
            ", directoryInputPath='" + directoryInputPath + '\'' +
            ", classPathList=" + classPathList +
            '}';
    }
}
