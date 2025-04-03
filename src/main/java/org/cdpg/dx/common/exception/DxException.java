package org.cdpg.dx.common.exception;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import java.util.concurrent.Callable;

public class DxException extends RuntimeException implements Context {
    private final String errorCode;

    public DxException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public void runOnContext(Handler<Void> action) {

    }

    @Override
    public <T> void executeBlocking(Handler<Promise<T>> blockingCodeHandler, boolean ordered,
                                    Handler<AsyncResult<@Nullable T>> asyncResultHandler) {

    }

    @Override
    public <T> Future<@Nullable T> executeBlocking(Callable<T> blockingCodeHandler, boolean ordered) {
        return null;
    }

    @Override
    public <T> Future<@Nullable T> executeBlocking(Handler<Promise<T>> blockingCodeHandler, boolean ordered) {
        return null;
    }

    @Override
    public String deploymentID() {
        return "";
    }

    @Override
    public @Nullable JsonObject config() {
        return null;
    }

    @Override
    public boolean isEventLoopContext() {
        return false;
    }

    @Override
    public boolean isWorkerContext() {
        return false;
    }

    @Override
    public ThreadingModel threadingModel() {
        return null;
    }

    @Override
    public <T> T get(Object key) {
        return null;
    }

    @Override
    public void put(Object key, Object value) {

    }

    @Override
    public boolean remove(Object key) {
        return false;
    }

    @Override
    public <T> T getLocal(Object key) {
        return null;
    }

    @Override
    public void putLocal(Object key, Object value) {

    }

    @Override
    public boolean removeLocal(Object key) {
        return false;
    }

    @Override
    public Vertx owner() {
        return null;
    }

    @Override
    public int getInstanceCount() {
        return 0;
    }

    @Override
    public Context exceptionHandler(@Nullable Handler<Throwable> handler) {
        return null;
    }

    @Override
    public @Nullable Handler<Throwable> exceptionHandler() {
        return null;
    }
}