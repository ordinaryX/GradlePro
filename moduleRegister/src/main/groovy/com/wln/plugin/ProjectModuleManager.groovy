package com.wln.plugin

import org.gradle.api.Project
import org.gradle.util.GradleVersion

import java.util.regex.Pattern

/**
 * 工程中的组件module管理工具
 * 1. 用于管理组件module以application或library方式进行编译
 * 2. 用于管理组件依赖（只在给当前module进行集成打包时才添加对组件的依赖，以便于进行代码隔离）
 */
class ProjectModuleManager {
    static final String PLUGIN_NAME = ModuleRegisterPlugin.PLUGIN_NAME

    //组件单独以app方式运行时使用的测试代码所在目录(manifest/java/assets/res等),这个目录下的文件不会打包进主app
    //主app，一直以application方式编译
    static final String MODULE_MAIN_APP = "mainApp"

    static String mainModuleName
    static boolean taskIsAssemble

    static boolean manageModule(Project project) {
        taskIsAssemble = false
        mainModuleName = null
        initByTask(project)

        def mainApp = isMainApp(project)

        boolean runAsApp = false
        if (mainApp) {
            runAsApp = true
        }
        project.ext.runAsApp = runAsApp
        println "${PLUGIN_NAME}: mainModuleName=${mainModuleName}, project=${project.name}, runAsApp=${runAsApp} . taskIsAssemble:${taskIsAssemble}. " +
                "settings(mainApp:${mainApp})"
        if (runAsApp) {
            project.apply plugin: 'com.android.application'
        } else {
            project.apply plugin: 'com.android.library'
        }
        //为build.gradle添加addComponent方法
        addComponentDependencyMethod(project)
        return runAsApp
    }

    //需要集成打包相关的task
    static final String TASK_TYPES = ".*((((ASSEMBLE)|(BUILD)|(INSTALL)|((BUILD)?TINKER)|(RESGUARD)).*)|(ASR)|(ASD))"

    static void initByTask(Project project) {
        def taskNames = project.gradle.startParameter.taskNames
        def allModuleBuildApkPattern = Pattern.compile(TASK_TYPES)
        for (String task : taskNames) {
            if (allModuleBuildApkPattern.matcher(task.toUpperCase()).matches()) {
                taskIsAssemble = true
                if (task.contains(":")) {
                    def arr = task.split(":")
                    println("current taskNameSplit array: $arr")
                    mainModuleName = arr[arr.length - 2].trim()
                }
                break
            }
        }
    }

    static boolean isMainApp(Project project) {
        return project.ext.has(MODULE_MAIN_APP) && project.ext.mainApp
    }

    //组件依赖的方法，用于进行代码隔离
    //对组件库的依赖格式： addNewComponent realDependency
    // 使用示例见demo/build.gradle
    //  realDependency: 组件库对应的实际依赖，可以是module依赖，也可以是maven依赖
    //    如果未配置realDependency，将自动依赖 project(":$dependencyName")
    //    realDependency可以为如下2种中的一种:
    //      module依赖 : project(':demo_component_b')
    //      maven依赖  : 'com.billy.demo:demoB:1.1.0' //如果使用了maven私服，请使用此方式
    static void addComponentDependencyMethod(Project project) {
        //当前task是否为给本module打apk包
        def curModuleIsBuildingApk = taskIsAssemble && isMainApp(project)
        project.ext.addNewComponent = { realDependency ->
            //不是在为本app module打apk包，不添加对组件的依赖
            if (!curModuleIsBuildingApk)
                return
            def dependencyMode = GradleVersion.version(project.gradle.gradleVersion) >= GradleVersion.version('4.1') ? 'api' : 'compile'
            if (realDependency) {
                //通过参数传递的依赖方式，如：
                // project(':moduleName')
                // 或
                // 'com.billy.demo:demoA:1.1.0'
                project.dependencies.add(dependencyMode, realDependency)
            } else {
                throw new RuntimeException(
                        "my >>>> add dependency by [ addNewComponent '$realDependency' ] occurred an error:" +
                                "\n'$realDependency' is not a module in current project" +
                                " and the param is not specified for realDependency" +
                                "\nPlease make sure the module is '$realDependency'" +
                                "\nelse" +
                                "\nyou can specify the real dependency via add the param, for example: " +
                                "addComponent 'com.billy.demo:demoB:1.1.0'" +
                                "\n or addComponent project(':component:home')")
            }

        }
    }
}