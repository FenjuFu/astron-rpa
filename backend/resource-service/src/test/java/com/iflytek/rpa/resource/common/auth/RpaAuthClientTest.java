package com.iflytek.rpa.resource.common.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.iflytek.rpa.resource.common.exp.ServiceException;
import com.iflytek.rpa.resource.common.response.ErrorCodeEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class RpaAuthClientTest {

    private static final String COOKIE = "SESSION=session-a";

    private MockRestServiceServer server;
    private RpaAuthClient rpaAuthClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://rpa-auth");
        server = MockRestServiceServer.bindTo(builder).build();
        rpaAuthClient = new RpaAuthClient(builder.build());
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getCurrentTenantIdForwardsCallerSessionCookie() {
        bindRequestWithCookie(COOKIE);
        server.expect(requestTo("http://rpa-auth/api/rpa-auth/tenant/getTenantId"))
                .andExpect(header(HttpHeaders.COOKIE, COOKIE))
                .andRespond(withSuccess("{\"code\":\"000000\",\"data\":\"tenant-a\"}", MediaType.APPLICATION_JSON));

        assertThat(rpaAuthClient.getCurrentTenantId()).isEqualTo("tenant-a");
        server.verify();
    }

    @Test
    void getCurrentUserIdReadsIdFromUserInfo() {
        bindRequestWithCookie(COOKIE);
        server.expect(requestTo("http://rpa-auth/api/rpa-auth/user/info"))
                .andExpect(header(HttpHeaders.COOKIE, COOKIE))
                .andRespond(withSuccess(
                        "{\"code\":\"000000\",\"data\":{\"id\":\"user-a\",\"name\":\"A\"},\"message\":\"\"}",
                        MediaType.APPLICATION_JSON));

        assertThat(rpaAuthClient.getCurrentUserId()).isEqualTo("user-a");
        server.verify();
    }

    @Test
    void rejectsRequestWithoutSessionCookieWithoutCallingAuthService() {
        bindRequestWithCookie(null);

        assertNotLogin(() -> rpaAuthClient.getCurrentTenantId());
        server.verify();
    }

    @Test
    void rejectsWhenAuthServiceReportsNotLoggedIn() {
        bindRequestWithCookie(COOKIE);
        server.expect(requestTo("http://rpa-auth/api/rpa-auth/tenant/getTenantId"))
                .andRespond(withSuccess(
                        "{\"code\":\"900001\",\"data\":\"\",\"message\":\"用户未登录\"}", MediaType.APPLICATION_JSON));

        assertNotLogin(() -> rpaAuthClient.getCurrentTenantId());
    }

    @Test
    void rejectsWhenAuthServiceReturnsUnauthorized() {
        bindRequestWithCookie(COOKIE);
        server.expect(requestTo("http://rpa-auth/api/rpa-auth/tenant/getTenantId"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertNotLogin(() -> rpaAuthClient.getCurrentTenantId());
    }

    @Test
    void rejectsBlankTenantId() {
        bindRequestWithCookie(COOKIE);
        server.expect(requestTo("http://rpa-auth/api/rpa-auth/tenant/getTenantId"))
                .andRespond(withSuccess("{\"code\":\"000000\",\"data\":\"\"}", MediaType.APPLICATION_JSON));

        assertNotLogin(() -> rpaAuthClient.getCurrentTenantId());
    }

    private static void bindRequestWithCookie(String cookie) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (cookie != null) {
            request.addHeader(HttpHeaders.COOKIE, cookie);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private static void assertNotLogin(Runnable call) {
        assertThatThrownBy(call::run)
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(ErrorCodeEnum.E_NOT_LOGIN.getCode());
    }
}
