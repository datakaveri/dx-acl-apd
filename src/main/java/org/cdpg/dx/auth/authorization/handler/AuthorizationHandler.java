package org.cdpg.dx.auth.authorization.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.cdpg.dx.common.models.DxRole;
import org.cdpg.dx.common.models.JwtData;
import org.cdpg.dx.util.RoutingContextHelper;
import org.cdpg.dx.auth.authorization.exception.AuthorizationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Set;

/**
 * Handler for performing role-based authorization.
 */
public class AuthorizationHandler implements Handler<RoutingContext> {
    private static final Logger LOGGER = LogManager.getLogger(AuthorizationHandler.class);

    @Override
    public void handle(RoutingContext context) {
        context.next();
    }

    /**
     * Assigns required roles for an API endpoint and returns a handler for authorization.
     *
     * @param rolesForApi Allowed roles for the endpoint
     * @return Handler that enforces role-based authorization
     */
    public Handler<RoutingContext> setUserRolesForEndpoint(DxRole... rolesForApi) {
        Set<DxRole> allowedRoles = Set.of(rolesForApi); // Optimized for performance
        return context -> authorizeUser(context, allowedRoles);
    }

    /**
     * Validates the user's role against allowed roles for an endpoint.
     *
     * @param context     Routing context
     * @param allowedRoles Allowed roles for the API
     */
    private void authorizeUser(RoutingContext context, Set<DxRole> allowedRoles) {
        Optional<JwtData> jwtDataOpt = RoutingContextHelper.getJwtData(context);

        if (jwtDataOpt.isEmpty()) {
            logAndFail(context, "JWT data missing in request context", "JWT data not found");
            return;
        }

        JwtData jwtData = jwtDataOpt.get();
        Optional<DxRole> userRoleOpt = DxRole.fromString(jwtData.role());

        if (userRoleOpt.isEmpty()) {
            logAndFail(context, "Invalid or missing role in JWT", "Invalid or missing user role");
            return;
        }

        DxRole userRole = userRoleOpt.get();
        if (!allowedRoles.contains(userRole)) {
            logAndFail(context, "Unauthorized access attempt: Role not allowed", "Access denied to endpoint");
            return;
        }

        context.next();
    }

    /**
     * Logs an unauthorized access attempt and triggers `context.fail()`.
     *
     * @param context   Routing context
     * @param logMessage Log message for debugging
     * @param errorMessage Error message for the client
     */
    private void logAndFail(RoutingContext context, String logMessage, String errorMessage) {
        LOGGER.warn(logMessage);
        context.fail(new AuthorizationException(errorMessage));
    }


}
