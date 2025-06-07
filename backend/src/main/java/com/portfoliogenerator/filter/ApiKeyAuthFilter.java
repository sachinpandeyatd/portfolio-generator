package com.portfoliogenerator.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiKeyAuthFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthFilter.class);

	@Value("${app.security.api-key}")
	private String appSecurityApiKey;
	private static final String API_KEY_HEADER_NAME = "X-API-Key";

	private static final Set<String> UNPROTECTED_PATHS = new HashSet<>(Arrays.asList("/", "/api/v1/portfolios/"));

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if (appSecurityApiKey == null || appSecurityApiKey.isEmpty()){
			logger.warn("App security api key is not configured properly.");
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String path = httpRequest.getRequestURI();
		String method = httpRequest.getMethod();

		if ("OPTIONS".equalsIgnoreCase(method)) {
			chain.doFilter(request, response);
			return;
		}

		for (String prefix : UNPROTECTED_PATHS) {
			if (path.startsWith(prefix) && path.endsWith("/download")) {
				chain.doFilter(request, response);
				return;
			}
		}

		// for the portfolio view paths
		if(path.matches("/[a-zA-Z0-9]{5}$")){
			chain.doFilter(request, response);
			return;
		}

		String providedApiKey = httpRequest.getHeader(API_KEY_HEADER_NAME);

		if (appSecurityApiKey != null && appSecurityApiKey.equals(providedApiKey)){
			chain.doFilter(request, response);
		}else {
			logger.error("Forbidden: Missing or invalid API Key. URI: {}, Method: {}, Received Key: '{}'",
					path, method, providedApiKey != null ? providedApiKey : "null");

			httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
			httpResponse.setContentType("application/json");
			httpResponse.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Valid API Key required.\"}");
		}
	}

	@Override
	public void destroy() {
		logger.info("ApiKeyAuthFilter destroyed.");
	}
}
