package org.cdpg.dx.catalogue.service;

import io.vertx.core.Future;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.cdpg.dx.catalogue.models.ResourceObj;

/**
 * Interface for the Catalogue Client, responsible for fetching resource/resource_group from the
 * Catalogue server and updating the resource_entity table in the database.
 */
public interface CatalogueService {

  /**
   * Fetches resource/resource_group from the Catalogue server and updates the resource_entity table in the
   * database.
   *
   * @param ids A set of unique IDs of resource/resource_group to be fetched.
   * @return A Future containing a list of ResourceObj representing the fetched resources. The
   *     Future is resolved with the fetched resourceObj list on success, or failed with an error
   *     message on failure.
   */
  Future<List<ResourceObj>> fetchItems(Set<UUID> ids);
}
