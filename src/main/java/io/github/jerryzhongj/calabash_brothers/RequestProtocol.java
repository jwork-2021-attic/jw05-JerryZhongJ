package io.github.jerryzhongj.calabash_brothers;

public class RequestProtocol {
    
    // SET_NAME name:string
    public static final byte SET_NAME = 0x0;
    // SET_CALABASH calabash
    public static final byte SET_CALABASH = 0x1;
    public static final byte MOVE_LEFT = 0x2;
    public static final byte MOVE_RIGHT = 0x3;
    public static final byte JUMP = 0x4;
    public static final byte SUPERMODE = 0x5;
}
