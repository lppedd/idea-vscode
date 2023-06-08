package com.github.lppedd.idea.vscode.run.states;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.runners.DebuggableRunProfileState;
import com.intellij.execution.runners.ProgramRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.concurrency.Promises;

/**
 * @author Edoardo Luppi
 */
public class VSCodeContinuationState implements DebuggableRunProfileState {
  private final VSCodeExecutionResult executionResult;

  public VSCodeContinuationState(@NotNull final VSCodeExecutionResult executionResult) {
    this.executionResult = executionResult;
  }

  public int getDebugPort() {
    return executionResult.getDebugPort();
  }

  @Override
  public @NotNull Promise<ExecutionResult> execute(final int debugPort) {
    return Promises.resolvedPromise(executionResult);
  }

  @Override
  public @Nullable ExecutionResult execute(@Nullable final Executor executor, @NotNull final ProgramRunner<?> runner) {
    return executionResult;
  }
}
