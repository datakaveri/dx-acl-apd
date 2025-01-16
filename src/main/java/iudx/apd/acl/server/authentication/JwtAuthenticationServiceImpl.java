package iudx.apd.acl.server.authentication;

import static iudx.apd.acl.server.apiserver.util.Constants.*;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import iudx.apd.acl.server.authentication.model.JwtData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JwtAuthenticationServiceImpl implements AuthenticationService {
  private static final Logger LOGGER = LogManager.getLogger(JwtAuthenticationServiceImpl.class);
  final JWTAuth jwtAuth;
  final String issuer;
  final String apdUrl;

  public JwtAuthenticationServiceImpl(final JWTAuth jwtAuth, final JsonObject config) {
    this.jwtAuth = jwtAuth;
    this.issuer = config.getString("issuer");
    this.apdUrl = config.getString("apdURL");
  }

  @Override
  public Future<JwtData> tokenIntrospect(JsonObject authenticationInfo) {
    Promise<JwtData> promise = Promise.promise();
    String token = authenticationInfo.getString(HEADER_TOKEN);
    ResultContainer resultContainer = new ResultContainer();
    Future<JwtData> jwtDecodeFuture = decodeJwt(token);

    Future<Boolean> validateJwtAccessFuture =
        jwtDecodeFuture.compose(
            decodeHandler -> {
              resultContainer.jwtData = decodeHandler;
              return validateJwtAccess(resultContainer.jwtData);
            });
    validateJwtAccessFuture
        .onSuccess(
            isValidJwt -> {
              promise.complete(resultContainer.jwtData);
            })
        .onFailure(
            failureHandler -> {
              LOGGER.error("error : {}", failureHandler.getMessage());
              promise.fail(failureHandler.getLocalizedMessage());
            });

    return promise.future();
  }

  @Override
  public Future<Void> tokenIntrospectForVerify(JsonObject authenticationInfo) {
    Promise<Void> promise = Promise.promise();
    String token = authenticationInfo.getString(HEADER_TOKEN);

    Future<JwtData> jwtDecodeFuture = decodeJwt(token);
    jwtDecodeFuture
        .onSuccess(
            jwtData -> {
              if (jwtData.getSub() == null) {
                LOGGER.error("No sub value in JWT");
                promise.fail("No sub value in JWT");
              } else if (!(jwtData.getIss() != null && issuer.equalsIgnoreCase(jwtData.getIss()))) {
                LOGGER.error("Incorrect issuer value in JWT");
                promise.fail("Incorrect issuer value in JWT");
              } else if (jwtData.getAud().isEmpty()) {
                LOGGER.error("No audience value in JWT");
                promise.fail("No audience value in JWT");
              } else if (!jwtData.getAud().equalsIgnoreCase(apdUrl)) {
                LOGGER.error("Incorrect audience value in JWT");
                promise.fail("Incorrect audience value in JWT");
              } else if (!jwtData.getSub().equalsIgnoreCase(jwtData.getIss())) {
                LOGGER.error("Incorrect subject value in JWT");
                promise.fail("Incorrect subject value in JWT");
              } else {
                LOGGER.info("Auth token verified.");
                promise.complete();
              }
            })
        .onFailure(
            failureHandler -> {
              LOGGER.error("FAILED to decode the token.");
              promise.fail(failureHandler.getLocalizedMessage());
            });

    return promise.future();
  }

  public Future<Boolean> validateJwtAccess(JwtData jwtData) {
    Promise<Boolean> promise = Promise.promise();
    if (!(jwtData.getIss() != null && issuer.equalsIgnoreCase(jwtData.getIss()))) {
      LOGGER.error("Incorrect issuer value in JWT");
      promise.fail("Incorrect issuer value in JWT");
    } else if (jwtData.getAud() == null) {
      LOGGER.error("No audience value in JWT");
      promise.fail("No audience value in JWT");
    } else if (!jwtData.getAud().equalsIgnoreCase(jwtData.getIid().split(":")[1])) {
      LOGGER.error("Incorrect audience value in JWT");
      promise.fail("Incorrect audience value in JWT");
    } else {
      promise.complete(true);
    }
    return promise.future();
  }

  public Future<JwtData> decodeJwt(String jwtToken) {
    Promise<JwtData> promise = Promise.promise();
    TokenCredentials creds = new TokenCredentials(jwtToken);

    jwtAuth
        .authenticate(creds)
        .onSuccess(
            user -> {
              JwtData jwtData = new JwtData(user.principal());
              jwtData.setExp(user.get("exp"));
              jwtData.setIat(user.get("iat"));
              promise.complete(jwtData);
            })
        .onFailure(
            err -> {
              LOGGER.error("failed to decode/validate jwt token : " + err.getMessage());
              promise.fail(err.getMessage());
            });

    return promise.future();
  }

  final class ResultContainer {
    JwtData jwtData;
  }
}
