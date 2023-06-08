package com.github.lppedd.idea.vscode.run.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Edoardo Luppi
 */
public class VSCodePathFileChooserDescriptor extends FileChooserDescriptor {
  public VSCodePathFileChooserDescriptor() {
    super(false, true, false, false, false, false);
  }

  @Override
  public @NotNull String getTitle() {
    return "VS Code Installation Path";
  }
}
