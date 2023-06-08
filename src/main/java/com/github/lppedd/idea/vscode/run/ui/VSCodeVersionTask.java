package com.github.lppedd.idea.vscode.run.ui;

import com.github.lppedd.idea.vscode.cli.VSCodeCli;
import com.github.lppedd.idea.vscode.cli.VSCodeCliException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Version;
import org.jetbrains.annotations.NotNull;

/**
 * @author Edoardo Luppi
 */
public class VSCodeVersionTask extends Task.WithResult<Version, VSCodeCliException> {
  private final VSCodeCli cli;

  public VSCodeVersionTask(@NotNull final Project project, @NotNull final VSCodeCli cli) {
    super(project, "VS Code Run Configuration", true);
    this.cli = cli;
  }

  @Override
  protected Version compute(@NotNull final ProgressIndicator indicator) throws VSCodeCliException {
    indicator.setIndeterminate(true);
    indicator.setText("Retrieving VS Code version...");
    return cli.getVersion(indicator);
  }
}
