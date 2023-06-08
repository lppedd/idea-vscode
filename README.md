# VS Code Development

Provides a Run/Debug Configuration to easily test your VS Code extension inside of IntelliJ IDEA.

## Current limitations

- Uses remote debugging, which is slower than native VS Code debugging
- Debugger does work immediately after invoking _Reload Window_
- Cannot debug tests

## Instructions

- Create a VS Code Run Configuration

  ![](.github/images/new-run-config.png "New VS Code Run Configuration")

- Select the path of your local VS Code installation, and the IDE module containing the extension

  ![](.github/images/edit-run-config.png "Edit VS Code Run Configuration")