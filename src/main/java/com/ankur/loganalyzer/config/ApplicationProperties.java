package com.ankur.loganalyzer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralized configuration properties for the LogSphere application.
 *
 * This class binds configuration from application.yml to strongly-typed properties,
 * providing type safety and IDE autocomplete for configuration values.
 *
 * Properties are organized by domain (Kafka, Cache, Parsing, etc.) for clarity.
 */
@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class ApplicationProperties {

    /**
     * Kafka consumer configuration
     */
    private Kafka kafka = new Kafka();

    /**
     * Cache configuration
     */
    private Cache cache = new Cache();

    /**
     * Parsing configuration
     */
    private Parsing parsing = new Parsing();

    /**
     * Thread pool configuration
     */
    private ThreadPool threadPool = new ThreadPool();

    /**
     * Analytics configuration
     */
    private Analytics analytics = new Analytics();

    /**
     * Log retention configuration
     */
    private Retention retention = new Retention();

    /**
     * Alert notification configuration
     */
    private Notification notification = new Notification();

    /**
     * Security bootstrap configuration
     */
    private Security security = new Security();

    private OpenSearch opensearch = new OpenSearch();

    @Getter
    @Setter
    public static class OpenSearch {
        private boolean enabled = false;
        private String host = "localhost";
        private int port = 9200;
        private String scheme = "http";
        private String indexPrefix = "logsphere";
    }

    @Getter
    @Setter
    public static class Kafka {
        private boolean enabled = false;
        private String bootstrapServers = "localhost:9092";
        private Consumer consumer = new Consumer();
        private Topic topic = new Topic();

        @Getter
        @Setter
        public static class Consumer {
            private String groupId = "logsphere-consumer-group";
            private String autoOffsetReset = "earliest";
            private int concurrency = 3;
        }

        @Getter
        @Setter
        public static class Topic {
            private String logs = "logs";
        }
    }

    @Getter
    @Setter
    public static class Cache {
        private int ttlMinutes = 5;
        private int maxSize = 10000;
        private String redisHost = "localhost";
        private int redisPort = 6379;
    }

    @Getter
    @Setter
    public static class Parsing {
        private int chunkSizeKb = 500;
        private int maxConcurrentParsers = 16;
        private Map<String, Integer> timeoutsByFormat = new HashMap<>();

        public Parsing() {
            timeoutsByFormat.put("json", 1000);
            timeoutsByFormat.put("regex", 2000);
            timeoutsByFormat.put("stacktrace", 5000);
        }
    }

    @Getter
    @Setter
    public static class ThreadPool {
        private PoolConfig ingestion = new PoolConfig(4, 8, 500);
        private PoolConfig parsing = new PoolConfig(8, 16, 1000);
        private PoolConfig analysis = new PoolConfig(8, 16, 500);
        private PoolConfig persistence = new PoolConfig(2, 4, 200);

        @Getter
        @Setter
        public static class PoolConfig {
            private int coreSize;
            private int maxSize;
            private int queueCapacity;

            public PoolConfig() {
            }

            public PoolConfig(int coreSize, int maxSize, int queueCapacity) {
                this.coreSize = coreSize;
                this.maxSize = maxSize;
                this.queueCapacity = queueCapacity;
            }
        }
    }

    @Getter
    @Setter
    public static class Analytics {
        private int zScoreThreshold = 3;
        private int windowSizeMinutes = 10;
        private double anomalyPercentile = 0.95;
        private boolean enablePatternClustering = true;
    }

    @Getter
    @Setter
    public static class Retention {
        private boolean enabled = true;
        private int parsedLogRetentionDays = 30;
        private int rawLogRetentionDays = 7;
        private int analysisResultRetentionDays = 90;
    }

    @Getter
    @Setter
    public static class Notification {
        private boolean emailEnabled = false;
        private String emailFrom = "alerts@logsphere.io";
        private List<String> emailTo = new ArrayList<>();
        private String emailSubjectPrefix = "[LogSphere Alert]";
        private boolean slackEnabled = false;
        private String slackWebhookUrl = "";
    }

    @Getter
    @Setter
    public static class Security {
        private BootstrapAdmin bootstrapAdmin = new BootstrapAdmin();

        @Getter
        @Setter
        public static class BootstrapAdmin {
            private boolean enabled = true;
            private String username = "admin";
            private String password = "change-me";
        }
    }
}
