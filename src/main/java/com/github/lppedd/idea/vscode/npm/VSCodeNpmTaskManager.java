package com.github.lppedd.idea.vscode.npm;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

/**
 * Normally a before-launch task blocks the execution of the main Run Configuration
 * until it terminates. This obviously doesn't fit in the VS Code development model,
 * as we want - for example - {@code npm run watch} to be running in the background.
 * <p>
 * The implementation of this service interface must keep track of the currently
 * running {@code npm} before-launch task.
 *
 * @author Edoardo Luppi
 */
public interface VSCodeNpmTaskManager {
  static @NotNull VSCodeNpmTaskManager getInstance() {
    return ApplicationManager.getApplication().getService(VSCodeNpmTaskManager.class);
  }

  /**
   * Returns whether a {@code npm} task is currently running or not.
   */
  boolean isTaskRunning(@NotNull final VSCodeNpmBeforeRunTask task);

  /**
   * Register a {@code npm} task as currently running.
   *
   * @param task       the task to register as running
   * @param disposable the parent disposable, used to remove the task
   *                   from the running list when disposed
   */
  void registerRunningTask(
      @NotNull final VSCodeNpmBeforeRunTask task,
      @NotNull final Disposable disposable);
}
