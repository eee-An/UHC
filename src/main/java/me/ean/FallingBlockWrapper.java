package me.ean;

import org.bukkit.Location;
import org.bukkit.entity.FallingBlock;

public class FallingBlockWrapper {
    FallingBlock block;
    int ageTicks = 0;
    final Location initialLocation;

    FallingBlockWrapper(FallingBlock block, Location initialLocation) {
        this.block = block;
        this.initialLocation = initialLocation;
    }
}
