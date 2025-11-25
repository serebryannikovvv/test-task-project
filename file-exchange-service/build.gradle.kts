plugins {
    id("java")
    id("application")
}

group = "com.fileservice"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClass.set("com.fileservice.Main")
}

dependencies {
    // JUnit 4
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.hamcrest:hamcrest:2.2")

    // Mockito для JUnit 4
    testImplementation("org.mockito:mockito-core:3.12.4")

    // Дополнительные утилиты для тестирования
    testImplementation("org.assertj:assertj-core:3.19.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.named<JavaExec>("run") {
    jvmArgs("-Xmx2g", "-Dfile.encoding=UTF-8")
    classpath += files(sourceSets.main.get().resources.srcDirs)
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
    from(sourceSets.main.get().resources.srcDirs)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// === Тесты запускаются через ./gradlew test ===
tasks.test {
    useJUnit()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}