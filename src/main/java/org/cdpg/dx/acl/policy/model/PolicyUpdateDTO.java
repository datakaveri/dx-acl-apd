
package org.cdpg.dx.acl.policy.model;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import java.util.Optional;

import io.vertx.core.json.JsonObject;
import org.cdpg.dx.acl.policy.util.Constants;
import java.util.Map;
import java.util.HashMap;

//public record PolicyUpdateDTO(
//        Optional<String> status,
//        Optional<String> expiryAt,
//        Optional<JsonObject> constraints
//) {
//    public Map<String, Object> toNonEmptyFieldsMap() {
//        Map<String, Object> map = new HashMap<>();
//        status.ifPresent(value -> map.put(Constants.STATUS, value));
//        expiryAt.ifPresent(value -> map.put(Constants.EXPIRY_AT, value));
//        constraints.ifPresent(value ->  map.put(Constants.CONSTRAINTS, value));
//        return map;
//    }
@DataObject
@JsonGen(publicConverter = false)
public class PolicyUpdateDTO {
    String status;
    String expiryAt;
    JsonObject constraints;
    public PolicyUpdateDTO(
        Optional<String> status,
        Optional<String> expiryAt,
        Optional<JsonObject> constraints
    ){
        this.status  = String.valueOf(status);
        this.expiryAt = String.valueOf(expiryAt);
        this.constraints = JsonObject.mapFrom(constraints);
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        PolicyUpdateDTOConverter.toJson(this, jsonObject);
        return jsonObject;
    }
    public PolicyUpdateDTO(JsonObject jsonObject){
        PolicyUpdateDTOConverter.fromJson(jsonObject, this);
    }
        public Map<String, Object> toNonEmptyFieldsMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.STATUS, status);
        map.put(Constants.EXPIRY_AT, expiryAt);
        map.put(Constants.CONSTRAINTS, constraints);
        return map;
    }
}

