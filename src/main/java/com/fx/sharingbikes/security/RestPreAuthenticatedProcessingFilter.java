package com.fx.sharingbikes.security;

import com.fx.sharingbikes.cache.CommonCacheUtil;
import com.fx.sharingbikes.common.constants.Constants;
import com.fx.sharingbikes.user.entity.UserElement;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class RestPreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {

    private AntPathMatcher matcher = new AntPathMatcher();

    private List<String> noneSecurityList;

    private CommonCacheUtil commonCacheUtil;

    public RestPreAuthenticatedProcessingFilter(List<String> noneSecurityList, CommonCacheUtil commonCacheUtil) {
        this.noneSecurityList = noneSecurityList;
        this.commonCacheUtil = commonCacheUtil;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        GrantedAuthority[] authorities = new GrantedAuthority[1];
        if (isNoneSecurity(request.getRequestURI()) || "OPTIONS".equals(request.getMethod())) {
            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_SOME");
            authorities[0]=authority;
            return new RestAuthenticationToken(Arrays.asList(authorities));
        }
        String version = request.getHeader(Constants.REQUEST_VERSION_KEY);
        String token = request.getHeader(Constants.REQUEST_TOKEN_KEY);
        if (version == null) {
            request.setAttribute("header-error", 400);
        }
        if(request.getAttribute("header-error") == null){
            try {
                if(!StringUtils.isBlank(token)){
                    UserElement userElement = commonCacheUtil.getUserByToken(token);
                    if (userElement != null) {
                        GrantedAuthority authority = new SimpleGrantedAuthority("BIKE_CLIENT");
                        authorities[0] = authority;
                        RestAuthenticationToken authToken = new RestAuthenticationToken(Arrays.asList(authorities));
                        authToken.setUser(userElement);
                        return authToken;
                    }else {
                        request.setAttribute("header-error", 401);
                    }
                } else {
                    log.warn("Got no token from request header");
                    request.setAttribute("header-error", 401);
                }
            }catch (Exception e){
                log.error("Fail to authenticate user", e);
            }
        }
        if(request.getAttribute("header-error") != null){
            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_NONE");
            authorities[0] = authority;
        }
        RestAuthenticationToken authToken = new RestAuthenticationToken(Arrays.asList(authorities));
        return authToken;
    }

    private boolean isNoneSecurity(String uri) {
        boolean result = false;
        if (this.noneSecurityList != null) {
            for (String pattern : this.noneSecurityList) {
                if (matcher.match(pattern, uri)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return null;
    }
}
