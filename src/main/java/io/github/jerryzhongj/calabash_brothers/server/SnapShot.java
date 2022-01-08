package io.github.jerryzhongj.calabash_brothers.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.github.jerryzhongj.calabash_brothers.server.World.Position;

class SnapShot {

    double width;
    double height;
    String background;
    Map<Entity, World.Position> positions;

    Map<CalabashBro, Double> hps;
    Map<CalabashBro, Double> mps;

    String winner;
    

    public SnapShot(double width, double height, String background, Map<Entity, Position> positions, Map<CalabashBro, Double> hps, Map<CalabashBro, Double> mps) {
        this.width = width;
        this.height  = height;
        this.positions = positions;
        this.hps = hps;
        this.mps = mps;
       
    }

    public SnapShot(){
        this.width = 0;
        this.height = 0;
        this.positions = new HashMap<>();
        this.hps = new HashMap<>();
        this.mps = new HashMap<>();
    }

}
