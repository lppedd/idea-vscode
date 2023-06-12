package com.github.lppedd.idea.vscode.run;

import com.github.lppedd.idea.vscode.VSCodeIcons;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Edoardo Luppi
 */
public class VSCodeConfigurationType implements ConfigurationType, DumbAware {
  public static final String ID = "LppeddIdeaVSCode";
  public static final String NAME = "VS Code";

  @Override
  public @NotNull String getId() {
    return ID;
  }

  @Override
  public @NotNull String getDisplayName() {
    return NAME;
  }

  @Override
  public @NotNull Icon getIcon() {
    return VSCodeIcons.VSCODE_16;
  }

  @Override
  public @NotNull String getConfigurationTypeDescription() {
    return "Visual Studio Code configuration type";
  }

  @Override
  public @NotNull ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{
        new VSCodeConfigurationFactory(this)
    };
  }
}
