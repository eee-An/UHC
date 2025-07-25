// TopKiller.java
package me.ean;

import org.bukkit.entity.Player;

public class TopKiller implements Comparable<TopKiller> {
    private final Player player;
    private int kills;

    public TopKiller(Player player, int kills) {
        this.player = player;
        this.kills = kills;
    }

    public Player getPlayer() { return player; }
    public int getKills() { return kills; }
    public void incrementKills() { kills++; }

    @Override
    public int compareTo(TopKiller other) {
        return Integer.compare(other.kills, this.kills); // Descending
    }
}