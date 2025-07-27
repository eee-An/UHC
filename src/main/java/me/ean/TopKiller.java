// TopKiller.java
package me.ean;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class TopKiller implements Comparable<TopKiller> {
    private final UUID uuid;
    private final String playerName;
    private int kills;

    public TopKiller(Player player, int kills) {
        this.uuid = player.getUniqueId();
        this.playerName = player.getName();
        this.kills = kills;
    }

    @Deprecated
    public Player getPlayer() {
        // Ne koristiti ovo jer ne garantiramo da je igrač online!
        // Sve što nam treba o ovom igraču trebamo posebno pohraniti ovdje
        // Kao playerName
        return Bukkit.getPlayer(uuid);
    }

    public void incrementKills() { kills++; }

    @Override
    public int compareTo(TopKiller other) {
        return Integer.compare(other.kills, this.kills); // Descending
    }
}