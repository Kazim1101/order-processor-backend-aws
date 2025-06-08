plugins {
  java
  id("io.freefair.aspectj.post-compile-weaving") version "6.5.0.2" apply (false)
  id("com.github.johnrengelman.shadow") version "7.1.2" apply (false)
  id("jacoco-report-aggregation")
}

subprojects {
  group = "de.order.processor"
  version = ""

  plugins.withType(org.gradle.api.plugins.JavaPlugin::class) {
    extensions.configure(JavaPluginExtension::class.java) {
      sourceCompatibility = JavaVersion.VERSION_11
      targetCompatibility = JavaVersion.VERSION_11
    }
  }

  repositories {
    mavenCentral()
  }

  tasks.withType<Test> {
    useJUnitPlatform()

    // Don't fail test due to missing X-Ray context
    environment["LAMBDA_TASK_ROOT"] = "handler"

    testLogging {
      events("passed", "skipped", "failed")
      showStandardStreams = true
      exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
      showCauses = true
      showStackTraces = true
    }
  }

}

tasks.named("clean") {
  dependsOn(subprojects.flatMap { project: Project -> project.tasks }.filter { task -> task.name == "clean" })
}

tasks.register("deploy") {
  group = "cdk"
  dependsOn(subprojects.flatMap { project: Project -> project.tasks }.filter { task -> task.name == "deploy" })
}

tasks.register("destroy") {
  group = "cdk"
  dependsOn(subprojects.flatMap { project: Project -> project.tasks }.filter { task -> task.name == "destroy" })
}

// ##########  ##########
tasks.named("build") {
    finalizedBy("testCodeCoverageReport")
}

repositories {
    mavenCentral()
}


dependencies {
    subprojects.forEach { subproject ->
        jacocoAggregation(project(subproject.path))
    }
}
