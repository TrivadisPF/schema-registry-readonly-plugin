# Resource Extension Plugin for setting Confluent Schema Registry to read-only

[download link](https://github.com/TrivadisPF/schema-registry-readonly-plugin/releases/download/1.0.0/schema-registry-readonly-plugin-1.0.0.jar)

This project holds a resource extension for the Confluent Schema Registry that allows to set a schema registry instance to read-only.


It is baed on the idea presented in the blog article [Fun with Confluent Schema Registry Extensions](https://yokota.blog/2019/01/14/fun-with-confluent-schema-registry-extensions/) by Robert Yokota. 
Please note that Subject mode support mentioned in the article is now part of Confluent Schema Registry (version 5.2 and later).

## Subject Mode Support of the Schema Registry

Since version 5.2 the Schema Registry supports setting the Schema-Registry or a certain subject to either READONLY or READWRITE, by using the mode resource. 

This functionality has to be enabled by setting following parameter in the schema-`registry.properties` file.

```
mode.mutability=true
```

Now you can use the `mode` resource:

### Setting the whole schema-registry to read only

To set the whole Schema Registry instance to read-only, perform

```
curl -X PUT -H "Content-Type: application/json"   http://<schema-registry-url>/mode --data '{"mode": "READWRITE"}'
```

if you now try to register a schema you should see an error:

```
$ curl  -XPOST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data '{
  "schema": "{\"type\":\"record\",\"name\":\"Payment\",\"namespace\":\"com.trivadis.examples.clients.simpleavro\",\"fields\":[{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"temp\",\"type\":\"double\"}]}"
}' http://172.16.252.11:8081/subjects/test-value/versions
{"error_code":42205,"message":"Subject test-value is in read-only mode"}
```

### Setting a specific subject to read only

To set the `test_value` subject to read only:

```
curl -X PUT -H "Content-Type: application/json"   http://<schema-registry-url>/mode/test-value --data '{"mode": "READWRITE"}'
```

Use the mode `READWRITE` to set it back to writeable. 

### What happens with a mode setting when a schema registry instance is restarted?

Changing the mode of a subject or the schema registry is persisted into the `_schemas` topic. Therefore it will survive a restart of the schema registry. 

However there is currently no way to set a schema registry to "READONLY" without calling the `mode` REST API once. 


## Resource extensions Plugin to force a read-only Schema Registry instance

To force a schema registry instance to read-only upon startup, the  `SchemaRegistryReadOnlyResourceExtension` resource extension plugin can be used.

To use the resource extension, first copy the [extension jar](https://github.com/TrivadisPF/schema-registry-readonly-plugin/releases/download/1.0.0/schema-registry-readonly-plugin-1.0.0.jar) to ${CONFLUENT_HOME}/share/java/schema-registry. 

Next add the following to `${CONFLUENT_HOME}/etc/schema-registry/schema-registry.properties`:

```
resource.extension.class=com.trivadis.kafka.schemaregistry.rest.extensions.SchemaRegistryReadOnlyResourceExtension
```

Now after we start the Schema Registry, trying to register a subject will throw an exception, similar as above when using the Mode support of the schema registry.

```
$ curl  -XPOST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data '{
  "schema": "{\"type\":\"record\",\"name\":\"Payment\",\"namespace\":\"com.trivadis.examples.clients.simpleavro\",\"fields\":[{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"temp\",\"type\":\"double\"}]}"
}' http://172.16.252.11:8081/subjects/test-value/versions
{"error_code":42205,"message":"This schema-registry instance is read-only (forced by the SchemaRegistryReadOnlyResourceExtension)"}
```

The only difference is the error message itself, telling that the whole instance is in read-only mode.

