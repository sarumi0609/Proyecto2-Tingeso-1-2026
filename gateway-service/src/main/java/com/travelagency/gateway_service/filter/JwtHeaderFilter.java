package com.travelagency.gateway_service.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class JwtHeaderFilter implements Filter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String[] parts = token.split("\\.");
                if (parts.length >= 2) {
                    String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                    JsonNode claims = objectMapper.readTree(payload);

                    String userId = claims.has("sub") ? claims.get("sub").asText() : null;

                    String role = "USER";
                    JsonNode realmAccess = claims.get("realm_access");
                    if (realmAccess != null && realmAccess.has("roles")) {
                        for (JsonNode r : realmAccess.get("roles")) {
                            if ("ADMIN".equals(r.asText())) {
                                role = "ADMIN";
                                break;
                            }
                        }
                    }

                    final String finalUserId = userId;
                    final String finalRole = role;

                    chain.doFilter(new HttpServletRequestWrapper(httpRequest) {
                        @Override
                        public String getHeader(String name) {
                            if ("X-User-Id".equalsIgnoreCase(name)) return finalUserId;
                            if ("X-User-Role".equalsIgnoreCase(name)) return finalRole;
                            return super.getHeader(name);
                        }
                        @Override
                        public Enumeration<String> getHeaders(String name) {
                            if ("X-User-Id".equalsIgnoreCase(name) || "X-User-Role".equalsIgnoreCase(name))
                                return Collections.enumeration(Collections.singletonList(getHeader(name)));
                            return super.getHeaders(name);
                        }
                        @Override
                        public Enumeration<String> getHeaderNames() {
                            List<String> names = Collections.list(super.getHeaderNames());
                            names.add("X-User-Id");
                            names.add("X-User-Role");
                            return Collections.enumeration(names);
                        }
                    }, response);
                    return;
                }
            } catch (Exception ignored) {
                // Token inválido — continúa sin headers
            }
        }

        chain.doFilter(request, response);
    }
}