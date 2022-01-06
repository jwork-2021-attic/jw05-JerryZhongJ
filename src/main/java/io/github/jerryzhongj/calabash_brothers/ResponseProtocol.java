package io.github.jerryzhongj.calabash_brothers;

public class ResponseProtocol {

    // SET_SIZE w:double h:double
    public static final byte SET_SIZE = 0x0;

    // ADD id:byte type:byte. Types determine which image to post.
    public static final byte ADD = 0x2;

    // ANCHOR id:byte
    public static final byte ANCHOR = 0x3;

    // CLEAR id:byte. Remove id from world.
    public static final byte CLEAR = 0x4;

    public static final byte ALLCLEAR = 0x5;

    // SET_POS id:byte x:double y:double
    public static final byte SET_POS = 0x6;

    // SET_HP id:byte hp:double
    public static final byte SET_HP = 0x7;

    // SET_HP id:byte mp:double
    public static final byte SET_MP = 0x8;

    // SET_HP id name:string (max length: 30)
    public static final byte SET_NAME = 0x9;
    
    // COUNT_DOWN num:byte
    public static final byte COUNT_DOWN = 0xa;

    public static final byte WIN = 0xb;

    public static final byte LOSE = 0xc;
}
