package io.github.jerryzhongj.calabash_brothers.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.jerryzhongj.calabash_brothers.GlobalSettings;

// 不同玩家收到的数据是不同的，但大部分相同
// 不同玩家需要根据共同的profile进行修改
public class Server {
    private Selector selector;
    private List<Player> players= new LinkedList<>();
    private Loader loader = new Loader();

    public Server() throws IOException{
       
        selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress("localhost", GlobalSettings.PORT));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        
        ThreadPool.nonScheduled.submit(()->{
            // TODO: use exception handler
            try{
                daemon();
            }catch(IOException e){
                e.printStackTrace();
            }
        });
    }

    public void startWorld(){
        // Remove unready player
        Iterator<Player> it = players.iterator();
        while(it.hasNext()){
            if(!it.next().isReady())
                it.remove();
        }

        World world = loader.loadInitialWorld("default");
        // TODO
        world.setPlayers();
        world.ready();
        world.resume();
    }

    void daemon() throws IOException{
        while(true){
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectedKeys.iterator();
            while(it.hasNext()){
                SelectionKey key = it.next();
                if(key.isAcceptable()){
                    ServerSocketChannel server = (ServerSocketChannel)key.channel(); 
                    SocketChannel client = server.accept();
                    client.configureBlocking(false);
                    Player player = new Player();
                    client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, player);
                    players.add(player);
                }

                if(key.isReadable()){
                    SocketChannel client = (SocketChannel)key.channel();
                    Player player = (Player)key.attachment();
                    player.read(client);
                }

                if(key.isWritable()){
                    SocketChannel client = (SocketChannel)key.channel();
                    Player player = (Player)key.attachment();
                    player.write(client);
                }
                it.remove();
            }
            
        }
    }

    public void stop(){

    }
}
