package org.cdpg.dx.catalogue.client;

import static org.cdpg.dx.common.models.HttpStatusCode.INTERNAL_SERVER_ERROR;
import static org.cdpg.dx.util.Constants.*;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Client for communicating with the catalogue server. */
public class CatalogueWebClient implements CatalogueClient {

  private static final Logger LOGGER = LogManager.getLogger(CatalogueWebClient.class);
  private final String catHost;
  private final Integer catPort;
  private final String catRelationShipPath;
  public WebClient client;

  public CatalogueWebClient(JsonObject config, WebClient webClient) {
    this.client = webClient;
    this.catHost = config.getString("catServerHost");
    this.catPort = config.getInteger("catServerPort");
    this.catRelationShipPath = config.getString("dxCatalogueBasePath") + RELATIONSHIP_PATH;
  }

  /** Fetches and validates resource info, and provider ID from the catalogue server. */
  public Future<JsonArray> fetchItem(UUID id) {
    return client
        .get(catPort, catHost, catRelationShipPath)
        .addQueryParam(ID, String.valueOf(id))
        .addQueryParam("rel", "all")
        .send()
        .compose(this::handleResponse)
        .recover(
            error -> {
              LOGGER.error("fetchItem error {}: {}", id, error.getMessage());
              return Future.failedFuture(INTERNAL_SERVER_ERROR.getDescription());
            });
  }

  /** Handles and validates the response from the catalogue server. */
  private Future<JsonArray> handleResponse(HttpResponse<Buffer> response) {
    JsonObject resultBody = response.bodyAsJsonObject();
    LOGGER.info(resultBody.encode());
    if (resultBody.getString(TYPE).equals(CAT_SUCCESS_URN)) {
      JsonArray result = resultBody.getJsonArray(RESULTS);
      return Future.succeededFuture(result);
    } else {
      return Future.failedFuture(resultBody.getString(DETAIL));
    }
  }
}
