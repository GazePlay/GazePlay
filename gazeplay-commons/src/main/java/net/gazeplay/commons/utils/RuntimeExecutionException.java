package net.gazeplay.commons.utils;

import java.util.concurrent.ExecutionException;

public class RuntimeExecutionException extends RuntimeException {

    public RuntimeExecutionException(final String message, final ExecutionException cause) {
        super(message, cause);
    }

    public RuntimeExecutionException(final ExecutionException cause) {
        super(cause);
    }

}
