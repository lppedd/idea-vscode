package com.github.lppedd.idea.vscode.run.ui;

import com.github.lppedd.idea.vscode.VSCodeConstants;
import com.github.lppedd.idea.vscode.VSCodeUtils;
import com.github.lppedd.idea.vscode.cli.DefaultVSCodeCli;
import com.github.lppedd.idea.vscode.cli.VSCodeCliException;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.UI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author Edoardo Luppi
 */
public class VSCodePathPanel extends JBPanel<VSCodePathPanel> {
  private final Project project;
  private final TextFieldWithBrowseButton pathField;

  @NotNull
  private Consumer<String> versionListener = v -> {};

  public VSCodePathPanel(
      @NotNull final Project project,
      @NotNull final Disposable disposable) {
    super(new BorderLayout());
    this.project = project;

    final var descriptor = new VSCodePathFileChooserDescriptor();

    pathField = new TextFieldWithBrowseButton();
    pathField.addBrowseFolderListener(new TextBrowseFolderListener(descriptor));

    final var executablePathPanel = UI.PanelFactory.panel(pathField)
        .withComment("The host VS Code installation path", false)
        .createPanel();

    final var textField = pathField.getTextField();
    new ComponentValidator(disposable)
        .withValidator(this::validateInstallationPath)
        .withOutlineProvider(c -> textField)
        .andRegisterOnDocumentListener(textField)
        .installOn(textField)
        .revalidate();

    add(executablePathPanel, BorderLayout.NORTH);
  }

  public @Nullable String getInstallationPath() {
    final var text = pathField.getText().trim();
    return text.isEmpty() ? null : text;
  }

  public void setInstallationPath(@Nullable final String installationPath) {
    pathField.setText(installationPath == null ? "" : installationPath);
  }

  public void setVersionListener(@NotNull final Consumer<String> versionListener) {
    this.versionListener = versionListener;
  }

  private @Nullable ValidationInfo validateInstallationPath() {
    versionListener.accept("N/A");

    final var text = pathField.getText().trim();

    if (text.isEmpty()) {
      return new ValidationInfo("The VS Code installation path is mandatory", pathField);
    }

    final var installationPath = Path.of(text);

    if (!installationPath.isAbsolute() || !Files.isDirectory(installationPath)) {
      return new ValidationInfo("Invalid VS Code installation path", pathField);
    }

    final var cliPath = VSCodeUtils.getCliPath(installationPath);

    if (!Files.isRegularFile(cliPath)) {
      return new ValidationInfo("Could not find Code CLI executable", pathField);
    }

    try {
      final var cli = new DefaultVSCodeCli(cliPath);
      final var version = ProgressManager.getInstance().run(new VSCodeVersionTask(project, cli));

      versionListener.accept(version.toString());

      if (version.compareTo(VSCodeConstants.MIN_VERSION) < 0) {
        return new ValidationInfo("The minimum supported version is " + VSCodeConstants.MIN_VERSION, pathField);
      }
    } catch (final VSCodeCliException e) {
      return new ValidationInfo(e.getMessage(), pathField);
    } catch (final ProcessCanceledException e) {
      // Nothing to do here, the user canceled the version retrieval process
    }

    return null;
  }
}
