package org.cdpg.dx.acl.notification.dao.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;


public class AccessRequestDto {
  String id;
  String consumerId;
  String ownerId;
  String status;
  String itemId;
  String expiryAt;
  String createdAt;
  String updatedAt;
  JsonObject additionalInfo;
  JsonObject constraints;

  public AccessRequestDto() {}

  public AccessRequestDto(AccessRequestDto other) {
    this.id = other.getId();
    this.additionalInfo = other.getAdditionalInfo();
    this.itemId = other.getItemId();
    this.expiryAt = other.getExpiryAt();
    this.constraints = other.getConstraints();
    this.updatedAt = other.getUpdatedAt();
    this.createdAt = other.getCreatedAt();
    this.consumerId = other.getConsumerId();
    this.ownerId = other.getOwnerId();
    this.status = other.getStatus();
  }

  public AccessRequestDto(JsonObject jsonObject) {
    /* Converts JsonObject to AccessRequestDto class object or dataObject conversion [Deserialization] */
//    AccessRequestDtoConverter.fromJson(jsonObject, this);
  }

  /**
   * Converts Data object or Policy class object to json object [Serialization]
   *
   * @return JsonObject
   */
  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
//    AccessRequestDtoConverter.toJson(this, jsonObject);
    return jsonObject;
  }

  public String getId() {
    return id;
  }

  public AccessRequestDto setId(String id) {
    this.id = id;
    return this;
  }

  public String getConsumerId() {
    return consumerId;
  }

  public AccessRequestDto setConsumerId(String consumerId) {
    this.consumerId = consumerId;
    return this;
  }

  public String getOwnerId() {
    return ownerId;
  }

  public AccessRequestDto setOwnerId(String ownerId) {
    this.ownerId = ownerId;
    return this;
  }

  public String getStatus() {
    return status;
  }

  public AccessRequestDto setStatus(String status) {
    this.status = status;
    return this;
  }

  public String getItemId() {
    return itemId;
  }

  public AccessRequestDto setItemId(String itemId) {
    this.itemId = itemId;
    return this;
  }

  public String getExpiryAt() {
    return expiryAt;
  }

  public AccessRequestDto setExpiryAt(String expiryAt) {
    this.expiryAt = expiryAt;
    return this;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public AccessRequestDto setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public AccessRequestDto setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  public JsonObject getAdditionalInfo() {
    return additionalInfo;
  }

  public AccessRequestDto setAdditionalInfo(JsonObject additionalInfo) {
    this.additionalInfo = additionalInfo;
    return this;
  }

  public JsonObject getConstraints() {
    return constraints;
  }

  public AccessRequestDto setConstraints(JsonObject constraints) {
    this.constraints = constraints;
    return this;
  }
}
