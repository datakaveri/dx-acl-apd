package org.cdpg.dx.acl.policy.service;


import static org.cdpg.dx.acl.policy.util.Constants.CHECK_EXISTING_POLICY;
import static org.cdpg.dx.common.models.HttpStatusCode.INTERNAL_SERVER_ERROR;
import static org.cdpg.dx.common.models.HttpStatusCode.VERIFY_FORBIDDEN;
import static org.cdpg.dx.util.Constants.*;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import iudx.apd.acl.server.database.postgres.service.PostgresService;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.acl.policy.util.ItemType;
import org.cdpg.dx.acl.policy.util.Status;
import org.cdpg.dx.catalogue.service.CatalogueServiceImpl;
import org.cdpg.dx.common.models.HttpStatusCode;
import org.cdpg.dx.common.models.ResponseUrn;

public class VerifyPolicy {
  private static final Logger LOGGER = LogManager.getLogger(VerifyPolicy.class);
  private final PostgresService postgresqlService;
  private final CatalogueServiceImpl catalogueServiceImpl;

  public VerifyPolicy(PostgresService postgresqlService, CatalogueServiceImpl catalogueServiceImpl) {
    this.postgresqlService = postgresqlService;
    this.catalogueServiceImpl = catalogueServiceImpl;
  }

  public Future<JsonObject> initiateVerifyPolicy(JsonObject request) {
    Promise<JsonObject> promise = Promise.promise();

    UUID ownerId = UUID.fromString(request.getJsonObject("owner").getString("id"));
    String userEmail = request.getJsonObject("user").getString("email");
    UUID itemId = UUID.fromString(request.getJsonObject("item").getString("itemId"));
    ItemType itemType = ItemType.valueOf(request.getJsonObject("item").getString("itemType").toUpperCase());
    Future<JsonObject> checkForExistingPolicy =
        checkExistingPoliciesForId(itemId, ownerId, userEmail);

    Future<JsonObject> getPolicyDetail =
        checkForExistingPolicy.compose(
            isPolicyExist -> {
              if (isPolicyExist.containsKey("id")) {
                return Future.succeededFuture(isPolicyExist);
              } else if (ItemType.RESOURCE.equals(itemType)) {
                Set<UUID> itemSet = new HashSet<>();
                itemSet.add(itemId);
                return catalogueServiceImpl
                    .fetchItems(itemSet)
                    .compose(
                        resourceObjList -> {
                          if (resourceObjList.isEmpty()) {
                            return Future.failedFuture(
                                generateErrorResponse(
                                    VERIFY_FORBIDDEN, "Resource Group not found in CAT"));
                          }
                          UUID rsGrpId = resourceObjList.get(0).getResourceGroupId();

                          return checkExistingPoliciesForId(rsGrpId, ownerId, userEmail)
                              .compose(
                                  rsPolicy -> {
                                    if (rsPolicy.containsKey("id")) {
                                      return Future.succeededFuture(rsPolicy);
                                    } else {
                                      return Future.failedFuture(
                                          generateErrorResponse(
                                              VERIFY_FORBIDDEN,
                                              "No policy exist for given item's Resource Group"));
                                    }
                                  });
                        });
              } else {
                return Future.failedFuture(
                    generateErrorResponse(
                        VERIFY_FORBIDDEN, "No policy exist for given Resource Group"));
              }
            });

    getPolicyDetail
        .onSuccess(
            successHandler -> {
              JsonObject responseJson =
                  new JsonObject()
                      .put(TYPE, ResponseUrn.VERIFY_SUCCESS_URN.getUrn())
                      .put("apdConstraints", successHandler.getJsonObject("constraints"));
              promise.complete(responseJson);
            })
        .onFailure(promise::fail);

    return promise.future();
  }

  private Future<JsonObject> checkExistingPoliciesForId(
      UUID itemId, UUID ownerId, String userEmailId) {
    Tuple selectTuples = Tuple.of(itemId, ownerId, Status.ACTIVE, userEmailId);
    Promise<JsonObject> promise = Promise.promise();
    postgresqlService
        .getPool()
        .withConnection(
            conn ->
                conn.preparedQuery(CHECK_EXISTING_POLICY)
                    .execute(selectTuples)
                    .onFailure(
                        failureHandler -> {
                          LOGGER.error(
                              "isPolicyForIdExist fail :: " + failureHandler.getLocalizedMessage());
                          promise.fail(
                              generateErrorResponse(
                                  INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR.getDescription()));
                        })
                    .onSuccess(
                        policyExists -> {
                          if (policyExists.size() > 0) {
                            JsonObject policyConstraints = new JsonObject();
                            for (Row row : policyExists) {
                              policyConstraints.put(
                                  "constraints", row.getJsonObject("constraints"));
                              policyConstraints.put("id", row.getValue("_id"));
                            }
                            promise.complete(policyConstraints);
                          } else {
                            LOGGER.trace("No policy found");
                            promise.complete(new JsonObject());
                          }
                        }));

    return promise.future();
  }

  public String generateErrorResponse(HttpStatusCode httpStatusCode, String errorMessage) {
    return new JsonObject()
        .put(TYPE, httpStatusCode.getValue())
        .put(TITLE, httpStatusCode.getUrn())
        .put(DETAIL, errorMessage)
        .encode();
  }
}
