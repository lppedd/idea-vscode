package com.github.lppedd.idea.vscode.run.debug;

import com.intellij.execution.ExecutionResult;
import com.intellij.javascript.debugger.DebuggableFileFinder;
import com.intellij.javascript.debugger.SyntheticSuspendBreakpointInfo;
import com.intellij.xdebugger.XDebugSession;
import com.jetbrains.nodeJs.NodeChromeDebugProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.debugger.ExecutionStackView;
import org.jetbrains.debugger.Script;
import org.jetbrains.debugger.SuspendContext;
import org.jetbrains.debugger.connection.RemoteVmConnection;
import org.jetbrains.wip.WipVm;

import java.util.Objects;

/**
 * @author Edoardo Luppi
 */
public class VSCodeDebugProcess extends NodeChromeDebugProcess {
  public VSCodeDebugProcess(
      @NotNull final XDebugSession session,
      @NotNull final DebuggableFileFinder finder,
      @NotNull final RemoteVmConnection<? extends WipVm> connection,
      @Nullable final ExecutionResult executionResult) {
    super(session, finder, connection, executionResult);
  }

  @Override
  protected @Nullable SyntheticSuspendBreakpointInfo getSyntheticSuspend(
      @NotNull final SuspendContext<?> context,
      @NotNull final Script script) {
    final var syntheticSuspend = super.getSyntheticSuspend(context, script);

    if (syntheticSuspend != null && !syntheticSuspend.isSkipped()) {
      final var viewSupport = getDebuggerViewSupport();
      final var vmPresentableName = context.getVm().getPresentableName();
      final var stackView = new ExecutionStackView(context, viewSupport, script, null, vmPresentableName, false);
      final var topFrame = stackView.getTopFrame();

      if (topFrame != null) {
        final var topFramePosition = topFrame.getSourcePosition();

        if (topFramePosition != null) {
          final var breakpoint = Objects.requireNonNull(syntheticSuspend.getBreakpoint());
          final var sourcePosition = breakpoint.getSourcePosition();

          if (sourcePosition != null && !sourcePosition.getFile().equals(topFramePosition.getFile())) {
            resume(stackView.getVm());
            return new SyntheticSuspendBreakpointInfo(null);
          }
        }
      }
    }

    return syntheticSuspend;
  }
}
