/*
 * Copyright (C) 2022-2025 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.gradle.node.npm.task.NpmSetupTask
import java.nio.file.Paths

description = "Hedera Mirror Node imports data from consensus nodes and serves it via an API"

plugins {
    id("com.diffplug.spotless")
    id("com.github.node-gradle.node")
    id("idea")
    id("java-platform")
    id("org.sonarqube")
    id("snykcode-extension")
}

// Can't use typed variable syntax due to Dependabot limitations
extra.apply {
    set("grpcVersion", "1.70.0")
    set("json-smart.version", "2.5.2") // Temporary until next Spring Boot
    set("mapStructVersion", "1.6.3")
    set("netty.version", "4.1.118.Final") // Temporary until next Spring Boot
    set("nodeJsVersion", "18.20.5")
    set("protobufVersion", "3.25.5")
    set("reactorGrpcVersion", "1.2.4")
    set("vertxVersion", "4.5.12")
    set("tuweniVersion", "2.3.1")
}

// Creates a platform/BOM with specific versions so subprojects don't need to specify a version when
// using a dependency
dependencies {
    constraints {
        val grpcVersion: String by rootProject.extra
        val mapStructVersion: String by rootProject.extra
        val protobufVersion: String by rootProject.extra
        val reactorGrpcVersion: String by rootProject.extra
        val testcontainersSpringBootVersion: String by rootProject.extra
        val tuweniVersion: String by rootProject.extra
        val vertxVersion: String by rootProject.extra

        api("com.esaulpaugh:headlong:10.0.2")
        api("com.github.meanbeanlib:meanbean:3.0.0-M9")
        api("com.github.vertical-blank:sql-formatter:2.0.5")
        api("org.bouncycastle:bcprov-jdk18on:1.80")
        api("com.bucket4j:bucket4j-core:8.10.1")
        api("com.google.cloud:spring-cloud-gcp-dependencies:5.8.0")
        api("com.google.guava:guava:33.4.0-jre")
        api("com.google.protobuf:protobuf-java:$protobufVersion")
        api("com.graphql-java-generator:graphql-java-client-runtime:2.8")
        api("com.graphql-java:graphql-java-extended-scalars:22.0")
        api("com.graphql-java:graphql-java-extended-validation:22.0")
        api("com.hedera.hashgraph:app:0.58.4")
        api("com.hedera.evm:hedera-evm:0.54.2")
        api("com.hedera.hashgraph:hedera-protobuf-java-api:0.59.1")
        api("com.hedera.hashgraph:sdk:2.46.0")
        api("com.ongres.scram:client:2.1")
        api("com.playtika.testcontainers:embedded-google-pubsub:3.1.11")
        api("com.salesforce.servicelibs:reactor-grpc-stub:$reactorGrpcVersion")
        api("commons-beanutils:commons-beanutils:1.10.0")
        api("commons-io:commons-io:2.18.0")
        api("io.cucumber:cucumber-bom:7.21.1")
        api("io.github.mweirauch:micrometer-jvm-extras:0.2.2")
        api("io.grpc:grpc-bom:$grpcVersion")
        api("io.hypersistence:hypersistence-utils-hibernate-63:3.9.1")
        api("io.projectreactor:reactor-core-micrometer:1.2.2")
        api("io.swagger:swagger-annotations:1.6.15")
        api("io.vertx:vertx-pg-client:$vertxVersion")
        api("io.vertx:vertx-codegen:$vertxVersion")
        api("io.vertx:vertx-core:$vertxVersion")
        api("jakarta.inject:jakarta.inject-api:2.0.1")
        api("javax.inject:javax.inject:1")
        api("net.devh:grpc-spring-boot-starter:3.1.0.RELEASE")
        api("net.java.dev.jna:jna:5.16.0")
        api("org.apache.commons:commons-collections4:4.4")
        api("org.apache.commons:commons-compress:1.27.1")
        api("org.apache.commons:commons-math3:3.6.1")
        api("org.apache.tuweni:tuweni-bytes:$tuweniVersion")
        api("org.apache.tuweni:tuweni-units:$tuweniVersion")
        api("org.apache.velocity:velocity-engine-core:2.4.1")
        api("org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api:5.0.2")
        api("org.gaul:s3proxy:2.6.0")
        api("org.hyperledger.besu:secp256k1:0.8.2")
        api("org.hyperledger.besu:evm:24.3.3")
        api("org.mapstruct:mapstruct:$mapStructVersion")
        api("org.mapstruct:mapstruct-processor:$mapStructVersion")
        api("org.msgpack:jackson-dataformat-msgpack:0.9.9")
        api("org.springdoc:springdoc-openapi-webflux-ui:1.8.0")
        api("org.springframework.cloud:spring-cloud-dependencies:2024.0.0")
        api("org.testcontainers:junit-jupiter:1.20.4")
        api("org.mockito:mockito-inline:5.2.0")
        api("software.amazon.awssdk:bom:2.30.16")
        api("uk.org.webcompere:system-stubs-jupiter:2.1.7")
        api("org.web3j:core:4.12.2")
        api("tech.pegasys:jc-kzg-4844:1.0.0")
        api("com.hedera.cryptography:hedera-cryptography-bls:0.1.1-SNAPSHOT")
    }
}

allprojects {
    apply(plugin = "jacoco")
    apply(plugin = "org.sonarqube")

    sonarqube {
        properties {
            property("sonar.host.url", "https://sonarcloud.io")
            property("sonar.organization", "hashgraph")
            property("sonar.projectKey", "hedera-mirror-node")
            property("sonar.sourceEncoding", "UTF-8")
            property("sonar.issue.ignore.multicriteria", "e1,e2,e3,e4,e5,e6")
            property("sonar.issue.ignore.multicriteria.e1.resourceKey", "**/*.java")
            property("sonar.issue.ignore.multicriteria.e1.ruleKey", "java:S6212")
            property("sonar.issue.ignore.multicriteria.e2.resourceKey", "**/*.java")
            property("sonar.issue.ignore.multicriteria.e2.ruleKey", "java:S125")
            property("sonar.issue.ignore.multicriteria.e3.resourceKey", "**/*.java")
            property("sonar.issue.ignore.multicriteria.e3.ruleKey", "java:S2187")
            property("sonar.issue.ignore.multicriteria.e4.resourceKey", "**/*.js")
            property("sonar.issue.ignore.multicriteria.e4.ruleKey", "javascript:S3758")
            property("sonar.issue.ignore.multicriteria.e5.resourceKey", "**/stateproof/*.sql")
            property("sonar.issue.ignore.multicriteria.e5.ruleKey", "plsql:S1192")
            property("sonar.issue.ignore.multicriteria.e6.resourceKey", "**/*.java")
            property("sonar.issue.ignore.multicriteria.e6.ruleKey", "java:S2970")
            property(
                "sonar.exclusions",
                "src/main/java/com/hedera/services/**,src/test/java/com/hedera/services/**",
            )
        }
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

// Spotless uses Prettier and it requires Node.js
node {
    val nodeJsVersion: String by rootProject.extra
    download = true
    version = nodeJsVersion
    workDir = rootDir.resolve(".gradle").resolve("nodejs")
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

spotless {
    val licenseHeader =
        """
/*
 * Copyright (C) ${'$'}YEAR Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */${"\n\n"}
"""
            .trimIndent()

    val npmExec =
        when (System.getProperty("os.name").lowercase().contains("windows")) {
            true -> Paths.get("npm.cmd")
            else -> Paths.get("bin", "npm")
        }
    val npmSetup = tasks.named("npmSetup").get() as NpmSetupTask
    val npmExecutable = npmSetup.npmDir.get().asFile.toPath().resolve(npmExec)

    isEnforceCheck = false

    if (!System.getenv().containsKey("CI")) {
        ratchetFrom("origin/main")
    }

    format("go") {
        endWithNewline()
        licenseHeader(licenseHeader, "package").updateYearWithLatest(true)
        target("hedera-mirror-rosetta/**/*.go")
        targetExclude("build/**")
        trimTrailingWhitespace()
    }
    format("javascript") {
        endWithNewline()
        leadingTabsToSpaces(2)
        licenseHeader(licenseHeader, "$").updateYearWithLatest(true)
        prettier()
            .npmExecutable(npmExecutable)
            .npmInstallCache(Paths.get("${rootProject.rootDir}", ".gradle", "spotless"))
            .config(mapOf("bracketSpacing" to false, "printWidth" to 120, "singleQuote" to true))
        target("hedera-mirror-rest/**/*.js", "hedera-mirror-test/**/*.js")
        targetExclude("**/build/**", "**/node_modules/**", "**/__tests__/integration/*.test.js")
    }
    java {
        endWithNewline()
        palantirJavaFormat()
        licenseHeader(licenseHeader, "package").updateYearWithLatest(true)
        target("**/*.java")
        targetExclude(
            "**/build/**",
            "hedera-mirror-rest/**",
            "hedera-mirror-rosetta/**",
            // Known issue with Java 21: https://github.com/palantir/palantir-java-format/issues/933
            "hedera-mirror-rest-java/**/EntityServiceImpl.java",
        )
        toggleOffOn()
    }
    kotlin {
        endWithNewline()
        ktfmt().kotlinlangStyle()
        licenseHeader(licenseHeader, "package").updateYearWithLatest(true)
        target("buildSrc/**/*.kt")
        targetExclude("**/build/**")
    }
    kotlinGradle {
        endWithNewline()
        ktfmt().kotlinlangStyle()
        licenseHeader(licenseHeader, "(description|import|plugins)").updateYearWithLatest(true)
        target("*.kts", "*/*.kts", "buildSrc/**/*.kts", "hedera-mirror-rest/*/*.kts")
        targetExclude("**/build/**")
    }
    format("miscellaneous") {
        endWithNewline()
        leadingTabsToSpaces(2)
        prettier().npmExecutable(npmExecutable)
        target("**/*.json", "**/*.md", "**/*.yml", "**/*.yaml")
        targetExclude("**/build/**", "**/charts/**", "**/node_modules/**", "**/package-lock.json")
        trimTrailingWhitespace()
    }
    format("proto") {
        endWithNewline()
        leadingTabsToSpaces(4)
        licenseHeader(licenseHeader, "(package|syntax)").updateYearWithLatest(true)
        target("hedera-mirror-protobuf/**/*.proto")
        targetExclude("build/**")
        trimTrailingWhitespace()
    }
    sql {
        endWithNewline()
        leadingTabsToSpaces()
        target("hedera-mirror-(common|importer|rest)/**/*.sql")
        targetExclude("**/build/**", "**/node_modules/**")
        trimTrailingWhitespace()
    }
}

fun replaceVersion(files: String, match: String) {
    ant.withGroovyBuilder {
        "replaceregexp"("match" to match, "replace" to project.version, "flags" to "gm") {
            "fileset"(
                "dir" to rootProject.projectDir,
                "includes" to files,
                "excludes" to "**/node_modules/",
            )
        }
    }
}

tasks.nodeSetup { onlyIf { !this.nodeDir.get().asFile.exists() } }

tasks.register("release") {
    description = "Replaces release version in files."
    group = "release"
    doLast {
        replaceVersion("charts/**/Chart.yaml", "(?<=^(appVersion|version): ).+")
        replaceVersion("docker-compose.yml", "(?<=gcr.io/mirrornode/hedera-mirror-.+:).+")
        replaceVersion("gradle.properties", "(?<=^version=).+")
        replaceVersion(
            "hedera-mirror-rest/**/package*.json",
            "(?<=\"@hashgraph/(check-state-proof|mirror-rest|mirror-monitor)\",\\s{3,7}\"version\": \")[^\"]+",
        )
        replaceVersion("hedera-mirror-rest/**/openapi.yml", "(?<=^  version: ).+")
        replaceVersion(
            "tools/traffic-replay/log-downloader/package*.json",
            "(?<=\"@hashgraph/mirror-log-downloader\",\\s{3,7}\"version\": \")[^\"]+",
        )
    }
}

tasks.sonar { dependsOn(tasks.build) }

tasks.spotlessApply { dependsOn(tasks.nodeSetup) }

tasks.spotlessCheck { dependsOn(tasks.nodeSetup) }
