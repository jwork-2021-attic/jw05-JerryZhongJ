package io.github.jerryzhongj.calabash_brothers.server;

import java.util.HashMap;
import java.util.Map;

import io.github.jerryzhongj.calabash_brothers.server.World.Position;

public class SnapShot {

    public double width;
    public double height;
    public String background;
    public Map<Entity, World.Position> positions;
    public Map<CalabashBro, Boolean> facings;
    public Map<CalabashBro, Double> hps;
    public Map<CalabashBro, Double> mps;

    public String winner;
    

    public SnapShot(double width, double height, String background, Map<Entity, Position> positions, Map<CalabashBro, Double> hps, Map<CalabashBro, Double> mps, Map<CalabashBro, Boolean> facings) {
        this.width = width;
        this.height  = height;
        this.background = background;
        this.positions = positions;
        this.hps = hps;
        this.mps = mps;
        this.facings = facings;
    }

    public SnapShot(){
        this.width = 0;
        this.height = 0;
        this.positions = new HashMap<>();
        this.hps = new HashMap<>();
        this.mps = new HashMap<>();
        this.facings = new HashMap<>();
    }

}
