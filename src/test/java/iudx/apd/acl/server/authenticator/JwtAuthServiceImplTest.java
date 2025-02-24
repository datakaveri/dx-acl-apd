package iudx.apd.acl.server.authenticator;

import static iudx.apd.acl.server.apiserver.util.Constants.API_ENDPOINT;
import static iudx.apd.acl.server.apiserver.util.Constants.API_METHOD;
import static iudx.apd.acl.server.apiserver.util.Constants.HEADER_TOKEN;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micrometer.core.ipc.http.HttpSender.Method;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import iudx.apd.acl.server.apiserver.util.Role;
import iudx.apd.acl.server.authentication.JwtAuthenticationServiceImpl;
import iudx.apd.acl.server.common.Api;
import iudx.apd.acl.server.common.RoutingContextHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * TODO: Add all the failing and succeeding tests for user access wrt to an API in
 * TestApiServerVerticle as ApiServerVerticle is defining the user access list for a given API using
 * the router operation handler
 */
@Disabled
@ExtendWith({VertxExtension.class, MockitoExtension.class})
public class JwtAuthServiceImplTest {

  private static final Logger LOGGER = LogManager.getLogger(JwtAuthServiceImplTest.class);
  private static JsonObject authConfig;
  private static JwtAuthenticationServiceImpl jwtAuthenticationService;
  private static Api apis;

  @BeforeAll
  @DisplayName("Initialize Vertx and deploy Auth Verticle")
  static void init(Vertx vertx, VertxTestContext testContext) {
    authConfig = new JsonObject();
    authConfig.put("issuer", "authvertx.iudx.io");
    authConfig.put("apdURL", "acl-apd.iudx.io");
    apis = Api.getInstance("/ngsi-ld/v1");
    JWTAuthOptions jwtAuthOptions = new JWTAuthOptions();
    jwtAuthOptions.addPubSecKey(
        new PubSecKeyOptions()
            .setAlgorithm("ES256")
            .setBuffer(
                "-----BEGIN CERTIFICATE-----\n"
                    + "MIIBnDCCAT+gAwIBAgIEEC1BXTAMBggqhkjOPQQDAgUAMEIxCTAHBgNVBAYTADEJMAcGA1UECBMAMQkwBwYDVQQHEwAxCTAHBgNVBAoTADEJMAcGA1UECxMAMQkwBwYDVQQDEwAwHhcNMjMwNjA1MDUwODQ4WhcNMjQwNjA0MDUwODQ4WjBCMQkwBwYDVQQGEwAxCTAHBgNVBAgTADEJMAcGA1UEBxMAMQkwBwYDVQQKEwAxCTAHBgNVBAsTADEJMAcGA1UEAxMAMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAErVwOLOln7NhUdfZOQFfTOtJ62AgdKUeYZm8DgWCWJHaaXT95dipr94zJM+inSuqohVFCTxjajdTH8/O9JO43rKMhMB8wHQYDVR0OBBYEFNH2u8eeqj3509HAFJQS4F5NF4TQMAwGCCqGSM49BAMCBQADSQAwRgIhAL7zHYdN6PFTccFm1y07X0t2mJxNfgOaxihTi2tA9D8AAiEAomGmBvXA72X1gfhK3dhaDSd52BN1fUP/ALYNiyuXHg0=\n"
                    + "-----END CERTIFICATE-----"));
    jwtAuthOptions.getJWTOptions().setIgnoreExpiration(true);

    JWTAuth jwtAuth = JWTAuth.create(vertx, jwtAuthOptions);

    RoutingContextHelper routingContextHelper = new RoutingContextHelper();
    jwtAuthenticationService = new JwtAuthenticationServiceImpl(jwtAuth);

    LOGGER.info("Auth tests setup complete");

    testContext.completeNow();
  }

  @Test
  @DisplayName("success - allow access for identity -> consumer")
  public void allow4ConsumerIdentityToken(VertxTestContext testContext) {
    authConfig.put(API_METHOD, Method.GET);
    authConfig.put(API_ENDPOINT, apis.getPoliciesUrl());
    authConfig.put(HEADER_TOKEN, JwtTokenHelper.consumerToken);
    jwtAuthenticationService
        .decodeToken(JwtTokenHelper.consumerToken)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                testContext.completeNow();
              } else {
                testContext.failNow("invalid access");
              }
            });
  }

  @Test
  @DisplayName("success - allow access for identity -> consumer delegate")
  public void allow4ConsumerDelegateIdentityToken(VertxTestContext testContext) {
    authConfig.put(API_METHOD, Method.GET);
    authConfig.put(API_ENDPOINT, apis.getPoliciesUrl());
    authConfig.put(HEADER_TOKEN, JwtTokenHelper.consumerDelegateToken);
    jwtAuthenticationService
        .decodeToken(JwtTokenHelper.consumerDelegateToken)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                JsonObject result = handler.result().toJson();
                assertEquals(Role.CONSUMER.getRole(), result.getString("role").toLowerCase());
                assertEquals(true, result.getBoolean("isDelegate"));
                testContext.completeNow();
              } else {
                testContext.failNow("invalid access");
              }
            });
  }

  @Test
  @DisplayName("success - allow access for identity -> provider")
  public void allow4ProviderIdentityToken(VertxTestContext testContext) {
    authConfig.put(API_METHOD, Method.GET);
    authConfig.put(API_ENDPOINT, apis.getPoliciesUrl());
    authConfig.put(HEADER_TOKEN, JwtTokenHelper.providerToken);
    jwtAuthenticationService
        .decodeToken(JwtTokenHelper.providerToken)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                testContext.completeNow();
              } else {
                testContext.failNow("invalid access");
              }
            });
  }

  @Test
  @DisplayName("success - allow access for identity -> provider delegate")
  public void allow4ProviderDelegateIdentityToken(VertxTestContext testContext) {
    authConfig.put(API_METHOD, Method.GET);
    authConfig.put(API_ENDPOINT, apis.getRequestPoliciesUrl());
    authConfig.put(HEADER_TOKEN, JwtTokenHelper.providerDelegateToken);
    jwtAuthenticationService
        .decodeToken(JwtTokenHelper.providerDelegateToken)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                JsonObject result = handler.result().toJson();
                assertEquals(Role.PROVIDER.getRole(), result.getString("drl").toLowerCase());
                assertEquals("delegate", result.getString("role"));
                testContext.completeNow();
              } else {
                testContext.failNow("invalid access");
              }
            });
  }

  /*Add this test from ApiServerVerticle as the user access list wrt to an API is provider with router handler */
  @Test
  @DisplayName("fail - not access to consumer for POST policy")
  public void invalidRequestOfConsumerPostPolicy(VertxTestContext testContext) {
    authConfig.put(API_METHOD, Method.POST);
    authConfig.put(API_ENDPOINT, apis.getPoliciesUrl());
    authConfig.put(HEADER_TOKEN, JwtTokenHelper.consumerDelegateToken);
    jwtAuthenticationService
        .decodeToken(JwtTokenHelper.consumerDelegateToken)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                testContext.failNow("invalid access");
                testContext.completeNow();
              } else {
                testContext.completeNow();
              }
            });
  }

  /*Add this test from ApiServerVerticle as the user access list wrt to an API is provider with router handler */
  @Test
  @DisplayName("fail - not access to consumer for DELETE policy")
  public void invalidRequestOfConsumerDeletePolicy(VertxTestContext testContext) {
    authConfig.put(API_METHOD, Method.DELETE);
    authConfig.put(API_ENDPOINT, apis.getPoliciesUrl());
    authConfig.put(HEADER_TOKEN, JwtTokenHelper.consumerDelegateToken);
    jwtAuthenticationService
        .decodeToken(JwtTokenHelper.consumerDelegateToken)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                testContext.failNow("invalid access");
                testContext.completeNow();
              } else {
                testContext.completeNow();
              }
            });
  }

  /*Add this test from ApiServerVerticle as the user access list wrt to an API is provider with router handler */
  @Test
  @DisplayName("fail - not access to provider for POST notification")
  public void invalidRequestOfProviderPostNotification(VertxTestContext testContext) {
    authConfig.put(API_METHOD, Method.POST);
    authConfig.put(API_ENDPOINT, apis.getRequestPoliciesUrl());
    authConfig.put(HEADER_TOKEN, JwtTokenHelper.providerToken);
    jwtAuthenticationService
        .decodeToken(JwtTokenHelper.providerToken)
        .onComplete(
            handler -> {
              LOGGER.info("result is : " + handler);
              if (handler.succeeded()) {
                testContext.failNow("invalid access");
              } else {
                testContext.completeNow();
              }
            });
  }

  /*Add this test from ApiServerVerticle as the user access list wrt to an API is provider with router handler */
  @Test
  @DisplayName("fail - not access to provider for DELETE notification")
  public void invalidRequestOfProviderDeleteNotification(VertxTestContext testContext) {
    authConfig.put(API_METHOD, Method.DELETE);
    authConfig.put(API_ENDPOINT, apis.getRequestPoliciesUrl());
    authConfig.put(HEADER_TOKEN, JwtTokenHelper.providerToken);
    jwtAuthenticationService
        .decodeToken(JwtTokenHelper.providerToken)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                testContext.failNow("invalid access");
              } else {
                testContext.completeNow();
              }
            });
  }

  @Test
  @DisplayName("decode invalid jwt")
  public void decodeJwtFailure(VertxTestContext testContext) {
    jwtAuthenticationService
        .decodeToken(JwtTokenHelper.invalidToken)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                testContext.failNow(handler.cause());
              } else {
                testContext.completeNow();
              }
            });
  }
}
