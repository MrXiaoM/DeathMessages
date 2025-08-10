import java.util.Locale

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "dev.mrshawn"
version = "1.4.22"

repositories {
    if (Locale.getDefault().country == "CN") {
        maven("https://repo.huaweicloud.com/repository/maven/")
    }
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.helpch.at/releases/")
    maven("https://jitpack.io/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://mvn.lumine.io/repository/maven-public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-api:4.22.0")

    // NBT API
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.15.0")

    // PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.11.6")

    // MythicMobs 4 and 5
    compileOnly("io.lumine:Mythic-Dist:4.13.0")
    compileOnly("io.lumine:Mythic:5.6.2")
    compileOnly("io.lumine:LumineUtils:1.20-SNAPSHOT")

    // LangUtils (legacy)
    compileOnly("com.github.MascusJeoraly:LanguageUtils:1.9")

    implementation("com.github.technicallycoded:FoliaLib:0.4.4")
    for (proj in project.project(":WorldGuard").subprojects) {
        implementation(proj)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = sourceCompatibility
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("")
        mapOf(
            "com.tcoded.folialib" to "folialib",
        ).forEach { (original,target) ->
            relocate(original, "dev.mrshawn.deathmessages.libs.$target")
        }
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(sourceSets.main.get().resources.srcDirs) {
            expand("version" to project.version)
            include("plugin.yml")
        }
    }
    withType<JavaCompile>().configureEach {
        options.encoding = "utf-8"
    }
}
