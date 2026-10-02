package com.vehiclerental.exception;

public class VehicleInUseException extends RuntimeException {
    public VehicleInUseException(String message) {
        super(message);
    }
}
