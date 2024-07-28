package io.github.hizumiaoba.mctimemachine.api;

import java.nio.file.attribute.FileTime;

public record WorldAttribute(long size, FileTime createdAt, int savedWorldCount) {

}
