package org.cdpg.dx.acl.policy.dao;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import org.cdpg.dx.acl.policy.dao.model.PolicyDto;
import org.cdpg.dx.acl.policy.util.Status;


/** Handles sql queries without joins like insert query, update query to policy table */
public interface PolicyDao {
  /**
   * Gets active consumer policy for a for an item regardless of the constraint
   *
   * @param itemId resource for which a policy is created
   * @param consumerEmailId email ID of the consumer for whom the policy is created
   * @return details of the policy with active status + not expired, as a Future data object
   */
  public Future<PolicyDto> getActiveConsumerPolicyFromDB(String itemId, String consumerEmailId);

  /**
   * Fetches policy details of a policy that is not expired [expiryAt > NOW()]. It fetches the
   * policy based on resource, provider and consumer information
   *
   * @param itemId ID of the resource
   * @param ownerId ID of the owner of the resource
   * @param status of the policy
   * @param consumerEmailId email ID of the user for whom the policy is created
   * @return Future of Policy Data object
   */
  public Future<PolicyDto> checkExistingPolicyFromDb(
      String itemId, String ownerId, Status status, String consumerEmailId);

  /**
   * Creates an Active policy in the database by adding resource info, consumer info, provider info
   * and the constraints to access the policy
   *
   * @param consumerEmailId email ID of the user for whom the policy is created
   * @param itemId or resource ID
   * @param ownerId Provider ID or ID of the owner of the resource
   * @param expiryAt The time at which the policy expires
   * @param constraints to access the resource
   * @return Future of policy data object
   */
  public Future<PolicyDto> createPolicyInDb(
      String consumerEmailId,
      String itemId,
      String ownerId,
      LocalDateTime expiryAt,
      JsonObject constraints);

  /**
   * Performs soft delete on active policy by setting the `status` of the policy to DELETED where
   * expiryAt > NOW()
   *
   * @param policyId to be deleted
   * @return details of the policy that is deleted as a data object
   */
  public Future<PolicyDto> deletePolicyInDb(String policyId);
}
