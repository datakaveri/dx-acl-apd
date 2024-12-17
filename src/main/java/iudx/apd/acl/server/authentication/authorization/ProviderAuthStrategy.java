package iudx.apd.acl.server.authentication.authorization;


import static iudx.apd.acl.server.authentication.model.Method.*;

import iudx.apd.acl.server.common.Api;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProviderAuthStrategy implements AuthorizationStrategy {
  private static volatile ProviderAuthStrategy instance;
  Map<String, List<AuthorizationRequest>> providerAuthorizationRules = new HashMap<>();

  private ProviderAuthStrategy(Api apis) {
    buildPermissions(apis);
  }

  public static ProviderAuthStrategy getInstance(Api apis) {
    if (instance == null) {
      synchronized (ProviderAuthStrategy.class) {
        if (instance == null) {
          instance = new ProviderAuthStrategy(apis);
        }
      }
    }
    return instance;
  }

  private void buildPermissions(Api apis) {
    // api access list/rules
    List<AuthorizationRequest> apiAccessList = new ArrayList<>();
    apiAccessList.add(new AuthorizationRequest(POST, apis.getPoliciesUrl()));
    apiAccessList.add(new AuthorizationRequest(GET, apis.getPoliciesUrl()));
    apiAccessList.add(new AuthorizationRequest(DELETE, apis.getPoliciesUrl()));

    apiAccessList.add(new AuthorizationRequest(GET, apis.getRequestPoliciesUrl()));
    apiAccessList.add(new AuthorizationRequest(PUT, apis.getRequestPoliciesUrl()));
    providerAuthorizationRules.put("api", apiAccessList);
  }

  @Override
  public boolean isAuthorized(AuthorizationRequest authRequest) {
    return providerAuthorizationRules.get("api").contains(authRequest);
  }
}
