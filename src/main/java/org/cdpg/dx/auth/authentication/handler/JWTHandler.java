package org.cdpg.dx.auth.authentication.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.cdpg.dx.auth.authentication.service.JWTService;
import org.cdpg.dx.util.RoutingContextHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auth.authentication.exception.AuthenticationException;

/** DX Authentication handler to authenticate token passed in HEADER */
public class JWTHandler implements Handler<RoutingContext> {
    private static final Logger LOGGER = LogManager.getLogger(JWTHandler.class);
    private final JWTService jwtService;

    public JWTHandler(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void handle(RoutingContext context) {
        LOGGER.debug("Processing request: {}", context.request().path());

        RoutingContextHelper.getToken(context)
                .filter(token -> !token.isEmpty())
                .ifPresentOrElse(
                        token -> validateToken(context, token),
                        () -> handleInvalidToken(context, "Invalid or missing token")
                );
    }

    private void validateToken(RoutingContext context, String token) {
        jwtService.decodeToken(token)
                .onSuccess(jwtData -> {
                    RoutingContextHelper.setJwtData(context, jwtData);
                    LOGGER.info("Token successfully validated for request: {}", context.request().path());
                    context.next();
                })
                .onFailure(failure -> {
                    LOGGER.error("Token decoding failed: {}", failure.getMessage());
                    handleInvalidToken(context, "Token validation failed");
                });
    }

    private void handleInvalidToken(RoutingContext context, String message) {
        LOGGER.error(message);
        context.fail(new AuthenticationException(message));
    }
}
