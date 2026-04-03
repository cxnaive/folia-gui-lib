plugins {
    java
    `java-library`
    id("com.gradleup.shadow") version "9.3.1"
}

group = "com.thenextlvl"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.1.0")
    implementation(project(":"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("folia-gui-toolkit-${version}.jar")

    dependencies {
        include(project(":"))
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}