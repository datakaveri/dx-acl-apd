package org.cdpg.dx.acl.policy.dao.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;

public class ResourceEntityDto {
  String id;
  String providerId;
  String resourceGroupId;
  String itemType;
  String resourceServerUrl;
  String createdAt;
  String updatedAt;

  public ResourceEntityDto() {}

  public ResourceEntityDto(ResourceEntityDto other) {
    this.id = other.getId();
    this.providerId = other.getProviderId();
    this.resourceGroupId = other.getResourceGroupId();
    this.itemType = other.getItemType();
    this.resourceServerUrl = other.getResourceServerUrl();
    this.updatedAt = other.getUpdatedAt();
    this.createdAt = other.getCreatedAt();
  }

  public ResourceEntityDto(JsonObject jsonObject) {
//    ResourceEntityDtoConverter.fromJson(jsonObject, this);
  }

  /**
   * Converts Data object or Policy class object to json object [Serialization]
   *
   * @return JsonObject
   */
//  public JsonObject toJson() {
//    JsonObject jsonObject = new JsonObject();
//    ResourceEntityDtoConverter.toJson(this, jsonObject);
//    return jsonObject;
//  }

  public String getId() {
    return id;
  }

  public ResourceEntityDto setId(String id) {
    this.id = id;
    return this;
  }

  public String getProviderId() {
    return providerId;
  }

  public ResourceEntityDto setProviderId(String providerId) {
    this.providerId = providerId;
    return this;
  }

  public String getResourceGroupId() {
    return resourceGroupId;
  }

  public ResourceEntityDto setResourceGroupId(String resourceGroupId) {
    this.resourceGroupId = resourceGroupId;
    return this;
  }

  public String getItemType() {
    return itemType;
  }

  public ResourceEntityDto setItemType(String itemType) {
    this.itemType = itemType;
    return this;
  }

  public String getResourceServerUrl() {
    return resourceServerUrl;
  }

  public ResourceEntityDto setResourceServerUrl(String resourceServerUrl) {
    this.resourceServerUrl = resourceServerUrl;
    return this;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public ResourceEntityDto setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public ResourceEntityDto setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }
}
