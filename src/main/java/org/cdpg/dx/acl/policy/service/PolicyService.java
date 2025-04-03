package org.cdpg.dx.acl.policy.service;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.cdpg.dx.common.models.Response;
import org.cdpg.dx.common.models.User;

@VertxGen
@ProxyGen
public interface PolicyService {

  /* factory method */
  @GenIgnore
  static PolicyService createProxy(Vertx vertx, String address) {
    return new PolicyServiceVertxEBProxy(vertx, address);
  }

  /* service operation */
  // TODO: add different data object in param and the response (in future)
  // TODO: return policy object after inserting into the postgres not the response object
  // TODO: logic before and after policy creation will be added in the policy serviceimpl

  Future<JsonObject> createPolicy(JsonObject request, User user);

  Future<Response> deletePolicy(String policyId, User user);

  Future<JsonObject> getPolicy(User user);

  Future<JsonObject> verifyPolicy(JsonObject jsonArray);
}
