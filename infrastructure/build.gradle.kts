plugins {
  java
  application
}

dependencies {
  implementation("software.amazon.awscdk:aws-cdk-lib:2.111.0")
  implementation("software.constructs:constructs:10.1.120")
  implementation("com.amazonaws:aws-java-sdk-cloudformation:1.12.120")

  compileOnly("org.projectlombok:lombok:1.18.24")

  annotationProcessor("org.projectlombok:lombok:1.18.24")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.24")
  compileOnly("org.projectlombok:lombok:1.18.24")
  annotationProcessor("org.projectlombok:lombok:1.18.24")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.24")
}

repositories {
  mavenCentral()
}

application {
  mainClass.set("de.order.processor.infrastructure.OrderProcessorApp")
}

tasks.register<Exec>("synth") {
  dependsOn("build")
  commandLine("npx", "cdk", "synth", "--require-approval", "never", "OrderProcessorStack")
}

tasks.register<Exec>("deploy") {
  dependsOn("build")
  commandLine("npx", "cdk", "deploy", "--require-approval", "never", "OrderProcessorStack")
}

tasks.clean {
  delete.add("cdk.out")
}

tasks.register<Exec>("deployPipeline") {
  dependsOn("build")
  commandLine("npx", "cdk", "deploy", "--require-approval", "never", "--app", "../gradlew build :infrastructure:runPipeline", "PipelineStack")
}

tasks.register<Exec>("synthPipeline") {
  dependsOn("build")
  commandLine("npx", "cdk", "synth", "--require-approval", "never", "--app", "../gradlew build :infrastructure:runPipeline", "PipelineStack")
}
