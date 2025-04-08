package org.cdpg.dx.acl.policy.service;

import static org.cdpg.dx.acl.policy.util.Constants.*;
import static org.cdpg.dx.common.models.HttpStatusCode.*;
import static org.cdpg.dx.util.Constants.*;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import iudx.apd.acl.server.database.postgres.service.PostgresService;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.acl.policy.model.CreatePolicyRequest;
import org.cdpg.dx.acl.policy.util.ItemType;
import org.cdpg.dx.acl.policy.util.Status;
import org.cdpg.dx.catalogue.models.ResourceObj;
import org.cdpg.dx.catalogue.service.CatalogueServiceImpl;
import org.cdpg.dx.common.models.HttpStatusCode;
import org.cdpg.dx.common.models.ResponseUrn;
import org.cdpg.dx.common.models.User;

public class CreatePolicy {
  private static final Logger LOGGER = LogManager.getLogger(CreatePolicy.class);
  private final PostgresService postgresqlService;
  private final CatalogueServiceImpl catalogueServiceImpl;

  public CreatePolicy(PostgresService postgresqlService, CatalogueServiceImpl catalogueServiceImpl) {
    this.postgresqlService = postgresqlService;
    this.catalogueServiceImpl = catalogueServiceImpl;
  }

  public Future<JsonObject> initiateCreatePolicy(JsonObject request, User user) {
    Promise<JsonObject> promise = Promise.promise();
    JsonArray policyList = request.getJsonArray("request");
    UUID userId = UUID.fromString(user.getUserId());
    try {
      List<CreatePolicyRequest> createPolicyRequestList =
          CreatePolicyRequest.jsonArrayToList(policyList, request.getLong("defaultExpiryDays"));
      Set<UUID> itemIdList =
          createPolicyRequestList.stream()
              .map(CreatePolicyRequest::getItemId)
              .collect(Collectors.toSet());
      Set<ItemType> itemType =
          createPolicyRequestList.stream()
              .map(CreatePolicyRequest::getItemType)
              .collect(Collectors.toSet());

      if (itemType.contains(ItemType.RESOURCE_GROUP)) {
        LOGGER.debug("Contains resource group");
        return Future.failedFuture(
            generateErrorResponse(BAD_REQUEST, "Policy creation for resource group is restricted"));
      }

      Future<Set<UUID>> checkIfItemPresent = checkForItemsInDb(itemIdList, itemType, user);
      Future<Boolean> isPolicyAlreadyExist =
          checkIfItemPresent.compose(
              providerIdSet -> {
                if (providerIdSet.size() == 1 && providerIdSet.contains(userId)) {
                  return checkExistingPoliciesForId(createPolicyRequestList, userId);
                } else {
                  LOGGER.error("Item does not belong to the policy creator.");
                  return Future.failedFuture(
                      generateErrorResponse(
                          FORBIDDEN,
                          "Access Denied: You do not have ownership rights for this resource."));
                }
              });

      Future<JsonObject> insertPolicy =
          isPolicyAlreadyExist.compose(
              policyDoesNotExist -> {
                return createPolicy(createPolicyRequestList, userId)
                    .compose(
                        createPolicySuccessHandler -> {
                          JsonArray responseArray = createResponseArray(createPolicySuccessHandler);
                          LOGGER.debug("Policy is created with info {}", responseArray);
                          JsonObject responseJson =
                              new JsonObject()
                                  .put("type", ResponseUrn.SUCCESS_URN.getUrn())
                                  .put("title", ResponseUrn.SUCCESS_URN.getMessage())
                                  .put(DETAIL, "Policy created successfully");
                          return Future.succeededFuture(responseJson);
                        });
              });

      insertPolicy
          .onSuccess(promise::complete)
          .onFailure(
              f -> {
                LOGGER.info("Policy could not be created {}", f.getLocalizedMessage());
                promise.fail(f);
              });
    } catch (IllegalArgumentException e) {
      promise.fail(generateErrorResponse(BAD_REQUEST, e.getMessage()));
    }
    return promise.future();
  }

  private Future<Set<UUID>> checkForItemsInDb(
      Set<UUID> itemIdList, Set<ItemType> itemTypeRequest, User user) {
    Promise<Set<UUID>> promise = Promise.promise();

    postgresqlService
        .getPool()
        .withConnection(
            sqlConnection ->
                sqlConnection
                    .preparedQuery(ENTITY_TABLE_CHECK)
                    .execute(Tuple.of(itemIdList.toArray(UUID[]::new)))
                    .onFailure(
                        existingIdFailureHandler -> {
                          LOGGER.error(
                              "checkForItemsInDb db fail {}",
                              existingIdFailureHandler.getLocalizedMessage());
                          promise.fail(
                              generateErrorResponse(
                                  INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR.getDescription()));
                        })
                    .onSuccess(
                        existingIdSuccessHandler -> {
                          Set<UUID> providerIdSet = new HashSet<>();
                          Set<UUID> existingItemIds = new HashSet<>();
                          Set<ItemType> itemTypeDb = new HashSet<>();
                          Set<String> rsServerUrlSetDb = new HashSet<>();
                          if (existingIdSuccessHandler.size() > 0) {
                            for (Row row : existingIdSuccessHandler) {
                              providerIdSet.add(row.getUUID("provider_id"));
                              existingItemIds.add(row.getUUID("_id"));
                              itemTypeDb.add(ItemType.valueOf(row.getString("item_type")));
                              rsServerUrlSetDb.add(row.getString("resource_server_url"));
                            }
                            itemIdList.removeAll(existingItemIds);
                          }
                          if (!itemIdList.isEmpty()) {
                            Future<List<ResourceObj>> resourceObjList =
                                catalogueServiceImpl.fetchItems(itemIdList);
                            Future<Set<UUID>> providerIdsFromCat =
                                resourceObjList
                                    .compose(
                                        success -> {
                                          Set<ItemType> itemTypeCat =
                                              success.stream()
                                                  .map(ResourceObj::getItemType)
                                                  .collect(Collectors.toSet());
                                          Set<String> rsServerUrlCat =
                                              success.stream()
                                                  .map(ResourceObj::resourceServerUrl)
                                                  .collect(Collectors.toSet());
                                          if (!itemTypeRequest.containsAll(itemTypeCat)) {
                                            return Future.failedFuture("Invalid item type.");
                                          } else if (!rsServerUrlCat.contains(
                                              user.getResourceServerUrl())) {
                                            return Future.failedFuture(generateErrorResponse(
                                                FORBIDDEN,
                                                "Access Denied: You do not have ownership rights for this resource."));
                                          } else {
                                            return insertItemsIntoDb(success);
                                          }
                                        })
                                    .onFailure(
                                        failureHandler -> {
                                          String failureMessage = failureHandler.getMessage();
                                          if (failureMessage.contains(TYPE)
                                              && failureMessage.contains(TITLE)) {
                                            promise.fail(failureHandler.getMessage());
                                          } else {
                                            promise.fail(
                                                generateErrorResponse(BAD_REQUEST, failureMessage));
                                          }
                                        });
                            providerIdsFromCat
                                .onSuccess(
                                    insertItemsSuccessHandler -> {
                                      providerIdSet.addAll(insertItemsSuccessHandler);
                                      promise.complete(providerIdSet);
                                    })
                                .onFailure(
                                    insertItemsFailureHandler -> {
                                      LOGGER.error(
                                          "insertItemInDbFail "
                                              + insertItemsFailureHandler.getLocalizedMessage());

                                      promise.tryFail(
                                          insertItemsFailureHandler
                                              .getLocalizedMessage()
                                              .equalsIgnoreCase(
                                                  "Access Denied: You do not have "
                                                      + "ownership rights for this resource.")
                                              ? generateErrorResponse(
                                              FORBIDDEN,
                                              insertItemsFailureHandler.getLocalizedMessage())
                                              : generateErrorResponse(
                                              BAD_REQUEST,
                                              insertItemsFailureHandler.getLocalizedMessage()));
                                    });
                          } else {
                            if (!itemTypeDb.containsAll(itemTypeRequest)) {
                              promise.fail(
                                  generateErrorResponse(BAD_REQUEST, "Invalid item type."));
                            } else if (!rsServerUrlSetDb.contains(user.getResourceServerUrl())) {
                              promise.fail(
                                  generateErrorResponse(
                                      FORBIDDEN,
                                      "Access Denied: You do not have ownership rights for this resource."));
                            } else {
                              promise.complete(providerIdSet);
                            }
                          }
                        }));
    return promise.future();
  }

  private Future<Set<UUID>> insertItemsIntoDb(List<ResourceObj> resourceObjList) {
    Promise<Set<UUID>> promise = Promise.promise();
    List<Tuple> batch = new ArrayList<>();
    Set<UUID> providerIdSet =
        resourceObjList.stream().map(ResourceObj::providerId).collect(Collectors.toSet());

    for (ResourceObj resourceObj : resourceObjList) {
      UUID id = resourceObj.itemId();
      UUID provider = resourceObj.providerId();
      UUID resourceGroupId = resourceObj.getResourceGroupId();
      ItemType itemType = resourceObj.getItemType();
      String resourceServerUrl = resourceObj.resourceServerUrl();
      batch.add(Tuple.of(id, provider, resourceGroupId, itemType, resourceServerUrl));
    }

    // TODO: how to execute batch operation? how to execute batch operations using RS like server related PostgresServiceImpl || new Postgres
    postgresqlService
        .getPool()
        .withConnection(
            sqlConnection ->
                sqlConnection
                    .preparedQuery(INSERT_ENTITY_TABLE)
                    .executeBatch(batch)
                    .onFailure(
                        dbHandler -> {
                          LOGGER.error(
                              "insertItemsIntoDb db fail " + dbHandler.getLocalizedMessage());
                        })
                    .onSuccess(
                        dbSuccessHandler -> {
                          promise.complete(providerIdSet);
                        }));


    return promise.future();
  }

  private Future<Boolean> checkExistingPoliciesForId(
      List<CreatePolicyRequest> createPolicyRequestList, UUID providerId) {
    List<Tuple> selectTuples =
        createPolicyRequestList.stream()
            .map(
                createPolicyRequest ->
                    Tuple.of(
                        createPolicyRequest.getItemId(),
                        providerId,
                        Status.ACTIVE,
                        createPolicyRequest.getUserEmail()))
            .collect(Collectors.toList());
    Promise<Boolean> promise = Promise.promise();
    postgresqlService
        .getPool()
        .withTransaction(
            conn ->
                conn.preparedQuery(CHECK_EXISTING_POLICY)
                    .executeBatch(selectTuples)
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
                            List<UUID> responseArray = new ArrayList<>();
                            for (RowSet<Row> rowSet = policyExists;
                                 rowSet != null;
                                 rowSet = rowSet.next()) {
                              rowSet.forEach(row -> responseArray.add(row.getUUID("_id")));
                            }
                            LOGGER.error("Policy already Exist.");
                            promise.fail(
                                generateErrorResponse(
                                    CONFLICT,
                                    "Policy already exist for some of the request objects "
                                        + responseArray));
                          } else {
                            promise.complete(false);
                          }
                        }));

    return promise.future();
  }

  Future<RowSet<Row>> createPolicy(List<CreatePolicyRequest> createPolicyRequestList, UUID userId) {
    Promise<RowSet<Row>> promise = Promise.promise();
    List<Tuple> createPolicyTuple =
        createPolicyRequestList.stream()
            .map(
                createPolicyRequest ->
                    Tuple.of(
                        createPolicyRequest.getUserEmail(),
                        createPolicyRequest.getItemId(),
                        userId,
                        createPolicyRequest.getExpiryTime(),
                        createPolicyRequest.getConstraints()))
            .collect(Collectors.toList());

    postgresqlService
        .getPool()
        .withTransaction(
            conn -> {
              // Execute the batch query to create policies
              return conn.preparedQuery(CREATE_POLICY_QUERY)
                  .executeBatch(createPolicyTuple)
                  .onFailure(
                      failureHandler -> {
                        LOGGER.error(
                            "createPolicy fail :: " + failureHandler.getLocalizedMessage());
                        // Fail the promise with an error response
                        promise.fail(
                            generateErrorResponse(
                                INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR.getDescription()));
                      })
                  .onSuccess(promise::complete);
            });

    return promise.future();
  }

  private String generateErrorResponse(HttpStatusCode httpStatusCode, String errorMessage) {
    return new JsonObject()
        .put(TYPE, httpStatusCode.getValue())
        .put(TITLE, httpStatusCode.getUrn())
        .put(DETAIL, errorMessage)
        .encode();
  }

  private JsonArray createResponseArray(RowSet<Row> rows) {
    JsonArray response = new JsonArray();
    final JsonObject[] ownerJsonObject = {null};

    for (RowSet<Row> rowSet = rows; rowSet != null; rowSet = rowSet.next()) {
      rowSet.forEach(
          row -> {
            JsonObject jsonObject =
                new JsonObject()
                    .put("policyId", row.getUUID("_id").toString())
                    .put("userEmailId", row.getString("user_emailid"))
                    .put("itemId", row.getUUID("item_id").toString())
                    .put("expiryAt", row.getLocalDateTime("expiry_at").toString());

            if (ownerJsonObject[0] == null) {
              ownerJsonObject[0] =
                  new JsonObject().put("ownerId", row.getValue("owner_id").toString());
            }
            response.add(jsonObject);
          });
    }

    response.add(ownerJsonObject[0]);
    return response;
  }
}
