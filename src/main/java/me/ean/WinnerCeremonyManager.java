package me.ean;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class WinnerCeremonyManager {

    private final Main main;

    public WinnerCeremonyManager(Main main) {
        this.main = main;
    }

    public void celebrateWinner(Player winner, Location ceremonyLocation) {
        if (winner == null || ceremonyLocation == null) return;

        winner.teleport(ceremonyLocation);
        World world = ceremonyLocation.getWorld();
        if (world == null) return;

        Color[] colors = {Color.YELLOW, Color.RED, Color.ORANGE};

        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count++ >= 10) {
                    this.cancel();
                    return;
                }
                for (int i = 0; i < 6; i++) {
                    double angle = Math.toRadians(i * 60);
                    double xOffset = Math.cos(angle) * 2;
                    double zOffset = Math.sin(angle) * 2;
                    Location fireworkLoc = winner.getLocation().clone().add(xOffset, 1, zOffset);

                    Firework firework = (Firework) world.spawnEntity(fireworkLoc, EntityType.FIREWORK);
                    FireworkMeta meta = firework.getFireworkMeta();
                    meta.addEffect(FireworkEffect.builder()
                            .withColor(colors[i % 3])
                            .with(Type.BURST)
                            .trail(true)
                            .flicker(true)
                            .build());
                    meta.setPower(0);
                    firework.setFireworkMeta(meta);
                }
            }
        }.runTaskTimer(main, 0L, 40L);
    }
}