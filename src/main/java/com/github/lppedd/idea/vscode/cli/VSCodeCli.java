package com.github.lppedd.idea.vscode.cli;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Version;
import org.jetbrains.annotations.NotNull;

/**
 * @author Edoardo Luppi
 */
public interface VSCodeCli {
  @NotNull Version getVersion(@NotNull final ProgressIndicator indicator);
}
