package iudx.apd.acl.server.authentication.handler.authentication;

import static iudx.apd.acl.server.common.ResponseUrn.INVALID_TOKEN_URN;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import iudx.apd.acl.server.authentication.AuthenticationService;
import iudx.apd.acl.server.authentication.model.JwtData;
import iudx.apd.acl.server.common.HttpStatusCode;
import iudx.apd.acl.server.common.RoutingContextHelper;
import iudx.apd.acl.server.validation.exceptions.DxRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** DX Authentication handler to authenticate token passed in HEADER */
public class AuthHandler implements Handler<RoutingContext> {
  private final Logger LOGGER = LogManager.getLogger(AuthHandler.class);
  AuthenticationService authenticator;

  public AuthHandler(AuthenticationService authenticationService) {
    authenticator = authenticationService;
  }

  @Override
  public void handle(RoutingContext context) {
    String token = RoutingContextHelper.getToken(context);
    LOGGER.debug("Info :{}", context.request().path());
    Future<JwtData> jwtDataFuture = authenticator.decodeToken(token);

    jwtDataFuture
        .onSuccess(
            jwtData -> {
              RoutingContextHelper.setJwtData(context, jwtData);
              context.next();
            })
        .onFailure(
            failure -> {
              LOGGER.error("Token decode failed: {}", failure.getMessage());
              context.fail(
                  new DxRuntimeException(
                      HttpStatusCode.getByValue(401).getValue(), INVALID_TOKEN_URN));
            });
  }
}
