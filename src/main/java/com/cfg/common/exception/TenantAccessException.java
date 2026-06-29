package com.cfg.common.exception;

public class TenantAccessException extends RuntimeException {
    public TenantAccessException() {
        super("Access denied: resource does not belong to your restaurant");
    }

    public TenantAccessException(String message) {
        super(message);
    }
}
