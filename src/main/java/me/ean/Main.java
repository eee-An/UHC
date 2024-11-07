package me.ean;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        this.getCommand("testborder").setExecutor(new WorldBorderMover(this));
    }
}