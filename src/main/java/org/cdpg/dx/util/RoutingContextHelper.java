package org.cdpg.dx.util;

import static org.cdpg.dx.util.Constants.*;

import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.common.models.JwtData;

import java.util.Optional;
import org.cdpg.dx.common.models.User;

public final class RoutingContextHelper {

  private static final Logger LOGGER = LogManager.getLogger(RoutingContextHelper.class);

  // Constants for RoutingContext Keys
  private static final String JWT_DATA = "jwtData";
  private static final String USER = "user";

  // Token Handling Constants
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String BEARER_LOWER = "bearer";

  private RoutingContextHelper() {
    // Private constructor to prevent instantiation
  }

  public static void setUser(RoutingContext routingContext, User user) {
    routingContext.put(USER, user);
  }

  public static Optional<User> getUser(RoutingContext routingContext) {
    return Optional.ofNullable(routingContext.get(USER));
  }


  public static Optional<String> getToken(RoutingContext routingContext) {
    return Optional.ofNullable(routingContext.request().headers().get(HEADER_BEARER_AUTHORIZATION))
            .filter(token -> token.contains(BEARER_PREFIX) || token.contains(BEARER_LOWER))
            .map(RoutingContextHelper::extractToken);
  }

  public static String getRequestPath(RoutingContext routingContext) {
    return routingContext.request().path();
  }

  public static void setJwtData(RoutingContext routingContext, JwtData jwtData) {
    routingContext.put(JWT_DATA, jwtData);
  }

  public static Optional<JwtData> getJwtData(RoutingContext routingContext) {
    return Optional.ofNullable(routingContext.get(JWT_DATA));
  }

  private static String extractToken(String token) {
    return token.split(" ")[1].trim();
  }
}