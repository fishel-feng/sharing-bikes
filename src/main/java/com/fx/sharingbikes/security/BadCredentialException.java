package com.fx.sharingbikes.security;

import org.springframework.security.core.AuthenticationException;

public class BadCredentialException extends AuthenticationException {
    public BadCredentialException(String message) {
        super(message);
    }
}
