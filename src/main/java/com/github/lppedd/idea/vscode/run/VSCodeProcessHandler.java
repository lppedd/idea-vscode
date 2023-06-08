package com.github.lppedd.idea.vscode.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ColoredProcessHandler;
import org.jetbrains.annotations.NotNull;

/**
 * @author Edoardo Luppi
 */
public class VSCodeProcessHandler extends ColoredProcessHandler {
  private final GeneralCommandLine commandLine;
  private volatile boolean isTerminated;
  private volatile boolean isDestroyed;

  public VSCodeProcessHandler(@NotNull final GeneralCommandLine commandLine) throws ExecutionException {
    super(commandLine);
    this.commandLine = commandLine;
  }

  @SuppressWarnings("CopyConstructorMissesField")
  public VSCodeProcessHandler(@NotNull final VSCodeProcessHandler processHandler) {
    super(processHandler.getProcess(), processHandler.commandLine);
    this.commandLine = processHandler.commandLine;
  }

  public void setTerminated() {
    isTerminated = true;
  }

  public boolean isProcessTerminatedInternal() {
    return super.isProcessTerminated();
  }

  public boolean isProcessTerminatingInternal() {
    return super.isProcessTerminating();
  }

  @Override
  public boolean isProcessTerminated() {
    return isDestroyed || super.isProcessTerminated();
  }

  @Override
  public boolean isProcessTerminating() {
    return isDestroyed || super.isProcessTerminating();
  }

  @Override
  public void destroyProcess() {
    if (isTerminated) {
      isDestroyed = true;
    } else {
      super.destroyProcess();
    }
  }
}
