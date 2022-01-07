package io.github.jerryzhongj.calabash_brothers.server;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import java.util.Map;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;



import io.github.jerryzhongj.calabash_brothers.EntityType;
import io.github.jerryzhongj.calabash_brothers.RequestProtocol;
import io.github.jerryzhongj.calabash_brothers.ResponseProtocol;
import io.github.jerryzhongj.calabash_brothers.server.World.Position;

import lombok.Getter;

class Player implements Subscriber<SnapShot>{
    
    // This is used when setting up the world
    @Getter
    private byte calabashType = 0x00;
    @Getter
    private String name;

    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

    private CalabashBro calabash = null;
    private boolean firstReponse = true;

    void read(SocketChannel client){
        try {
            client.read(readBuffer);
            readBuffer.flip();
            readBuffer.mark();
            while(true){
                try{
                    byte command = readBuffer.get();
                    switch(command){
                        case RequestProtocol.SET_NAME:
                            name = getString(readBuffer);
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
            
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            readBuffer.compact();
        }
    }
    
    void write(SocketChannel client){
        synchronized(writeBuffer){
            
            try {
                writeBuffer.flip();
                client.write(writeBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                writeBuffer.compact();
                writeBuffer.notifyAll();
            }

        }
        


    }

    private String getString(ByteBuffer buffer) throws BufferUnderflowException{
        StringBuilder sb = new StringBuilder();
        char c;
        do{
            c = buffer.getChar();
            sb.append(c);
        }while(c != '\0');
        return sb.toString();
    }

    private void writeBuffer(byte b) throws InterruptedException{
        synchronized(writeBuffer){
            try{
                writeBuffer.put(b);
            }catch(BufferOverflowException e){
                writeBuffer.wait();
                writeBuffer.put(b);
            }
        }
    }

    private void writeBuffer(int i) throws InterruptedException{
        synchronized(writeBuffer){
            try{
                writeBuffer.putInt(i);
            }catch(BufferOverflowException e){
                writeBuffer.wait();
                writeBuffer.putInt(i);
            }
        }
    }

    private void writeBuffer(double d) throws InterruptedException{
        synchronized(writeBuffer){
            try{
                writeBuffer.putDouble(d);
            }catch(BufferOverflowException e){
                writeBuffer.wait();
                writeBuffer.putDouble(d);
            }
        }
    }

    private void writeBuffer(String s) throws InterruptedException{
        byte[] str = s.getBytes();
        synchronized(writeBuffer){
            for(int i = 0;i < str.length;){
                try{
                    writeBuffer.put(str[i]);
                    i++;
                }catch(BufferOverflowException e){
                    writeBuffer.wait();
                }   
            }
        }
    }

    boolean isReady(){
        return name != null && calabashType != 0x00;
    }

    void control(CalabashBro calabash){
        this.calabash = calabash;
    }


    private Subscription subscription;

    @Override
    public void onComplete() {
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
        
    }

    private SnapShot oldSnapShot = new SnapShot();
    @Override
    public void onNext(SnapShot item) {
        
        Map<Entity, World.Position> positions = new HashMap<>(item.positions);;
        Map<CalabashBro, Double> hps = new HashMap<>(item.hps);
        Map<CalabashBro, Double> mps = new HashMap<>(item.mps);
        double width = item.width;
        double height = item.height;
        
        // Some special process
        for(Map.Entry<Entity, World.Position> entry : item.positions.entrySet()){
            Entity e = entry.getKey();
            if(e instanceof CalabashBroVI){
                CalabashBroVI bro6 = (CalabashBroVI)e;
                if(bro6.isInvisible() && bro6 != calabash){
                    positions.remove(bro6);
                    hps.remove(bro6);
                    mps.remove(bro6);
                }
                    
            }
        }

        try{
            // Start to write
            // SET width height
            if(Math.abs(oldSnapShot.width - width) > 0.5 || Math.abs(oldSnapShot.height - height) > 0.5){
                writeBuffer(ResponseProtocol.SET_SIZE);
                writeBuffer(width);
                writeBuffer(height);
            }else{
                height = oldSnapShot.height;
                width = oldSnapShot.width;
            }

            // ADD and SET
            for(Map.Entry<Entity, World.Position> entry : positions.entrySet()){
                Entity e = entry.getKey();
            
                Position pos = entry.getValue();
                Position oldPos = oldSnapShot.positions.get(e);
                
                if(oldPos == null){
                    writeBuffer(ResponseProtocol.ADD);
                    writeBuffer(e.hashCode());
                    if(e instanceof CalabashBroI)
                        writeBuffer(EntityType.CALABASH_BRO_I);
                    if(e instanceof CalabashBroIII)
                        writeBuffer(EntityType.CALABASH_BRO_III);
                    if(e instanceof Concrete)
                        writeBuffer(EntityType.CONCRETE);
                    // TODO: more types

                }

                if(oldPos == null || Math.abs(oldPos.x - pos.x) > 0.5 || Math.abs(oldPos.y - pos.y) > 0.5){
                    writeBuffer(ResponseProtocol.SET_POS);
                    writeBuffer(pos.x);
                    writeBuffer(pos.y);
                }else{
                    entry.setValue(oldPos);
                }

                // for CLEAR
                oldSnapShot.positions.remove(e);
            }

            // CLEAR 
            for(Map.Entry<Entity, World.Position> entry : oldSnapShot.positions.entrySet()){
                writeBuffer(ResponseProtocol.CLEAR);
                writeBuffer(entry.getKey().hashCode());
            }

            // SET HP MP
            for(Map.Entry<CalabashBro, Double> entry : hps.entrySet()){
                CalabashBro bro = entry.getKey();
                double hp = entry.getValue();
                Double oldHp = oldSnapShot.hps.get(bro);
                if(oldHp == null || Math.abs(hp - oldHp) < 0.1){
                    writeBuffer(ResponseProtocol.SET_HP);
                    writeBuffer(bro.hashCode());
                    writeBuffer(hp);
                }else{
                    entry.setValue(oldHp);
                }
                
            }

            for(Map.Entry<CalabashBro, Double> entry : mps.entrySet()){
                CalabashBro bro = entry.getKey();
                double mp = entry.getValue();
                Double oldMp = oldSnapShot.mps.get(bro);
                if(oldMp == null || Math.abs(mp - oldMp) < 0.1){
                    writeBuffer(ResponseProtocol.SET_MP);
                    writeBuffer(bro.hashCode());
                    writeBuffer(mp);
                }else{
                    entry.setValue(oldMp);
                }
            }

            
            if(firstReponse){
                writeBuffer(ResponseProtocol.ANCHOR);
                writeBuffer(calabash.hashCode());
                for(CalabashBro bro : hps.keySet()){
                    writeBuffer(ResponseProtocol.SET_NAME);
                    writeBuffer(bro.getName());
                }
                firstReponse = false;
            }

            if(!calabash.isAlive())
                writeBuffer(ResponseProtocol.LOSE);

            // WINNER
            if(item.winner != null){
                writeBuffer(ResponseProtocol.WINNER);
                writeBuffer(item.winner);
            }
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        

        // Save old snapshot
        oldSnapShot = new SnapShot(width, height, positions, hps, mps);
        
        subscription.request(1);
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
        
    }
}
