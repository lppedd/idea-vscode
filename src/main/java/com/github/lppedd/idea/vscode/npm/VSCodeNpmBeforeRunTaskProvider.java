package com.github.lppedd.idea.vscode.npm;

import com.github.lppedd.idea.vscode.VSCodeIcons;
import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.ExecutionListener;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.impl.EditConfigurationsDialog;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.buildTools.HyperlinkListeningExecutionException;
import com.intellij.lang.javascript.buildTools.base.JsbtUtil;
import com.intellij.lang.javascript.buildTools.npm.beforeRun.NpmBeforeRunTaskDialog;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmConfigurationType;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunConfiguration;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunConfigurationProducer;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunConfigurationUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.HtmlBuilder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author Edoardo Luppi
 */
public class VSCodeNpmBeforeRunTaskProvider extends BeforeRunTaskProvider<VSCodeNpmBeforeRunTask> {
  public static final Key<VSCodeNpmBeforeRunTask> KEY_ID = Key.create("VSCodeNpmBeforeRunTaskProvider");
  public static final Key<VSCodeNpmBeforeRunTask> KEY_TASK = Key.create("VSCodeNpmBeforeRunTask");

  @Override
  public @NotNull Key<VSCodeNpmBeforeRunTask> getId() {
    return KEY_ID;
  }

  @Override
  public @NotNull String getName() {
    final var message = JavaScriptBundle.message("npm.before.run.task");
    return message + " (background)";
  }

  @Override
  public @NotNull Icon getIcon() {
    return VSCodeIcons.NPM_16;
  }

  @Override
  public @NotNull Icon getTaskIcon(@NotNull final VSCodeNpmBeforeRunTask task) {
    return VSCodeIcons.NPM_16;
  }

  @Override
  public @NotNull String getDescription(@NotNull final VSCodeNpmBeforeRunTask task) {
    final String path = task.getSettings().getPackageJsonSystemDependentPath();

    if (path.isEmpty()) {
      return JavaScriptBundle.message("npm.before.run.task");
    }

    final var fileName = PathUtil.getFileName(path);
    final var parentPath = PathUtil.getParentPath(path);
    final var parentName = PathUtil.getFileName(parentPath);
    final var folderAndFile = parentName.isEmpty()
        ? fileName
        : parentName + File.separator + fileName;
    final var scriptNames = task.getSettings().getScriptNames();
    final var presentableTasks = StringUtil.join(scriptNames, s -> "'" + s + "'", ", ");
    final var message = JavaScriptBundle.message("npm.before.run.task.descr", scriptNames.size(), presentableTasks, folderAndFile);
    return message + " (background)";
  }

  @Override
  public @NotNull VSCodeNpmBeforeRunTask createTask(@NotNull final RunConfiguration runConfiguration) {
    return new VSCodeNpmBeforeRunTask();
  }

  @Override
  public boolean isConfigurable() {
    return true;
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean configureTask(
      @NotNull final RunConfiguration runConfiguration,
      @NotNull final VSCodeNpmBeforeRunTask task) {
    final var oldSettings = task.getSettings();
    final var dialog = new NpmBeforeRunTaskDialog(runConfiguration.getProject(), oldSettings);

    if (dialog.showAndGet()) {
      final var newSettings = dialog.getSettings();

      if (!newSettings.equals(oldSettings)) {
        task.setSettings(newSettings);
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean canExecuteTask(
      @NotNull final RunConfiguration configuration,
      @NotNull final VSCodeNpmBeforeRunTask task) {
    try {
      NpmRunConfigurationUtil.check(configuration.getProject(), task.getSettings());
      return true;
    } catch (final RuntimeConfigurationError e) {
      return false;
    }
  }

  @Override
  public boolean executeTask(
      @NotNull final DataContext context,
      @NotNull final RunConfiguration configuration,
      @NotNull final ExecutionEnvironment environment,
      @NotNull final VSCodeNpmBeforeRunTask task) {
    final var project = configuration.getProject();
    final var taskManager = VSCodeNpmTaskManager.getInstance();

    if (taskManager.isTaskRunning(task)) {
      return true;
    }

    final var taskSettings = task.getSettings();
    final var runManager = RunManager.getInstance(environment.getProject());

    try {
      NpmRunConfigurationUtil.check(project, taskSettings);
    } catch (final RuntimeConfigurationError e) {
      final var message = new HtmlBuilder()
          .append(e.getMessage())
          .br()
          .appendLink("", JavaScriptBundle.message("buildTools.edit.run.configuration"))
          .toString();

      final var exception = new HyperlinkListeningExecutionException(message, () -> {
        runManager.setSelectedConfiguration(environment.getRunnerAndConfigurationSettings());

        final var configurationsDialog = new EditConfigurationsDialog(environment.getProject());
        configurationsDialog.show();
      });

      ExecutionUtil.handleExecutionError(environment.getProject(), "Run", "Npm script before launch", exception);
      return false;
    }

    final var settings = runManager.createConfiguration("", NpmConfigurationType.class);
    final var npmRunConfiguration = (NpmRunConfiguration) settings.getConfiguration();
    NpmRunConfigurationProducer.setupConfigurationFromSettings(npmRunConfiguration, taskSettings);
    npmRunConfiguration.putUserData(KEY_TASK, task);

    @SuppressWarnings("UnstableApiUsage")
    final var messageBusConnection = project.getMessageBus().simpleConnect();
    messageBusConnection.subscribe(ExecutionManager.EXECUTION_TOPIC, new ExecutionListener() {
      @Override
      public void processNotStarted(
          @NotNull final String executorId,
          @NotNull final ExecutionEnvironment npmEnvironment) {
        if (npmEnvironment.getRunProfile() instanceof final NpmRunConfiguration npmRunConfiguration) {
          final var runningTask = npmRunConfiguration.getUserData(KEY_TASK);

          if (task == runningTask) {
            messageBusConnection.disconnect();
          }
        }
      }

      @Override
      public void processStarting(
          @NotNull final String executorId,
          @NotNull final ExecutionEnvironment npmEnvironment,
          @NotNull final ProcessHandler npmHandler) {
        if (npmEnvironment.getRunProfile() instanceof final NpmRunConfiguration npmRunConfiguration) {
          final var runningTask = npmRunConfiguration.getUserData(KEY_TASK);

          if (task == runningTask) {
            npmHandler.addProcessListener(new ProcessListener() {
              final Disposable taskDisposable = Disposer.newDisposable();

              @Override
              public void startNotified(@NotNull final ProcessEvent event) {
                messageBusConnection.disconnect();
                taskManager.registerRunningTask(task, taskDisposable);

                // Let's wait for 5 seconds, the same as VS Code, and then signal the process
                // as terminated. The process will continue running in the background, but this
                // trick allow the main Run Configuration to continue.
                //
                // IntelliJ: RunConfigurationBeforeRunProvider#doRunTask
                // VS Code:  debugTaskRunner#runTask
                AppExecutorUtil.getAppScheduledExecutorService().schedule(() -> {
                  if (!npmHandler.isProcessTerminating() && !npmHandler.isProcessTerminated()) {
                    final var publisher = project.getMessageBus().syncPublisher(ExecutionManager.EXECUTION_TOPIC);
                    publisher.processTerminated(executorId, npmEnvironment, npmHandler, 0);
                  }
                }, 4500, TimeUnit.MILLISECONDS);
              }

              @Override
              public void processTerminated(@NotNull final ProcessEvent event) {
                Disposer.dispose(taskDisposable);
              }
            });
          }
        }
      }
    });

    final var result = JsbtUtil.executeBeforeRunTask(environment, settings);

    if (!result) {
      messageBusConnection.disconnect();
    }

    return result;
  }
}
