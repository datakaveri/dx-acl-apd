package org.cdpg.dx.acl.notification.dao.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;


public class ApprovedAccessRequestDto {
  String id;
  String policyId;
  String requestId;
  String createdAt;
  String updatedAt;

  public ApprovedAccessRequestDto() {}

  public ApprovedAccessRequestDto(ApprovedAccessRequestDto other) {
    this.id = other.getId();
    this.createdAt = other.getCreatedAt();
    this.updatedAt = other.getUpdatedAt();
    this.policyId = other.getPolicyId();
    this.requestId = other.getRequestId();
  }

  public ApprovedAccessRequestDto(JsonObject jsonObject) {
    /* Converts JsonObject to PolicyDto class object or dataObject conversion [Deserialization] */
//    ApprovedAccessRequestDtoConverter.fromJson(jsonObject, this);
  }

  /**
   * Converts Data object or Policy class object to json object [Serialization]
   *
   * @return JsonObject
   */
  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
//    ApprovedAccessRequestDtoConverter.toJson(this, jsonObject);
    return jsonObject;
  }

  public String getId() {
    return id;
  }

  public ApprovedAccessRequestDto setId(String id) {
    this.id = id;
    return this;
  }

  public String getPolicyId() {
    return policyId;
  }

  public ApprovedAccessRequestDto setPolicyId(String policyId) {
    this.policyId = policyId;
    return this;
  }

  public String getRequestId() {
    return requestId;
  }

  public ApprovedAccessRequestDto setRequestId(String requestId) {
    this.requestId = requestId;
    return this;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public ApprovedAccessRequestDto setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public ApprovedAccessRequestDto setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }
}
