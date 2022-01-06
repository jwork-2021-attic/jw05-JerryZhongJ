package io.github.jerryzhongj.calabash_brothers.server;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class Player {
    // This detemine how we handle the request.
    private boolean alive = true;
    // This is used when setting up the world
    private String characterName;
    // This 
    private String name;

    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer writeBuffer = ByteBuffer.allocate(1024);


    void read(SocketChannel client){
        // TODO
    }
    
    void write(SocketChannel client){
        // TODO

    }

    boolean isReady(){
        return name != null && characterName != null;
    }
}
