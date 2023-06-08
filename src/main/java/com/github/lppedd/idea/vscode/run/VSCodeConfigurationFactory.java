package com.github.lppedd.idea.vscode.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationSingletonPolicy;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author Edoardo Luppi
 */
public class VSCodeConfigurationFactory extends ConfigurationFactory {
  public VSCodeConfigurationFactory(@NotNull final VSCodeConfigurationType type) {
    super(type);
  }

  @Override
  public @NotNull String getId() {
    return VSCodeConfigurationType.ID;
  }

  @Override
  public @NotNull RunConfigurationSingletonPolicy getSingletonPolicy() {
    return RunConfigurationSingletonPolicy.SINGLE_INSTANCE_ONLY;
  }

  @Override
  public boolean isEditableInDumbMode() {
    return true;
  }

  @Override
  public @NotNull Class<? extends BaseState> getOptionsClass() {
    return VSCodeRunConfigurationOptions.class;
  }

  @Override
  public @NotNull RunConfiguration createTemplateConfiguration(@NotNull final Project project) {
    return new VSCodeRunConfiguration(project, this, VSCodeConfigurationType.NAME);
  }
}
