package rundeck.interceptors

import grails.config.Config

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import java.security.Principal


class AuthInterceptor {

    boolean enabled;
    String rolesAttribute;
    String userNameHeader;
    String rolesHeader;

    boolean before() {
        if (grailsApplication.equals(null)) {
            throw new IllegalStateException("grailsApplication not found in context");
        }

        Config config = grailsApplication.getConfig();
        Object o = config.get("rundeck.security.authorization.preauthenticated.enabled");
        enabled = Boolean.parseBoolean(o.toString());
        rolesAttribute = (String) config.get("rundeck.security.authorization.preauthenticated.attributeName");
        rolesHeader = (String) config.get("rundeck.security.authorization.preauthenticated.userRolesHeader");
        userNameHeader = (String) config.get("rundeck.security.authorization.preauthenticated.userNameHeader");


        //authfilter implementation
        HttpServletRequest requestModified;
        if (userNameHeader != null) {
            final String forwardedUser = request.getHeader(userNameHeader);

            log.info("User header " + userNameHeader);
            log.info("User / UUID recieved " + forwardedUser);
            requestModified =
                    new HttpServletRequestWrapper((HttpServletRequest) request) {
                        @Override
                        public String getRemoteUser() {
                            return forwardedUser;
                        }

                        @Override
                        public Principal getUserPrincipal() {
                            Principal principle = new Principal() {
                                @Override
                                public String getName() {
                                    return forwardedUser;
                                }
                            };
                            return principle;
                        }
                    };
        }else {
            log.info("\n\n-----------------------------------request.remoteUser: " + request.remoteUser)
            requestModified = request;
        }
        if (rolesAttribute != null && rolesHeader != null) {
            // Get the roles sent by the proxy and add them onto the request as an attribute for
            // PreauthenticatedAttributeRoleSource
            final String forwardedRoles = request.getHeader(rolesHeader);
            requestModified.setAttribute(rolesAttribute, forwardedRoles);
            log.info("Roles header " + rolesHeader);
            log.info("Roles received " + forwardedRoles);
        }

    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
