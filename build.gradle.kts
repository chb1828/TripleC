plugins {
    idea
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.10.6"
}

group = "io.github"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.module:jackson-module-blackbird")
    implementation("com.influxdb:influxdb-spring:${project.properties["influxdbSpringVersion"]}")
    implementation("io.github.oshai:kotlin-logging-jvm:${project.properties["kotlinLoggingVersion"]}")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-registry-influx")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

sourceSets {
    main {
        java {
            srcDirs("src/main/java", "src/main/kotlin")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
