# Kafka Integration Guide

This document explains how Apache Kafka is used in the Image Processing System and how to run it.

---

## Overview

Kafka decouples image uploading from image processing. When a user uploads an image, the `upload-service` publishes an event to Kafka and immediately returns a response. The `processing-service` then consumes that event asynchronously and performs the actual image processing in the background.

```
User → upload-service (Producer) → [image-uploaded topic] → processing-service (Consumer)
```

---

## Topics

Three topics are defined in `upload-service/.../kafka/ImageTopicConfig.java`:

| Topic | Publisher | Consumer | Purpose |
|---|---|---|---|
| `image-uploaded` | upload-service | processing-service | Triggers processing when an image is uploaded |
| `image-completed` | _(defined, unused)_ | _(none)_ | Reserved for completion events |
| `image-failed` | _(defined, unused)_ | _(none)_ | Reserved for failure events |

Topics are auto-created by Spring Kafka on startup via `@Bean NewTopic` definitions.

---

## Message Flow

1. User sends a `POST /api/images/upload` request to the **gateway** (port `8081`), which routes it to the **upload-service** (port `8082`).
2. The upload-service saves the image file to disk and records metadata in PostgreSQL.
3. `ImageEventProducer` publishes an `ImageUpload` object to the `image-uploaded` topic, using the image ID as the message key.
4. The **processing-service** (port `8083`) consumer group `processing-service-group` picks up the message.
5. `ImageEventConsumer` calls `ImageProcessingService.process()`, which:
   - Sets the image status to `PROCESSING` in the database
   - Reads the original image from disk
   - Resizes the image if it exceeds 1920px wide (aspect ratio preserved)
   - Applies a diagonal semi-transparent `PROCESSED` watermark
   - Saves the result to `C:\image-storage\processed\`
   - Updates the image record in the database with status `COMPLETED`, dimensions, and the processed file path
   - On failure: updates status to `FAILED` with the error message

---

## Producer

**Service:** `upload-service`
**Class:** `com.imageprocessing.upload_service.kafka.ImageEventProducer`

```java
kafkaTemplate.send("image-uploaded", data.getId().toString(), data);
```

- Serializes `ImageUpload` as JSON using `JsonSerializer`
- Message key is the image ID (ensures ordering for the same image)

**Kafka producer config (`upload-service/src/main/resources/application.properties`):**

```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

---

## Consumer

**Service:** `processing-service`
**Class:** `com.imageprocessing.processing_service.kafka.ImageEventConsumer`

```java
@KafkaListener(topics = "image-uploaded", groupId = "processing-service-group")
private void onImageUploaded(ImageUpload event) { ... }
```

- Deserializes JSON into `ImageUpload` using `JsonDeserializer`
- Consumer group: `processing-service-group` (offsets are committed per group)
- `auto-offset-reset=earliest` — if no offset exists yet, start from the beginning of the topic

**Kafka consumer config (`processing-service/src/main/resources/application.properties`):**

```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=processing-service-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
spring.kafka.consumer.properties.spring.json.use.type.headers=false
spring.kafka.consumer.properties.spring.json.value.default.type=com.imageprocessing.processing_service.model.ImageUpload
```

---

## How to Run

### Prerequisites

- Java 17+
- Maven
- PostgreSQL running on `localhost:5432` with a database named `image` and  create the `image_upload` table using the provided SQL script
- Apache Kafka running on `localhost:9092`

### 1. Start Kafka

**Using JVM Based Apache Kafka Docker Image:**

Get the Docker image:

```bash
docker pull apache/kafka:4.2.0
```

Start the Kafka Docker container:

```bash
docker run -p 9092:9092 apache/kafka:4.2.0
```

### 2. Start the Services

Open three terminals and run each service:

```bash
# Terminal 1 — Gateway (port 8081)
cd gateway
mvn spring-boot:run

# Terminal 2 — Upload Service / Kafka Producer (port 8082)
cd upload-service
mvn spring-boot:run

# Terminal 3 — Processing Service / Kafka Consumer (port 8083)
cd processing-service
mvn spring-boot:run
```

The topics (`image-uploaded`, `image-completed`, `image-failed`) are created automatically when the upload-service starts.



**Upload an image to trigger the full flow:**

```bash
curl -F "file=@/path/to/image.jpg" http://localhost:8081/api/images/upload
```

Then check the console consumer — you should see a JSON message appear. The processing-service logs will show the image being processed.

---

## Troubleshooting

| Symptom | Likely Cause | Fix |
|---|---|---|
| `Connection refused` on port 9092 | Kafka is not running | Start Kafka first |
| Messages published but not consumed | Consumer group offset issue | Set `auto-offset-reset=earliest` or reset the offset |
| `ClassCastException` during deserialization | Type header mismatch | Ensure `spring.json.use.type.headers=false` is set in the consumer config |
| Image status stuck at `PROCESSING` | processing-service crashed mid-run | Restart processing-service; it will re-consume from the committed offset |

---

## Known Issues

- The `image-completed` and `image-failed` topics are defined but nothing publishes to them. Failed processing is only recorded in the database; there is no Kafka event emitted for downstream consumers to react to.
- There is a typo in `processing-service/application.properties` — `spring.json.use.type.headers=falsep` should be `false`. This does not affect runtime because the property value is ultimately parsed as a boolean and Spring may tolerate the trailing character, but it should be corrected.
