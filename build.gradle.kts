import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.kotlin.dsl.named

var springBootVersion = "3.5.16"

plugins {
    id("maven-publish")
    id("java-library")
    id("io.github.ben-manes.versions") version "0.66.0"
}

group = "no.novari"
version = findProperty("version")?.toString() ?: "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withSourcesJar()
}

// Dependabot resolves with JDK 21; an explicit release keeps JVM 25 as the resolution target so gradle.lockfile gets updated.
tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.fintlabs.no/releases")
    }
    mavenCentral()
}

dependencyLocking {
    lockAllConfigurations()
}

dependencies {
    constraints {
        implementation("at.yawk.lz4:lz4-java:1.12.1") {
            because("Fixes CVE-2026-59949 in the kafka-clients transitive dependency")
        }
        implementation("org.xerial.snappy:snappy-java:1.1.10.12") {
            because("Fixes CVE-2026-93451, CVE-2026-108106 and CVE-2026-116590 in kafka-clients")
        }
    }

    implementation(platform("com.fasterxml.jackson:jackson-bom:2.22.4"))
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.26.1"))
    implementation(platform("io.netty:netty-bom:4.2.19.Final"))
    // Exported to consumers
    api(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    annotationProcessor(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    compileOnly(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testAnnotationProcessor(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testCompileOnly(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))

    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    api("org.springframework.boot:spring-boot-starter-oauth2-client")
    api("org.springframework.boot:spring-boot-starter-cache")

    api("org.springframework.security:spring-security-core")

    api("org.springframework.boot:spring-boot-autoconfigure")

    api("no.novari:kafka:6.3.0")
    api("no.novari:flyt-cache:3.0.0")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("io.projectreactor:reactor-test")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.fintlabs.no/releases")
            credentials {
                username = System.getenv("REPOSILITE_USERNAME")
                password = System.getenv("REPOSILITE_PASSWORD")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return !isStable
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}
