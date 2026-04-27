package com.ankur.loganalyzer.client;

import com.ankur.loganalyzer.model.LogSource;
import com.ankur.loganalyzer.repository.LogSourceRepository;
import com.ankur.loganalyzer.service.LogIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.kafka.clients.consumer.ConsumerRecords;
//import org.apache.kafka.clients.consumer.KafkaConsumer;
//import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.common.serialization.StringDeserializer;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "kafka", name = "enabled", havingValue = "true", matchIfMissing = false)
public class KafkaConsumerService {

    private final LogIngestionService logIngestionService;
    private final LogSourceRepository logSourceRepository;

    @Value("${kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /*
    @KafkaListener(
            topics = "${kafka.topic.logs:logs}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeLogs(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        if (records.isEmpty()) {
            ack.acknowledge();
            return;
        }

        log.info("Received {} log records from Kafka", records.size());

        List<String> logLines = new ArrayList<>();
        for (ConsumerRecord<String, String> record : records) {
            logLines.add(record.value());
        }

        try {
            LogSource source = getOrCreateKafkaSource();
            LogIngestionService.IngestionResult result = logIngestionService.ingestRawLines(logLines, source);

            log.info("Kafka batch ingestion complete: total={}, parsed={}, failed={}",
                    result.totalLines(), result.parsedSuccessfully(), result.parseFailures());

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process Kafka batch", e);
            // Don't acknowledge - will be reprocessed
        }
    }
    */

    /*
    public List<String> fetchFromTopic(String topic, Integer partition, Long offset, int limit) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "logsphere-fetch-" + System.currentTimeMillis());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, limit);

        List<String> logLines = new ArrayList<>();

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            if (partition != null) {
                TopicPartition tp = new TopicPartition(topic, partition);
                consumer.assign(Collections.singletonList(tp));

                if (offset != null) {
                    consumer.seek(tp, offset);
                } else {
                    consumer.seekToBeginning(Collections.singletonList(tp));
                }
            } else {
                consumer.subscribe(Collections.singletonList(topic));
            }

            int pollAttempts = 0;
            int maxPollAttempts = 5;

            while (logLines.size() < limit && pollAttempts < maxPollAttempts) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));
                pollAttempts++;

                for (ConsumerRecord<String, String> record : records) {
                    if (logLines.size() >= limit) break;
                    logLines.add(record.value());
                }

                if (records.isEmpty()) {
                    break;
                }
            }

            log.info("Fetched {} log records from Kafka topic: {}", logLines.size(), topic);
        } catch (Exception e) {
            log.error("Failed to fetch logs from Kafka topic: {}", topic, e);
        }

        return logLines;
    }

    private LogSource getOrCreateKafkaSource() {
        return logSourceRepository.findByName("kafka")
                .orElseGet(() -> logSourceRepository.save(
                        LogSource.builder()
                                .name("kafka")
                                .type(LogSource.SourceType.KAFKA)
                                .build()));
    }
    */
}
