package io.github.hizumiaoba.mctimemachine.api;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface HttpRequest<T> {

  CompletableFuture<T> handleAsync();

  <U> CompletableFuture<U> handleAsync(Function<? super T, ? extends U> fn);

  CompletableFuture<Void> handleAsync(Consumer<? super T> action);

  default T handleSync() {
    return handleAsync().join();
  }
}
