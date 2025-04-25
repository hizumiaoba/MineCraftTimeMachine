package io.github.hizumiaoba.mctimemachine.api.fs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class DirectoryTraversalProgressEvent {
  private final long current;
  private final long total;
}
