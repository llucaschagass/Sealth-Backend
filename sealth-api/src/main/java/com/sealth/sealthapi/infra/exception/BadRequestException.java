package com.sealth.sealthapi.infra.exception;

/** Requisição bem formada, mas que não pode ser atendida (resulta em 400). */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
