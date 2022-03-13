package com.github.taehagen.projectstemintellij.projectmanager

import com.github.taehagen.projectstemintellij.Stateful
import com.github.taehagen.projectstemintellij.services.StateService
import com.intellij.execution.RunManager
import com.intellij.execution.application.ApplicationConfiguration
import com.intellij.execution.application.ApplicationConfigurationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiManager
import java.io.IOException
import java.io.PrintWriter
import java.nio.file.Paths


class ProjectManager(val project: Project) : Stateful() {
    var selectedItem: Item? = null
        set(value) {
            field = value
            println(value?.title)
            stateChanged()
        }

    var openFiles = ArrayList<VirtualFile>()

    fun getVFForFile(file: File): VirtualFile? {
        return openFiles.find { file.name == it.name }
    }

    /**
     * Attempts to restore the IDE's state. If no state can be restored, returns false
     */
    fun restoreState(user: User): Boolean {
        val state = project.service<StateService>().myState
        val itemId = state.openItem
        val moduleId = state.openModule
        val courseId = state.openCourse
        if (itemId == -1 || moduleId == -1 || courseId == -1)
            return false
        val item = user.courses.find { it.id == courseId }?.getModules()?.find { it.id == moduleId }?.getItems()?.find { it.id == itemId } ?: return false
        item.getDetails()
        selectedItem = item

        ApplicationManager.getApplication().invokeLater {
            val fem = FileEditorManager.getInstance(project)
            val courseDir = Paths.get(project.basePath, "src").toFile()
            courseDir.mkdirs()
            item.files.sortWith { f1, f2 ->
                if (f1.preferred == f2.preferred) 0 else if (f1.preferred) 1 else -1
            }
            for (file in item.files) {
                val f = courseDir.resolve(file.name)
                if (!f.exists()) {
                    val p = PrintWriter(f)
                    p.print(file.content)
                    p.close()
                }
                val vf = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(f.toPath())!!
                fem.openFile(vf, file.preferred)
                openFiles.add(vf)
            }
        }
        return true
    }

    fun saveFiles(): Boolean {
        val item = selectedItem ?: return false
        for (file in item.files) {
            val data = getVFForFile(file)?.contentsToByteArray() ?: continue
            file.stagingContent = String(data)
        }
        return item.updateFiles()
    }

    fun openFiles() {
        val state = project.service<StateService>().myState
        if (state.openItem == selectedItem?.id)
            return
        val fem = FileEditorManager.getInstance(project)
        for (file in fem.openFiles) {
            fem.closeFile(file)
            ApplicationManager.getApplication().runWriteAction {
                try {
                    file.delete(null)
                } catch (e: IOException) {
                    // we don't care lol
                }
            }
        }
        val courseDir = Paths.get(project.basePath, "src").toFile()
        courseDir.mkdirs()
        for (file in courseDir.listFiles())
            if (!file.isDirectory)
                file.delete()
        openFiles.clear()
        val item = selectedItem ?: return
        item.files.sortWith { f1, f2 ->
            if (f1.preferred == f2.preferred) 0 else if (f1.preferred) 1 else -1
        }
        for (file in item.files) {
            val f = courseDir.resolve(file.name)
            val p = PrintWriter(f)
            p.print(file.content)
            p.close()
            val vf = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(f.toPath())!!
            fem.openFile(vf, file.preferred)
            openFiles.add(vf)
        }
        DumbService.getInstance(project).smartInvokeLater {
            updateConfigs() // invoke after index update
        }
        state.openItem = item.id
        state.openModule = item.module.id
        state.openCourse = item.module.course.id
    }

    fun updateConfigs() {
        val item = selectedItem ?: return
        var mainClass: PsiClass? = null
        for (vf in openFiles) {
            val psi = PsiManager.getInstance(project).findFile(vf)
            if (psi != null) {
                try {
                    val main = ApplicationConfigurationType.getMainClass(psi)
                    if (main != null)
                        mainClass = main
                } catch (e: Throwable) {
                    // do nothing, slow operations on EDT
                }
            }
        }
        if (mainClass != null) {
            val runManager = RunManager.getInstance(project)
            for (conf in runManager.allSettings)
                runManager.removeConfiguration(conf)

            val conf = ApplicationConfiguration(item.title, project)
            conf.setMainClass(mainClass)
            val createdConf = runManager.createConfiguration(
                conf,
                ApplicationConfigurationType.getInstance().configurationFactories[0]
            )
            runManager.addConfiguration(createdConf)
            runManager.selectedConfiguration = createdConf
        }
    }
}