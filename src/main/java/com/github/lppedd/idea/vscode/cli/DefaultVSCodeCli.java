package com.github.lppedd.idea.vscode.cli;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Version;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Edoardo Luppi
 */
public class DefaultVSCodeCli implements VSCodeCli {
  private final Path executable;

  public DefaultVSCodeCli(@NotNull final Path executable) {
    this.executable = executable;
  }

  @Override
  public @NotNull Version getVersion(@NotNull final ProgressIndicator indicator) {
    checkExecutable();

    final var commandLine = new GeneralCommandLine()
        .withExePath(executable.toString())
        .withParameters("--version")
        .withCharset(StandardCharsets.UTF_8);

    try {
      final var processHandler = new CapturingProcessHandler(commandLine);
      final var processOutput = processHandler.runProcessWithProgressIndicator(indicator, 10_000, true);

      if (processOutput.isCancelled()) {
        throw new ProcessCanceledException();
      }

      if (processOutput.isTimeout()) {
        throw new VSCodeCliException("Version retrieval timed out");
      }

      final var stdoutLines = processOutput.getStdoutLines();

      if (stdoutLines.size() != 3) {
        throw new VSCodeCliException("Not a VS Code CLI executable");
      }

      final var versionStr = stdoutLines.get(0).trim();
      return Objects.requireNonNull(Version.parseVersion(versionStr));
    } catch (final ExecutionException e) {
      throw new VSCodeCliException("Internal error during version retrieval. See log file.", e);
    }
  }

  private void checkExecutable() {
    if (Files.notExists(executable)) {
      throw new VSCodeCliException("The CLI executable does not exist");
    }
  }
}
