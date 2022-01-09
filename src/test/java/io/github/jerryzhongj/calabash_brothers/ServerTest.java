package io.github.jerryzhongj.calabash_brothers;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.github.jerryzhongj.calabash_brothers.server.CalabashBro;
import io.github.jerryzhongj.calabash_brothers.server.Entity;
import io.github.jerryzhongj.calabash_brothers.server.Server;
import io.github.jerryzhongj.calabash_brothers.server.SnapShot;
import io.github.jerryzhongj.calabash_brothers.server.World;
import io.github.jerryzhongj.calabash_brothers.server.World.Position;

public class ServerTest {
    Server server;
    
    @Before
    public void startServer(){
        try {
            server = new Server(new Loader());    
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    @Test
    public void testServer() throws InterruptedException{
        final int playerNum = 5;
        for(int i = 0;i < playerNum;i++){
            int ii = i;
            new Thread(()->{
                try (Socket socket = new Socket("localhost", Settings.PORT)) {
                    DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                    Thread.sleep((long)(Math.random() * 1000));
                    byte[] bytes = ("Player " + ii).getBytes();
                    output.writeByte(RequestProtocol.SET_NAME);
                    output.writeInt(bytes.length);
                    output.write(bytes);
                    Thread.sleep((long)(Math.random() * 1000));
                    output.writeByte(RequestProtocol.SET_CALABASH);
                    output.writeInt(EntityType.CALABASH_BRO_I.getCode());
                    System.out.printf("Player %d done.\n", ii);
                    while(true){
                        ;
                    }
                } catch (IOException|InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }).start();
        }

        
        Thread.sleep(5000);
        server.startWorld("test");
        World world = server.getWorld();

        new Thread(()->{
            Position[] positions = new Position[playerNum];
            try {
                while(true){
                    Thread.sleep(1000 / 24);
                    SnapShot snapshot = world.getSnapShot();
                    for(Map.Entry<Entity, Position> entry : snapshot.positions.entrySet()){
                        Entity e = entry.getKey();
                        Position pos = entry.getValue();
                        if(e instanceof CalabashBro){
                            int num = Integer.valueOf(((CalabashBro)e).getName().substring(7));
                            positions[num] = pos;
                        }
                    }
                    for(int i = 0;i < playerNum;i++)
                        System.out.print(positions[i].toString() + "\t");
                    System.out.print("\n");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        
        Thread.sleep(10000);
        server.close();
        Assert.assertTrue(true);
    
    }

    @Test
    public void testResponse() throws InterruptedException{
        final int playerNum = 2;
        for(int i = 0;i < playerNum;i++){
            int ii = i;
            new Thread(()->{
                try (Socket socket = new Socket("localhost", Settings.PORT)) {
                    DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                    DataInputStream input = new DataInputStream(socket.getInputStream());

                    Thread.sleep((long)(Math.random() * 1000));
                    byte[] bytes = ("Player " + ii).getBytes();
                    output.writeByte(RequestProtocol.SET_NAME);
                    output.writeInt(bytes.length);
                    output.write(bytes);
                    Thread.sleep((long)(Math.random() * 1000));
                    output.writeByte(RequestProtocol.SET_CALABASH);
                    output.writeInt(EntityType.CALABASH_BRO_I.getCode());
                    System.out.printf("Player %d done.\n", ii);

                    while(true){
                        byte command = input.readByte();
                        switch(command){
                            case ResponseProtocol.ADD:
                                int id = input.readInt();
                                EntityType type = EntityType.getType(input.readInt());
                                System.out.printf("Player %d: ADD %d %s.\n", ii, id, type.getName());
                                break;
                            case ResponseProtocol.SET_POS:
                                id = input.readInt();
                                double x = input.readDouble();
                                double y = input.readDouble();
                                
                                System.out.printf("Player %d: SET_POS %d %f %f.\n", ii, id, x, y);
                                break;
                            case ResponseProtocol.SET_NAME:
                                id = input.readInt();
                                int byte_len = input.readInt();
                                bytes = new byte[byte_len];
                                input.read(bytes);
                                System.out.printf("Player %d: SET_NAME %d %s.\n", ii, id, new String(bytes));
                                break;
                            case ResponseProtocol.SET_HP:
                                id = input.readInt();
                                Double hp = input.readDouble();
                                System.out.printf("Player %d: SET_HP %d %f.\n", ii, id, hp);
                                break;
                            case ResponseProtocol.SET_MP:
                                id = input.readInt();
                                Double mp = input.readDouble();
                                System.out.printf("Player %d: SET_MP %d %f.\n", ii, id, mp);
                                break;
                            case ResponseProtocol.CLEAR:
                                id = input.readInt();
                                System.out.printf("Player %d: CLEAR %d.\n", ii, id);
                                break;
                            case ResponseProtocol.ANCHOR:
                                id = input.readInt();
                                System.out.printf("Player %d: ANCHOR %d.\n", ii, id);
                                break;
                            case ResponseProtocol.SET_SIZE:
                                double w = input.readDouble();
                                double h = input.readDouble();
                                System.out.printf("Player %d: SET_SIZE %f %f.\n", ii, w, h);
                                break;
                            case ResponseProtocol.SET_BACKGROUND:
                                byte_len = input.readInt();
                                bytes = new byte[byte_len];
                                input.read(bytes);
                                System.out.printf("Player %d: SET_BACKGROUND %s.\n", ii, new String(bytes));
                                break;
                            case ResponseProtocol.WINNER:
                                //TODO
                                break;
            
                            case ResponseProtocol.LOSE:
                                //TODO
                                break;
                        }
                    }
                } catch (IOException|InterruptedException e) {
                    
                }
            }).start();
        }

        Thread.sleep(5000);
        server.startWorld("test");
        Thread.sleep(20000);

    }
}
