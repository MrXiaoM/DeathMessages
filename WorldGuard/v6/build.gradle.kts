dependencies {
    compileOnly(project(":WorldGuard:shared"))

    val worldGuard = "6.2"
    compileOnly("com.sk89q.worldguard:worldguard-legacy:$worldGuard") {
        exclude(group="org.spigotmc", module="spigot-api")
        exclude(group="org.bukkit", module="bukkit")
    }
}
