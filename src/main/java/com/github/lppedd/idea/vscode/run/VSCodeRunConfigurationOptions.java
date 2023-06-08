package com.github.lppedd.idea.vscode.run;

import com.intellij.execution.configurations.RunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;
import com.intellij.util.xmlb.annotations.OptionTag;
import org.jetbrains.annotations.Nullable;

/**
 * Persisted Run Configuration settings.
 *
 * @author Edoardo Luppi
 */
public class VSCodeRunConfigurationOptions extends RunConfigurationOptions {
  private final StoredProperty<String> installationPathProperty = string(null).provideDelegate(this, "installationPath");
  private final StoredProperty<String> moduleNameProperty = string(null).provideDelegate(this, "moduleName");

  @OptionTag("INSTALLATION_PATH")
  public @Nullable String getInstallationPath() {
    return installationPathProperty.getValue(this);
  }

  @OptionTag("MODULE_NAME")
  public @Nullable String getModuleName() {
    return moduleNameProperty.getValue(this);
  }

  public void setInstallationPath(@Nullable final String installationPath) {
    this.installationPathProperty.setValue(this, installationPath);
  }

  public void setModuleName(@Nullable final String moduleName) {
    this.moduleNameProperty.setValue(this, moduleName);
  }
}
