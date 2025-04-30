package org.cdpg.dx.acl.policy.dao.model;

import io.vertx.core.json.JsonObject;
import org.cdpg.dx.acl.policy.util.ItemType;

public class FetchPolicyDto {
  String policyId;
  String itemId;
  ItemType itemType;
  String expiryAt;
  String resourceServerUrl;
  JsonObject constraints;
  String status;
  String updatedAt;
  String createdAt;
  User consumer;
  User provider;

  public FetchPolicyDto() {}

  public FetchPolicyDto(FetchPolicyDto other) {
    this.policyId = other.getPolicyId();
    this.itemId = other.getItemId();
    this.itemType = other.getItemType();
    this.expiryAt = other.getExpiryAt();
    this.constraints = other.getConstraints();
    this.status = other.getStatus();
    this.updatedAt = other.getUpdatedAt();
    this.createdAt = other.getCreatedAt();
    this.consumer = other.getConsumer();
    this.provider = other.getProvider();
    this.resourceServerUrl = other.getResourceServerUrl();
  }

  public FetchPolicyDto(JsonObject jsonObject) {
    setPolicyId(jsonObject.getString("policyId"));
    setItemId(jsonObject.getString("itemId"));
    setExpiryAt(jsonObject.getString("expiryAt"));
    setItemType(ItemType.valueOf(jsonObject.getString("itemType")));
    setStatus(jsonObject.getString("status"));
    setCreatedAt(jsonObject.getString("createdAt"));
    setUpdatedAt(jsonObject.getString("updatedAt"));
    setResourceServerUrl(jsonObject.getString("resourceServerUrl"));
    setConsumer(
        new User()
            .setEmail(jsonObject.getString("consumerEmailId"))
            .setId(jsonObject.getString("consumerId"))
            .setName(
                new Name()
                    .setFirstName(jsonObject.getString("consumerFirstName"))
                    .setLastName(jsonObject.getString("consumerLastName"))));
    setProvider(
        new User()
            .setEmail(jsonObject.getString("ownerEmailId"))
            .setId(jsonObject.getString("ownerId"))
            .setName(
                new Name()
                    .setFirstName(jsonObject.getString("ownerFirstName"))
                    .setLastName(jsonObject.getString("ownerLastName"))));
    setConstraints(new JsonObject(jsonObject.getString("constraints")));
  }

  public String getResourceServerUrl() {
    return resourceServerUrl;
  }

  public FetchPolicyDto setResourceServerUrl(String resourceServerUrl) {
    this.resourceServerUrl = resourceServerUrl;
    return this;
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    return jsonObject;
  }

  public String getPolicyId() {
    return policyId;
  }

  public void setPolicyId(String policyId) {
    this.policyId = policyId;
  }

  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public ItemType getItemType() {
    return itemType;
  }

  public void setItemType(ItemType itemType) {
    this.itemType = itemType;
  }

  public String getExpiryAt() {
    return expiryAt;
  }

  public void setExpiryAt(String expiryAt) {
    this.expiryAt = expiryAt;
  }

  public JsonObject getConstraints() {
    return constraints;
  }

  public void setConstraints(JsonObject constraints) {
    this.constraints = constraints;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public User getConsumer() {
    return consumer;
  }

  public void setConsumer(User consumer) {
    this.consumer = consumer;
  }

  public User getProvider() {
    return provider;
  }

  public void setProvider(User provider) {
    this.provider = provider;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public static class User {
    String email;
    Name name;
    String id;

    public String getEmail() {
      return email;
    }

    public User setEmail(String email) {
      this.email = email;
      return this;
    }

    public Name getName() {
      return name;
    }

    public User setName(Name name) {
      this.name = name;
      return this;
    }

    public String getId() {
      return id;
    }

    public User setId(String id) {
      this.id = id;
      return this;
    }
  }

  public static class Name {
    String firstName;
    String lastName;

    public String getFirstName() {
      return firstName;
    }

    public Name setFirstName(String firstName) {
      this.firstName = firstName;
      return this;
    }

    public String getLastName() {
      return lastName;
    }

    public Name setLastName(String lastName) {
      this.lastName = lastName;
      return this;
    }
  }
}
