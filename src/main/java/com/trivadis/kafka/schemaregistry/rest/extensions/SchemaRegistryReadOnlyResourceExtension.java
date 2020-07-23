/*
 * Copyright 2019 Trivadis AG
 *
 * based on example code taken from: https://github.com/rayokota/schema-registry-mode-plugin
 */

package com.trivadis.kafka.schemaregistry.rest.extensions;

import com.trivadis.kafka.schemaregistry.rest.extensions.filter.PostRedirectFilter;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import io.confluent.kafka.schemaregistry.rest.extensions.SchemaRegistryResourceExtension;
import io.confluent.kafka.schemaregistry.storage.SchemaRegistry;
import com.trivadis.kafka.schemaregistry.rest.extensions.filter.ModeFilter;

import javax.ws.rs.core.Configurable;
import java.io.IOException;

public class SchemaRegistryReadOnlyResourceExtension
    implements SchemaRegistryResourceExtension {

    @Override
    public void register(
        Configurable<?> configurable,
        SchemaRegistryConfig schemaRegistryConfig,
        SchemaRegistry schemaRegistry
    ) {
        configurable.register(new PostRedirectFilter());
        configurable.register(new ModeFilter());
    }

    @Override
    public void close() throws IOException {
    }
}
