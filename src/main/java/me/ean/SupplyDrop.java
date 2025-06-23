package me.ean;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SupplyDrop {
    private final List<FallingBlock> parts = new ArrayList<>();
    private final World world;
    private final int x1, y1, z1, x2, y2, z2;

    public SupplyDrop(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.world = world;
        assert x1 <= x2 && y1 <= y2 && z1 <= z2;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    public void dropAt(Location location) {
        if (location.getWorld() == null) {
            location.setWorld(world);
        }

        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    Material m = world.getBlockAt(x, y, z).getType();
                    if (m.isAir()) {
                        continue;
                    }
//                    Bukkit.broadcastMessage("bacamo: material " + world.getBlockAt(x, y, z).getType());
                    FallingBlock fb = location.getWorld().spawnFallingBlock(
                            location.clone().add(x-x1, y-y1, z-z1),
                            world.getBlockData(x, y, z)
                    );
                    fb.setDropItem(false);
                    fb.setCancelDrop(true);
                    fb.setGlowing(true);
                    parts.add(fb);
                }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                boolean nestoJePalo = false;
                Location baseLocation = null;

                for (FallingBlock fallingBlock : parts) {
                    if (fallingBlock.isDead() || fallingBlock.isOnGround()) {
                        if (baseLocation == null) {
                            // Use the first landed block to determine the new base location
                            baseLocation = fallingBlock.getLocation().getBlock().getLocation();
                            baseLocation.setX(location.getX());
                            baseLocation.setZ(location.getZ());


                            // poruka buducem ianu koji ce citati ovaj kod: preskoci sljedecih 10 linija
                            double Y = baseLocation.getY();
                            for (FallingBlock fb2 : parts) {
                                if (fb2 != fallingBlock) {
                                    double Y2 = fb2.getLocation().getBlock().getY();
//                                    Bukkit.broadcastMessage("test " + fb2.getBlockData().getMaterial() + " | Y=" + Y + ", Y2=" + Y2);
                                    if (Y2 +1 < Y) {
                                        Y = Y2 + 1;
                                    }
                                }
                            }
                            baseLocation.setY(Y);
                        }
                        nestoJePalo = true;
                        break;
                    }
                }

                if (nestoJePalo) {
                    // Rebuild the structure
                    for (int x = x1; x <= x2; x++) {
                        for (int y = y1; y <= y2; y++) {
                            for (int z = z1; z <= z2; z++) {
                                if (world.getBlockAt(x, y, z).getType().isAir())
                                    continue;

                                Location targetLocation = baseLocation.clone().add(x - x1, y - y1, z - z1);
                                targetLocation.getBlock().setBlockData(world.getBlockData(x, y, z));

                                // TODO: ovdje dodajemo kod koji ce provjeriti jesmo li postavili barrel i ako je barrel
                                // onda ga napunimo itemstackovima

                                if (world.getBlockAt(targetLocation).getType() == Material.BARREL) {
                                    // Access the barrel's inventory and populate it
                                    Barrel barrel = (Barrel) targetLocation.getBlock().getState();
                                    populateLoot(barrel);
                                    //barrel.update();

                                    Bukkit.broadcastMessage(Main.getInstance().getConfig().getString("supply-drop-landing-message")
                                            .replace("{x}", String.valueOf(barrel.getLocation().getBlockX()))
                                            .replace("{y}", String.valueOf(barrel.getLocation().getBlockY()))
                                            .replace("{z}", String.valueOf(barrel.getLocation().getBlockZ())));

                                }
                            }
                        }
                    }

                    // Cleanup remaining falling blocks
                    for (FallingBlock block : parts) {
                        if (!block.isDead()) {
                            block.remove();
                        }
                    }

                    this.cancel();
                    parts.clear();
                    return;
                }

                // Continue applying custom gravity if none landed
                for (FallingBlock fallingBlock : parts) {
                    if (!fallingBlock.isDead() && !fallingBlock.isOnGround()) {
                        Vector vel = fallingBlock.getVelocity();
                        vel.setY(-0.05);
                        fallingBlock.setVelocity(vel);
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }


    private void populateLoot(Barrel barrel) {
        String lootTableName = Main.getInstance().getConfig().getString("supply-drop-loot-table");

        try {
            // Set the loot table for the barrel
            barrel.setLootTable(Bukkit.getLootTable(org.bukkit.NamespacedKey.fromString(lootTableName)));
            barrel.update();
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Invalid loot table name: " + lootTableName);
        }
    }
}
