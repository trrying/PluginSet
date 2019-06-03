// Top-level build file where you can add configuration options common to all sub-projects/modules.

gradle.ext.groupIdName = 'com.owm.component'
gradle.ext.artifactIdName = 'register'
gradle.ext.versionName = '1.0.6'

buildscript {
    repositories {
        google()
        jcenter()

        // 本地仓库
//        maven { url uri("./plugin") }
        // bintray仓库
//        maven { url "https://dl.bintray.com/trryings/component-register/" }

        // gradle plugins 插件maven
        maven { url "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        //noinspection GradleDependency
        classpath 'com.android.tools.build:gradle:3.2.0'

        // 组件注册插件
        classpath 'com.owm.component:register:1.0.6'

        // gradle plugins 插件
        classpath "com.gradle.publish:plugin-publish-plugin:0.9.7"

        // 发布到jcenter 需要的插件
        classpath 'com.github.dcendents:android-maven-gradle-plugin:1.5'
        classpath 'com.jfrog.bintray.gradle:gradle-bintray-plugin:1.7.3'
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
