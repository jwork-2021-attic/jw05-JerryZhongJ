package io.github.jerryzhongj.calabash_brothers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.event.AncestorEvent;

import org.xml.sax.InputSource;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;

class Game{
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private AnchorPane anchorPane;
    @Getter
    private Scene scene;
    private MapProperty<Integer, Element> elements;
    private Element anchoredElement;
    private DoubleProperty camX;
    private DoubleProperty camY;
    Game() {
        
    }
    
    void connect(String serverName, String name, byte CalabashType) throws UnknownHostException, IOException{
        Socket server = new Socket(serverName, GlobalSettings.PORT);
        outputStream = new DataOutputStream(server.getOutputStream());
        inputStream = new DataInputStream(server.getInputStream());
        outputStream.write(RequestProtocol.SET_NAME);
        outputStream.writeChars(name);
        outputStream.write(RequestProtocol.SET_CALABASH);
        outputStream.write(CalabashType);

        new Thread(()->{
            // TODO: Thread Exception handler
            try{
                recieveData();
            }catch(IOException e){
                e.printStackTrace();
            }
            
        }).start();
    }

    static class Element{

        DoubleProperty x = new SimpleDoubleProperty(0);
        DoubleProperty y = new SimpleDoubleProperty(0);
        BooleanProperty reverse = new SimpleBooleanProperty(false);
        byte elementType;
        Element(byte type){
            elementType = type;
        }
        
    }

    static class Character extends Element{
        StringProperty name;
        DoubleProperty hp;
        DoubleProperty mp;
        Character(byte type) {
            super(type);
        }
    }

    private String readChars(DataInputStream in) throws IOException{
        StringBuilder sb = new StringBuilder();
        char c;
        do{
            c = in.readChar();
            sb.append(c);
        }while(c != '\0');
        return sb.toString();
    }

    private void recieveData() throws IOException{
        while(true){
            byte command = inputStream.readByte();
            switch(command){
                case ResponseProtocol.ADD:
                    int id = inputStream.readInt();
                    byte type = inputStream.readByte();
                    Platform.runLater(()->{
                        elements.put(id, new Element(type));
                    });
                    break;
                case ResponseProtocol.SET_POS:
                    id = inputStream.readInt();
                    double x = inputStream.readDouble();
                    double y = inputStream.readDouble();
                    Element e = elements.get(id);
                    if(e != null){
                        Platform.runLater(()->{
                            e.x.set(x);
                            e.y.set(y);
                        });
                    }
                    
                    break;
                case ResponseProtocol.SET_NAME:
                    id = inputStream.readInt();
                    String name = readChars(inputStream);
                    e = elements.get(id);
                    if(e != null && e instanceof Character){
                        Platform.runLater(()->{
                            ((Character)e).name.set(name);       
                        });
                    }
                    break;
                case ResponseProtocol.SET_HP:
                    id = inputStream.readInt();
                    Double hp = inputStream.readDouble();
                    e = elements.get(id);
                    if(e != null && e instanceof Character){
                        Platform.runLater(()->{
                            ((Character)e).hp.set(hp);    
                        });
                    }
                    break;
                case ResponseProtocol.SET_MP:
                    id = inputStream.readInt();
                    Double mp = inputStream.readDouble();
                    e = elements.get(id);
                    if(e != null && e instanceof Character){
                        Platform.runLater(()->{
                            ((Character)e).mp.set(mp);    
                        });
                    }
                    break;
                case ResponseProtocol.CLEAR:
                    id = inputStream.readInt();
                    Platform.runLater(()->{
                        elements.remove(id);
                    });
                    break;
                case ResponseProtocol.ANCHOR:
                    id = inputStream.readInt();
                    e = elements.get(id);
                    if(e != null)
                        anchoredElement = e;
                case ResponseProtocol.WINNER:
                    //TODO
                    break;

                case ResponseProtocol.LOSE:
                    //TODO
                    break;
            }
        }
    }
}
