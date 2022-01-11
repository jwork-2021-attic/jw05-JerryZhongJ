package io.github.jerryzhongj.calabash_brothers.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

import io.github.jerryzhongj.calabash_brothers.EntityType;
import io.github.jerryzhongj.calabash_brothers.Loader;
import io.github.jerryzhongj.calabash_brothers.Settings;
import io.github.jerryzhongj.calabash_brothers.ThreadPool;
import javafx.util.Pair;
import lombok.Getter;

// 不同玩家收到的数据是不同的，但大部分相同
// 不同玩家需要根据共同的profile进行修改
public class Server {
    private Selector selector;
    private SubmissionPublisher<SnapShot> publisher = new SubmissionPublisher<>();
    @Getter
    private List<Player> players= new LinkedList<>();
    @Getter
    private Loader loader;
    @Getter
    private World world;
    private ScheduledFuture<?> publishTask;
    private List<Channel> channels = new LinkedList<>();
    private Future<?> daemon;
    private ScheduledFuture<?> sendPlayerList;
    public Server(Loader loader) throws IOException{
        this.loader = loader;
        selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress("localhost", Settings.PORT));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        channels.add(serverChannel);

        daemon = ThreadPool.nonScheduled.submit(()->{
            // TODO: use exception handler
            try{
                daemon();
            }catch(IOException e){
                e.printStackTrace();
            }catch(ClosedSelectorException e){
                // just quit;
            }
        });

        sendPlayerList = ThreadPool.scheduled.scheduleAtFixedRate(()->{
            sendPlayerList();
        }, 0, 500, TimeUnit.MILLISECONDS);
        
    }

    public void startWorld(String mapName){
        sendPlayerList.cancel(false);

        world = loader.loadInitialWorld(mapName);
        
        for(Player player : players){
            if(!player.isReady())
                continue;
            
            world.setPlayer(player);
            publisher.subscribe(player);
        }

        world.resume();

        publishTask = ThreadPool.scheduled.scheduleAtFixedRate(()->{
            try{
                publisher.submit(world.getSnapShot());
            }catch(Exception e){
                e.printStackTrace();
            }
        }, 0, 1000 / Settings.FPS, TimeUnit.MILLISECONDS);
        

    }

    private void sendPlayerList(){
        // Call every player send a new player list
        Iterator<Player> it = players.iterator();
        while(it.hasNext()){
            Player p = it.next();
            if(!p.isOnline())
                it.remove();
        }
        int i = 0;
        Pair<String, EntityType>[] list = new Pair[players.size()];
        for(Player p : players) {
            list[i] = new Pair<>(p.getName(), p.getCalabashType());
            i++;
        }
        // System.out.println("Send " + list.length);
        for(Player p : players)
            p.sendPlayerList(list);
    }

    private void daemon() throws IOException{
        while(true){
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectedKeys.iterator();
            while(it.hasNext()){
                SelectionKey key = it.next();
                if(key.isAcceptable()){
                    ServerSocketChannel server = (ServerSocketChannel)key.channel(); 
                    SocketChannel client = server.accept();
                    System.out.printf("Server: Get connection from %s.\n", client.getRemoteAddress().toString());
                    client.configureBlocking(false);
                    Player player = new Player(client);
                    client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, player);

                    channels.add(client);
                    players.add(player);

                }

                if(key.isReadable()){
                    // SocketChannel client = (SocketChannel)key.channel();
                    Player player = (Player)key.attachment();
                    // System.out.printf("Server: Readable from %s to %s.\n", client.getRemoteAddress().toString(), player.getName());
                    player.read();
                }

                if(key.isWritable()){
                    // SocketChannel client = (SocketChannel)key.channel();
                    Player player = (Player)key.attachment();
                    player.write();
                }
                it.remove();
            }
            
        }
    }

    public void close(){
        
        try {
            daemon.cancel(true);
            if(world != null){
                world.pause();
                publishTask.cancel(false);
                publisher.close();
            }        
            selector.close();
            for(Channel channel : channels)
                channel.close();
            
        } catch (IOException e) {
            
            e.printStackTrace();
        }
        
        
    }
}
