package com.github.lppedd.idea.vscode.run.states;

import com.github.lppedd.idea.vscode.VSCodeSessionManager;
import com.github.lppedd.idea.vscode.VSCodeUtils;
import com.github.lppedd.idea.vscode.run.VSCodeProcessHandler;
import com.github.lppedd.idea.vscode.run.VSCodeRunConfiguration;
import com.github.lppedd.idea.vscode.run.VSCodeRunConfigurationOptions;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.DebuggableRunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.concurrency.Promises;

import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Edoardo Luppi
 */
public class VSCodeCommandLineRunState extends CommandLineState implements DebuggableRunProfileState {
  private static final int NO_DEBUG = -1;

  private final VSCodeRunConfiguration runConfiguration;
  private final Executor executor;

  public VSCodeCommandLineRunState(
      @NotNull final VSCodeRunConfiguration runConfiguration,
      @NotNull final Executor executor,
      @NotNull final ExecutionEnvironment environment) {
    super(environment);
    this.runConfiguration = runConfiguration;
    this.executor = executor;
  }

  @Override
  protected @NotNull ProcessHandler startProcess() {
    throw new UnsupportedOperationException("This should never be called. See startProcessInternal");
  }

  @Override
  protected @Nullable ConsoleView createConsole(@NotNull final Executor executor) {
    throw new UnsupportedOperationException("This should never be called. See createConsoleInternal");
  }

  @Override
  public @NotNull Promise<ExecutionResult> execute(final int debugPort) {
    try {
      return Promises.resolvedPromise(executeInternal(executor, debugPort));
    } catch (final ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public @NotNull ExecutionResult execute(
      @NotNull final Executor executor,
      @NotNull final ProgramRunner<?> runner) throws ExecutionException {
    return executeInternal(executor, NO_DEBUG);
  }

  public @Nullable ConsoleView createConsoleInternal(
      @NotNull final Executor executor,
      @NotNull final VSCodeProcessHandler processHandler) throws ExecutionException {
    final var console = super.createConsole(executor);

    if (console != null) {
      console.addMessageFilter((line, entireLength) -> {
        if (line.contains("Lifecycle#onWillShutdown")) {
          VSCodeSessionManager.getInstance().setShutDown(processHandler);
        }

        return null;
      });
    }

    return console;
  }

  private @NotNull VSCodeExecutionResult executeInternal(
      @NotNull final Executor executor,
      final int debugPort) throws ExecutionException {
    final var processHandler = startProcessInternal(debugPort);
    final var console = createConsoleInternal(executor, processHandler);
    final AnAction[] actions;

    if (console != null) {
      console.attachToProcess(processHandler);
      actions = createActions(console, processHandler, executor);
    } else {
      actions = new AnAction[0];
    }

    return new VSCodeExecutionResult(this, debugPort, console, processHandler, actions);
  }

  private @NotNull VSCodeProcessHandler startProcessInternal(final int debugPort) throws ExecutionException {
    final var options = runConfiguration.getOptions();
    final var installationPath = Objects.requireNonNull(options.getInstallationPath());
    final var cliPath = VSCodeUtils.getCliPath(Path.of(installationPath));

    final var commandLine = new GeneralCommandLine();
    commandLine.setExePath(cliPath.toString());

    if (debugPort != NO_DEBUG) {
      commandLine.addParameter("--inspect-extensions=" + debugPort);
    }

    final var module = Objects.requireNonNull(getCurrentModule(runConfiguration.getProject(), options));
    final var moduleRoot = ModuleRootManager.getInstance(module).getContentRoots()[0];

    commandLine.addParameter("--extensionDevelopmentPath=" + moduleRoot.getPath());
    commandLine.addParameter("--verbose");

    return new VSCodeProcessHandler(commandLine);
  }

  private @NotNull Module getCurrentModule(
      @NotNull final Project project,
      @NotNull final VSCodeRunConfigurationOptions options) {
    final var moduleName = options.getModuleName();

    if (moduleName == null) {
      throw new IllegalStateException("The module name should not be null here");
    }

    final var module = ModuleManager.getInstance(project).findModuleByName(moduleName);

    if (module == null) {
      throw new IllegalStateException("The module should not be null here");
    }

    return module;
  }
}
