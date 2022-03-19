package one.xis.js;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.delayedExecutor;

class JavascriptLoader {
    private final JavascriptResourcePaths resourcePaths = new JavascriptResourcePaths();

    void test() throws ExecutionException, InterruptedException {
       
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Executor afterTenSecs = delayedExecutor(10L, TimeUnit.SECONDS);
        CompletableFuture<String> future
                = CompletableFuture.supplyAsync(() -> "someValue", afterTenSecs);

        future.thenAccept(System.out::println).join();
    }
}
