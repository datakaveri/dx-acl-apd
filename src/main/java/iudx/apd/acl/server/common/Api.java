package iudx.apd.acl.server.common;

import static iudx.apd.acl.server.apiserver.util.Constants.POLICIES_API;
import static iudx.apd.acl.server.apiserver.util.Constants.REQUEST_POLICY_API;
import static iudx.apd.acl.server.apiserver.util.Constants.VERIFY_POLICY_API;

public class Api {

  private static volatile Api apiInstance;
  private final String dxApiBasePath;
  private StringBuilder policiesUrl;
  private StringBuilder verifyUrl;
  private StringBuilder requestPoliciesUrl;

  private Api(String dxApiBasePath) {
    this.dxApiBasePath = dxApiBasePath;
    buildPaths();
  }

  public static Api getInstance(String dxApiBasePath) {
    if (apiInstance == null) {
      synchronized (Api.class) {
        if (apiInstance == null) {
          apiInstance = new Api(dxApiBasePath);
        }
      }
    }
    return apiInstance;
  }

  private void buildPaths() {
    policiesUrl = new StringBuilder(dxApiBasePath).append(POLICIES_API);
    requestPoliciesUrl = new StringBuilder(dxApiBasePath).append(REQUEST_POLICY_API);
    verifyUrl = new StringBuilder(VERIFY_POLICY_API);
  }

  public String getPoliciesUrl() {
    return policiesUrl.toString();
  }

  public String getRequestPoliciesUrl() {
    return requestPoliciesUrl.toString();
  }

  public String getVerifyUrl() {
    return verifyUrl.toString();
  }

  @Override
  public String toString() {
    return "Api{" +
        "dxApiBasePath='" + dxApiBasePath + '\'' +
        ", policiesUrl=" + policiesUrl +
        ", verifyUrl=" + verifyUrl +
        ", requestPoliciesUrl=" + requestPoliciesUrl +
        '}';
  }
}
