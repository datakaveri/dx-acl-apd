package org.cdpg.dx.acl.policy.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import java.util.Objects;

@DataObject(generateConverter = true)
public class Policy {
    private final String policyId;
    private final Role consumerEmailId;
    private final String itemId;
    private final String ownerId;
    private final String policyStatus;
    private final String expiryAt;
    private final JsonObject constraints;

    public Policy(JsonObject policyDetails) {
        this.policyId = policyDetails.getString("_id");
        this.consumerEmailId = Role.fromString(policyDetails.getString("user_emailid"));
        this.itemId = policyDetails.getString("item_id");
        this.ownerId = policyDetails.getString("owner_id");
        this.policyStatus = policyDetails.getString("status");
        this.expiryAt = policyDetails.getString("expiry_at");
        this.constraints = policyDetails.getJsonObject("constraints");

        /* Converts JsonObject to User class object or dataObject conversion [Deserialization] */
        PolicyConverter.fromJson(policyDetails, this);
    }

    /**
     * Converts Data object or User class object to json object [Serialization]
     *
     * @return JsonObject
     */
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        PolicyConverter.toJson(this, jsonObject);
        return jsonObject;
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
        return Objects.equals(policyId, policy.policyId) && consumerEmailId == policy.consumerEmailId &&
            Objects.equals(itemId, policy.itemId) && Objects.equals(ownerId, policy.ownerId) &&
            Objects.equals(policyStatus, policy.policyStatus) && Objects.equals(expiryAt, policy.expiryAt) &&
            Objects.equals(constraints, policy.constraints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(policyId, consumerEmailId, itemId, ownerId, policyStatus, expiryAt, constraints);
    }
}