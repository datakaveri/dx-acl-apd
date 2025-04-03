package org.cdpg.dx.acl.policy;

import static iudx.apd.acl.server.common.Constants.POLICY_DAO_ADDRESS;
import static iudx.apd.acl.server.common.Constants.POLICY_SERVICE_ADDRESS;
import static org.cdpg.dx.acl.policy.common.Constants.PG_SERVICE_ADDRESS;
import static org.cdpg.dx.util.Constants.APD_URL;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.serviceproxy.ServiceBinder;
import iudx.apd.acl.server.policy.*;
import org.cdpg.dx.acl.policy.model.Policy;
import org.cdpg.dx.acl.policy.repository.PolicyDAO;
import org.cdpg.dx.acl.policy.repository.PolicyDAOImpl;
import org.cdpg.dx.acl.policy.service.*;
import org.cdpg.dx.acl.policy.service.PolicyService;
import org.cdpg.dx.acl.policy.service.PolicyServiceImpl;
import org.cdpg.dx.catalogue.client.CatalogueWebClient;
import org.cdpg.dx.catalogue.service.CatalogueServiceImpl;
import org.cdpg.dx.database.postgres.service.PostgresService;

public class PolicyVerticle extends AbstractVerticle {
  private PostgresService postgresqlService;
  private PolicyServiceImpl policyService;
  private DeletePolicy deletePolicy;
  private CreatePolicy createPolicy;
  private VerifyPolicy verifyPolicy;
  private GetPolicy getPolicy;
  private CatalogueServiceImpl catalogueServiceImpl;
  private CatalogueWebClient catalogueWebClient;
  public WebClient client;
  public PolicyDAO policyDAO;
  public PostgresService postgresService;
  @Override
  public void start() {
    WebClientOptions clientOptions =
        new WebClientOptions().setSsl(true).setVerifyHost(false).setTrustAll(true);
    client = WebClient.create(vertx, clientOptions);
//    postgresqlService = PostgresService.createProxy(vertx, POSTGRES_SERVICE_ADDRESS);
    postgresService = PostgresService.createProxy(vertx, PG_SERVICE_ADDRESS);
//    deletePolicy = new DeletePolicy(postgresqlService);
//    getPolicy = new GetPolicy(postgresqlService);
    catalogueWebClient = new CatalogueWebClient(config(),client);
    catalogueServiceImpl = new CatalogueServiceImpl(config().getString(APD_URL), catalogueWebClient);
//    createPolicy = new CreatePolicy(postgresqlService, catalogueServiceImpl);
//    verifyPolicy = new VerifyPolicy(postgresqlService, catalogueServiceImpl);
//    policyService =
//        new PolicyServiceImpl(deletePolicy, createPolicy, getPolicy, verifyPolicy, config());
    policyDAO = new PolicyDAOImpl(postgresqlService);
//    new ServiceBinder(vertx)
//        .setAddress(POLICY_SERVICE_ADDRESS)
//        .register(Policy.class, policyService);

  }
}
