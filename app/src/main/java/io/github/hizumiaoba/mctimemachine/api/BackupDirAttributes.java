package io.github.hizumiaoba.mctimemachine.api;

import java.nio.file.attribute.FileTime;

public record BackupDirAttributes(String dirName, long size, boolean isSpecial, FileTime createdAt,
                                  int savedWorldCount) {

}
