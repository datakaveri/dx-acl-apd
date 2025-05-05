package org.cdpg.dx.acl.policy.dao;

import io.vertx.core.Future;
import java.util.List;
import org.cdpg.dx.acl.policy.dao.model.ResourceEntityDto;


public interface ResourceEntityDao {

  public Future<List<ResourceEntityDto>> getResourceFromDb(
      String... resourceIds);

  public Future<ResourceEntityDto> createResourceInDb(
      String resourceId,
      String providerId,
      String resourceGroupId,
      String itemType,
      String resourceServerUrl,
      boolean onConflictDoNothing);

  public Future<ResourceEntityDto> checkResourcesFromDb(String resourceId, String providerId);
}
