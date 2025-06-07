package com.portfoliogenerator.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitingFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

	private final Bandwidth uploadLimit = Bandwidth.classic(3, Refill.greedy(3, Duration.ofMinutes(1)));
	private final Bandwidth generalApiLimit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));

	private final Map<String, Bucket> uploadCache = new ConcurrentHashMap<>();
	private final Map<String, Bucket> generalApiCache = new ConcurrentHashMap<>();


	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("RateLimitingFilter initialized.");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String clientIp = getClientIp(httpRequest);
		String path = httpRequest.getRequestURI();

		Bucket bucket;
		String limitType;

		if(path.equals("/api/v1/resume/upload")){
			bucket = uploadCache.computeIfAbsent(clientIp, k -> Bucket.builder().addLimit(uploadLimit).build());
			limitType = "UPLOAD";
		}else if(path.startsWith("/api/")){
			bucket = generalApiCache.computeIfAbsent(clientIp, k -> Bucket.builder().addLimit(generalApiLimit).build());
			limitType = "GENERAL_API";
		}else{
			chain.doFilter(request, response);
			return;
		}

		ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
		if (probe.isConsumed()){
			httpResponse.addHeader("X-Rate-Limit-Remaining-" + limitType, String.valueOf(probe.getRemainingTokens()));
			chain.doFilter(request, response);
		}else{
			long waitForRefillNanos = probe.getNanosToWaitForRefill();
			long waitForRefillMillis = waitForRefillNanos / 1_000_000;
			httpResponse.addHeader("X-Rate-Limit-Retry-After-Milliseconds-" + limitType, String.valueOf(waitForRefillMillis));

			logger.warn("Rate limit exceeded for IP: {}, Path: {}, LimitType: {}. Wait for {} ms.",
					clientIp, path, limitType, waitForRefillMillis);

			httpResponse.setStatus(429);
			httpResponse.setContentType("application/json");
			httpResponse.getWriter().write("{\"error\": \"Too Many Requests\", " +
					"\"message\": \"You have exceeded your request rate limit. Please try again later.\"}");
		}
	}

	private String getClientIp(HttpServletRequest httpRequest) {
		String remoteAddress = "";

		if (httpRequest != null){
			remoteAddress = httpRequest.getHeader("X-FORWARDED-FOR");

			if(remoteAddress == null || remoteAddress.isEmpty()){
				remoteAddress = httpRequest.getRemoteAddr();
			}else {
				remoteAddress = remoteAddress.split(",")[0].trim();
			}
		}

		return remoteAddress;
	}

	@Override
	public void destroy() {
		logger.info("RateLimitingFilter destroyed.");
	}
}
