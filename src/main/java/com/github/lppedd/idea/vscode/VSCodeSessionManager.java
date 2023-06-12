package com.github.lppedd.idea.vscode;

import com.github.lppedd.idea.vscode.run.VSCodeProcessHandler;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of this service interface must keep track
 * of VS Code Run Configuration executions states.
 * <p>
 * This is especially useful when restart the Run Configuration
 * on debugger disconnection, as - for example - it doesn't make
 * sense to restart if a shutdown of the VS Code instance was requested.
 *
 * @author Edoardo Luppi
 */
public interface VSCodeSessionManager {
  static @NotNull VSCodeSessionManager getInstance() {
    return ApplicationManager.getApplication().getService(VSCodeSessionManager.class);
  }

  /**
   * Returns whether the VS Code session's process has been shut down.
   */
  boolean isShutDown(@NotNull final VSCodeProcessHandler processHandler);

  /**
   * Sets the VS Code session's process as shut down.
   * <p>
   * No further processing should be done on that instance from now on.
   */
  void setShutDown(@NotNull final VSCodeProcessHandler processHandler);
}
