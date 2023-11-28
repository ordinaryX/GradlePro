package com.wln.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class ModuleRegisterPlugin implements Plugin<Project> {

    public static final String PLUGIN_NAME = 'wln-Module-register'

    @Override
    void apply(Project project) {
        println "project(${project.name}) apply ${PLUGIN_NAME} plugin"
        ProjectModuleManager.manageModule(project)
    }
}
