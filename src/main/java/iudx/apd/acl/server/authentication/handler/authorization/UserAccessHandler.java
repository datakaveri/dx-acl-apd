package iudx.apd.acl.server.authentication.handler.authorization;

import static iudx.apd.acl.server.authentication.model.DxRole.DELEGATE;
import static iudx.apd.acl.server.authentication.util.Constants.INSERT_USER_TABLE;
import static iudx.apd.acl.server.common.ResponseUrn.*;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import iudx.apd.acl.server.aaaService.AuthClient;
import iudx.apd.acl.server.apiserver.util.User;
import iudx.apd.acl.server.authentication.model.DxRole;
import iudx.apd.acl.server.authentication.model.JwtData;
import iudx.apd.acl.server.authentication.model.UserInfo;
import iudx.apd.acl.server.common.HttpStatusCode;
import iudx.apd.acl.server.common.RoutingContextHelper;
import iudx.apd.acl.server.policy.PostgresService;
import iudx.apd.acl.server.validation.exceptions.DxRuntimeException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserAccessHandler implements Handler<RoutingContext> {
  private static final Logger LOGGER = LogManager.getLogger(UserAccessHandler.class);
  private final AuthClient authClient;
  private final UserInfo userInfo;
  private PgPool pool;

  public UserAccessHandler(AuthClient client, UserInfo userInfo, PostgresService postgresService) {
    authClient = client;
    this.userInfo = userInfo;
    this.pool = postgresService.getPool();
  }

  /**
   * After JWT Authentication, User Access Validation is handled here
   *
   * @param event the event to handle
   */
  @Override
  public void handle(RoutingContext event) {
    Future<User> getUserInfoFuture = getUserFromAuth(event);
    getUserInfoFuture
        .onSuccess(
            user -> {
              Future<Void> insertUserInDbIfNotPresent = insertUserIntoDb(user);
              insertUserInDbIfNotPresent
                  .onSuccess(
                      handler -> {
                        LOGGER.debug("User successfully inserted in DB");
                        /* set user in routing context */
                        RoutingContextHelper.setUser(event, user);
                        event.next();
                      })
                  .onFailure(
                      dbFailureMessage -> {
                        LOGGER.error(
                            "Failed to insert user with ID {} in DB", user.getUserId());
                        event.fail(new DxRuntimeException(HttpStatusCode.getByValue(500).getValue(), DB_ERROR_URN));
                      });
            })
        .onFailure(
            failureMessage -> {
              LOGGER.error(
                  "User info fetch from DX Auth failed : {}",
                  failureMessage.getCause().getMessage());
              processAuthFailure(event, failureMessage.getCause().getMessage());
            });
  }

  private Future<Void> insertUserIntoDb(User user) {
    LOGGER.debug("inside insert user in DB method");
    Promise<Void> promise = Promise.promise();
    Collector<Row, ?, List<JsonObject>> rowListCollector =
        Collectors.mapping(row -> row.toJson(), Collectors.toList());
    Tuple tuple =
        Tuple.of(user.getUserId(), user.getEmailId(), user.getFirstName(), user.getLastName());
    pool.withConnection(
            sqlConnection ->
                sqlConnection
                    .preparedQuery(INSERT_USER_TABLE)
                    .collecting(rowListCollector)
                    .execute(tuple)
                    .map(rows -> rows.value()))
        .onSuccess(
            successHandler -> {
              LOGGER.debug("User with ID {} inserted :", user.getUserId());
              promise.complete();
            })
        .onFailure(
            failureHandler -> {
              LOGGER.error("Failure while executing the query : {}", failureHandler.getMessage());
              promise.fail("Failure while executing user insertion query in the user_table during authentication");
            });
    return promise.future();
  }



  private Future<User> getUserFromAuth(RoutingContext event) {
    JwtData jwtData = RoutingContextHelper.getJwtData(event);
    DxRole role = DxRole.fromRole(jwtData);
    boolean isDelegate = jwtData.getRole().equalsIgnoreCase(DELEGATE.getRole());
    UUID id = UUID.fromString(isDelegate ? jwtData.getDid() : jwtData.getSub());
    userInfo.setDelegate(isDelegate).setRole(role).setAudience(jwtData.getAud()).setUserId(id);
    LOGGER.info("Getting user from Auth");
    return authClient.fetchUserInfo(userInfo);
  }

  private void processAuthFailure(RoutingContext event, String failureMessage) {
    LOGGER.error("Error : Authentication Failure : {}", failureMessage);
    if (failureMessage.equalsIgnoreCase("User information is invalid")) {
      LOGGER.error("User information is invalid");
      event.fail(
          new DxRuntimeException(
              HttpStatusCode.INTERNAL_SERVER_ERROR.getValue(), INTERNAL_SERVER_ERROR));
    }
    event.fail(
        new DxRuntimeException(HttpStatusCode.getByValue(401).getValue(), INVALID_TOKEN_URN));
  }
}
