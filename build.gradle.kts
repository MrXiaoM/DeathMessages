import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "dev.mrshawn"
version = "1.4.18"

repositories {
    maven("https://repo.huaweicloud.com/repository/maven/")
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://mvn.lumine.io/repository/maven-public")
    maven("https://ci.nyaacat.com/maven/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.2")
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.11.1")
    compileOnly("io.lumine:Mythic-Dist:4.13.0")
    compileOnly("io.lumine:Mythic:5.4.1")
    compileOnly("io.lumine:LumineUtils:1.20-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.0")
    compileOnly("com.sk89q.worldedit:worldedit-core:7.2.0")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.1")
    compileOnly("com.sk89q.worldguard:worldguard-core:7.0.1")
    compileOnly("com.meowj:LangUtils:2.3-SNAPSHOT")

    implementation("commons-io:commons-io:2.9.0")
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
            "org.apache.commons.io" to "commons-io"
        ).forEach { (original,target) ->
            relocate(original, "dev.mrshawn.deathmessages.shadow.$target")
        }
        minimize()
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
