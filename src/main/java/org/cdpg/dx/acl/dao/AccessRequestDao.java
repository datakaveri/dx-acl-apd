package org.cdpg.dx.acl.dao;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.util.List;
import org.cdpg.dx.acl.dao.model.AccessRequestDto;

public interface AccessRequestDao {
  public Future<List<AccessRequestDto>> getAccessRequestFromDb(
      String requestId,
      String itemId,
      String userId,
      String status,
      String ownerId,
      boolean isUpdatedAtDesc);

  public Future<AccessRequestDto> createAccessRequestInDb(
      String userId,
      String itemId,
      String ownerId,
      String status,
      String expiryAt,
      JsonObject constraints,
      JsonObject additionalInfo);

  public Future<AccessRequestDao> withdrawAccessRequestInDb(String requestId, String status);

  public Future<AccessRequestDto> rejectAccessRequestInDb(String requestId, String status);
}
