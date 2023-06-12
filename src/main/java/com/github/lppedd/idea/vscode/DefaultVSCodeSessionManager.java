package com.github.lppedd.idea.vscode;

import com.github.lppedd.idea.vscode.run.VSCodeProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Edoardo Luppi
 */
class DefaultVSCodeSessionManager implements VSCodeSessionManager {
  private final Set<ProcessHandler> HANDLERS = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

  @Override
  public boolean isShutDown(@NotNull final VSCodeProcessHandler processHandler) {
    return HANDLERS.contains(processHandler);
  }

  @Override
  public void setShutDown(@NotNull final VSCodeProcessHandler processHandler) {
    if (!HANDLERS.contains(processHandler)) {
      processHandler.addProcessListener(new ProcessListener() {
        @Override
        public void processTerminated(@NotNull final ProcessEvent event) {
          AppExecutorUtil.getAppScheduledExecutorService().schedule(() -> {
            HANDLERS.remove(processHandler);
          }, 1, TimeUnit.MINUTES);
        }
      });

      HANDLERS.add(processHandler);
    }
  }
}
