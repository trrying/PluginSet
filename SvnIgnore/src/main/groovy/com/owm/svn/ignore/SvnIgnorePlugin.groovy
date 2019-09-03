package com.owm.svn.ignore

import org.gradle.api.Plugin
import org.gradle.api.Project
import groovy.xml.XmlUtil

/**
 * Created by "ouweiming" on 2019/8/28.
 */
class SvnIgnorePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        try {
            def ignoredMap = [
                    ["name": "path", value: ".idea/"],
                    ["name": "path", value: ".gradle/"],
                    ["name": "path", value: "gradle/"],
                    ["name": "path", value: "build/"],
                    ["name": "path", value: "gradlew"],
                    ["name": "path", value: "gradlew.bat"],
                    ["name": "path", value: "local.properties"],
                    ["name": "mask", value: "*.iml"],
            ]
            project.rootDir.list().each {
                if (project.file("${project.rootDir}/$it/build").exists()) {
                    ignoredMap.add(["name": "path", "value": "$it/build/"])
                }
            }
            File workspaceFile = project.file("${project.rootDir}/.idea/workspace.xml")
            def workspace = new XmlParser().parseText(workspaceFile.getText())
            def changeListManager = workspace.component.find {
                it.attribute("name") == "ChangeListManager"
            } ?: workspace.appendNode("component", ["name": "ChangeListManager"])
            ignoredMap.each {
                def ignoredNode = changeListManager.ignored.find { ignoredNode -> ignoredNode.attribute(it.name) == it.value }
                if (ignoredNode == null) {
                    changeListManager.appendNode("ignored", ["${it.name}": "${it.value}"])
                }
            }
            PrintWriter pw = new PrintWriter(workspaceFile)
            pw.write(XmlUtil.serialize(workspace))
            pw.close()
        } catch (Exception ignore) { println "svn-ignore error ${ignore.message}" }
    }

}
