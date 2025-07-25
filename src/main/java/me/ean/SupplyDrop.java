package me.ean;

import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.*;
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

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static org.bukkit.Bukkit.getLogger;

import org.bukkit.event.HandlerList;

public class SupplyDrop implements Listener {
    private final List<FallingBlockWrapper> parts = new ArrayList<>();
    private final World world;
    private Location dropLocation;
    private Location barrelLocation;
    private DropCompassBar compassBar;


    public SupplyDrop(World world) {
        this.world = world;
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
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
                    FallingBlock fallingBlock = spawnLocation.getWorld().spawnFallingBlock(spawnLocation, BukkitAdapter.adapt(blockState));
                    fallingBlock.setDropItem(false);
                    fallingBlock.setGlowing(true);
                    parts.add(new FallingBlockWrapper(fallingBlock));
                }
            });

            spawnBeaconWithBeam(world, location.getBlockX(), location.getBlockZ());
            Main.getInstance().setDropState(DropState.FALLING);

            // Continue with the existing logic for handling falling blocks
            new BukkitRunnable() {
                @Override
                public void run() {
                    boolean nestoJePalo = false;
                    Location baseLocation = null;

                    for (int i = 0; i < parts.size(); i++) {
                        FallingBlockWrapper wrapper = parts.get(i);
                        wrapper.ageTicks++;

                        boolean landed = wrapper.block.isDead() || wrapper.block.isOnGround();


                        // Respawn block before it dies
                        if (wrapper.ageTicks > 580 && !wrapper.block.isDead()) {
                            Location loc = wrapper.block.getLocation();
                            FallingBlock newBlock = loc.getWorld().spawnFallingBlock(loc, wrapper.block.getBlockData());
                            newBlock.setDropItem(false);
                            newBlock.setGlowing(true);
                            parts.set(i, new FallingBlockWrapper(newBlock));
                            wrapper.block.remove();
                            continue;
                        }

                        if (landed) {
                            if (baseLocation == null) {
                                baseLocation = wrapper.block.getLocation().getBlock().getLocation();
                                baseLocation.setX(location.getX());
                                baseLocation.setZ(location.getZ());
                            }
                            nestoJePalo = true;
                            Main.getInstance().setDropState(DropState.LANDED);
                            break;
                        }
                    }

                    // Paste the schematic at the base location

                    // In your BukkitRunnable, before pasting:
                    if (nestoJePalo && baseLocation != null) {
                        // Remove all falling blocks first
                        for (FallingBlockWrapper wrapper : parts) {
                            if (!wrapper.block.isDead()) {
                                wrapper.block.remove();
                            }
                        }
                        parts.clear();

                        BlockVector3 pasteLocation = BlockVector3.at(spawnBase.getBlockX(), baseLocation.getBlockY(), spawnBase.getBlockZ());
                        pasteSchematic(schematicFile, baseLocation.getWorld(), pasteLocation);

                        SupplyDrop.this.dropLocation = baseLocation;
                        SupplyDrop.this.compassBar = new DropCompassBar(baseLocation);

                        Main.getInstance().getParticleManager().spawnSupplyDropParticles(baseLocation);
//                        Bukkit.broadcastMessage("Particles spawned at drop location: " + baseLocation);

                        this.cancel();
                        return;
                    }

                    // Continue applying custom gravity if none landed
                    for (FallingBlockWrapper wrapper : parts) {
                        if (!wrapper.block.isDead() && !wrapper.block.isOnGround()) {
                            Vector vel = wrapper.block.getVelocity();
                            vel.setY(-Main.getInstance().getConfigValues().getSupplyDropDroppingSpeed());
                            wrapper.block.setVelocity(vel);
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
        String lootTableName = Main.getInstance().getConfigValues().getSupplyDropLootable();

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
            try (FileInputStream fis = new FileInputStream(schematicFile); ClipboardReader reader = format.getReader(fis)) {
                Clipboard clipboard = reader.read();

                // Adapt the Bukkit world to a WorldEdit world
                com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(bukkitWorld);

                // Define the paste location
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(adaptedWorld)) {
                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                    Operations.complete(holder.createPaste(editSession).to(location).ignoreAirBlocks(false).build());
                }
                // Iterate through the schematic region to find barrels
                clipboard.getRegion().forEach(blockVector -> {
                    BlockVector3 relativeVector = blockVector.subtract(clipboard.getRegion().getMinimumPoint());
                    Location blockLocation = new Location(bukkitWorld, location.x() + relativeVector.x(), location.y() + relativeVector.y(), location.z() + relativeVector.z());

                    if (blockLocation.getBlock().getState() instanceof Barrel barrel) {
                        populateLoot(barrel); // Populate loot in the barrel
//                        // Optionally, you can set the barrel's custom name or other properties here

                        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
                        barrelLocation = barrel.getLocation();

//                        // Log the coordinates of the drop
//                        Bukkit.broadcastMessage(Main.getInstance().getConfig().getString("supply-drop-landing-message")
//                                .replace("{x}", String.valueOf(barrel.getLocation().getBlockX()))
//                                .replace("{y}", String.valueOf(barrel.getLocation().getBlockY()))
//                                .replace("{z}", String.valueOf(barrel.getLocation().getBlockZ())));
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
            Location relativeLocation = baseLocation.clone().add(relativeVector.x(), relativeVector.y(), relativeVector.z());
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

    @EventHandler
    public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent event) {
//        Bukkit.broadcastMessage("Event pokrenut!");
        if (event.getClickedBlock() == null) return;
        if (dropLocation == null) return;

        if (event.getClickedBlock().getType() == org.bukkit.Material.BARREL && event.getClickedBlock().getLocation().equals(barrelLocation) && event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {

//            Bukkit.broadcastMessage("Otvoren drop!");
            if (compassBar != null) dropOpened();

        }
    }

    private static final int SHOW_DROP_MESSAGE_TICKS = 140; // 7 seconds (20 ticks per second)
    private int ticksElapsed = 0;

    public void dropOpened() {
        HandlerList.unregisterAll(this);
        Main.getInstance().setDropState(DropState.OPENED);

        ticksElapsed = 0;
        if (compassBar != null) {
            compassBar.setTitle(Main.getInstance().getConfigValues().getSupplyDropOpenedMessage());
            new BukkitRunnable() {
                @Override
                public void run() {
                    compassBar.remove();
                }
            }.runTaskLater(Main.getInstance(), SHOW_DROP_MESSAGE_TICKS);
        }

    }

    // Spawns a beacon beam at the drop center
    public void spawnBeaconWithBeam(World world, int x, int z) {
        int yBeacon = world.getHighestBlockYAt(x, z);
        Location beaconLoc = new Location(world, x, yBeacon, z);

        // Place 3x3 iron block base
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Location baseLoc = new Location(world, x + dx, yBeacon - 1, z + dz);
                baseLoc.getBlock().setType(Material.IRON_BLOCK);
            }
        }

        // Place the beacon
        beaconLoc.getBlock().setType(Material.BEACON);
        // Make sure nothing is above the beacon except air or glass
    }
}