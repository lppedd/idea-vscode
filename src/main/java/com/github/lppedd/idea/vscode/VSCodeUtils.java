package com.github.lppedd.idea.vscode;

import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * @author Edoardo Luppi
 */
public class VSCodeUtils {
  /**
   * Returns the CLI executable path given a VS Code installation path.
   */
  public static @NotNull Path getCliPath(@NotNull final Path installationPath) {
    if (SystemInfo.isWindows) {
      return installationPath.resolve("bin/code.cmd");
    }

    if (SystemInfo.isMac) {
      return installationPath.resolve("Contents/Resources/app/bin/code");
    }

    return installationPath.resolve("bin/code");
  }
}
