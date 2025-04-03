package iudx.apd.acl.server.apiserver;

import static iudx.apd.acl.server.apiserver.response.ResponseUtil.generateResponse;
import static iudx.apd.acl.server.apiserver.util.Constants.*;
import static iudx.apd.acl.server.apiserver.util.Util.errorResponse;
import static iudx.apd.acl.server.auditing.util.Constants.USERID;
import static iudx.apd.acl.server.common.Constants.*;
import static iudx.apd.acl.server.common.HttpStatusCode.BAD_REQUEST;
import static org.cdpg.dx.acl.policy.common.Constants.PG_SERVICE_ADDRESS;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import iudx.apd.acl.server.aaaService.AuthClient;
import iudx.apd.acl.server.apiserver.util.User;
import iudx.apd.acl.server.auditing.AuditingService;
import iudx.apd.acl.server.authentication.AuthenticationService;
import iudx.apd.acl.server.authentication.handler.Authentication;
import iudx.apd.acl.server.authentication.handler.authentication.AuthHandler;
import iudx.apd.acl.server.authentication.handler.authentication.TokenIntrospectHandler;
import iudx.apd.acl.server.authentication.handler.authorization.AuthorizationHandler;
import iudx.apd.acl.server.authentication.handler.authorization.UserAccessHandler;
import iudx.apd.acl.server.authentication.model.DxRole;
import iudx.apd.acl.server.authentication.model.UserInfo;
import iudx.apd.acl.server.common.Api;
import iudx.apd.acl.server.common.HttpStatusCode;
import iudx.apd.acl.server.common.ResponseUrn;
import iudx.apd.acl.server.common.RoutingContextHelper;
import iudx.apd.acl.server.notification.NotificationService;
import iudx.apd.acl.server.policy.PolicyService;
import iudx.apd.acl.server.validation.FailureHandler;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.acl.policy.repository.PolicyDAO;
import org.cdpg.dx.acl.policy.repository.PolicyDAOImpl;
import org.cdpg.dx.database.postgres.service.PostgresService;

/**
 * The ACL-APD Server API Verticle.
 *
 * <h1>ACL-APD Server API Verticle</h1>
 *
 * <p>The API Server verticle implements the IUDX ACL-APD Server APIs. It handles the API requests
 * from the clients and interacts with the associated Service to respond.
 *
 * @version 1.0
 * @see io.vertx.core.Vertx
 * @see AbstractVerticle
 * @see HttpServer
 * @see Router
 * @see io.vertx.servicediscovery.ServiceDiscovery
 * @see io.vertx.servicediscovery.types.EventBusService
 * @see io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
 * @since 2020-05-31
 */
public class ApiServerVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LogManager.getLogger(ApiServerVerticle.class);
  Api api;
  private HttpServer server;
  private Router router;
  private int port;
  private String dxApiBasePath;
  private PolicyService policyService;
  private PolicyDAO policyDAO;
  private String detail;
  private NotificationService notificationService;
  private AuditingService auditingService;
  private AuthenticationService authenticator;
  private AuthClient authClient;
  private PostgresService pgService;
  private WebClient webClient;
  private WebClientOptions webClientOptions;
  private AuthHandler authHandler;
  private UserAccessHandler userAccessHandler;
  private Handler<RoutingContext> providerApiAccessHandler;
  private Handler<RoutingContext> consumerApiAccessHandler;
  private Handler<RoutingContext> apiAccessHandler;
  private UserInfo userInfo;

  /**
   * This method is used to start the Verticle. It deploys a verticle in a cluster, reads the
   * configuration, obtains a proxy for the Event bus services exposed through service discovery,
   * start an HTTPs server at port 8443 or an HTTP server at port 8080.
   *
   * @throws Exception which is a startup exception
   */
  @Override
  public void start() throws Exception {

    /* Define the APIs, methods, endpoints and associated methods. */
    dxApiBasePath = config().getString("dxApiBasePath");
    this.api = Api.getInstance(dxApiBasePath);
    webClientOptions = new WebClientOptions();
    webClientOptions.setTrustAll(false).setVerifyHost(true).setSsl(true);
    webClient = WebClient.create(vertx, webClientOptions);

    /* Initialize service proxy */
    pgService =  PostgresService.createProxy(vertx, PG_SERVICE_ADDRESS);
    policyService = PolicyService.createProxy(vertx, POLICY_SERVICE_ADDRESS);
//    policyDAO = PolicyDAO.createProxy(vertx, POLICY_DAO_ADDRESS);

    notificationService = NotificationService.createProxy(vertx, NOTIFICATION_SERVICE_ADDRESS);
    auditingService = AuditingService.createProxy(vertx, AUDITING_SERVICE_ADDRESS);
    authenticator = AuthenticationService.createProxy(vertx, AUTH_SERVICE_ADDRESS);
    authClient = new AuthClient(config(), webClient);
//    pgService = new PostgresService(config(), vertx);
    policyDAO = new PolicyDAOImpl(pgService);
    FailureHandler failureHandler = new FailureHandler();
    authHandler = new AuthHandler(authenticator);
    providerApiAccessHandler =
        new AuthorizationHandler().setUserRolesForEndpoint(DxRole.PROVIDER, DxRole.DELEGATE);
    consumerApiAccessHandler =
        new AuthorizationHandler().setUserRolesForEndpoint(DxRole.CONSUMER, DxRole.DELEGATE);
    apiAccessHandler =
        new AuthorizationHandler()
            .setUserRolesForEndpoint(DxRole.CONSUMER, DxRole.PROVIDER, DxRole.DELEGATE);
    String apdUrl = config().getString("apdURL");
    Handler<RoutingContext> validateToken = new TokenIntrospectHandler().validateToken();
    Handler<RoutingContext> validateKeycloakToken =
        new TokenIntrospectHandler().validateKeycloakToken(apdUrl);

    userInfo = new UserInfo();

//    userAccessHandler = new UserAccessHandler(authClient, userInfo, pgService);

    /* Initialize Router builder */
    RouterBuilder.create(vertx, "docs/openapi.yaml")
        .onSuccess(
            routerBuilder -> {
              routerBuilder.securityHandler("authorization", new Authentication());

              routerBuilder
                  .operation(CREATE_POLICY_API)
                  .handler(authHandler)
                  .handler(validateToken)
                  .handler(providerApiAccessHandler)
                  .handler(userAccessHandler)
                  .handler(this::postPoliciesHandler)
                  .failureHandler(failureHandler);

              routerBuilder
                  .operation(GET_POLICY_API)
                  .handler(authHandler)
                  .handler(validateToken)
                  .handler(apiAccessHandler)
                  .handler(userAccessHandler)
                  .handler(this::getPoliciesHandler)
                  .failureHandler(failureHandler);

              routerBuilder
                  .operation(DELETE_POLICY_API)
//                  .handler(authHandler)
//                  .handler(validateAccessHandler.setUserRolesForEndpoint(PROVIDER, DELEGATE))
//                  .handler(userAccessHandler)
                  .handler(this::deletePoliciesHandler)
                  .failureHandler(failureHandler);

              routerBuilder
                  .operation(CREATE_NOTIFICATIONS_API)
                  .handler(authHandler)
                  .handler(validateToken)
                  .handler(consumerApiAccessHandler)
                  .handler(userAccessHandler)
                  .handler(this::postAccessRequestHandler)
                  .failureHandler(failureHandler);

              routerBuilder
                  .operation(UPDATE_NOTIFICATIONS_API)
                  .handler(authHandler)
                  .handler(validateToken)
                  .handler(providerApiAccessHandler)
                  .handler(userAccessHandler)
                  .handler(this::putAccessRequestHandler)
                  .failureHandler(failureHandler);

              routerBuilder
                  .operation(GET_NOTIFICATIONS_API)
                  .handler(authHandler)
                  .handler(validateToken)
                  .handler(apiAccessHandler)
                  .handler(userAccessHandler)
                  .handler(this::getAccessRequestHandler)
                  .failureHandler(failureHandler);

              routerBuilder
                  .operation(DELETE_NOTIFICATIONS_API)
                  .handler(authHandler)
                  .handler(validateToken)
                  .handler(consumerApiAccessHandler)
                  .handler(userAccessHandler)
                  .handler(this::deleteAccessRequestHandler)
                  .failureHandler(failureHandler);

              routerBuilder
                  .operation(VERIFY_API)
                  .handler(authHandler)
                  .handler(validateKeycloakToken)
                  .handler(userAccessHandler)
                  .handler(this::verifyRequestHandler)
                  .failureHandler(failureHandler);

              routerBuilder.rootHandler(TimeoutHandler.create(100000, 408));
              configureCorsHandler(routerBuilder);
              routerBuilder.rootHandler(BodyHandler.create().setHandleFileUploads(false));
              router = routerBuilder.createRouter();
              putCommonResponseHeaders();
              configureErrorHandlers(router);

              /* Documentation routes */
              /* Static Resource Handler */
              /* Get openapiv3 spec */
              router
                  .get(ROUTE_STATIC_SPEC)
                  .produces(APPLICATION_JSON)
                  .handler(
                      routingContext -> {
                        HttpServerResponse response = routingContext.response();
                        response.sendFile("docs/openapi.yaml");
                      });
              /* Get redoc */
              router
                  .get(ROUTE_DOC)
                  .produces(MIME_TEXT_HTML)
                  .handler(
                      routingContext -> {
                        HttpServerResponse response = routingContext.response();
                        response.sendFile("docs/apidoc.html");
                      });

              /* Read ssl configuration. */
              HttpServerOptions serverOptions = new HttpServerOptions();
              setServerOptions(serverOptions);
              serverOptions.setCompressionSupported(true).setCompressionLevel(5);
              server = vertx.createHttpServer(serverOptions);
              server.requestHandler(router).listen(port);

              printDeployedEndpoints(router);
              /* Print the deployed endpoints */
              LOGGER.info("API server deployed on: " + port);
            })
        .onFailure(
            failure -> {
              LOGGER.error(
                  "Failed to initialize router builder {}", failure.getCause().getMessage());
            });
  }

  private void verifyRequestHandler(RoutingContext routingContext) {
    JsonObject requestBody = routingContext.body().asJsonObject();
    HttpServerResponse response = routingContext.response();
    policyService
        .verifyPolicy(requestBody)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                LOGGER.info("Policy verified successfully ");
                handleSuccessResponse(
                    response, HttpStatusCode.SUCCESS.getValue(), handler.result().toString());
              } else {
                LOGGER.error("Policy could not be verified {}", handler.cause().getMessage());
                handleFailureResponse(routingContext, handler.cause().getMessage());
              }
            });
  }

  private void postAccessRequestHandler(RoutingContext routingContext) {
    JsonObject request = routingContext.body().asJsonObject();
    HttpServerResponse response = routingContext.response();
    User user = RoutingContextHelper.getUser(routingContext);
    notificationService
        .createNotification(request, user)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                LOGGER.info("Notification created successfully : {}", handler.result().encode());
                JsonObject responseJson =
                    new JsonObject()
                        .put(TYPE, handler.result().getString(TYPE))
                        .put(TITLE, handler.result().getString(TITLE))
                        .put(DETAIL, handler.result().getValue(DETAIL));
                handleSuccessResponse(
                    response, handler.result().getInteger(STATUS_CODE), responseJson.toString());
                Future.future(fu -> handleAuditLogs(routingContext));
              } else {
                LOGGER.error("Failed to create notification : {}", handler.cause().getMessage());
                handleFailureResponse(routingContext, handler.cause().getMessage());
              }
            });
  }

  private void printDeployedEndpoints(Router router) {
    for (Route route : router.getRoutes()) {
      if (route.getPath() != null) {
        LOGGER.debug("API Endpoints deployed : " + route.methods() + " : " + route.getPath());
      }
    }
  }

  private void putAccessRequestHandler(RoutingContext routingContext) {
    JsonObject notification = routingContext.body().asJsonObject();
    HttpServerResponse response = routingContext.response();
    User user = RoutingContextHelper.getUser(routingContext);
    notificationService
        .updateNotification(notification, user)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                LOGGER.info("Update Notification succeeded : {} ", handler.result().encode());
                JsonObject responseJson =
                    new JsonObject()
                        .put(TYPE, handler.result().getString(TYPE))
                        .put(TITLE, handler.result().getString(TITLE))
                        .put(DETAIL, handler.result().getValue(DETAIL));
                handleSuccessResponse(
                    response, handler.result().getInteger(STATUS_CODE), responseJson.toString());
                Future.future(fu -> handleAuditLogs(routingContext));
              } else {
                LOGGER.error("Update Notification failed : {} ", handler.cause().getMessage());
                handleFailureResponse(routingContext, handler.cause().getMessage());
              }
            });
  }

  private void deleteAccessRequestHandler(RoutingContext routingContext) {
    JsonObject notification = routingContext.body().asJsonObject();
    HttpServerResponse response = routingContext.response();
    User user = RoutingContextHelper.getUser(routingContext);
    notificationService
        .deleteNotification(notification, user)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                LOGGER.info("Delete Notification succeeded : {} ", handler.result().encode());
                JsonObject responseJson =
                    new JsonObject()
                        .put(TYPE, handler.result().getString(TYPE))
                        .put(TITLE, handler.result().getString(TITLE))
                        .put(DETAIL, handler.result().getValue(DETAIL));
                handleSuccessResponse(
                    response, handler.result().getInteger(STATUS_CODE), responseJson.toString());
                Future.future(fu -> handleAuditLogs(routingContext));
              } else {
                LOGGER.error("Delete Notification failed : {} ", handler.cause().getMessage());
                handleFailureResponse(routingContext, handler.cause().getMessage());
              }
            });
  }

  private void getAccessRequestHandler(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    User user = RoutingContextHelper.getUser(routingContext);
    notificationService
        .getNotification(user)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                String result = handler.result().getJsonObject(RESULT).encode();
                handleSuccessResponse(response, handler.result().getInteger(STATUS_CODE), result);
              } else {
                handleFailureResponse(routingContext, handler.cause().getMessage());
              }
            });
  }

  private void postPoliciesHandler(RoutingContext routingContext) {
    JsonObject requestBody = routingContext.body().asJsonObject();
    HttpServerResponse response = routingContext.response();
    User user = RoutingContextHelper.getUser(routingContext);
    policyService
        .createPolicy(requestBody, user)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                LOGGER.info("Policy created successfully ");
                handleSuccessResponse(
                    response, HttpStatusCode.SUCCESS.getValue(), handler.result().toString());
                Future.future(fu -> handleAuditLogs(routingContext));
              } else {
                LOGGER.error("Policy could not be created");
                handleFailureResponse(routingContext, handler.cause().getMessage());
              }
            });
  }

  private void deletePoliciesHandler(RoutingContext routingContext) {
    JsonObject policy = routingContext.body().asJsonObject();
    HttpServerResponse response = routingContext.response();
    String policyId = policy.getString("id");
    LOGGER.info("inside handler : hereeeee");
    LOGGER.info("what is policy Dao : {}", policyDAO);
//    User user = RoutingContextHelper.getUser(routingContext);
    policyDAO
        .delete(policyId)
        .onComplete(
            isDeleted -> {
              if (isDeleted.succeeded()) {
                LOGGER.info("Delete policy succeeded ");
                JsonObject responseJson =
                    new JsonObject()
                        .put(TYPE, ResponseUrn.SUCCESS_URN.getUrn())
                        .put(TITLE, ResponseUrn.SUCCESS_URN.getMessage())
                        .put(DETAIL, "Policy Deleted");
                handleSuccessResponse(
                    response, HttpStatusCode.SUCCESS.getValue(), responseJson.toString());
                Future.future(fu -> handleAuditLogs(routingContext));
              } else {
                LOGGER.error("Delete policy failed : {} ", isDeleted.cause().getMessage());
                handleFailureResponse(routingContext, isDeleted.cause().getMessage());
              }
            });
  }

  private void getPoliciesHandler(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();

    User user = RoutingContextHelper.getUser(routingContext);
    policyService
        .getPolicy(user)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                String result = handler.result().getJsonObject(RESULT).encode();
                handleSuccessResponse(response, handler.result().getInteger(STATUS_CODE), result);
              } else {
                handleFailureResponse(routingContext, handler.cause().getMessage());
              }
            });
  }

  /**
   * Configures the CORS handler on the provided router.
   *
   * @param routerBuilder The router builder instance to configure the CORS handler on.
   */
  private void configureCorsHandler(RouterBuilder routerBuilder) {
    routerBuilder.rootHandler(
        CorsHandler.create("*").allowedHeaders(ALLOWED_HEADERS).allowedMethods(ALLOWED_METHODS));
  }

  /**
   * Configures error handlers for the specified status codes on the provided router.
   *
   * @param router The router instance to configure the error handlers on.
   */
  private void configureErrorHandlers(Router router) {
    HttpStatusCode[] statusCodes = HttpStatusCode.values();
    Stream.of(statusCodes)
        .forEach(
            code -> {
              router.errorHandler(
                  code.getValue(),
                  errorHandler -> {
                    HttpServerResponse response = errorHandler.response();
                    if (response.headWritten()) {
                      try {
                        response.close();
                      } catch (RuntimeException e) {
                        LOGGER.error("Error: " + e);
                      }
                      return;
                    }
                    response
                        .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .setStatusCode(code.getValue())
                        .end(errorResponse(code));
                  });
            });
  }

  /** Sets common response headers to be included in HTTP responses. */
  private void putCommonResponseHeaders() {
    router
        .route()
        .handler(
            requestHandler -> {
              requestHandler
                  .response()
                  .putHeader("Cache-Control", "no-cache, no-store,  must-revalidate,max-age=0")
                  .putHeader("Pragma", "no-cache")
                  .putHeader("Expires", "0")
                  .putHeader("X-Content-Type-Options", "nosniff");
              requestHandler.next();
            });
  }

  /**
   * starts an HTTP server with the specified HTTP port. If the HTTP port is not specified in the
   * configuration, default ports (8080 for HTTP) will be used.
   *
   * @param serverOptions The server options to be configured.
   */
  private void setServerOptions(HttpServerOptions serverOptions) {
    LOGGER.debug("Info: Starting HTTP server");
    serverOptions.setSsl(false);
    port = config().getInteger("httpPort") == null ? 8080 : config().getInteger("httpPort");
  }

  /**
   * Handles HTTP Success response from the server
   *
   * @param response HttpServerResponse object
   * @param statusCode statusCode to respond with
   * @param result respective result returned from the service
   */
  private void handleSuccessResponse(HttpServerResponse response, int statusCode, String result) {
    response.putHeader(HEADER_X_CONTENT_TYPE_OPTIONS, X_CONTENT_TYPE_OPTIONS_NOSNIFF);
    response.putHeader(CONTENT_TYPE, APPLICATION_JSON).setStatusCode(statusCode).end(result);
  }

  /**
   * Handles Failed HTTP Response
   *
   * @param routingContext Routing context object
   * @param failureMessage Failure message for response
   */
  private void handleFailureResponse(RoutingContext routingContext, String failureMessage) {
    HttpServerResponse response = routingContext.response();
    LOGGER.debug("Failure Message : {} ", failureMessage);

    try {
      JsonObject jsonObject = new JsonObject(failureMessage);
      int type = jsonObject.getInteger(TYPE);
      String title = jsonObject.getString(TITLE);

      HttpStatusCode status = HttpStatusCode.getByValue(type);

      ResponseUrn urn;

      // get the urn by either type or title
      if (title != null) {
        urn = ResponseUrn.fromCode(title);
      } else {

        urn = ResponseUrn.fromCode(String.valueOf(type));
      }
      if (jsonObject.getString(DETAIL) != null) {
        detail = jsonObject.getString(DETAIL);
        response
            .putHeader(CONTENT_TYPE, APPLICATION_JSON)
            .setStatusCode(type)
            .end(generateResponse(status, urn, detail).toString());
      } else {
        response
            .putHeader(CONTENT_TYPE, APPLICATION_JSON)
            .setStatusCode(type)
            .end(generateResponse(status, urn).toString());
      }

    } catch (DecodeException exception) {
      LOGGER.error("Error : Expecting JSON from backend service [ jsonFormattingException ] ");
      handleResponse(response, BAD_REQUEST, ResponseUrn.BACKING_SERVICE_FORMAT_URN);
    }
  }

  private void handleResponse(
      HttpServerResponse response, HttpStatusCode statusCode, ResponseUrn urn) {
    handleResponse(response, statusCode, urn, statusCode.getDescription());
  }

  private void handleResponse(
      HttpServerResponse response,
      HttpStatusCode statusCode,
      ResponseUrn urn,
      String failureMessage) {
    response
        .putHeader(CONTENT_TYPE, APPLICATION_JSON)
        .setStatusCode(statusCode.getValue())
        .end(generateResponse(statusCode, urn, failureMessage).toString());
  }

  // TODO: Call this method where we need auditing and Send correct itemType
  private Future<Void> handleAuditLogs(RoutingContext context) {
    LOGGER.debug("handleAuditLogs started");
    HttpServerRequest request = context.request();
    HttpServerResponse response = context.response();
    User user = RoutingContextHelper.getUser(context);
    JsonObject requestBody = context.body().asJsonObject();
    String userId = user.getUserId();
    long size = response.bytesWritten();

    ZonedDateTime zst = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
    long time = zst.toInstant().toEpochMilli();
    String isoTime = zst.truncatedTo(ChronoUnit.SECONDS).toString();

    JsonObject auditLog = new JsonObject();
    auditLog.put(USERID, userId);
    auditLog.put(BODY, requestBody);
    auditLog.put(API, request.uri());
    auditLog.put(HTTP_METHOD, request.method().toString());
    auditLog.put(EPOCH_TIME, time);
    auditLog.put(ISO_TIME, isoTime);
    auditLog.put(RESPONSE_SIZE, size);

    Promise<Void> promise = Promise.promise();
    LOGGER.debug("AuditLog: " + auditLog);
    auditingService
        .insertAuditlogIntoRmq(auditLog)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                LOGGER.info("Audit data published into RMQ.");
                promise.complete();
              } else {
                LOGGER.error("failed: " + handler.cause().getMessage());
                promise.complete();
              }
            });

    return promise.future();
  }
}
