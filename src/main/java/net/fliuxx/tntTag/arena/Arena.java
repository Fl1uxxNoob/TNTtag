package net.fliuxx.tntTag.arena;

import org.bukkit.Location;

public class Arena {
    private final String name;
    private final Location spawn;

    public Arena(String name, Location spawn) {
        this.name = name;
        this.spawn = spawn;
    }

    public String getName() {
        return name;
    }

    public Location getSpawn() {
        return spawn;
    }
}
