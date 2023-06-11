package com.github.lppedd.idea.vscode.run;

import com.github.lppedd.idea.vscode.run.ui.VSCodeSettingsPanel;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Edoardo Luppi
 */
public class VSCodeSettingsEditor extends SettingsEditor<VSCodeRunConfiguration> {
  private final Disposable disposable;
  private final VSCodeSettingsPanel panel;

  public VSCodeSettingsEditor(@NotNull final Project project) {
    // noinspection IncorrectParentDisposable
    disposable = Disposer.newDisposable(project, "VS Code run config settings editor");
    panel = new VSCodeSettingsPanel(project, disposable);
  }

  @Override
  public void resetEditorFrom(@NotNull final VSCodeRunConfiguration runConfiguration) {
    panel.resetRunConfiguration(runConfiguration);
  }

  @Override
  public void applyEditorTo(@NotNull final VSCodeRunConfiguration runConfiguration) {
    panel.applyRunConfiguration(runConfiguration);
  }

  @Override
  public @NotNull JComponent createEditor() {
    return panel;
  }

  @Override
  protected void disposeEditor() {
    Disposer.dispose(disposable);
  }
}
