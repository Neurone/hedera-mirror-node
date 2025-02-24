// SPDX-License-Identifier: Apache-2.0

import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer

description = "Hedera Mirror Node Test"

plugins {
    id("com.gradleup.shadow")
    id("docker-conventions")
    id("java-conventions")
    id("openapi-conventions")
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation(platform("io.cucumber:cucumber-bom"))
    implementation("io.cucumber:cucumber-java")
    implementation("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("io.grpc:grpc-inprocess")
    testImplementation("com.esaulpaugh:headlong")
    testImplementation("com.google.guava:guava")
    // With 2.38.0, the SDK updated the headlong dependency from 10.0.0 to 12.1.0. Web3 is not ready
    // for that yet.
    testImplementation("com.hedera.hashgraph:sdk") {
        exclude(group = "com.esaulpaugh", module = "headlong")
    }
    testImplementation("io.cucumber:cucumber-junit-platform-engine")
    testImplementation("io.cucumber:cucumber-spring")
    testImplementation("io.grpc:grpc-okhttp")
    testImplementation(
        group = "io.netty",
        name = "netty-resolver-dns-native-macos",
        classifier = "osx-aarch_64",
    )
    testImplementation("jakarta.inject:jakarta.inject-api")
    testImplementation("net.java.dev.jna:jna")
    testImplementation("org.apache.commons:commons-lang3")
    testImplementation("org.awaitility:awaitility")
    testImplementation("org.junit.platform:junit-platform-suite")
    testImplementation("org.springframework.boot:spring-boot-starter-aop")
    testImplementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.retry:spring-retry")
    testImplementation("org.apache.tuweni:tuweni-bytes")
    testImplementation("commons-codec:commons-codec")
    testImplementation("org.web3j:core")
}

// Disable the default test task and only run acceptance tests during the standalone "acceptance"
// task
tasks.named("test") { enabled = false }

tasks.register<Test>("acceptance") {
    description = "Acceptance tests configuration"
    group = "verification"
    val maxParallelism = project.property("maxParallelism") as String
    jvmArgs = listOf("-Xmx1024m", "-Xms1024m")
    maxParallelForks =
        if (maxParallelism.isNotBlank()) maxParallelism.toInt()
        else Runtime.getRuntime().availableProcessors()
    useJUnitPlatform {}

    // Copy relevant system properties to the forked test process
    System.getProperties()
        .filter { it.key.toString().matches(Regex("^(cucumber|hedera|spring)\\..*")) }
        .forEach { systemProperty(it.key.toString(), it.value) }
}

tasks.build { dependsOn("shadowJar") }

tasks.shadowJar {
    dependsOn(tasks.compileTestJava)
    from(sourceSets.main.get().output)
    from(sourceSets.test.get().output)
    configurations =
        listOf(
            project.configurations.runtimeClasspath.get(),
            project.configurations.testRuntimeClasspath.get(),
        )
    manifest { attributes["Main-Class"] = "com.hedera.mirror.test.TestApplication" }
    mergeServiceFiles()
    append("META-INF/spring.handlers")
    append("META-INF/spring.schemas")
    append("META-INF/spring.tooling")
    val transformer = PropertiesFileTransformer()
    transformer.mergeStrategy = "append"
    transformer.paths = listOf("META-INF/spring.factories")
    transform(transformer)
}

tasks.dockerBuild { dependsOn(tasks.shadowJar) }
