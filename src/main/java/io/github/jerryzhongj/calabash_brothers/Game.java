package io.github.jerryzhongj.calabash_brothers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;

import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import lombok.Getter;


class Game{
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Pane canvas = new Pane();
    @Getter
    private Scene scene = new Scene(canvas);
    private MapProperty<Integer, Element> elements = new SimpleMapProperty<>();
    private DoubleProperty anchoredX = new SimpleDoubleProperty();
    private DoubleProperty anchoredY = new SimpleDoubleProperty();
    private DoubleProperty camX = new SimpleDoubleProperty();
    private DoubleProperty camY = new SimpleDoubleProperty();
    private DoubleProperty width = new SimpleDoubleProperty(Settings.PREF_WIDTH);
    private DoubleProperty height = new SimpleDoubleProperty(Settings.PREF_HEIGHT);

    private Map<EntityType, Double> offsetXs = new HashMap<>();
    private Map<EntityType, Double> offsetYs = new HashMap<>();
    
    private Loader loader;
    private FXMLLoader characterLoader = new  FXMLLoader(getClass().getResource("/FXML/Character.fxml"));
    Game(Loader loader) {
        this.loader = loader;

        canvas.setPrefSize(Settings.PREF_WIDTH, Settings.PREF_HEIGHT);
        canvas.maxHeightProperty().bind(height);
        canvas.maxWidthProperty().bind(width);

        // Loading meta info
        // TODO: more types
        offsetXs.put(EntityType.CALABASH_BRO_I, loader.loadEntityOffsetX(EntityType.CALABASH_BRO_I));
        offsetXs.put(EntityType.CALABASH_BRO_III, loader.loadEntityOffsetX(EntityType.CALABASH_BRO_III));
        offsetXs.put(EntityType.CONCRETE, loader.loadEntityOffsetX(EntityType.CONCRETE));

        offsetYs.put(EntityType.CALABASH_BRO_I, loader.loadEntityOffsetY(EntityType.CALABASH_BRO_I));
        offsetYs.put(EntityType.CALABASH_BRO_III, loader.loadEntityOffsetY(EntityType.CALABASH_BRO_III));
        offsetYs.put(EntityType.CONCRETE, loader.loadEntityOffsetY(EntityType.CONCRETE));

        // Setting Camera
        DoubleBinding camXBindings = Bindings.createDoubleBinding(()->{
            double ax = anchoredX.get();
            double cx = camX.get();
            double canvasWidth = canvas.widthProperty().get();
            double relative = (ax - cx) / canvasWidth;
            double w = width.get();
            // Place the character in the middle
            if(relative < 0.25)
                cx = ax - 0.25 * canvasWidth;
            if(relative > 0.75)
                cx = ax - 0.75 * canvasWidth;
            // 
            if(cx < -w / 2)
                cx = -w / 2;
            if(cx > w / 2 - canvasWidth)
                cx = w / 2 - canvasWidth;

            return cx;
        }, anchoredX, canvas.widthProperty(), width);

        DoubleBinding camYBindings = Bindings.createDoubleBinding(()->{
            double ay = anchoredY.get();
            double cy = camY.get();
            double canvasHeight = canvas.heightProperty().get();
            double relative = (cy - ay) / canvasHeight;
            double h = height.get();
            if(relative < 0.25)
                cy = ay + 0.25 * canvasHeight;
            if(relative > 0.75)
                cy = ay + 0.75 * canvasHeight;

            if(cy < canvasHeight)
                cy = canvasHeight;
            if(cy > h)
                cy = h;

            return cy;
        }, anchoredY, canvas.heightProperty(), height);

        camX.bind(camXBindings);
        camY.bind(camYBindings);

        elements.addListener((MapChangeListener.Change<? extends Integer, ? extends Element> change) ->{
                if(change.wasAdded()){
                    Element e = change.getValueAdded();
                    String location = "/images/"+e.elementType.getName()+".png";
                    Node node;
                    switch(e.elementType){
                        case CALABASH_BRO_I:
                        case CALABASH_BRO_III:
                        case CALABASH_BRO_VI:
                            Character c = (Character)e;
                            try {
                                node = (VBox)characterLoader.load();
                                ImageView imgView = (ImageView)node.lookup("#imgView");
                                Rectangle hpBar = (Rectangle)node.lookup("#hpBar");
                                Rectangle mpBar = (Rectangle)node.lookup("#mpBar");
                                imgView.setImage(new Image(getClass().getResource(location).toString()));
                                hpBar.widthProperty().bind(c.hp.divide(Settings.MAX_HP).multiply(Settings.BAR_WIDTH));
                                mpBar.widthProperty().bind(c.mp.divide(Settings.MAX_HP).multiply(Settings.BAR_WIDTH));
                            } catch (IOException e1) {
                                
                                e1.printStackTrace();
                            }
                        default:
                            node = new ImageView(getClass().getResource(location).toString());
                            break;
                    }
                    double offsetX = offsetXs.get(e.elementType);
                    double offsetY = offsetYs.get(e.elementType);

                    node.layoutXProperty().bind(e.x.subtract(camX).subtract(offsetX));
                    node.layoutYProperty().bind(camY.subtract(e.y).subtract(offsetY));
                    e.node = node;
                }

                if(change.wasRemoved()){
                    //TODO  
                }
                
            }
        );
    }

    void connect(String serverName, String name, EntityType calabashType) throws UnknownHostException, IOException{
        Socket server = new Socket(serverName, Settings.PORT);
        outputStream = new DataOutputStream(server.getOutputStream());
        inputStream = new DataInputStream(server.getInputStream());
        outputStream.write(RequestProtocol.SET_NAME);
        outputStream.writeChars(name);
        outputStream.write(RequestProtocol.SET_CALABASH);
        outputStream.write(calabashType.getCode());

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
        EntityType elementType;
        Node node;
        Element(EntityType type){
            elementType = type;
        }
        
    }

    static class Character extends Element{
        StringProperty name;
        DoubleProperty hp;
        DoubleProperty mp;
        Character(EntityType type) {
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
                    EntityType type = EntityType.getType(inputStream.readInt());
                    Platform.runLater(()->{
                        switch(type){
                            case CALABASH_BRO_I:    
                            case CALABASH_BRO_III:
                            case CALABASH_BRO_VI:
                                elements.put(id, new Character(type));
                                break;
                            default:
                                elements.put(id, new Element(type));
                                break;
                        }
                        
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
                    if(e != null){
                        Platform.runLater(()->{
                            anchoredX.bind(e.x);
                            anchoredY.bind(e.y);
                        });
                    }
                    break;
                case ResponseProtocol.SET_SIZE:
                    id = inputStream.readInt();
                    double w = inputStream.readDouble();
                    double h = inputStream.readDouble();
                    Platform.runLater(()->{
                        width.set(w);
                        height.set(h);
                    });
                    break;
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
