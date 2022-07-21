package com.linuxgods.kreiger.idea

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls

/** File name status bar widget factory. */
class FileNameStatusBarWidgetFactory : StatusBarEditorBasedWidgetFactory() {
  /** Get id. */
  override fun getId(): @NonNls String = "FileName"

  /** Get display name. */
  override fun getDisplayName(): @Nls String = "File Name"

  /**
   * Create widget
   *
   * @param project
   */
  override fun createWidget(project: Project): StatusBarWidget = FileNameStatusBarWidget(project)

  /**
   * Dispose widget
   *
   * @param widget
   */
  override fun disposeWidget(widget: StatusBarWidget) {
    Disposer.dispose(widget)
  }
}
