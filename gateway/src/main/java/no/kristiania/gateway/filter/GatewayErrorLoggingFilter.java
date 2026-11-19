package no.kristiania.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class GatewayErrorLoggingFilter implements WebFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(GatewayErrorLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange).doOnError(ex -> logError(exchange, ex));
    }

    private void logError(ServerWebExchange exchange, Throwable ex) {
        int status = ex instanceof ResponseStatusException rse ? rse.getStatusCode().value() : 500;
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String routeId = route != null ? route.getId() : "none";
        ServerHttpRequest request = exchange.getRequest();
        log.warn("Gateway error status={} routeId={} method={} path={} type={}",
                status, routeId, request.getMethod(), request.getPath().value(),
                ex.getClass().getSimpleName());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
