package io.github.hizumiaoba.mctimemachine.api.fs;

import java.io.File;

public record DirectoryProcessedEvent(long totalProcessedFilesCount, File targetDirectory) {}
