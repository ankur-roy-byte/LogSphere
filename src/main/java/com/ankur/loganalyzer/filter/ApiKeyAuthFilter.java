package com.ankur.loganalyzer.filter;

import com.ankur.loganalyzer.model.ApiKey;
import com.ankur.loganalyzer.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    static final String HEADER = "X-Api-Key";

    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String rawKey = request.getHeader(HEADER);
        if (rawKey != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<ApiKey> keyOpt = apiKeyService.validate(rawKey);
            if (keyOpt.isPresent()) {
                ApiKey key = keyOpt.get();
                apiKeyService.recordUsage(key);
                var auth = new UsernamePasswordAuthenticationToken(
                        "apikey:" + key.getName(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ANALYST"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}
