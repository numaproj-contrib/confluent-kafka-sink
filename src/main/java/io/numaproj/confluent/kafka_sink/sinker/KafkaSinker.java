package io.numaproj.confluent.kafka_sink.sinker;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.numaproj.confluent.kafka_sink.config.KafkaSinkerConfig;
import io.numaproj.numaflow.sinker.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
public class KafkaSinker extends Sinker implements DisposableBean {

    private final String topicName;
    private final Schema schema;
    private final KafkaProducer<String, GenericRecord> producer;
    private final SchemaRegistryClient schemaRegistryClient;

    @Autowired
    public KafkaSinker(
            KafkaSinkerConfig config,
            KafkaProducer<String, GenericRecord> producer,
            SchemaRegistryClient schemaRegistryClient) {
        // TODO - the instance variables are messy here, because they are dependent on each other. Clean it up to make more modular.
        this.topicName = config.getTopicName();
        this.producer = producer;
        this.schemaRegistryClient = schemaRegistryClient;
        this.schema = this.getSchemaForTopic(this.topicName);
        if (this.schema == null) {
            throw new RuntimeException("Failed to retrieve schema for topic " + this.topicName);
        }
        log.info("KafkaSinker initialized with topic name: {}, schema: {}",
                config.getTopicName(), this.schema);
    }

    @Override
    public ResponseList processMessages(DatumIterator datumIterator) {
        ResponseList.ResponseListBuilder responseListBuilder = ResponseList.newBuilder();
        while (true) {
            Datum datum;
            try {
                datum = datumIterator.next();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                continue;
            }
            // null means the iterator is closed, so we break the loop
            if (datum == null) {
                break;
            }
            try {
                String key = UUID.randomUUID().toString();
                String msg = new String(datum.getValue());
                // writing original message to kafka
                log.info("Sending message to kafka: key - {}, value - {}", key, msg);
                String jsonData = new String(datum.getValue());
                DatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);
                Decoder decoder = DecoderFactory.get().jsonDecoder(schema, jsonData);
                GenericRecord result = reader.read(null, decoder);
                ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(this.topicName, key, result);
                // TODO - this is an async call, should be sync.
                this.producer.send(record);
                responseListBuilder.addResponse(Response.responseOK(datum.getId()));
            } catch (Exception e) {
                log.error("Failed to process message - {}", e.getMessage());
                responseListBuilder.addResponse(Response.responseFailure(
                        datum.getId(),
                        e.getMessage()));
            }
        }
        return responseListBuilder.build();
    }

    @Override
    public void destroy() {
        log.info("send shutdown signal");
        log.info("kafka producer closed");
    }

    private Schema getSchemaForTopic(String topicName) {
        try {
            // Retrieve the latest schema metadata for the {topicName}-value
            SchemaMetadata schemaMetadata = schemaRegistryClient.getLatestSchemaMetadata(topicName + "-value");
            // TODO - support other schema types. JSON, Protobuf etc.
            if (!Objects.equals(schemaMetadata.getSchemaType(), "AVRO")) {
                throw new RuntimeException("Schema type is not AVRO for topic {}." + topicName);
            }
            AvroSchema avroSchema = (AvroSchema) schemaRegistryClient.getSchemaById(schemaMetadata.getId());
            log.info("Retrieved schema for topic {}: {}", topicName, avroSchema.rawSchema());
            return avroSchema.rawSchema();
        } catch (IOException | RestClientException e) {
            // If there's any problem in fetching the schema or if the schema does not exist,
            // print the stack trace and return null.
            System.err.println("Failed to retrieve schema for topic " + topicName + ". " + e.getMessage());
            return null;
        }
    }
}
