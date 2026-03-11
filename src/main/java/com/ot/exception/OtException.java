package com.ot.exception;


public abstract class OtException extends RuntimeException {

    public OtException(String message) {
        super(message);
    }

}