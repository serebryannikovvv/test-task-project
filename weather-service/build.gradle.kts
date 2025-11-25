plugins {
    java
    application
}

group = "com.weather"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.jetty:jetty-server:9.4.56.v20240826")
    implementation("org.eclipse.jetty:jetty-servlet:9.4.56.v20240826")
    implementation("redis.clients:jedis:5.2.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
    implementation("org.jfree:jfreechart:1.5.4")

    //Логгирование
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("ch.qos.logback:logback-classic:1.2.13")
    implementation("ch.qos.logback:logback-core:1.2.13")
    application
}

application {
    mainClass.set("com.weather.Main")
}

// Чтобы можно было запустить: ./gradlew run
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}