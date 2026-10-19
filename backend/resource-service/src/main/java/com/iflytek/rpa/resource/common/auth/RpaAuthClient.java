package com.iflytek.rpa.resource.common.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.iflytek.rpa.resource.common.exp.ServiceException;
import com.iflytek.rpa.resource.common.response.ErrorCodeEnum;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 通过 rpa-auth 解析当前请求的登录用户与租户
 * 转发调用方的 Cookie（Session），与 robot-service 获取用户/租户的方式保持一致
 */
@Component
public class RpaAuthClient {

    private static final String SUCCESS_CODE = "000000";
    private static final String USER_INFO_PATH = "/api/rpa-auth/user/info";
    private static final String TENANT_ID_PATH = "/api/rpa-auth/tenant/getTenantId";
    private static final int TIMEOUT_MILLIS = 5000;

    private final RestClient restClient;

    @Autowired
    public RpaAuthClient(@Value("${auth.base-url}") String baseUrl) {
        this(RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(timeoutRequestFactory())
                .build());
    }

    RpaAuthClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * 获取当前登录用户所在租户ID，未登录时抛出异常
     */
    public String getCurrentTenantId() {
        String tenantId = fetch(TENANT_ID_PATH, new ParameterizedTypeReference<AuthResponse<String>>() {});
        if (StringUtils.isBlank(tenantId)) {
            throw new ServiceException(ErrorCodeEnum.E_NOT_LOGIN);
        }
        return tenantId;
    }

    /**
     * 获取当前登录用户ID，未登录时抛出异常
     */
    public String getCurrentUserId() {
        AuthUser user = fetch(USER_INFO_PATH, new ParameterizedTypeReference<AuthResponse<AuthUser>>() {});
        if (user == null || StringUtils.isBlank(user.getId())) {
            throw new ServiceException(ErrorCodeEnum.E_NOT_LOGIN);
        }
        return user.getId();
    }

    private <T> T fetch(String path, ParameterizedTypeReference<AuthResponse<T>> type) {
        String cookie = currentCookie();
        if (StringUtils.isBlank(cookie)) {
            throw new ServiceException(ErrorCodeEnum.E_NOT_LOGIN);
        }

        AuthResponse<T> response;
        try {
            response = restClient
                    .get()
                    .uri(path)
                    .header(HttpHeaders.COOKIE, cookie)
                    .retrieve()
                    .body(type);
        } catch (RestClientResponseException e) {
            throw new ServiceException(ErrorCodeEnum.E_NOT_LOGIN);
        } catch (RestClientException e) {
            throw new ServiceException(ErrorCodeEnum.E_API_EXCEPTION.getCode(), "认证服务不可用");
        }

        if (response == null || !SUCCESS_CODE.equals(response.getCode())) {
            throw new ServiceException(ErrorCodeEnum.E_NOT_LOGIN);
        }
        return response.getData();
    }

    private static String currentCookie() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        return request.getHeader(HttpHeaders.COOKIE);
    }

    private static SimpleClientHttpRequestFactory timeoutRequestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(TIMEOUT_MILLIS);
        requestFactory.setReadTimeout(TIMEOUT_MILLIS);
        return requestFactory;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class AuthResponse<T> {
        private String code;
        private T data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class AuthUser {
        private String id;
    }
}
