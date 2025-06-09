package de.order.processor.infrastructure.configuration;

import software.amazon.awscdk.services.logs.RetentionDays;

public class Configuration {

    public static String DEFAULT_ENCODING = "UTF-8";
    public static String DEFAULT_TZ = "Europe/Berlin";
    public static String DEFAULT_JAVA_OPTS = "-XX:+TieredCompilation -XX:TieredStopAtLevel=1";
    public static String METRICS_NAMESPACE = "OrderProcessorApp";
    public static RetentionDays DEFAULT_LOG_RETENTION = RetentionDays.FIVE_DAYS;
    public static String STAGE = System.getenv("STAGE");

    public static final de.order.processor.infrastructure.configuration.GitHubRepository GITHUB_REPOSITORY = de.order.processor.infrastructure.configuration.GitHubRepository.builder()
        .owner("Kazim1101")
        .repository("order-processor-backend-aws")
        .branch("main")
        .build();

}
