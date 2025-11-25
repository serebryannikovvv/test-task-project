plugins {
    id("java")
    id("application")
}

group = "com.liquidsort"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

application {
    mainClass.set("com.liquidsort.Main")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "com.liquidsort.Main")
    }
}

tasks.register("runBFS") {
    group = "application"
    description = "Запуск с классическим BFS"
    doLast {
        javaexec {
            mainClass.set("com.liquidsort.Main")
            classpath = sourceSets.main.get().runtimeClasspath
            systemProperty("solver", "bfs")
            if (project.hasProperty("noanim")) {
                args = listOf("--no-animation")
            }
        }
    }
}

tasks.register("runDFS") {
    group = "application"
    description = "Запуск с DFS (может найти решение быстрее)"
    doLast {
        javaexec {
            mainClass.set("com.liquidsort.Main")
            classpath = sourceSets.main.get().runtimeClasspath
            systemProperty("solver", "dfs")
            if (project.hasProperty("noanim")) {
                args = listOf("--no-animation")
            }
        }
    }
}

