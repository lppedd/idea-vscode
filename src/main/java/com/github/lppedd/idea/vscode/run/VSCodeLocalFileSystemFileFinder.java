package com.github.lppedd.idea.vscode.run;

import com.intellij.javascript.debugger.LocalFileSystemFileFinder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Url;
import com.intellij.util.Urls;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author Edoardo Luppi
 */
public class VSCodeLocalFileSystemFileFinder extends LocalFileSystemFileFinder {
  private static final String TICINO_MOCK_URL = "mock://https://ticino.blob.core.windows.net";
  private static final String TICINO_AUTHORITY = "ticino.blob.core.windows.net";

  @Override
  public @NotNull List<Url> getRemoteUrls(@NotNull final VirtualFile file) {
    final var fileUrl = file.getUrl();

    if (fileUrl.startsWith(TICINO_MOCK_URL)) {
      final var ticinoFilePath = fileUrl.substring(TICINO_MOCK_URL.length());
      final var url = Urls.newUrl("https", TICINO_AUTHORITY, ticinoFilePath);
      return Collections.singletonList(url);
    }

    return super.getRemoteUrls(file);
  }
}
