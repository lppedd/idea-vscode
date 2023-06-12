package com.github.lppedd.idea.vscode.npm;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Edoardo Luppi
 */
class DefaultVSCodeNpmTaskManager implements VSCodeNpmTaskManager {
  private final Set<VSCodeNpmBeforeRunTask> TASKS = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

  @Override
  public boolean isTaskRunning(@NotNull final VSCodeNpmBeforeRunTask task) {
    return TASKS.contains(task);
  }

  @Override
  public void registerRunningTask(
      @NotNull final VSCodeNpmBeforeRunTask task,
      @NotNull final Disposable disposable) {
    Disposer.register(disposable, () -> TASKS.remove(task));
    TASKS.add(task);
  }
}
