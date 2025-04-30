package org.cdpg.dx.acl.policy.dao.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;


public class UserDto {
  String id;
  String emailId;
  String firstName;
  String lastName;
  String createdAt;
  String updatedAt;

  public UserDto() {}

  public UserDto(UserDto other) {
    this.createdAt = other.getCreatedAt();
    this.emailId = other.getEmailId();
    this.firstName = other.getFirstName();
    this.lastName = other.getLastName();
    this.updatedAt = other.getUpdatedAt();
    this.id = other.getId();
  }

//  public UserDto(JsonObject jsonObject) {
//    /* Converts JsonObject to PolicyDto class object or dataObject conversion [Deserialization] */
//    UserDtoConverter.fromJson(jsonObject, this);
//  }

  /**
   * Converts Data object or Policy class object to json object [Serialization]
   *
   * @return JsonObject
   */
//  public JsonObject toJson() {
//    JsonObject jsonObject = new JsonObject();
//    UserDtoConverter.toJson(this, jsonObject);
//    return jsonObject;
//  }

  public String getId() {
    return id;
  }

  public UserDto setId(String id) {
    this.id = id;
    return this;
  }

  public String getEmailId() {
    return emailId;
  }

  public UserDto setEmailId(String emailId) {
    this.emailId = emailId;
    return this;
  }

  public String getFirstName() {
    return firstName;
  }

  public UserDto setFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public String getLastName() {
    return lastName;
  }

  public UserDto setLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public UserDto setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public UserDto setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }
}
