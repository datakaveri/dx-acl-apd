package org.cdpg.dx.util;


import static org.cdpg.dx.common.models.ResponseUrn.INVALID_TOKEN_URN;
import static org.cdpg.dx.util.Constants.*;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import org.cdpg.dx.common.exception.DxRuntimeException;
import org.cdpg.dx.common.models.HttpStatusCode;
import org.cdpg.dx.common.models.JwtData;
import org.cdpg.dx.common.models.User;

public class RoutingContextHelper {
  private static final Logger LOGGER = LogManager.getLogger(iudx.apd.acl.server.common.RoutingContextHelper.class);
  private static final String JWT_DATA = "jwtData";

  public static void setUser(RoutingContext routingContext, iudx.apd.acl.server.apiserver.util.User user) {
    routingContext.put(USER, user);
  }

  public static User getUser(RoutingContext routingContext) {
    return routingContext.get(USER);
  }

  public static JsonObject getAuthInfo(RoutingContext routingContext) {
    return new JsonObject()
        .put(API_ENDPOINT, getRequestPath(routingContext))
        .put(HEADER_TOKEN, getToken(routingContext))
        .put(API_METHOD, getMethod(routingContext));
  }

  public static String getToken(RoutingContext routingContext) {
    /* token would can be of the type : Bearer <JWT-Token>, <JWT-Token> */
    /* Send Bearer <JWT-Token> if Authorization header is present */
    /* allowing both the tokens to be authenticated for now */
    /* TODO: later, 401 error is thrown if the token does not contain Bearer keyword */
    String token = routingContext.request().headers().get(HEADER_BEARER_AUTHORIZATION);
    boolean isValidBearerToken = token != null && token.trim().split(" ").length == 2;
    boolean isBearerAuthHeaderPresent = isValidBearerToken && (token.contains(HEADER_TOKEN_BEARER));
    boolean isKcTokenPresent = isValidBearerToken && (token.contains("bearer"));
    String[] tokenWithoutBearer = new String[] {};
    if (isValidBearerToken) {
      if (isBearerAuthHeaderPresent) {
        tokenWithoutBearer = (token.split(HEADER_TOKEN_BEARER));
      } else if (isKcTokenPresent) {
        tokenWithoutBearer = (token.split("bearer"));
      }
      token = tokenWithoutBearer[1].replaceAll("\\s", "");
      return token;
    }
    return routingContext.request().headers().get(HEADER_TOKEN);
  }
  public static JsonObject getVerifyAuthInfo(RoutingContext routingContext) {
    return new JsonObject()
        .put(API_ENDPOINT, getRequestPath(routingContext))
        .put(HEADER_TOKEN, getVerifyToken(routingContext))
        .put(API_METHOD, getMethod(routingContext));
  }

  private static String getVerifyToken(RoutingContext routingContext) {
    String token = routingContext.request().headers().get(AUTHORIZATION_KEY);
    if (token.trim().split(" ").length == 2) {
      token = token.trim().split(" ")[1];
      return token;
    } else {
      throw new DxRuntimeException(HttpStatusCode.getByValue(401).getValue(), INVALID_TOKEN_URN);
    }
  }

  public static String getMethod(RoutingContext routingContext) {
    return routingContext.request().method().toString();
  }

  public static String getRequestPath(RoutingContext routingContext) {
    return routingContext.request().path();
  }

  public static void setJwtData(RoutingContext routingContext, JwtData jwtData) {
    routingContext.put(JWT_DATA, jwtData);
  }

  public static JwtData getJwtData(RoutingContext routingContext) {
    return routingContext.get(JWT_DATA);
  }
}
