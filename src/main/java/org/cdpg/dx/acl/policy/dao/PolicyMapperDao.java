package org.cdpg.dx.acl.policy.dao;

import io.vertx.core.Future;
import java.util.List;
import org.cdpg.dx.acl.policy.dao.model.FetchPolicyDto;
import org.cdpg.dx.acl.policy.dao.model.PolicyResourceUrlDto;
import org.cdpg.dx.common.models.User;

/**
 * Handles complex sql queries or database joins
 */
public interface PolicyMapperDao {
  /**
   * Policies related to consumer or provider is fetched.
   * @param consumer : consumer user
   * @return DB query result as FetchPolicyDto
   */
  public Future<List<FetchPolicyDto>> getPolicyForConsumerQuery(User consumer);
  /**
   * Policies related to consumer or provider is fetched.
   * @param provider :   provider user
   * @return DB query result as FetchPolicyDto
   */
  public Future<List<FetchPolicyDto>> getPolicyForProviderQuery(User provider);

  /**
   * Fetches the policy information of a policy to be deleted based on its ID. Performs an inner join on the resource entity wrt to itemId
   * @param policyId ID of the policy whose information is to be fetched
   * @return Policy information like ownerId, status and resource information like Resource server url as a data transfer object
   */
  public Future<PolicyResourceUrlDto> checkIfPolicyIsPresentQuery(String policyId);

}
