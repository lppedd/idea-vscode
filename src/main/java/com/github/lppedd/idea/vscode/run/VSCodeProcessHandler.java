package com.github.lppedd.idea.vscode.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ColoredProcessHandler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Edoardo Luppi
 */
public class VSCodeProcessHandler extends ColoredProcessHandler {
  private final AtomicBoolean isTerminated = new AtomicBoolean();
  private final AtomicBoolean isDestroyed = new AtomicBoolean();
  private final GeneralCommandLine commandLine;

  public VSCodeProcessHandler(@NotNull final GeneralCommandLine commandLine) throws ExecutionException {
    super(commandLine);
    this.commandLine = commandLine;
  }

  public VSCodeProcessHandler(@NotNull final VSCodeProcessHandler processHandler) {
    super(processHandler.getProcess(), processHandler.commandLine);
    this.commandLine = processHandler.commandLine;
  }

  public void setTerminated() {
    if (!isTerminated.compareAndSet(false, true)) {
      throw new IllegalStateException("Expected 'false' for isTerminated");
    }
  }

  public boolean isProcessTerminatedInternal() {
    return super.isProcessTerminated();
  }

  public boolean isProcessTerminatingInternal() {
    return super.isProcessTerminating();
  }

  @Override
  public boolean isProcessTerminated() {
    return isDestroyed.get() || super.isProcessTerminated();
  }

  @Override
  public boolean isProcessTerminating() {
    return isDestroyed.get() || super.isProcessTerminating();
  }

  @Override
  public void destroyProcess() {
    if (isTerminated.get()) {
      if (!isDestroyed.compareAndSet(false, true)) {
        throw new IllegalStateException("Expected 'false' for isDestroyed");
      }
    } else {
      super.destroyProcess();
    }
  }
}
