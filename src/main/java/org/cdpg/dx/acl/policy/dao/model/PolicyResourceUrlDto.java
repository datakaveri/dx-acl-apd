package org.cdpg.dx.acl.policy.dao.model;

import static org.cdpg.dx.acl.policy.dao.util.Constants.*;

import io.vertx.core.json.JsonObject;
import java.util.Objects;
import org.cdpg.dx.acl.policy.util.Status;

public class PolicyResourceUrlDto {
  private String ownerId;
  private Status status;
  private String resourceServerUrl;

  public PolicyResourceUrlDto() {}

  public PolicyResourceUrlDto(JsonObject jsonObject) {

    setOwnerId(jsonObject.getString(DB_OWNER_ID));
    setStatus(Status.valueOf(jsonObject.getString(DB_STATUS)));
    setResourceServerUrl(jsonObject.getString(DB_RESOURCE_SERVER_URL));
  }

  public String getOwnerId() {
    return ownerId;
  }

  public PolicyResourceUrlDto setOwnerId(String ownerId) {
    this.ownerId = ownerId;
    return this;
  }

  public Status getStatus() {
    return status;
  }

  public PolicyResourceUrlDto setStatus(Status status) {
    this.status = status;
    return this;
  }

  public String getResourceServerUrl() {
    return resourceServerUrl;
  }

  public PolicyResourceUrlDto setResourceServerUrl(String resourceServerUrl) {
    this.resourceServerUrl = resourceServerUrl;
    return this;
  }

  @Override
  public String toString() {
    return "PolicyResourceUrlDto{"
        + "ownerId='"
        + ownerId
        + '\''
        + ", status="
        + status
        + ", resourceServerUrl='"
        + resourceServerUrl
        + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PolicyResourceUrlDto that)) return false;
    return Objects.equals(ownerId, that.ownerId)
        && status == that.status
        && Objects.equals(resourceServerUrl, that.resourceServerUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ownerId, status, resourceServerUrl);
  }
}
