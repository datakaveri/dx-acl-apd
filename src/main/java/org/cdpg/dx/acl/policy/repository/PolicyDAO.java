package org.cdpg.dx.acl.policy.repository;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import iudx.apd.acl.server.policy.PolicyService;
import iudx.apd.acl.server.policy.PolicyServiceVertxEBProxy;
import org.cdpg.dx.acl.policy.model.Policy;
import org.cdpg.dx.acl.policy.model.PolicyUpdateDTO;

import java.util.UUID;


public interface PolicyDAO {
    Future<Policy> create(Policy policy);

    Future<Policy> getById(String policyId);

    Future<Boolean> update(String id, PolicyUpdateDTO updateDTO);

    Future<Boolean> delete(String policyId); // Return boolean for clarity
}