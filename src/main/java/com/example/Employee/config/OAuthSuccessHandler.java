package com.example.Employee.config;

import com.example.Employee.Entity.User;
import com.example.Employee.Repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = token.getPrincipal();

        assert oAuth2User != null;
        String email = oAuth2User.getAttribute("email");
        if(email == null){
            email = oAuth2User.getAttribute("login");
        }
        String name = oAuth2User.getAttribute("name");

        Optional<User> existingUser = userRepository.findByUsername(email);

        if (existingUser.isEmpty()) {
            User newUser = new User();
            newUser.setUsername(email);
            newUser.setPassword("");
            newUser.setRole("USER");

            userRepository.save(newUser);
        }

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                        .withUsername(Objects.requireNonNull(email))
                        .password("")
                        .authorities("ROLE_USER")
                        .build();

        String jwt = jwtUtil.generateToken(userDetails);
        response.setContentType("application/json");
        response.getWriter().write("{\"token\":\"" + jwt + "\"}");
    }
}