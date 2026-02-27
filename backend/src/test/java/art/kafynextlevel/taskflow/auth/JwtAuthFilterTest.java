package art.kafynextlevel.taskflow.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthFilterTest {

    private JwtService jwtService;
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        JwtAuthProperties properties = new JwtAuthProperties();
        properties.setSecret("taskflow-jwt-secret-for-tests-32-bytes");
        properties.setAccessTokenTtl(Duration.ofMinutes(15));
        properties.setRefreshTokenTtl(Duration.ofDays(7));

        jwtService = new JwtService(properties);
        jwtAuthFilter = new JwtAuthFilter(jwtService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_skipsWhenAuthorizationHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/secure/ping");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void doFilter_populatesSecurityContextWhenAccessTokenIsValid() throws Exception {
        UUID userId = UUID.randomUUID();
        String accessToken = jwtService.issueTokens(userId, "secured.user@example.com").accessToken();

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/secure/ping");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isInstanceOf(TokenPrincipal.class);
        TokenPrincipal principal = (TokenPrincipal) authentication.getPrincipal();
        assertThat(principal.userId()).isEqualTo(userId);
        assertThat(principal.email()).isEqualTo("secured.user@example.com");
    }

    @Test
    void doFilter_keepsRequestUnauthenticatedWhenAccessTokenIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/secure/ping");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void doFilter_doesNotOverrideExistingAuthentication() throws Exception {
        Authentication existingAuthentication = new UsernamePasswordAuthenticationToken("existing", null);
        SecurityContextHolder.getContext().setAuthentication(existingAuthentication);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/secure/ping");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existingAuthentication);
    }
}
