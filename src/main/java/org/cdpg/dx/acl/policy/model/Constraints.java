package org.cdpg.dx.acl.policy.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;
import java.util.List;
//Removing the class because the constraint structure should not be defined in ACL-APD
//@DataObject
//@JsonGen(publicConverter = false)
//public class Constraints {
//  private List<String> access;
//
//  public Constraints(){}
//  public Constraints(Constraints other){
//    this.access = other.getAccess();
//  }
//  public Constraints(JsonObject jsonObject){
//    ConstraintsConverter.fromJson(jsonObject, this);
//  }
//  public JsonObject toJson() {
//    JsonObject jsonObject = new JsonObject();
//    ConstraintsConverter.toJson(this, jsonObject);
//    return jsonObject;
//  }
//  public List<String> getAccess() { return this.access; }
//  public void setAccess(List<String> value) { this.access = value; }
//
//
//}