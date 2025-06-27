package me.ean;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.entity.FallingBlock;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.WorldEdit;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static org.bukkit.Bukkit.getLogger;

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

        File schematicFile = new File(Main.getInstance().getDataFolder(), "balon.schem");
        if (!schematicFile.exists()) {
            getLogger().warning("Schematic file 'balon.schem' not found!");
            return;
        }

        try (FileInputStream fis = new FileInputStream(schematicFile)) {
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                getLogger().severe("Failed to determine the format of the schematic file: " + schematicFile.getName());
                return;
            }

            Clipboard clipboard;
            try (ClipboardReader reader = format.getReader(fis)) {
                clipboard = reader.read();
            }

            // Use iterateSchematicBlocksWithoutGetters to process blocks
            iterateSchematicBlocksWithoutGetters(clipboard, location, (blockVector, spawnLocation) -> {
                com.sk89q.worldedit.world.block.BlockState blockState = clipboard.getBlock(blockVector);
                if (!blockState.getBlockType().getMaterial().isAir()) {
                    FallingBlock fallingBlock = spawnLocation.getWorld().spawnFallingBlock(
                            spawnLocation,
                            BukkitAdapter.adapt(blockState)
                    );
                    fallingBlock.setDropItem(false);
                    fallingBlock.setGlowing(true);
                    parts.add(fallingBlock);
                }
            });

        } catch (Exception e) {
            getLogger().severe("Failed to load schematic for falling blocks: " + e.getMessage());
            e.printStackTrace();
        }

        // Continue with the existing logic for handling falling blocks
        new BukkitRunnable() {
            @Override
            public void run() {
                boolean nestoJePalo = false;
                Location baseLocation = null;

                for (FallingBlock fallingBlock : parts) {
                    if (fallingBlock.isDead() || fallingBlock.isOnGround()) {
                        if (baseLocation == null) {
                            baseLocation = fallingBlock.getLocation().getBlock().getLocation();
                            baseLocation.setX(location.getX());
                            baseLocation.setZ(location.getZ());
                        }
                        nestoJePalo = true;
                        break;
                    }
                }

                if (nestoJePalo) {
                    // Paste the schematic at the base location
                    File schematicFile = new File(Main.getInstance().getDataFolder(), "balon.schem");
                    if (schematicFile.exists()) {
                        BlockVector3 pasteLocation = BlockVector3.at(baseLocation.getBlockX(), baseLocation.getBlockY(), baseLocation.getBlockZ());
                        pasteSchematic(schematicFile, baseLocation.getWorld(), pasteLocation);
                    } else {
                        getLogger().warning("Schematic file 'balon.schem' not found!");
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
            getLogger().warning("Invalid loot table name: " + lootTableName);
        }
    }

    public void pasteSchematic(File schematicFile, org.bukkit.World bukkitWorld, BlockVector3 location) {

//        File tschematicFile = new File(Main.getInstance().getDataFolder(), "balon.schem");
//        if (!tschematicFile.exists()) {
//            getLogger().severe("Schematic file not found at: " + tschematicFile.getAbsolutePath());
//        } else {
//            getLogger().info("Schematic file found: " + tschematicFile.getAbsolutePath());
//            ClipboardFormat format = ClipboardFormats.findByFile(tschematicFile);
//            if (format == null) {
//                getLogger().severe("Failed to determine the format of the schematic file: " + tschematicFile.getName());
//            } else {
//                getLogger().info("Schematic format detected: " + format.getName());
//            }
//        }

        try {
            // Log the file path for debugging
//            getLogger().info("Attempting to load schematic file: " + schematicFile.getAbsolutePath());

            // Determine the format of the schematic file
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                getLogger().severe("Failed to determine the format of the schematic file: " + schematicFile.getName());
                getLogger().severe("Ensure the file is in the correct .schem format and located in the correct directory.");
                return;
            }

            // Read the schematic file
            try (FileInputStream fis = new FileInputStream(schematicFile);
                 ClipboardReader reader = format.getReader(fis)) {
                Clipboard clipboard = reader.read();

                // Adapt the Bukkit world to a WorldEdit world
                com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(bukkitWorld);

                // Define the paste location
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(adaptedWorld)) {
                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                    Operations.complete(holder.createPaste(editSession)
                            .to(location)
                            .ignoreAirBlocks(false)
                            .build());
                }
            }
        } catch (Exception e) {
            getLogger().severe("Failed to load or paste schematic: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void iterateSchematicBlocksWithoutGetters(Clipboard clipboard, Location baseLocation, BiConsumer<BlockVector3, Location> blockProcessor) {
        BlockVector3 minPoint = clipboard.getRegion().getMinimumPoint();
        clipboard.getRegion().forEach(blockVector -> {
            BlockVector3 relativeVector = blockVector.subtract(minPoint);
            Location relativeLocation = baseLocation.clone().add(
                    relativeVector.x(),
                    relativeVector.y(),
                    relativeVector.z()
            );
            blockProcessor.accept(blockVector, relativeLocation);
        });
    }
}
