import java.util.Locale

subprojects {
    apply(plugin="java")
    repositories {
        if (Locale.getDefault().country == "CN") {
            maven("https://repo.huaweicloud.com/repository/maven/")
        }
        mavenCentral()
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://maven.enginehub.org/repo/")
    }
    dependencies {
        add("compileOnly", "org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    }
    extensions.configure(JavaPluginExtension::class) {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = sourceCompatibility
    }
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "utf-8"
    }
}
