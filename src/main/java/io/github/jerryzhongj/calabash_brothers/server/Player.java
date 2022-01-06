package io.github.jerryzhongj.calabash_brothers.server;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import io.github.jerryzhongj.calabash_brothers.RequestProtocol;
import lombok.Getter;

class Player {
    
    // This is used when setting up the world
    @Getter
    private byte calabashType = 0x00;
    @Getter
    private String name;

    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

    private CalabashBro calabash = null;

    void read(SocketChannel client){
        try {
            client.read(readBuffer);
            readBuffer.mark();
            while(true){
                try{
                    byte command = readBuffer.get();
                    switch(command){
                        case RequestProtocol.SET_NAME:
                            name = bufferGetString(readBuffer);
                            break;
                        case RequestProtocol.SET_CALABASH:
                            calabashType = readBuffer.get();
                            break;
                        case RequestProtocol.MOVE_LEFT:
                            if(calabash != null && calabash.isAlive())
                                calabash.moveLeft();
                                break;
                        case RequestProtocol.MOVE_RIGHT:
                            if(calabash != null && calabash.isAlive())
                            calabash.moveRight();
                            break;
                        case RequestProtocol.JUMP:
                            if(calabash != null && calabash.isAlive())
                            calabash.jump();
                            break;
                        case RequestProtocol.SUPERMODE:
                            if(calabash != null && calabash.isAlive())
                            calabash.superfy();
                            break;
                    }
                    // Mark when a whole command complete
                    readBuffer.mark();
                }catch(BufferUnderflowException e){
                    break;
                }
            }
            readBuffer.compact();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    void write(SocketChannel client){
        // TODO

    }

    private String bufferGetString(ByteBuffer buffer) throws BufferUnderflowException{
        StringBuilder sb = new StringBuilder();
        char c;
        do{
            c = buffer.getChar();
            sb.append(c);
        }while(c != '\0');
        return sb.toString();
    }

    boolean isReady(){
        return name != null && calabashType != 0x00;
    }

    void control(CalabashBro calabash){
        this.calabash = calabash;
    }
}
