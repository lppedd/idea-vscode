package com.github.lppedd.idea.vscode.run;

import com.github.lppedd.idea.vscode.VSCodeSessionManager;
import com.github.lppedd.idea.vscode.run.debug.VSCodeDebugProcess;
import com.github.lppedd.idea.vscode.run.states.VSCodeCommandLineRunState;
import com.github.lppedd.idea.vscode.run.states.VSCodeContinuationState;
import com.github.lppedd.idea.vscode.run.states.VSCodeExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AppUIUtil;
import com.intellij.util.io.socketConnection.ConnectionStatus;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.jetbrains.debugger.wip.WipLocalVmConnection;
import com.jetbrains.nodeJs.NodeJSFileFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.debugger.DebuggableRunConfiguration;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Edoardo Luppi
 */
public class VSCodeRunConfiguration
    extends RunConfigurationBase<VSCodeRunConfigurationOptions>
    implements DebuggableRunConfiguration, RemoteRunProfile {
  private final AtomicBoolean isReloading = new AtomicBoolean();
  private final AtomicReference<ExecutionResult> lastExecutionResult = new AtomicReference<>();

  public VSCodeRunConfiguration(
      @NotNull final Project project,
      @NotNull final ConfigurationFactory factory,
      @NotNull final String name) {
    super(project, factory, name);
  }

  @Override
  public @NotNull VSCodeRunConfigurationOptions getOptions() {
    return (VSCodeRunConfigurationOptions) super.getOptions();
  }

  @Override
  public @NotNull SettingsEditor<VSCodeRunConfiguration> getConfigurationEditor() {
    return new VSCodeSettingsEditor(getProject());
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    final var options = getOptions();

    if (options.getInstallationPath() == null) {
      throw new RuntimeConfigurationError("The VS Code installation path is mandatory");
    }

    if (options.getModuleName() == null) {
      throw new RuntimeConfigurationError("The extension module is mandatory");
    }
  }

  @Override
  public RunProfileState getState(
      @NotNull final Executor executor,
      @NotNull final ExecutionEnvironment environment) throws ExecutionException {
    if (isReloading.compareAndSet(true, false)) {
      return new VSCodeContinuationState(((VSCodeExecutionResult) lastExecutionResult.get()).copy(executor));
    }

    return new VSCodeCommandLineRunState(this, executor, environment);
  }

  @Override
  public @NotNull InetSocketAddress computeDebugAddress(@NotNull final RunProfileState state) throws ExecutionException {
    if (state instanceof final VSCodeContinuationState continuationState) {
      return new InetSocketAddress(InetAddress.getLoopbackAddress(), continuationState.getDebugPort());
    }

    return DebuggableRunConfiguration.super.computeDebugAddress(state);
  }

  @Override
  public @NotNull XDebugProcess createDebugProcess(
      @NotNull final InetSocketAddress socketAddress,
      @NotNull final XDebugSession session,
      @Nullable final ExecutionResult executionResult,
      @NotNull final ExecutionEnvironment environment) {
    final var connection = new WipLocalVmConnection();
    connection.stateChanged(state -> {
      if (state.getStatus() == ConnectionStatus.DISCONNECTED) {
        onConnectionDisconnected(environment, Objects.requireNonNull(executionResult));
      }

      return null;
    });

    final var finder = new NodeJSFileFinder(environment.getProject());
    final var process = new VSCodeDebugProcess(session, finder, connection, executionResult);
    connection.open(socketAddress);
    return process;
  }

  private void onConnectionDisconnected(
      @NotNull final ExecutionEnvironment executionEnvironment,
      @NotNull final ExecutionResult executionResult) {
    final var processHandler = (VSCodeProcessHandler) executionResult.getProcessHandler();

    if (processHandler.isProcessTerminatedInternal() || processHandler.isProcessTerminatingInternal()) {
      return;
    }

    // Mark this ProcessHandler as terminated (even if the underlying process is still alive)
    // so that it doesn't interfere with the restart.
    // From now on this ProcessHandler instance is unusable, and should not be touched
    processHandler.setTerminated();

    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      try {
        // Wait a couple seconds before attaching the debugger.
        // Not sure how useful it is, but it should allow some VS Code services to load first
        Thread.sleep(1500);
      } catch (final InterruptedException e) {
        throw new RuntimeException(e);
      }

      // Restart the entire Run Configuration
      AppUIUtil.invokeOnEdt(() -> {
        if (!VSCodeSessionManager.getInstance().isShutDown(processHandler)) {
          if (!isReloading.compareAndSet(false, true)) {
            throw new IllegalStateException("Expected 'false' for isReloading");
          }

          lastExecutionResult.set(executionResult);
          ExecutionUtil.restart(executionEnvironment);
        }
      });
    });
  }
}
