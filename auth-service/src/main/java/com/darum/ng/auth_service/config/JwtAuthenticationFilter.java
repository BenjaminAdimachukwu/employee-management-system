package com.darum.ng.auth_service.config;

import com.darum.ng.auth_service.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;

        }

        try {
            final   String token = authorizationHeader.substring("Bearer ".length());
//final   String token = authorizationHeader.substring(7);
            final String username = jwtUtil.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if(jwtUtil.validateToken(token)) {
                   // String role = jwtUtil.extractClaim(token, claims ->  claims.get("role").toString());
                    String role = jwtUtil.extractClaim(token, claims ->  claims.get("role",  String.class));

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username, null, Collections.singleton(new SimpleGrantedAuthority(role)));

                            SecurityContextHolder.getContext().setAuthentication(authToken);

                }
            }
        } catch (Exception e) {
            logger.error("Error while processing jwt token", e);
        }
    }
}



