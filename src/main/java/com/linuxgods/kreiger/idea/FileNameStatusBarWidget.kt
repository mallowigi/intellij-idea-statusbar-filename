package com.linuxgods.kreiger.idea

import com.intellij.ide.IdeBundle
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.impl.EditorWindow
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsContexts.StatusBarText
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.Pair
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.VfsPresentationUtil
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget.MultipleTextValuesPresentation
import com.intellij.openapi.wm.StatusBarWidget.WidgetPresentation
import com.intellij.openapi.wm.impl.status.EditorBasedWidget
import com.intellij.ui.popup.list.ListPopupImpl
import com.intellij.util.Consumer
import com.intellij.util.IconUtil
import com.intellij.util.SlowOperations
import org.jetbrains.annotations.NonNls
import java.awt.event.MouseEvent
import java.util.stream.Collectors
import javax.swing.Icon

@Suppress("UnstableApiUsage")
internal class FileNameStatusBarWidget(project: Project) : EditorBasedWidget(project), MultipleTextValuesPresentation {
  @NlsSafe
  private var text: String? = null
  private var icon: Icon? = null

  override fun ID(): @NonNls String = "Filename"

  override fun getPresentation(): WidgetPresentation = this

  override fun getTooltipText(): @NlsContexts.Tooltip String? = null

  override fun getClickConsumer(): Consumer<MouseEvent>? = null

  override fun install(statusBar: StatusBar) {
    super.install(statusBar)
    DumbService.getInstance(myProject).runWhenSmart { update(selectedFile) }
  }

  override fun selectionChanged(event: FileEditorManagerEvent) {
    update(event.newFile)
  }

  private fun update(file: VirtualFile?) {
    if (null == file) return
    text = getFileTitle(file)
    icon = IconUtil.getIcon(file, 0, myProject)
    myStatusBar.updateWidget(ID())
  }

  private fun getFileTitle(file: VirtualFile): String {
    return SlowOperations.allowSlowOperations<String, RuntimeException> {
      VfsPresentationUtil.getUniquePresentableNameForUI(
        myProject,
        file
      )
    }
  }

  override fun getPopupStep(): ListPopup = ListPopupImpl(myProject, RecentFilesPopupStep(myProject))

  override fun getSelectedValue(): @StatusBarText String? = text

  override fun getIcon(): Icon? = icon

  private inner class RecentFilesPopupStep private constructor(fileEditorManager: FileEditorManagerImpl) :
    BaseListPopupStep<VirtualFile>(
      IdeBundle.message("title.popup.recent.files"),
      getSelectionHistory(fileEditorManager)
    ) {
    private val fileEditorManager: FileEditorManager

    constructor(project: Project?) : this(FileEditorManagerImpl.getInstance(project!!) as FileEditorManagerImpl)

    init {
      this.fileEditorManager = fileEditorManager
    }

    override fun getIconFor(file: VirtualFile): Icon = IconUtil.getIcon(file, 0, myProject)

    override fun getTextFor(file: VirtualFile): String = getFileTitle(file)

    override fun onChosen(file: VirtualFile, finalChoice: Boolean): PopupStep<*>? {
      fileEditorManager.openFile(file, true)
      return FINAL_CHOICE
    }
  }

  companion object {
    private fun getSelectionHistory(fileEditorManager: FileEditorManagerImpl): List<VirtualFile?> {
      val selectionHistory = fileEditorManager.selectionHistory.stream()
        .map { pair: Pair<VirtualFile?, EditorWindow?> -> pair.getFirst() }
        .collect(Collectors.toList())
      selectionHistory.reverse()
      return selectionHistory
    }
  }
}
