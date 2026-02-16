import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.kotlin.dsl.named

var springBootVersion = "3.5.10"

plugins {
    id("maven-publish")
    id("java-library")
    id("com.github.ben-manes.versions") version "0.53.0"
}

group = "no.novari"
version = findProperty("version")?.toString() ?: "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withSourcesJar()
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.fintlabs.no/releases")
    }
    mavenCentral()
}

dependencies {
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

    api("no.novari:kafka:6.0.0")
    api("no.novari:flyt-cache:3.0.0")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("io.projectreactor:reactor-test")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.springframework.security:spring-security-test")

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
