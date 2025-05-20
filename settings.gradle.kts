rootProject.name = "DeathMessages"

include(":WorldGuard")
File("WorldGuard").listFiles()?.forEach { folder ->
    if (File(folder, "build.gradle.kts").exists()) {
        include(":WorldGuard:${folder.name}")
    }
}
