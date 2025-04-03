package org.cdpg.dx.acl.policy.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import iudx.apd.acl.server.policy.CreatePolicy;
import iudx.apd.acl.server.policy.DeletePolicy;
import iudx.apd.acl.server.policy.GetPolicy;
import iudx.apd.acl.server.policy.VerifyPolicy;
import java.util.UUID;
import org.cdpg.dx.acl.policy.util.Constants;
import org.cdpg.dx.common.models.Response;
import org.cdpg.dx.common.models.ResponseUrn;
import org.cdpg.dx.common.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolicyServiceImpl implements PolicyService {
  private static final Logger LOG = LoggerFactory.getLogger(PolicyServiceImpl.class);
  private final DeletePolicy deletePolicy;
  private final GetPolicy getPolicy;
  private final CreatePolicy createPolicy;
  private final VerifyPolicy verifyPolicy;

  JsonObject config;

  public PolicyServiceImpl(
      DeletePolicy deletePolicy,
      CreatePolicy createPolicy,
      GetPolicy getPolicy,
      VerifyPolicy verifyPolicy,
      JsonObject config) {
    this.deletePolicy = deletePolicy;
    this.createPolicy = createPolicy;
    this.getPolicy = getPolicy;
    this.verifyPolicy = verifyPolicy;
    this.config = config;
  }

  @Override
  public Future<JsonObject> createPolicy(JsonObject request, User user) {
    Promise<JsonObject> promise = Promise.promise();
   return null;
  }

  @Override
  public Future<Response> deletePolicy(String policyId, User user) {
  return null;
  }

  @Override
  public Future<JsonObject> getPolicy(User user) {
return null;
  }

  @Override
  public Future<JsonObject> verifyPolicy(JsonObject jsonArray) {
    Promise<JsonObject> promise = Promise.promise();

    this.verifyPolicy
        .initiateVerifyPolicy(jsonArray)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                promise.complete(handler.result());
              } else {
                LOG.error("Failed to verify policy");
                promise.fail(handler.cause().getMessage());
              }
            });
    return promise.future();
  }
}
