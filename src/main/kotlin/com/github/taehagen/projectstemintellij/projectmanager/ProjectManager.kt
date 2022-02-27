package com.github.taehagen.projectstemintellij.projectmanager

import com.github.taehagen.projectstemintellij.Stateful
import com.github.taehagen.projectstemintellij.runOnIoThread
import com.intellij.execution.RunManager
import com.intellij.execution.application.ApplicationConfiguration
import com.intellij.execution.application.ApplicationConfigurationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiManager
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

    fun saveFiles(): Boolean {
        val item = selectedItem ?: return false
        for (file in item.files) {
            val data = getVFForFile(file)?.contentsToByteArray() ?: continue
            file.stagingContent = String(data)
        }
        return item.updateFiles()
    }

    fun openFiles() {
        val fem = FileEditorManager.getInstance(project)
        for (file in fem.openFiles) {
            fem.closeFile(file)
            ApplicationManager.getApplication().runWriteAction {
                file.delete(null)
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
    }

    fun updateConfigs() {
        val item = selectedItem ?: return
        var mainClass: PsiClass? = null
        for (vf in openFiles) {
            val psi = PsiManager.getInstance(project).findFile(vf)
            if (psi != null) {
                val main = ApplicationConfigurationType.getMainClass(psi)
                if (main != null)
                    mainClass = main
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