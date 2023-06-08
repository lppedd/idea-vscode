package com.github.lppedd.idea.vscode;

import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Edoardo Luppi
 */
public class VSCodeIcons {
  /**
   * VS Code logo, 16x16 px.
   */
  public static final Icon LOGO_16 = getIcon("/icons/vscode.svg");

  @SuppressWarnings("SameParameterValue")
  private static @NotNull Icon getIcon(@NotNull final String path) {
    return IconLoader.getIcon(path, VSCodeIcons.class);
  }
}
