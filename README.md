# Android Studio gradle插件开发

在Project ```build.gradle```添加```classpath```

```gradle
buildscript {
    dependencies {
        // 组件注册插件
        classpath 'com.owm.component:register:1.0.6'
    }
}
```

在需要注册模块```build.gradle```添加配置参数

```gradle
apply plugin: 'com.android.application'
android {
	...
}
dependencies {
	...
}

apply plugin: 'com.owm.component.register'
componentRegister {
    // 是否开启debug模式，输出详细日志
    isDebug = false
    // 是否启动组件注册
    componentRegisterEnable = true
    // 组件注册代码注入类
    componentMain = "com.owm.pluginset.application.App"
    // 注册代码注入类的方法
    componentMethod = "instanceModule"
    // 注册组件容器 HashMap，如果没有该字段则创建一个 public static final HashMap<String, Object> componentMap = new HashMap<>();
    componentContainer = "componentMap"
    // 注册组件配置
    componentRegisterList = [
        [
            "componentName": "LoginInterface",
            "instanceClass": "com.owm.module.login.LoginManager",
            "enable"       : true, // 默认为：true
            "singleton"    : false, // 默认为：false，是否单例实现，为true调用Xxx.getInstance()，否则调用new Xxx();
        ],
    ]
}

```

上述配置表示在```com.owm.pluginset.application.App```类中```instanceModule```方法内首部添加 ```componentMap.put("LoginInterface", new com.owm.module.login.LoginManager());```代码。

```java
class App {
    public static final HashMap<String, Object> componentMap = new HashMap<>();
    public void instanceModule() {
        componentMap.put("LoginInterface", new com.owm.module.login.LoginManager());
    }
}
```

![1559558005181](https://raw.githubusercontent.com/trrying/images/master/images/1559558005181.png)



**详细例子和插件代码：[https://github.com/trrying/PluginSet](https://github.com/trrying/PluginSet)**



## 1. 背景

组件化开发需要动态注册各个组件服务。

## 2. 知识点

- Android Studio 构建流程；
- Android Gradle Plugin & Transform；
- Groovy编程语言；
- javassist/asm字节码修改；

使用Android Studio编译流程如下图：

![简单构建流程](https://raw.githubusercontent.com/trrying/images/master/images/androidStudioBuild.png)

如果把整个构建编译流程看成是河流的话，在java 编译阶段有3条河流入，分别是：

1. aapt(Android Asset Package Tool)根据资源文件生成的R文件；
2. app 源码；
3. aidl文件生成接口；
上面3条河流汇集后将被编译成class文件，现在要做的就是使用Gralde Plugin 注册一个```Transform```，在Java Compileer 之后插入，处理class文件。处理完成后交给下一步流程去继续构建。

## 3. 构建插件模块

​	

​					

#### 3.1 创建插件模块

在项目中创建Android Library Module（其他模块也行，只要目录结构对应下面），创建完成后删除多余目录和文件，只保留```src```目录和```build.gradle```文件。
目录结构如下：

```
PluginSet
│
├─ComponentRegister
│  │  .gitignore
│  │  build.gradle
│  │  ComponentRegister.iml
│  │
│  └─src
│      └─main
│          ├─groovy // 
│          │  └─com
│          │      └─owm
│          │          └─component
│          │              └─register
│          │                  ├─plugin
│          │                  │      RegisterPlugin.groovy
│          │
│          └─resources
│              └─META-INF
│                  └─gradle-plugins
│                          com.owm.component.register.properties

```

主要关注有两个点

1. ```src/main/groovy``` 目录放置插件代码
2. ```src/main/resources``` 放置插件配置信息
   在```src/main/resources```下面的 ```resources/META-INF/gradle-plugins ```存放配置信息。这里可以放置多个配置信息，**每个配置信息是一个插件。**
   **配置文件名就是插件名**，例如我这里是```com.owm.component.register.properties```，应用时：```apply plugin: 'com.owm.component.register'```

#### 3.2 创建插件代码目录

创建```src/main/groovy``` 目录，再在该目录下创建包名路径和对应groovy类文件。

#### 3.3 创建插件配置文件

在```src/main/resources```目录下创建 ```resources/META-INF/gradle-plugins ```目录，再创建```com.owm.component.register.properties```配置文件。
配置文件内容如下：

```gradle
implementation-class=com.owm.component.register.plugin.RegisterPlugin
```
这里是配置```org.gradle.api.Plugin``` 接口的实现类，也就是配置插件的核心入口。
```gradle
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.api;

public interface Plugin<T> {
    void apply(T t);
}
```

#### 3.4 配置gradle

ComponentRegister插件模块build.gradle配置如下：

```gradle
apply plugin: 'groovy'
apply plugin: 'maven'

dependencies {
    //gradle sdk
    implementation gradleApi()
    //groovy sdk
    implementation localGroovy()

    //noinspection GradleDependency
    implementation "com.android.tools.build:gradle:3.2.0"
    implementation "javassist:javassist:3.12.1.GA"
    implementation "commons-io:commons-io:2.6"
}

repositories {
    mavenCentral()
}

//打包到本地Maven库
uploadArchives {
    repositories {
        mavenDeployer {
            // 本地Maven仓库
            repository(url: uri('../plugin'))

            // maven 配置  classpath "com.owm.component:register:1.0.5"
            pom.groupId = 'com.owm.component'
            pom.artifactId = 'register'
            pom.version = '1.0.5'
        }
    }
}

```

gralde sync 后可以在Android Studio Gradle面板找到```uploadArchives``` Task

![uploadArchives Task](https://raw.githubusercontent.com/trrying/images/master/images/1558690922771.png)

当插件编写完成，双击运行```uploadArchives```Task会在配置的本地Maven仓库中生成插件。

![gradle plugin](https://raw.githubusercontent.com/trrying/images/master/images/1558690922772.png)

## 4. 组件注册插件功能实现

#### 4.1 实现Plugin接口

按照```src/main/resources/resources/META-INF/gradle-plugins/com.owm.component.register.properties```配置文件中```implementation-class```的值来创建创建Plugin接口实现类。

在Plugin实现类中完成配置参数获取和注册Transform。

**注意**加载配置项需要延迟一点，例如```project.afterEvaluate{}```里面获取。

```groovy
class RegisterPlugin implements Plugin<Project> {

    // 定义gradle配置名称
    static final String EXT_CONFIG_NAME = 'componentRegister'

    @Override
    void apply(Project project) {
        LogUtils.i("RegisterPlugin 1.0.5 $project.name")

        // 注册Transform
        def transform = registerTransform(project)

        // 创建配置项
        project.extensions.create(EXT_CONFIG_NAME, ComponentRegisterConfig)

        project.afterEvaluate {
            // 获取配置项
            ComponentRegisterConfig config = project.extensions.findByName(EXT_CONFIG_NAME)
            // 配置项设置设置默认值
            config.setDefaultValue()

            LogUtils.i("RegisterPlugin apply config = ${config}")

            transform.update = config.componentRegisterEnable
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

```

#### 4.2 继承Transform

集成Transform实现抽象方法；

```getName()```：配置名字；

```getInputTypes()```：配置处理内容，例如class内容，jar内容，资源内容等，可多选

```getScopes()```：配置处理范围，例如当前模块，子模块等，可多选；

```isIncremental()```：是否支持增量；

```transform(transformInvocation)```：转换逻辑处理；

**注意：**library模块范围只能配置为当前模块；

```groovy
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

```

在```transform(TransformInvocation transformInvocation)```方法中的参数包含需要操作的数据。使用```getInputs()```获取输入的class或者jar内容，遍历扫描到匹配的类，将的组件实例代码注入到类中，再将文件复制到```getOutputProvider()```获取class或者jar对应的输出路径里面。

```groovy
package com.android.build.api.transform;

import java.util.Collection;

public interface TransformInvocation {
    Context getContext();

    // 输入内容
    Collection<TransformInput> getInputs();

    Collection<TransformInput> getReferencedInputs();

    Collection<SecondaryInput> getSecondaryInputs();

    // 输出内容提供者
    TransformOutputProvider getOutputProvider();

    boolean isIncremental();
}
```

#### 4.3  使用javassist注入组件注册代码

使用javassist来作为代码插桩工具，后续熟练字节码编辑再桩位asm实现；

- 加载需要的类路径和jar；
- 获取注入代码的类和方法；
- 根据配置信息将组件实例代码注入；
- **释放ClassPool占用资源；**

```groovy
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

```

#### 4.4 使用缓存决解UpToDate

Transform没有配置可更新或者强制更新选项，Transform依赖的Task无法获取（或者许能通过名字获取）。

这样就造成判断是否需要更新执行Transform的条件是内置定义好的，而Gradle配置改变时无法改变UpToDate条件，所以会导致修改了Gradle配置选项但是注入代码没有改变。

![UpToDate](https://raw.githubusercontent.com/trrying/images/master/images/1559120577150.png)

##### 4.4.1```Gralde 4.10.1```跳过任务执行

这里来了解下Task缓存判断条件

- onlyif

  ```groovy
  task.onlyif{ false } // return false 跳过任务执行
  ```

- StopExecutionException

  ```groovy
  task.doFirst{
      throw new StopExecutionException()
  }
  ```

- enable

  ```groovy
  task.enbale = false
  ```

- input和output

  > As part of incremental build, Gradle tests whether any of the task inputs or outputs have changed since the last build. If they haven’t, Gradle can consider the task up to date and therefore skip executing its actions. Also note that incremental build won’t work unless a task has at least one task output, although tasks usually have at least one input as well.
  >
  >
  >
  > 谷歌翻译：作为增量构建的一部分，Gradle会测试自上次构建以来是否有任何任务输入或输出已更改。如果他们没有，Gradle可以认为该任务是最新的，因此跳过执行其操作。另请注意，除非任务至少有一个任务输出，否则增量构建将不起作用，尽管任务通常也至少有一个输入。

  ![Task 编译](https://raw.githubusercontent.com/trrying/images/master/images/taskInputsOutputs.png)

  在第一次执行任务之前，Gradle会对输入进行快照。此快照包含输入文件的路径和每个文件内容的哈希。Gradle然后执行任务。如果任务成功完成，Gradle将获取输出的快照。此快照包含输出文件集和每个文件内容的哈希值。Gradle会在下次执行任务时保留两个快照。

  每次之后，在执行任务之前，Gradle会获取输入和输出的新快照。如果新快照与先前的快照相同，则Gradle会假定输出是最新的并跳过任务。如果它们不相同，Gradle将执行该任务。Gradle会在下次执行任务时保留两个快照。

**决解方法：**基于第4点输入和输出快照变化会使Taask执行条件为true，所以我们可以在需要重新注入代码时，把输出内容的代码注入类删除即可保证任务正常执行，同时也可以保证缓存使用加快编译速度。

```groovy
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

```

#### 5.本地仓库使用

在项目build.gradle添加依赖 ```classpath 'com.owm.component:register:1.0.5'```

```gradle
buildscript {
    repositories {
        google()
        jcenter()
        maven { url uri("./plugin") }
    }
    dependencies {
        //noinspection GradleDependency
        classpath 'com.android.tools.build:gradle:3.2.0'
        classpath 'com.owm.component:register:1.0.5'
    }
}
```

在注入组件注册代码模块build.gradle应用插件和添加配置

```gradle
apply plugin: 'com.android.application'

boolean loginModuleEnable = true

android {
    ...
}

dependencies {
    ...
    if (loginModuleEnable) {
        implementation project(':ModuleLogin')
    }
}

apply plugin: 'com.owm.component.register'
componentRegister {
    // 是否开启debug模式，输出详细日志
    isDebug = false
    // 是否启动组件注册
    componentRegisterEnable = true
    // 组件注册代码注入类
    componentMain = "com.owm.pluginset.application.App"
    // 注册代码注入类的方法
    componentMethod = "instanceModule"
    // 注册组件容器 HashMap，如果没有该字段则创建一个 public static final HashMap<String, Object> componentMap = new HashMap<>();
    componentContainer = "componentMap"
    // 注册组件配置
    componentRegisterList = [
        [
            "componentName": "LoginInterface",
            "instanceClass": "com.owm.module.login.LoginManager",
            "enable"       : loginModuleEnable, // 默认为：true
            "singleton"    : false, // 默认为：false，是否单例实现，为true调用Xxx.getInstance()，否则调用new Xxx();
        ],
    ]
}
```

创建组件注册代码注入类，创建注入类的方法和容器。

```java
package com.owm.pluginset.application;

import android.app.Application;

import com.owm.lib.api.ApiManager;
import com.owm.lib.api.LoginInterface;

import java.util.HashMap;

public class App extends Application {

    public static final HashMap<String, Object> componentMap = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        instanceModule();
    }

    public void instanceModule() {
        // 插件会在这里插入代码
        if (componentMap.containsKey("LoginInterface")) {
            ApiManager.getInstance().setLoginInterface((LoginInterface) componentMap.get("LoginInterface"));
        }
    }

}
```

在```build/rebuid```重新构建一下就会生成插入代码。

![代码注入成功](https://raw.githubusercontent.com/trrying/images/master/images/1559125904909.png)

第26行```componentMap.put("LoginInterface", new LoginManager());```就是利用javassist插入的组件注册代码。





**参考资料**

- [Android Gradle plugin 开发并上传到JCenter](https://blog.csdn.net/tl792814781/article/details/80489312)
- [Gradle插件](https://docs.gradle.org/4.10.1/userguide/plugins.html)
- [AutoRegister](https://juejin.im/post/5a2b95b96fb9a045284669a9)
