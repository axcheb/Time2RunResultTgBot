plugins {
    kotlin("jvm") version "1.6.20"
    kotlin("kapt") version "1.6.20"
}

group = "ru.time2run"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")

    implementation("dev.inmo:tgbotapi:0.38.10")

    implementation("it.skrape:skrapeit:1.2.1")

    implementation("com.github.mfarsikov:kotlite-core:0.6.0")
    kapt("com.github.mfarsikov:kotlite-kapt:0.6.0")

    implementation("org.flywaydb:flyway-core:8.5.4")

    implementation("org.xerial:sqlite-jdbc:3.36.0.3")

    implementation("io.github.microutils:kotlin-logging:2.1.21")
}

kapt {
    arguments {
        arg("kotlite.db.qualifiedName", "ru.time2run.DB") // default database class name
    }
}

val fatJar = task("fatJar", type = Jar::class) {
    archiveBaseName.value("${project.name}-fat")
    manifest {
        attributes["Implementation-Title"] = "Time2Run TG bot"
        attributes["Implementation-Version"] = archiveVersion
        attributes["Main-Class"] = "ru.time2run.MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map{ if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
