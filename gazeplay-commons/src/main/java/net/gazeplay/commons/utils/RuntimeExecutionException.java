package net.gazeplay.commons.utils;

import java.util.concurrent.ExecutionException;

public class RuntimeExecutionException extends RuntimeException {

    public RuntimeExecutionException(String message, ExecutionException cause) {
        super(message, cause);
    }

    public RuntimeExecutionException(ExecutionException cause) {
        super(cause);
    }

}
