package io.github.hizumiaoba.mctimemachine.internal.concurrent;

import com.google.common.base.Strings;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class ConcurrentThreadFactory implements ThreadFactory {

  private final AtomicLong count = new AtomicLong(1L);
  private final String threadIdentifier;
  private final String threadRole;
  private boolean isDaemon = false;

  public ConcurrentThreadFactory(String threadIdentifier, String threadRole, boolean isDaemon) {
    this.threadRole = threadRole;
    this.threadIdentifier = threadIdentifier;
    this.isDaemon = isDaemon;
  }

  @Override
  public Thread newThread(Runnable r) {
    String threadName = "";
    if(Strings.isNullOrEmpty(threadRole)) {
      threadName = String.format("%s-worker-pool-%d", threadIdentifier, count.getAndIncrement());
    } else {
      threadName = String.format("%s-%s-pool-%d", threadIdentifier, threadRole, count.getAndIncrement());
    }
    Thread t = new Thread(r, threadName);
    t.setDaemon(this.isDaemon);
    return t;
  }
}
