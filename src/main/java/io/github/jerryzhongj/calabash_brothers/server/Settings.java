package io.github.jerryzhongj.calabash_brothers.server;

class Settings {
    // Thread
    static final int SCHEDULED_POOL_SIZE = 2;
    
    // Game

    static final int FPS = 60;

    static final double BOUDARY_SHORT = 1;
    static final double BOUDARY_LONG = 10;

    static final double GRAVITY = 10;
    static final double MAX_FALL_SPEED = 30;

    static final double DEFAULT_SPEED = 20;
    static final double INITIAL_HP = 100;
    static final double DEFAULT_DAMAGE = 10;   
    
    static final double ATTACK_HEIGHT = 20;
    static final double ATTACK_LENGTH = 50;
    static final double GET_PUNCH_VELOCITY_X = 10;
    static final double GET_PUNCH_VELOCITY_Y = 10;
    
    static final double SUPER_MP_COST = 20;
    static final int SUPER_TIME_LIMIT = 10000;

    // I
    static final double BRO_I_BUFF = 1.5;

    // III
    static final double BRO_III_INITIAL_PROTECT = 0.7;
    static final double BRO_III_SUPER_PROTECT = 0;


}
