package org.cdpg.dx.acl.policy.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;
import java.util.Objects;

@DataObject
@JsonGen
public class Policy {
  private String policyId;
  private Role consumerEmailId;
  private String itemId;
  private String ownerId;
  private String policyStatus;
  private String expiryAt;
  private JsonObject constraints;

  public Policy() {}
  public Policy(Policy other){
    this.policyId = other.getPolicyId();
    this.consumerEmailId = other.getConsumerEmailId();
    this.itemId = other.getItemId();
    this.ownerId = other.getOwnerId();
    this.policyStatus = other.getPolicyStatus();
    this.expiryAt = other.getExpiryAt();
    this.constraints = other.getConstraints();
  }

  public Policy(JsonObject jsonObject) {
    /* Converts JsonObject to Policy class object or dataObject conversion [Deserialization] */
    PolicyConverter.fromJson(jsonObject, this);
  }

  /**
   * Converts Data object or Policy class object to json object [Serialization]
   *
   * @return JsonObject
   */
  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    PolicyConverter.toJson(this, jsonObject);
    return jsonObject;
  }

  public void setPolicyId(String policyId) {
    this.policyId = policyId;
  }

  public void setConsumerEmailId(Role consumerEmailId) {
    this.consumerEmailId = consumerEmailId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public void setPolicyStatus(String policyStatus) {
    this.policyStatus = policyStatus;
  }

  public void setExpiryAt(String expiryAt) {
    this.expiryAt = expiryAt;
  }

  public void setConstraints(JsonObject constraints) {
    this.constraints = constraints;
  }

  public String getPolicyId() {
    return policyId;
  }

  public Role getConsumerEmailId() {
    return consumerEmailId;
  }

  public String getItemId() {
    return itemId;
  }

  public String getOwnerId() {
    return ownerId;
  }

  public String getPolicyStatus() {
    return policyStatus;
  }

  public String getExpiryAt() {
    return expiryAt;
  }

  public JsonObject getConstraints() {
    return constraints;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Policy policy)) return false;
    return Objects.equals(policyId, policy.policyId)
        && consumerEmailId == policy.consumerEmailId
        && Objects.equals(itemId, policy.itemId)
        && Objects.equals(ownerId, policy.ownerId)
        && Objects.equals(policyStatus, policy.policyStatus)
        && Objects.equals(expiryAt, policy.expiryAt)
        && Objects.equals(constraints, policy.constraints);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        policyId, consumerEmailId, itemId, ownerId, policyStatus, expiryAt, constraints);
  }
}
