package org.cdpg.dx.auth.authentication.service;

import io.vertx.core.Future;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.common.models.JwtData;

import java.util.List;

public class JWTServiceImpl implements JWTService {
    private static final Logger LOGGER = LogManager.getLogger(JWTServiceImpl.class);
    private final JWTAuth jwtAuth;

    public JWTServiceImpl(final JsonObject jwks) {
        JWTAuthOptions jwtAuthOptions = new JWTAuthOptions().setJwks(List.of(jwks));
        this.jwtAuth = JWTAuth.create(io.vertx.core.Vertx.currentContext().owner(), jwtAuthOptions);
    }

    @Override
    public Future<JwtData> decodeToken(String token) {
        return jwtAuth.authenticate(new TokenCredentials(token))
                .map(user -> {
                    JsonObject principal = user.principal();
                    //TODO: make sure JWT structure is defined as in the JwtData
                    return new JwtData(principal);
                })
                .onFailure(err -> LOGGER.error("Failed to decode/validate JWT token: {}", err.getMessage()));
    }
}
