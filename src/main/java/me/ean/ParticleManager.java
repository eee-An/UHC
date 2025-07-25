package me.ean;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public class ParticleManager {

    private final Main plugin;

    public ParticleManager(Main plugin) {
        this.plugin = plugin;
    }

    public void spawnBorderCenterParticles(World world, Location center) {
        if (world != null) {
            int highestY = world.getHighestBlockYAt(center); // Get the surface Y coordinate
            center.setY(highestY + 2); // Set the Y coordinate slightly above the surface

            // Spawn particles at the surface
            world.spawnParticle(Particle.END_ROD, center.getX(), center.getY(), center.getZ(), 10, 0.1, 0.1, 0.1, 0.01, null, true);

            // Log the coordinates to the console
            //Bukkit.getLogger().info("Particles spawned at: X=" + center.getX() + ", Y=" + center.getY() + ", Z=" + center.getZ());
        }
    }

    public void spawnCustomEffect(Location location) {
        World world = location.getWorld();
        if (world != null) {
            world.spawnParticle(Particle.FLAME, location.getX(), location.getY(), location.getZ(), 200, 0.5, 0.5, 0.5, 0.05, null, true);
        }
    }

    //TODO: Particles for supply drops, player deaths, etc.

    public void spawnSupplyDropParticles(Location location) {
        World world = location.getWorld();
        if (world != null) {
            world.spawnParticle(Particle.CLOUD, location.getX(), location.getY() + 1, location.getZ(), 50, 0.5, 0.5, 0.5, 0.05, null, true);
            world.spawnParticle(Particle.FIREWORKS_SPARK, location.getX(), location.getY() + 1 , location.getZ(), 20, 0.2, 0.2, 0.2, 0.01, null, true);
        }
    }

    public void spawnPlayerDeathParticles(Location location) {
        World world = location.getWorld();
        if (world != null) {
            world.spawnParticle(Particle.SMOKE_LARGE, location.getX(), location.getY() + 1, location.getZ(), 30, 0.5, 0.5, 0.5, 0.05, null, true);
            world.spawnParticle(Particle.REDSTONE, location.getX(), location.getY() + 1, location.getZ(), 20, 0.2, 0.2, 0.2, 0.05, new Particle.DustOptions(org.bukkit.Color.RED, 1), true);
        }
    }

    public void spawnLightningStrikeParticles(Location location) {
        World world = location.getWorld();
        if (world != null) {
           world.strikeLightningEffect(location); // Optional: Summon a lightning strike at the location

        }
    }
}