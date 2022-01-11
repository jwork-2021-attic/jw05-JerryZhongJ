package io.github.jerryzhongj.calabash_brothers.server;

import java.util.HashMap;
import java.util.Map;

import io.github.jerryzhongj.calabash_brothers.server.World.Position;

public class SnapShot {

    static class CalabashStatus{
        public double hp;
        public double mp;
        public boolean facingRight;
        public boolean superMode;


        public CalabashStatus(double hp, double mp, boolean facingRight, boolean superMode) {
            this.hp = hp;
            this.mp = mp;
            this.facingRight = facingRight;
            this.superMode = superMode;
        }

    }

    public double width;
    public double height;
    public String background;
    public Map<Entity, World.Position> positions;
    public Map<CalabashBro, CalabashStatus> calabashes;

    public String winner;
    
    

    public SnapShot(double width, double height, String background, Map<Entity, Position> positions, Map<CalabashBro, CalabashStatus> calabashes) {
        this.width = width;
        this.height  = height;
        this.background = background;
        this.positions = positions;
        this.calabashes = calabashes;
    }

    public SnapShot(){
        this.width = 0;
        this.height = 0;
        this.positions = new HashMap<>();
        this.calabashes = new HashMap<>();
    }

}
