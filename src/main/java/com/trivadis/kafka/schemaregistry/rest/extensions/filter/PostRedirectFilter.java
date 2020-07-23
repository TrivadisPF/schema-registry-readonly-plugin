package com.trivadis.kafka.schemaregistry.rest.extensions.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;

@Provider
@PreMatching
public class PostRedirectFilter implements ContainerRequestFilter {

    private static final String redirectPostReqSuffix = "/versions";

    @Context
    UriInfo uriInfo;

    public PostRedirectFilter() { }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String restMethod = requestContext.getMethod();
        String uri = uriInfo.getPath();

        if (restMethod.equals("POST") && uri.endsWith(redirectPostReqSuffix)) {
            final String uriSubstring = "/" + uri.substring(0, uri.length() - redirectPostReqSuffix.length());
            requestContext.setRequestUri(URI.create(uriSubstring));
        }
    }
}
