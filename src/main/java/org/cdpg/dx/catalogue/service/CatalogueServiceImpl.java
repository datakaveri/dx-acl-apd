package org.cdpg.dx.catalogue.service;

import static org.cdpg.dx.common.models.HttpStatusCode.*;
import static org.cdpg.dx.util.Constants.*;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.catalogue.client.CatalogueClient;
import org.cdpg.dx.catalogue.models.ResourceObj;
import org.cdpg.dx.common.exception.DxRuntimeException;
import org.cdpg.dx.common.models.ResponseUrn;
import org.cdpg.dx.common.models.Util;

/** Implementation of CatalogueService for handling resource meta data from catalogue. */
public class CatalogueServiceImpl implements CatalogueService {

  private static final Logger LOGGER = LogManager.getLogger(CatalogueServiceImpl.class);
  private final String apdUrl;
  public CatalogueClient client;

  public CatalogueServiceImpl(String apdUrl, CatalogueClient client) {
    this.apdUrl = apdUrl;
    this.client = client;
  }

  @Override
  public Future<List<ResourceObj>> fetchItems(Set<UUID> ids) {
    List<ResourceObj> resourceObjList = new ArrayList<>();
    Promise<List<ResourceObj>> promise = Promise.promise();
    for (UUID id : ids) {
      client
          .fetchItem(id)
          .onSuccess(
              catJsonArrayResponse -> {
                List<JsonObject> resultJsonList =
                    catJsonArrayResponse.stream().map(obj -> (JsonObject) obj).toList();

                UUID provider = null;
                UUID resourceGroup = null;
                String resServerUrl = null;
                boolean isItemGroupLevelResource = false;
                boolean isProviderId = false;
                boolean isResourceServerId = false;
                boolean isInvalidId = false;
                String apdUrlOfResource = "";

                for (JsonObject resultJson : resultJsonList) {
                  String type = resultJson.getString(TYPE);
                  String idFromResponse = resultJson.getString(ID);
                  /* check if the id being sent is a provider id*/
                  if (type.contains(PROVIDER_TAG)) {
                    isProviderId = idFromResponse.equals(id.toString());
                  }
                  /* check if the id being sent is a resource server id*/
                  if (type.contains(RESOURCE_SERVER_TAG)) {
                    isResourceServerId = idFromResponse.equals(id.toString());
                  }

                  isInvalidId = isProviderId || isResourceServerId;
                  if (!isInvalidId
                      && idFromResponse != null
                      && idFromResponse.equals(id.toString())) {
                    List<String> tags = Util.toList(resultJson.getJsonArray(TYPE));
                    isItemGroupLevelResource = tags.contains(RESOURCE_GROUP_TAG);
                  }

                  JsonArray typeArray = resultJson.getJsonArray(TYPE);
                  if (typeArray.contains(RESOURCE_GROUP_TAG)) {
                    resourceGroup = UUID.fromString(idFromResponse);
                  } else if (typeArray.contains(PROVIDER_TAG)) {
                    provider = UUID.fromString(resultJson.getString(OWNER_ID));
                  } else if (typeArray.contains(RESOURCE_TAG)) {
                    resServerUrl = resultJson.getString(RS_URL);
                  } else if (typeArray.contains(RESOURCE_ITEM_TAG)) {
                    apdUrlOfResource = resultJson.getString(APD_URL);
                  }
                }
                boolean isInfoFromCatInvalid =
                    id == null || provider == null || resServerUrl == null;
                if (isInfoFromCatInvalid && !isInvalidId) {
                  LOGGER.error("Something from catalogue is null. The resourceId is {}", id);
                  LOGGER.error("The ownerId is {}", provider);
                  LOGGER.error("The resource server URL is {}", resServerUrl);
                  String failureMessage =
                      "Something went wrong while fetching resource info from Catalogue";
                  throw new DxRuntimeException(
                      INTERNAL_SERVER_ERROR.getValue(),
                      ResponseUrn.INTERNAL_SERVER_ERROR,
                      failureMessage);
                } else if (isProviderId || isResourceServerId) {
                  LOGGER.error("isProviderId: {}", isProviderId);
                  LOGGER.error("isResourceServerId: {}", isResourceServerId);
                  String failureMessage =
                      "Given id is invalid - it is a provider or resource server id";
                  throw new DxRuntimeException(
                      BAD_REQUEST.getValue(), ResponseUrn.BAD_REQUEST_URN, failureMessage);
                } else if (isItemGroupLevelResource) {
                  String failureMessage = "Given id is invalid - it is group level resource";
                  throw new DxRuntimeException(
                      BAD_REQUEST.getValue(), ResponseUrn.BAD_REQUEST_URN, failureMessage);
                } else if (!isItemGroupLevelResource && !apdUrl.equals(apdUrlOfResource)) {
                  /* if the resource has an APD URL that is not equal to the current APD URL*/
                  String failureMessage =
                      "Resource is forbidden to access, as the APD URL for the resource : "
                          + apdUrlOfResource
                          + " is different than the current APD : "
                          + apdUrl;
                  throw new DxRuntimeException(
                      FORBIDDEN.getValue(), ResponseUrn.FORBIDDEN_URN, failureMessage);
                } else {
                  ResourceObj resourceObj =
                      new ResourceObj(
                          id, provider, resourceGroup, resServerUrl, isItemGroupLevelResource);
                  resourceObjList.add(resourceObj);
                  promise.complete(resourceObjList);
                }
              })
          .onFailure(promise::fail);
    }
    return promise.future();
  }
}
