package com.github.lppedd.idea.vscode.npm;

import com.intellij.execution.BeforeRunTask;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunSettings;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Edoardo Luppi
 */
public class VSCodeNpmBeforeRunTask extends BeforeRunTask<VSCodeNpmBeforeRunTask> {
  @NotNull
  private UUID uuid = UUID.randomUUID();

  @NotNull
  private NpmRunSettings settings = NpmRunSettings.builder().build();

  public VSCodeNpmBeforeRunTask() {
    super(VSCodeNpmBeforeRunTaskProvider.KEY_ID);
  }

  public @NotNull NpmRunSettings getSettings() {
    return settings;
  }

  public void setSettings(@NotNull final NpmRunSettings settings) {
    this.settings = settings;
  }

  @SuppressWarnings("deprecation")
  public void writeExternal(@NotNull final Element element) {
    super.writeExternal(element);
    element.setAttribute("vscUUID", uuid.toString());
    settings.writeExternal(element);
  }

  @SuppressWarnings("deprecation")
  public void readExternal(@NotNull final Element element) {
    super.readExternal(element);
    uuid = UUID.fromString(element.getAttributeValue("vscUUID"));
    settings = NpmRunSettings.readExternal(element);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid);
  }

  @Override
  public boolean equals(@Nullable final Object o) {
    return o instanceof final VSCodeNpmBeforeRunTask other && uuid.equals(other.uuid);
  }
}
