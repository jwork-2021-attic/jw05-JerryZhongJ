package io.github.jerryzhongj.calabash_brothers;

public class Settings {
    public static final int PORT = 7777;
    public static final int SCHEDULED_POOL_SIZE = 4;

    public static final double PREF_HEIGHT = 800;
    public static final double PREF_WIDTH = 1000;

    public static final int UPDATE_RATE = 60;
    public static final int FPS = 24;

    public static final double BOUNDARY_SHORT = 10;
    public static final double BOUNDARY_LONG = 50;

    public static final double GRAVITY = 400;
    public static final double FRACTION = 200;
    public static final double MAX_SPEED = 400;

    public static final double MAX_HP = 100;
    public static final double MAX_MP = 100;

    public static final double DEFAULT_SPEED = 400;
    public static final double DEFAULT_DAMAGE = 4;   
    public static final double DEFAULT_MP_COST = 20;
    public static final long DEFAULT_SUPER_TIME = 6000;
    public static final double MP_GROW_PER_SENCOND = 1.5;
    
    public static final double ATTACK_HEIGHT = 100;
    public static final double ATTACK_LENGTH = 200;
    public static final double GET_PUNCH_VELOCITY_X = 150;
    public static final double GET_PUNCH_VELOCITY_Y = 150;
    
    
    public static final double BAR_WIDTH = 100;

    // I
    public static final double CALABASH_I_DAMAGE = DEFAULT_DAMAGE * 1.5;
    public static final double CALABASH_I_SUPER_DAMAGE = DEFAULT_DAMAGE * 2;

    // II
    public static final double CALABASH_II_SPEED = DEFAULT_SPEED * 1.2;
    public static final double CALABASH_II_RADIUS = 400;

    // III
    public static final double CALABASH_III_PROTECT = 0.7;
    public static final double CALABASH_III_SUPER_PROTECT = 0;
}
