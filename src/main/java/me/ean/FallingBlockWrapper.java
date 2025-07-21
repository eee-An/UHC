package me.ean;

import org.bukkit.entity.FallingBlock;

public class FallingBlockWrapper {
    FallingBlock block;
    int ageTicks = 0;

    FallingBlockWrapper(FallingBlock block) {
        this.block = block;
    }
}
