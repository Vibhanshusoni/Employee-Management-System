package com.example.Employee.Security;

import com.example.Employee.Entity.User;
import com.example.Employee.Exceptions.EmployeeNotFoundException;
import com.example.Employee.Repository.UserRepository;
import com.example.Employee.Service.TokenBlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklist;
    private final UserRepository userRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
//To skip the Validation filter
        if (path.startsWith("/api/login") ||
                path.startsWith("/api/refresh") ||
                path.startsWith("/oauth2") ||
                path.startsWith("/login/oauth2")) {

            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            if (tokenBlacklist.isBlacklisted(token)) {
                logger.warn("Blacklisted token used");
                sendUnauthorized(response, "Token revoked");
                return;
            }

            String username = jwtUtil.extractUsername(token);

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new EmployeeNotFoundException("User not found"));

                if (!user.isEnabled() || user.isBlocked()) {
                    logger.warn("Blocked/disabled user attempted access: {}", username);
                    sendUnauthorized(response, "Account is blocked or disabled");
                    return;
                }

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(token, userDetails)) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.trace("JWT authentication successful for user: {}", username);

                } else {
                    sendUnauthorized(response, "Invalid JWT token");
                    return;
                }
            }

        } catch (ExpiredJwtException ex) {
            logger.error("JWT token expired: {}", ex.getMessage());
            sendUnauthorized(response, "Token expired");
            return;

        } catch (Exception ex) {
            logger.error("JWT validation failed", ex);
            sendUnauthorized(response, "Invalid JWT token");
            return;
        }

        filterChain.doFilter(request, response);
    }


    private void sendUnauthorized(HttpServletResponse response, String message)
            throws IOException {
        logger.trace("Authorization failed and going to print unauthorized exception or User already logged out ");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"error\":\"" + message + "\"}"
        );
    }
}