dependencies {
    compileOnly(project(":WorldGuard:shared"))

    val worldGuard = "7.0.1"
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:$worldGuard") {
        exclude(group="org.spigotmc", module="spigot-api")
        exclude(group="org.bukkit", module="bukkit")
    }
    compileOnly("com.sk89q.worldguard:worldguard-core:$worldGuard")
}
