# Android Studio gradle插件开发----组件注册插件

**组件注册插件**是解决在模块化化开发中无反射、无新增第三方框架、可混淆的需求。在Android Studio编译阶段根据宿主Module的```build.gradle```中的配置信息注入组件注册代码。

## 效果：

使用插件前App源码：

![1559701162143](https://raw.githubusercontent.com/trrying/images/master/images/1559701162143.png)

使用插件后反编译App：

![1559701477775](https://raw.githubusercontent.com/trrying/images/master/images/1559701477775.png)

## 使用：

1. 在Project ```build.gradle```添加```classpath```

```gradle
buildscript {
    dependencies {
        // 组件注册插件
        classpath 'com.owm.component:register:1.1.2'
    }
}
```

2. 注册模块```build.gradle```添加配置参数

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

3. 在```componentMain```类创建```componentMap```方法和```componentMap```容器。

```java
class App {
    public static final HashMap<String, Object> componentMap = new HashMap<>();
    public void instanceModule() {
        componentMap.put("LoginInterface", new com.owm.module.login.LoginManager());
    }
}
```

Gradle同步重新构建项目出现下列输入表示注入成功。

![1559558005181](https://raw.githubusercontent.com/trrying/images/master/images/1559558005181.png)

**详细例子和插件代码：[https://github.com/trrying/PluginSet](https://github.com/trrying/PluginSet)**


## 1. 背景

组件化开发需要动态注册各个组件服务，解决在模块化化开发中无需反射、无需第三方框架、可混淆的需求。


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

// 发布到 plugins.gradle.org 双击Gradle面板 PluginSet:ComponentRegister -> Tasks -> plugin portal -> publishPlugins
apply from: "../script/gradlePlugins.gradle"

// 发布到本地maven仓库 双击Gradle面板 PluginSet:ComponentRegister -> Tasks -> upload -> uploadArchives
apply from: "../script/localeMaven.gradle"

//发布 Jcenter 双击Gradle面板 PluginSet:ComponentRegister -> Tasks -> publishing -> bintrayUpload
apply from: '../script/bintray.gradle'

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

```

#### 4.2 继承Transform

集成Transform实现抽象方法；

```getName()```：配置名字；

```getInputTypes()```：配置处理内容，例如class内容，jar内容，资源内容等，可多选

```getScopes()```：配置处理范围，例如当前模块，子模块等，可多选；

```isIncremental()```：是否支持增量；

```transform(transformInvocation)```：转换逻辑处理；

- 判断jar是否需要导包，是则加入导包列表
- 判断class是否需要导包，是则加入导包列表
- 根据config配置注入代码
- 保存缓存

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
        configInfo.configString = config.configString()

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
        configInfo.destList.add(config.mainClassPath)
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
        def result = ["state": false, "message":"component insert cant insert"]
        def classPathCache = []
        LogUtils.i("InsertCodeUtils config = ${config}")

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
                File mainClassPathFile = new File(config.mainClassPath)
                if (mainClassPathFile.name.endsWith('.jar')) {
                    // 将修改的类保存到jar中
                    saveToJar(config, mainClassPathFile, ctClass.toBytecode())
                } else {
                    ctClass.writeFile(config.mainClassPath)
                }
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

    static saveToJar(ComponentRegisterConfig config, File jarFile, byte[] codeBytes) {
        if (!jarFile) {
            return
        }
        def mainJarFile = null
        JarOutputStream jarOutputStream = null
        InputStream inputStream = null

        try {
            String mainClass = "${config.componentMain.replace(".", "/")}.class"

            def tempJarFile = new File(config.mainJarFilePath)
            if (tempJarFile.exists()) {
                tempJarFile.delete()
            }

            mainJarFile = new JarFile(jarFile)
            jarOutputStream = new JarOutputStream(new FileOutputStream(tempJarFile))
            Enumeration enumeration = mainJarFile.entries()

            while (enumeration.hasMoreElements()) {
                try {
                    JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                    String entryName = jarEntry.getName()
                    ZipEntry zipEntry = new ZipEntry(entryName)
                    inputStream = mainJarFile.getInputStream(jarEntry)
                    jarOutputStream.putNextEntry(zipEntry)
                    if (entryName == mainClass) {
                        jarOutputStream.write(codeBytes)
                    } else {
                        jarOutputStream.write(IOUtils.toByteArray(inputStream))
                    }
                } catch (Exception e) {
                    LogUtils.r("""error : ${e.getMessage()}""")
                    if (LogUtils.logEnable) { e.printStackTrace() }
                } finally {
                    FileUtils.close(inputStream)
                    if (jarOutputStream != null) {
                        jarOutputStream.closeEntry()
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.r("""error : ${e.getMessage()}""")
            if (LogUtils.logEnable) { e.printStackTrace() }
        } finally {
            FileUtils.close(jarOutputStream, mainJarFile)
        }
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
    static scanImportClass(String classPath, ComponentRegisterConfig config) {
        ClassPool classPool = null
        def classPathCache = null
        try {
            classPool = new ClassPool()
            classPathCache = classPool.appendClassPath(classPath)
            def clazz = config.classNameList.find {
                classPool.getOrNull(it) != null
            }
            if (clazz != null) {
                config.classPathList.add(classPath)
            }
            if (clazz == config.componentMain) {
                if (classPath.endsWith(".jar")) {
                    File src = new File(classPath)
                    File dest = new File(src.getParent(), "temp_${src.getName()}")
                    org.apache.commons.io.FileUtils.copyFile(src, dest)
                    config.mainClassPath = dest.toString()
                    config.mainJarFilePath = classPath
                } else {
                    config.mainClassPath = classPath
                }
            }
        }  catch (Exception e) {
            LogUtils.r("""error : ${e.getMessage()}""")
            if (LogUtils.logEnable) { e.printStackTrace() }
        } finally {
            if (classPool != null && classPathCache != null) classPool.removeClassPath(classPathCache)
        }
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

**参考资料**

- [Android Gradle plugin 开发并上传到JCenter](https://blog.csdn.net/tl792814781/article/details/80489312)
- [Gradle插件](https://docs.gradle.org/4.10.1/userguide/plugins.html)
- [AutoRegister](https://juejin.im/post/5a2b95b96fb9a045284669a9)
