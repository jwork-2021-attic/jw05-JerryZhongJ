package io.github.jerryzhongj.calabash_brothers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import io.github.jerryzhongj.calabash_brothers.server.Server;
import lombok.Getter;

public class BackEnd {
    private Server server;
    private Socket socket;

    @Getter
    private DataOutputStream output;
    @Getter
    private DataInputStream input;
    
    @Getter
    private Loader loader = new Loader();


    public void startServer() throws IOException{
        if(server != null)
            closeServer();
        server = new Server(loader);
    }

    public void closeServer(){
        if(server == null)
            return;
        server.close();
        server = null;
    }

    public boolean serverIsOn(){
        return server != null;
    }

    public void serverStartWorld(String map){
        server.startWorld(map);
    }

    public void connect(String serverName) throws UnknownHostException, IOException{
        if(socket != null)
            disconnect();
        socket = new Socket(serverName, Settings.PORT);
        output = new DataOutputStream(socket.getOutputStream());
        input = new DataInputStream(socket.getInputStream());

    }

    public boolean isConnecting(){
        return socket != null;
    }

    public void disconnect(){
        if(socket == null)
            return;
        try {
            socket.close();
            socket = null;
            output = null;
            input = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    
}
