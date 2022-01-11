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
import io.github.jerryzhongj.calabash_brothers.ThreadPool;
import io.github.jerryzhongj.calabash_brothers.server.SnapShot.CalabashStatus;
import io.github.jerryzhongj.calabash_brothers.server.World.Position;
import javafx.util.Pair;
import lombok.Getter;

public class Player implements Subscriber<SnapShot>{
    
    // This is used when setting up the world
    @Getter
    private EntityType calabashType;
    @Getter
    private String name;
    @Getter
    private CalabashBro calabash = null;
    @Getter
    private SocketChannel client;
    @Getter
    private boolean online;

    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

    
    private boolean firstReponse = true;
    public Player(SocketChannel client){
        this.client = client;
        online = true;
    }

    void read(){
        try {
            client.read(readBuffer);
            readBuffer.flip();

            readBuffer.mark();
            try{
                while(true){
                    byte command = readBuffer.get();
                    // System.out.printf("%s get command %h\n", name, command);
                    switch(command){
                        case RequestProtocol.SET_NAME:
                            name = readString(readBuffer);
                            // System.out.printf("%s: SET_NAME %s.\n", name, name);
                            break;
                        case RequestProtocol.SET_CALABASH:
                            calabashType = EntityType.getType(readBuffer.getInt());
                            // System.out.printf("%s: SET_CALABASH %s.\n", name, calabashType.getName());
                            break;
                        case RequestProtocol.MOVE_LEFT:
                            if(calabash != null && calabash.isAlive())
                                calabash.moveLeft();
                            // System.out.printf("%s: MOVE_LEFT.\n", name);
                            break;
                        case RequestProtocol.MOVE_RIGHT:
                            if(calabash != null && calabash.isAlive())
                                calabash.moveRight();
                            // System.out.printf("%s: MOVE_RIGHT.\n", name);
                            break;
                        case RequestProtocol.STOP:
                            if(calabash != null && calabash.isAlive())
                                calabash.stop();
                            // System.out.printf("%s: STOP.\n", name);
                            break;
                        case RequestProtocol.JUMP:
                            if(calabash != null && calabash.isAlive())
                                calabash.jump();
                            // System.out.printf("%s: JUMP.\n", name);
                            break;
                        case RequestProtocol.PUNCH:
                            if(calabash != null && calabash.isAlive())
                                calabash.punch();
                            // System.out.printf("%s: PUNCH.\n", name);
                            break;
                        case RequestProtocol.SUPERMODE:
                            if(calabash != null && calabash.isAlive())
                                calabash.superMode();
                            break;
                    }
                
                // Mark when a whole command complete
                readBuffer.mark();
               }
            }catch(BufferUnderflowException e){
                readBuffer.reset();
            }
            readBuffer.compact();
        } catch (IOException e) {
            e.printStackTrace();
            online = false;
            System.out.println(name + " disconnect.");
            try {
                client.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        
    }
    
    public void write(){
        synchronized(writeBuffer){
            try {
                writeBuffer.flip();
                client.write(writeBuffer);
            } catch (IOException e) {
                e.printStackTrace();
                online = false;
                System.out.println(name + " disconnect.");
                try {
                    client.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }finally{
                writeBuffer.compact();
                writeBuffer.notifyAll();
            }

        }
        


    }

    private String readString(ByteBuffer buffer) throws BufferUnderflowException{
        int byte_len = buffer.getInt();
        byte[] bytes = new byte[byte_len];
        buffer.get(bytes);
        return new String(bytes);
    }

    private void writeBuffer(byte b) throws InterruptedException{
        while(true){
            try{
                writeBuffer.put(b);
                break;
            }catch(BufferOverflowException e){
                writeBuffer.wait();
            }
        }   
    }

    private void writeBuffer(int i) throws InterruptedException{
        
        while(true){
            try{
                writeBuffer.putInt(i);
                break;
            }catch(BufferOverflowException e){
                writeBuffer.wait();
            }
        }
            
    }

    private void writeBuffer(double d) throws InterruptedException{
        
        while(true){
            try{
                writeBuffer.putDouble(d);
                break;
            }catch(BufferOverflowException e){
                writeBuffer.wait();
            }
        }
            
    }

    private void writeBuffer(String s) throws InterruptedException{
        byte[] str = s.getBytes();
    
        writeBuffer(str.length);
        for(int i = 0;i < str.length;){
            try{
                writeBuffer.put(str[i]);
                i++;
            }catch(BufferOverflowException e){
                writeBuffer.wait();
            }   
        }
        
    }

    private void writeBuffer(boolean b) throws InterruptedException{
        while(true){
            try{
                writeBuffer.putInt(b ? 1 : 0);
                break;
            }catch(BufferOverflowException e){
                writeBuffer.wait();
            }
        }
            
    }

    boolean isReady(){
        return online && name != null && calabashType != null;
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
        System.exit(1);
    }

    private SnapShot oldSnapShot = new SnapShot();
    @Override
    public void onNext(SnapShot item) {
        
        Map<Entity, World.Position> positions = new HashMap<>(item.positions);
        Map<CalabashBro, CalabashStatus> calabashes = new HashMap<>(item.calabashes);
        double width = item.width;
        double height = item.height;
        String background = item.background;

        // Some special process
        for(Map.Entry<Entity, World.Position> entry : item.positions.entrySet()){
            Entity e = entry.getKey();
            if(e instanceof CalabashBroVI){
                CalabashBroVI bro6 = (CalabashBroVI)e;
                if(bro6.isInvisible() && bro6 != calabash){
                    positions.remove(bro6);
                    calabashes.remove(bro6);
                }
            }
        }
        synchronized(writeBuffer){
            try{
                // Start to write
                if(firstReponse)
                    writeBuffer(ResponseProtocol.START_GAME);
                
                // SET width height
                if(Math.abs(oldSnapShot.width - width) > 0.5 || Math.abs(oldSnapShot.height - height) > 0.5){
                    writeBuffer(ResponseProtocol.SET_SIZE);
                    writeBuffer(width);
                    writeBuffer(height);
                }else{
                    height = oldSnapShot.height;
                    width = oldSnapShot.width;
                }
    
                // SET_BACKGROUND
                if(!background.equals(oldSnapShot.background)){
                    writeBuffer(ResponseProtocol.SET_BACKGROUND);
                    writeBuffer(background);
                }
    
                // ADD and SET
                for(Map.Entry<Entity, World.Position> entry : positions.entrySet()){
                    Entity e = entry.getKey();
                
                    Position pos = entry.getValue();
                    Position oldPos = oldSnapShot.positions.get(e);
                    
                    if(oldPos == null){
                        writeBuffer(ResponseProtocol.ADD);
                        writeBuffer(e.hashCode());
                        writeBuffer(e.getType().getCode());
                    }
    
                    if(oldPos == null || Math.abs(oldPos.x - pos.x) > 0.5 || Math.abs(oldPos.y - pos.y) > 0.5){
                        writeBuffer(ResponseProtocol.SET_POS);
                        writeBuffer(e.hashCode());
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
    
                // SET HP
                for(Map.Entry<CalabashBro, CalabashStatus> entry : calabashes.entrySet()){
                    CalabashBro bro = entry.getKey();
                    CalabashStatus status = entry.getValue();
                    CalabashStatus oldStatus = oldSnapShot.calabashes.get(bro);

                    if(oldStatus == null || Math.abs(status.hp - oldStatus.hp) > 0.1){
                        writeBuffer(ResponseProtocol.SET_HP);
                        writeBuffer(bro.hashCode());
                        writeBuffer(status.hp);
                    }else{
                        status.hp = oldStatus.hp;
                    }

                    if(oldStatus == null || Math.abs(status.mp - oldStatus.mp) > 0.1){
                        writeBuffer(ResponseProtocol.SET_MP);
                        writeBuffer(bro.hashCode());
                        writeBuffer(status.mp);
                    }else{
                        status.mp = oldStatus.mp;
                    }

                    if(oldStatus == null || oldStatus.facingRight != status.facingRight){
                        writeBuffer(ResponseProtocol.SET_FACING);
                        writeBuffer(bro.hashCode());
                        writeBuffer(status.facingRight);
                    }

                    if(oldStatus == null || oldStatus.superMode != status.superMode){
                        writeBuffer(ResponseProtocol.SET_SUPERMODE);
                        writeBuffer(bro.hashCode());
                        writeBuffer(status.superMode);
                    }
                }
    
                
    
                if(firstReponse){
                    writeBuffer(ResponseProtocol.ANCHOR);
                    writeBuffer(calabash.hashCode());
                    for(CalabashBro bro : calabashes.keySet()){
                        writeBuffer(ResponseProtocol.SET_NAME);
                        writeBuffer(bro.hashCode());
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
                return;
            }
        }
        
        

        // Save old snapshot
        oldSnapShot = new SnapShot(width, height, background, positions, calabashes);
        
        subscription.request(1);
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
        
    }

    public void sendPlayerList(Pair<String, EntityType>[] list){
        // Should not block!
        ThreadPool.nonScheduled.submit(()->{
           synchronized(writeBuffer){
            try {
                writeBuffer(ResponseProtocol.PLAYER_LIST);
                writeBuffer(list.length);
                for(Pair<String, EntityType> pair : list){
                    writeBuffer(pair.getKey());
                    writeBuffer(pair.getValue().getCode());
                }
            } catch (InterruptedException e) {
                
                e.printStackTrace();
                return;
            }
           } 
        });
    }

}
