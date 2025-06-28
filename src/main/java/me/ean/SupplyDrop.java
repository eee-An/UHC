package me.ean;

import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static org.bukkit.Bukkit.getLogger;

public class SupplyDrop {
    private final List<FallingBlock> parts = new ArrayList<>();
    private final World world;

    public SupplyDrop(World world) {
        this.world = world;
    }

    public void dropAt(Location location) throws FileNotFoundException {
        if (location.getWorld() == null) {
            location.setWorld(world);
        }

        // In your dropAt method, after determining baseLocation:
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
            BlockVector3 centerOffset = findCenterOffset(clipboard);

            // Calculate spawn base so barrel lands at target location
            Location spawnBase = location.clone().subtract(centerOffset.x(), centerOffset.y(), centerOffset.z());
            // Use iterateSchematicBlocksWithoutGetters to process blocks
            iterateSchematicBlocksWithoutGetters(clipboard, spawnBase, (blockVector, spawnLocation) -> {
                BlockState blockState = clipboard.getBlock(blockVector);
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


            // Continue with the existing logic for handling falling blocks
            new BukkitRunnable() {
                @Override
                public void run() {
                    boolean nestoJePalo = false;
                    Location baseLocation = null;

                    for (FallingBlock fallingBlock : parts) {
                        boolean landed = fallingBlock.isDead() || fallingBlock.isOnGround();

                        if (!landed) {
                            Location loc = fallingBlock.getLocation();
                            Location below = loc.clone().subtract(0, 1, 0);
                            boolean blockBelowSolid = !below.getBlock().isEmpty();

                            // Only consider landed if the block is just above a solid block and very close to the next integer Y
                            if (blockBelowSolid && (loc.getY() - below.getBlockY() < 0.1)) {
                                landed = true;
                            }
                        }

                        if (landed) {
                            if (baseLocation == null) {
                                baseLocation = fallingBlock.getLocation().getBlock().getLocation();
                                baseLocation.setX(location.getX());
                                baseLocation.setZ(location.getZ());
                            }
                            nestoJePalo = true;
                            break;
                        }
                    }

                    // Paste the schematic at the base location

                    // In your BukkitRunnable, before pasting:
                    if (nestoJePalo && baseLocation != null) {
                        // Remove all falling blocks first
                        for (FallingBlock block : parts) {
                            if (!block.isDead()) {
                                block.remove();
                            }
                        }
                        parts.clear();

                        BlockVector3 pasteLocation = BlockVector3.at(spawnBase.getBlockX(),baseLocation.getBlockY(),spawnBase.getBlockZ());
                        pasteSchematic(schematicFile, baseLocation.getWorld(), pasteLocation);

                        this.cancel();
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
        } catch (Exception e) {
            getLogger().severe("Failed to load schematic for falling blocks: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void populateLoot(Barrel barrel) {
        String lootTableName = Main.getInstance().getConfig().getString("supply-drop-loot-table");

        try {
            // Set the loot table for the barrel
            barrel.setLootTable(Bukkit.getLootTable(NamespacedKey.fromString(lootTableName)));
            barrel.update();
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid loot table name: " + lootTableName);
        }
    }

    public void pasteSchematic(File schematicFile, World bukkitWorld, BlockVector3 location) {

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
                // Iterate through the schematic region to find barrels
                clipboard.getRegion().forEach(blockVector -> {
                    BlockVector3 relativeVector = blockVector.subtract(clipboard.getRegion().getMinimumPoint());
                    Location blockLocation = new Location(
                            bukkitWorld,
                            location.x() + relativeVector.x(),
                            location.y() + relativeVector.y(),
                            location.z() + relativeVector.z()
                    );

                    if (blockLocation.getBlock().getState() instanceof Barrel barrel) {
                        populateLoot(barrel); // Populate loot in the barrel

                        // Log the coordinates of the drop
                        Bukkit.broadcastMessage(Main.getInstance().getConfig().getString("supply-drop-landing-message")
                                .replace("{x}", String.valueOf(barrel.getLocation().getBlockX()))
                                .replace("{y}", String.valueOf(barrel.getLocation().getBlockY()))
                                .replace("{z}", String.valueOf(barrel.getLocation().getBlockZ())));
                    }
                });


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

    // Calculates the center offset of the schematic (relative to min point)
    private BlockVector3 findCenterOffset(Clipboard clipboard) {
        BlockVector3 min = clipboard.getRegion().getMinimumPoint();
        BlockVector3 max = clipboard.getRegion().getMaximumPoint();
        int centerX = (min.x() + max.x()) / 2 - min.x();
        int centerY = 0; // Y centering is usually not needed for drops
        int centerZ = (min.z() + max.z()) / 2 - min.z();
        return BlockVector3.at(centerX, centerY, centerZ);
    }

}