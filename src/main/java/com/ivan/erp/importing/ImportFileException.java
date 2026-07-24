package com.ivan.erp.importing;

public class ImportFileException extends RuntimeException {

    public ImportFileException(String message) {
        super(message);
    }

    public ImportFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
