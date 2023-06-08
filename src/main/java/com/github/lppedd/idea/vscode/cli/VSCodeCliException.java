package com.github.lppedd.idea.vscode.cli;

import org.jetbrains.annotations.NotNull;

/**
 * @author Edoardo Luppi
 */
public class VSCodeCliException extends RuntimeException {
  public VSCodeCliException(@NotNull final String message) {
    super(message);
  }

  public VSCodeCliException(@NotNull final String message, @NotNull final Throwable cause) {
    super(message, cause);
  }

  @Override
  public @NotNull String getMessage() {
    return super.getMessage();
  }
}
