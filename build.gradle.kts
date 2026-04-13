plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

val projectVersion = "1.0"
val apiVersion = "1.21.11"

repositories {
    mavenCentral()
    // Paper
    maven("https://repo.papermc.io/repository/maven-public/")
    // Skript
    maven("https://repo.skriptlang.org/releases")
    // WorldEdit
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    // Paper
    implementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    // Skript
    implementation("com.github.SkriptLang:Skript:2.14.0-pre1") {
        isTransitive = false
    }
    // WorldEdit
    //implementation("com.sk89q.worldedit:worldedit-bukkit:7.4.0") {
    //    exclude(module = "bstats-bukkit")
    //}

    // FastAsyncWorldEdit
    implementation(platform("com.intellectualsites.bom:bom-newest:1.55")) // Ref: https://github.com/IntellectualSites/bom
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") {
        isTransitive = false
    }
}

tasks {
    runServer {
        minecraftVersion("1.21.11")

        downloadPlugins {
            modrinth("Skript", "2.14.0")
            //modrinth("worldedit", "CkT32vix")
            modrinth("FastAsyncWorldEdit", "2.15.0")
            github("SkriptLang", "skript-reflect", "v2.6.2", "skript-reflect-2.6.2.jar")
            modrinth("skbee", "3.16.0")
        }
    }
}

val targetJavaVersion = 21
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"

    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

tasks.processResources {
    filteringCharset = "UTF-8"
    files("src/main/resources") {
        expand("version" to projectVersion, "apiversion" to apiVersion)
    }
}
