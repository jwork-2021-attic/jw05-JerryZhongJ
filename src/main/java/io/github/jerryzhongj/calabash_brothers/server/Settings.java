package io.github.jerryzhongj.calabash_brothers.server;

class Settings {
    // Thread
    static final int SCHEDULED_POOL_SIZE = 4;
    
    // Game

    static final int UPDATE_RATE = 60;

    static final double BOUNDARY_SHORT = 3;
    static final double BOUNDARY_LONG = 20;

    static final double GRAVITY = 10;
    static final double MAX_FALL_SPEED = 50;

    static final double DEFAULT_SPEED = 20;
    static final double INITIAL_HP = 100;
    static final double DEFAULT_DAMAGE = 10;   
    
    static final double ATTACK_HEIGHT = 10;
    static final double ATTACK_LENGTH = 20;
    static final double GET_PUNCH_VELOCITY_X = 10;
    static final double GET_PUNCH_VELOCITY_Y = 10;
    
    static final double SUPER_MP_COST = 20;
    static final int SUPER_TIME_LIMIT = 10000;

    // I
    static final double BRO_I_BUFF = 1.5;

    // III
    static final double BRO_III_INITIAL_PROTECT = 0.7;
    static final double BRO_III_SUPER_PROTECT = 0;

    static final double CALABASH_HEIGHT = 6;
    static final double CALABASH_WIDTH = 6;

    static final double CONCRETE_WIDTH = 20;
    static final double CONCRETE_HEIGHT = 10;

}
