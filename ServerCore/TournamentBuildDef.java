package com.rscr.server.external;

import com.rscr.server.model.container.Item;

/**
 * Created by tomassimkus on 22/02/2018.
 */
public class TournamentBuildDef {

    public int id;

    public int amount;

    public int[] stats;

    public Item[] gear;

    public Item[] supplies;

    public int[] getStats() { return stats; }

    public Item[] getGear() {
        return gear; }

    public Item[] getSupplies() {
        return supplies; }
}
