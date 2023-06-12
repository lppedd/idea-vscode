package com.github.lppedd.idea.vscode.run.states;

import com.github.lppedd.idea.vscode.run.VSCodeProcessHandler;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Edoardo Luppi
 */
public class VSCodeExecutionResult extends DefaultExecutionResult {
  private final VSCodeCommandLineRunState state;
  private final int debugPort;

  public VSCodeExecutionResult(
      @NotNull final VSCodeCommandLineRunState state,
      final int debugPort,
      @Nullable final ConsoleView consoleView,
      @NotNull final ProcessHandler processHandler,
      @Nullable final AnAction[] actions) {
    super(consoleView, processHandler, actions);
    this.state = state;
    this.debugPort = debugPort;
  }

  public int getDebugPort() {
    return debugPort;
  }

  public @NotNull VSCodeExecutionResult copy(@NotNull final Executor executor) throws ExecutionException {
    final var oldProcessHandler = (VSCodeProcessHandler) getProcessHandler();
    final var newProcessHandler = new VSCodeProcessHandler(oldProcessHandler);
    final var console = state.createConsoleInternal(executor, newProcessHandler);

    if (console != null) {
      console.attachToProcess(oldProcessHandler);
    }

    return new VSCodeExecutionResult(state, debugPort, console, newProcessHandler, getActions());
  }
}
