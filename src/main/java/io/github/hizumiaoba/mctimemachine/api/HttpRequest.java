package io.github.hizumiaoba.mctimemachine.api;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface HttpRequest<T> {

  CompletableFuture<T> handleAsync(String uuid);

  <U> CompletableFuture<U> handleAsync(String uuid, Function<? super T, ? extends U> fn);

  void handleAsync(String uuid, Consumer<? super T> action);

  default T handleSync(String uuid) {
    return handleAsync(uuid).join();
  }
}
