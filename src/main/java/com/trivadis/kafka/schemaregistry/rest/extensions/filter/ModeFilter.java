/*
 * Copyright 2017 Trivadis.
 *
 * based on example code taken from: https://github.com/rayokota/schema-registry-mode-plugin
*/

package com.trivadis.kafka.schemaregistry.rest.extensions.filter;

import io.confluent.kafka.schemaregistry.rest.exceptions.Errors;
import io.confluent.kafka.schemaregistry.rest.resources.ConfigResource;
import io.confluent.kafka.schemaregistry.rest.resources.SubjectVersionsResource;
import io.confluent.kafka.schemaregistry.rest.resources.SubjectsResource;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashSet;
import java.util.Set;


@Priority(Priorities.AUTHORIZATION)
public class ModeFilter implements ContainerRequestFilter {

    private static final Set<ResourceActionKey> subjectWriteActions =
        new HashSet<>();

    @Context
    ResourceInfo resourceInfo;

    @Context
    UriInfo uriInfo;

    @Context
    HttpServletRequest httpServletRequest;

    static {
        initializeSchemaRegistrySubjectWriteActions();
    }

    private static void initializeSchemaRegistrySubjectWriteActions() {
        subjectWriteActions.add(
            new ResourceActionKey(SubjectVersionsResource.class, "POST"));
        subjectWriteActions.add(
            new ResourceActionKey(SubjectVersionsResource.class, "DELETE"));
        subjectWriteActions.add(
            new ResourceActionKey(SubjectsResource.class, "DELETE"));
        subjectWriteActions.add(
            new ResourceActionKey(ConfigResource.class, "PUT"));
    }

    public ModeFilter() { }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        Class resource = resourceInfo.getResourceClass();
        String restMethod = requestContext.getMethod();

        ResourceActionKey key = new ResourceActionKey(resource, restMethod);
        if (subjectWriteActions.contains(key)) {
            throw Errors.operationNotPermittedException("This schema-registry instance is read-only (forced by the SchemaRegistryReadOnlyResourceExtension)");
        }
    }

    private static class ResourceActionKey {

        private final Class resourceClass;
        private final String restMethod;

        public ResourceActionKey(Class resourceClass, String restMethod) {
            this.resourceClass = resourceClass;
            this.restMethod = restMethod;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ResourceActionKey that = (ResourceActionKey) o;
            if (!resourceClass.equals(that.resourceClass)) {
                return false;
            }
            return restMethod.equals(that.restMethod);
        }

        @Override
        public int hashCode() {
            int result = resourceClass.hashCode();
            result = 31 * result + restMethod.hashCode();
            return result;
        }
    }
}
