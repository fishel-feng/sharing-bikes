package com.fx.sharingbikes.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class RestAuthenticationProvider implements AuthenticationProvider {
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication instanceof PreAuthenticatedAuthenticationToken) {
            PreAuthenticatedAuthenticationToken preAuth = (PreAuthenticatedAuthenticationToken) authentication;
            RestAuthenticationToken sysAuth = (RestAuthenticationToken) preAuth.getPrincipal();
            if (checkAuthorities(sysAuth)) {
                return sysAuth;
            }
        } else if (authentication instanceof RestAuthenticationToken) {
            RestAuthenticationToken sysAuth = (RestAuthenticationToken) authentication;
            if (checkAuthorities(sysAuth)) {
                return sysAuth;
            }
        }
        throw new BadCredentialException("unknown.error");
    }

    private boolean checkAuthorities(RestAuthenticationToken sysAuth) {
        if (sysAuth.getAuthorities() != null && sysAuth.getAuthorities().size() > 0) {
            GrantedAuthority gauth = sysAuth.getAuthorities().iterator().next();
            if ("BIKE_CLIENT".equals(gauth.getAuthority()) || "ROLE_SOMEONE".equals(gauth.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication) || RestAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
