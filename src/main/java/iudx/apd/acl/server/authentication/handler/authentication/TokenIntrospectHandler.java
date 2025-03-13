package iudx.apd.acl.server.authentication.handler.authentication;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import iudx.apd.acl.server.authentication.model.JwtData;
import iudx.apd.acl.server.common.HttpStatusCode;
import iudx.apd.acl.server.common.ResponseUrn;
import iudx.apd.acl.server.common.RoutingContextHelper;
import iudx.apd.acl.server.validation.exceptions.DxRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TokenIntrospectHandler implements Handler<RoutingContext> {
  private static final Logger LOGGER = LogManager.getLogger(TokenIntrospectHandler.class);

  @Override
  public void handle(RoutingContext event) {
    JwtData jwtData = RoutingContextHelper.getJwtData(event);
    if (jwtData.getSub() == null) {
      processAuthFailure(event, "No sub value in JWT");
    } else if (jwtData.getAud() == null || jwtData.getAud().isEmpty()) {
      processAuthFailure(event, "No audience value in JWT");
    }
    event.next();
  }

  private void processAuthFailure(RoutingContext event, String failureMessage) {
    LOGGER.error("TokenIntrospect failure : {}", failureMessage);
    event.fail(
        new DxRuntimeException(
            HttpStatusCode.UNAUTHORIZED.getValue(), ResponseUrn.INVALID_TOKEN_URN, failureMessage));
  }

  public Handler<RoutingContext> validateKeycloakToken(String rsUrl) {
    return context -> validateKeycloakToken(context, rsUrl);
  }

  public Handler<RoutingContext> validateToken() {
    return this::validateToken;
  }

  public Handler<RoutingContext> validateTokenForRs(String audience) {
    return context -> validateTokenForRs(context, audience);
  }

  /**
   * Validates if the keycloak token generated by DX Auth Server containing bearer
   *
   * @param event Routing context
   * @param url DX Resource Server URL, DX APD URL
   */
  private void validateKeycloakToken(RoutingContext event, String url) {
    JwtData jwtData = RoutingContextHelper.getJwtData(event);
    LOGGER.info("Verifying keycloak token");
    if (!jwtData.getAud().equalsIgnoreCase(url)) {
      processAuthFailure(event, "Incorrect audience value in JWT");
    } else if (!jwtData.getSub().equalsIgnoreCase(jwtData.getIss())) {
      processAuthFailure(event, "Incorrect subject value in JWT");
    }
    event.next();
  }

  private void validateToken(RoutingContext event) {
    JwtData jwtData = RoutingContextHelper.getJwtData(event);
    LOGGER.info("Verifying auth token");
    if (!jwtData.getAud().equalsIgnoreCase(jwtData.getIid().split(":")[1])) {
      processAuthFailure(event, "Incorrect audience value in JWT");
    }
    event.next();
  }

  private void validateTokenForRs(RoutingContext event, String audience) {
    String audienceInJwt = RoutingContextHelper.getJwtData(event).getAud();
    if (!audienceInJwt.equalsIgnoreCase(audience)) {
      processAuthFailure(event, "Incorrect audience value in jwt");
    }
    event.next();
  }
}
