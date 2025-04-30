package org.cdpg.dx.acl.notification.dao.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.util.List;
import org.cdpg.dx.acl.notification.dao.AccessRequestDao;
import org.cdpg.dx.acl.notification.dao.model.AccessRequestDto;
import org.cdpg.dx.database.postgres.models.Condition;
import org.cdpg.dx.database.postgres.models.InsertQuery;
import org.cdpg.dx.database.postgres.models.SelectQuery;
import org.cdpg.dx.database.postgres.models.UpdateQuery;

public class AccessRequestDaoImpl implements AccessRequestDao {
  SelectQuery selectQuery;
  InsertQuery insertQuery;
  UpdateQuery updateQuery;
  Condition condition;

  public AccessRequestDaoImpl(){
    selectQuery = new SelectQuery();
    insertQuery = new InsertQuery();
    updateQuery = new UpdateQuery();
    condition = new Condition();
  }
  public AccessRequestDaoImpl(SelectQuery selectQuery, InsertQuery insertQuery, UpdateQuery updateQuery, Condition condition){
    this.condition = condition;
    this.selectQuery = selectQuery;
    this.updateQuery = updateQuery;
    this.insertQuery = insertQuery;
  }

  @Override
  public Future<List<AccessRequestDto>> getAccessRequestFromDb(String requestId, String itemId, String userId,
                                                               String status, String ownerId, boolean isUpdatedAtDesc) {
    return null;
  }

  @Override
  public Future<AccessRequestDto> createAccessRequestInDb(String userId, String itemId, String ownerId, String status,
                                                          String expiryAt, JsonObject constraints,
                                                          JsonObject additionalInfo) {
    return null;
  }

  @Override
  public Future<AccessRequestDao> withdrawAccessRequestInDb(String requestId, String status) {
    return null;
  }

  @Override
  public Future<AccessRequestDto> rejectAccessRequestInDb(String requestId, String status) {
    return null;
  }
}
