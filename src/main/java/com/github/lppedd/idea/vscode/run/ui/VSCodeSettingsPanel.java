package com.github.lppedd.idea.vscode.run.ui;

import com.github.lppedd.idea.vscode.run.VSCodeRunConfiguration;
import com.github.lppedd.idea.vscode.run.VSCodeRunConfigurationOptions;
import com.intellij.application.options.ModulesComboBox;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.border.IdeaTitledBorder;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * @author Edoardo Luppi
 */
public class VSCodeSettingsPanel extends JBPanel<VSCodeSettingsPanel> {
  private final VSCodePathPanel pathPanel;
  private final ModulesComboBox moduleCombo;

  public VSCodeSettingsPanel(
      @NotNull final Project project,
      @NotNull final Disposable disposable) {
    super(new GridBagLayout());

    // Installation Settings
    final var installGb = new GridBag()
        .setDefaultInsets(JBUI.insets(6))
        .setDefaultAnchor(0, GridBagConstraints.NORTHEAST)
        .setDefaultAnchor(1, GridBagConstraints.NORTHWEST)
        .setDefaultWeightX(0, 0)
        .setDefaultWeightX(1, 1)
        .setDefaultFill(1, GridBagConstraints.HORIZONTAL);

    final var installPanel = new JBPanel<JBPanel<?>>(new GridBagLayout());
    installPanel.setBorder(new IdeaTitledBorder("Installation Settings", JBUI.scale(10), JBUI.emptyInsets()));
    installPanel.add(new JBLabel("Path:"), installGb.nextLine().next().insetTop(12));

    final var versionValueLabel = new JBLabel("N/A");
    versionValueLabel.setForeground(JBUI.CurrentTheme.ContextHelp.FOREGROUND);

    pathPanel = new VSCodePathPanel(project, disposable);
    pathPanel.setVersionListener(versionValueLabel::setText);

    installPanel.add(pathPanel, installGb.next());

    final var versionLabel = new JBLabel("Version:");
    versionLabel.setForeground(JBUI.CurrentTheme.ContextHelp.FOREGROUND);

    installPanel.add(versionLabel, installGb.nextLine().next().insetTop(0));
    installPanel.add(versionValueLabel, installGb.next().insets(0, 8, 0, 0));

    // Extension Settings
    final var extensionGb = new GridBag()
        .setDefaultInsets(JBUI.insets(6))
        .setDefaultAnchor(0, GridBagConstraints.NORTHEAST)
        .setDefaultAnchor(1, GridBagConstraints.NORTHWEST)
        .setDefaultWeightX(0, 0)
        .setDefaultWeightX(1, 1)
        .setDefaultFill(1, GridBagConstraints.HORIZONTAL);

    final var extensionPanel = new JBPanel<JBPanel<?>>(new GridBagLayout());
    extensionPanel.setBorder(new IdeaTitledBorder("Extension Settings", JBUI.scale(10), JBUI.insetsTop(15)));
    extensionPanel.add(new JBLabel("Module:"), extensionGb.nextLine().next().insetTop(12));

    moduleCombo = new ModulesComboBox();
    final var modulePanel = UI.PanelFactory.panel(moduleCombo)
        .withComment("The module where the development extension is located", false)
        .createPanel();

    extensionPanel.add(modulePanel, extensionGb.next());

    // This panel
    final var thisGb = new GridBag()
        .setDefaultFill(GridBagConstraints.HORIZONTAL)
        .setDefaultWeightX(1);

    add(installPanel, thisGb.nextLine().next());
    add(extensionPanel, thisGb.nextLine().next());

    // An empty component to fill all the remaining vertical space
    add(Box.createVerticalBox(), thisGb.nextLine().next().weighty(1));
  }

  public void resetRunConfiguration(@NotNull final VSCodeRunConfiguration runConfiguration) {
    final var options = runConfiguration.getOptions();
    final var currentInstallationPath = options.getInstallationPath();
    pathPanel.setInstallationPath(currentInstallationPath);

    final var project = runConfiguration.getProject();
    final var modules = ModuleManager.getInstance(project).getModules();
    moduleCombo.setModules(Arrays.asList(modules));

    final var currentModule = getCurrentModule(project, options);

    if (currentModule == null && modules.length > 0) {
      moduleCombo.setSelectedModule(modules[0]);
    } else {
      moduleCombo.setSelectedModule(currentModule);
    }
  }

  public void applyRunConfiguration(@NotNull final VSCodeRunConfiguration runConfiguration) {
    runConfiguration.getOptions().setInstallationPath(pathPanel.getInstallationPath());

    final var selectedModule = moduleCombo.getSelectedModule();

    if (selectedModule != null) {
      runConfiguration.getOptions().setModuleName(selectedModule.getName());
    }
  }

  private @Nullable Module getCurrentModule(
      @NotNull final Project project,
      @NotNull final VSCodeRunConfigurationOptions options) {
    final var moduleName = options.getModuleName();
    return moduleName != null
        ? ModuleManager.getInstance(project).findModuleByName(moduleName)
        : null;
  }
}
