package io.github.hizumiaoba.mctimemachine.api;

import java.nio.file.attribute.FileTime;

public record BackupDirAttributes(long size, boolean isSpecial, FileTime createdAt,
                                  int savedWorldCount) {

}
