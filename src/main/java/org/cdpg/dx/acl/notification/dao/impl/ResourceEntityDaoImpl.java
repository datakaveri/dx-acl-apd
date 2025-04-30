package org.cdpg.dx.acl.notification.dao.impl;

import io.vertx.core.Future;
import java.util.List;
import org.cdpg.dx.acl.notification.dao.ResourceEntityDao;
import org.cdpg.dx.acl.notification.dao.model.ResourceEntityDto;
import org.cdpg.dx.database.postgres.models.Condition;
import org.cdpg.dx.database.postgres.models.InsertQuery;
import org.cdpg.dx.database.postgres.models.SelectQuery;
import org.cdpg.dx.database.postgres.models.UpdateQuery;

public class ResourceEntityDaoImpl implements ResourceEntityDao {
  SelectQuery selectQuery;
  InsertQuery insertQuery;
  UpdateQuery updateQuery;
  Condition condition;

  public ResourceEntityDaoImpl(){
    selectQuery = new SelectQuery();
    insertQuery = new InsertQuery();
    updateQuery = new UpdateQuery();
    condition = new Condition();
  }
  public ResourceEntityDaoImpl(SelectQuery selectQuery, InsertQuery insertQuery, UpdateQuery updateQuery, Condition condition){
    this.condition = condition;
    this.selectQuery = selectQuery;
    this.updateQuery = updateQuery;
    this.insertQuery = insertQuery;
  }

  @Override
  public Future<List<ResourceEntityDto>> getResourceFromDb(String resourceId, String resourceServerUrl,
                                                           String providerId) {
    return null;
  }

  @Override
  public Future<ResourceEntityDto> createResourceInDb(String resourceId, String providerId, String resourceGroupId,
                                                      String itemType, String resourceServerUrl,
                                                      boolean onConflictDoNothing) {
    return null;
  }

  @Override
  public Future<List<ResourceEntityDto>> checkResourcesFromDb(List<String> resourceId) {
    return null;
  }
}
