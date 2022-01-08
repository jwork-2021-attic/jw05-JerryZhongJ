package io.github.jerryzhongj.calabash_brothers;

public class ResponseProtocol {

    // SET_SIZE w:double h:double
    public static final byte SET_SIZE = 0x0;

    // SET_BACKGROUND background name
    public static final byte SET_BACKGROUND = 0x1;

    // ADD id:int type:byte. Types determine which image to post.
    public static final byte ADD = 0x2;

    // ANCHOR id:int
    public static final byte ANCHOR = 0x3;

    // CLEAR id:int. Remove id from world.
    public static final byte CLEAR = 0x4;

    public static final byte ALLCLEAR = 0x5;

    // SET_POS id:int x:double y:double
    public static final byte SET_POS = 0x6;

    // SET_HP id:int hp:double
    public static final byte SET_HP = 0x7;

    // SET_HP id:int mp:double
    public static final byte SET_MP = 0x8;

    // SET_HP id name:string (max length: 30)
    public static final byte SET_NAME = 0x9;
    
    // COUNT_DOWN num:byte
    public static final byte COUNT_DOWN = 0xa;

    // WINNER name:string
    public static final byte WINNER = 0xb;

    public static final byte LOSE = 0xc;

    public static final byte WAIT = 0xd;
}
