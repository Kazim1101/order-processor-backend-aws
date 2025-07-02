import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    java
    id("io.freefair.aspectj.post-compile-weaving")
    id("com.github.johnrengelman.shadow")
    id("jacoco")
}

group = "de.order.processor"
version = ""

repositories {
    mavenCentral()
}

dependencies {
//    implementation(project(":common"))

    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("org.apache.httpcomponents:httpmime:4.5.14")
    implementation("com.amazonaws:aws-lambda-java-events:3.11.0")
    implementation("software.amazon.awssdk:s3:2.31.59")
    implementation("software.amazon.awssdk:dynamodb:2.31.59")
    implementation("software.amazon.awssdk:apigatewaymanagementapi:2.31.59")

    aspect("software.amazon.lambda:powertools-logging:1.13.0")
    aspect("software.amazon.lambda:powertools-metrics:1.13.0")
    aspect("software.amazon.lambda:powertools-tracing:1.13.0")
    implementation("software.amazon.lambda:powertools-serialization:1.13.0")
    implementation("org.json:json:20220924")

    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("net.logstash.logback:logstash-logback-encoder:7.2")

    testImplementation(platform("org.junit:junit-bom:5.9.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testCompileOnly("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.8.0")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.35.0")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.34.0")

    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    testCompileOnly("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")
}

repositories {
    mavenCentral()
}

tasks.withType<ShadowJar> {
    transform(Log4j2PluginsCacheFileTransformer())
    archiveVersion.set("")
    archiveClassifier.set("")
    archiveBaseName.set("lambda")
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
    reports {
        junitXml.required = true
        html.required = true
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}
