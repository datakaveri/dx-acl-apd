package org.cdpg.dx.catalogue.client;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import java.util.UUID;

public interface CatalogueClient {
  Future<JsonArray> fetchItem(UUID id);

  }
