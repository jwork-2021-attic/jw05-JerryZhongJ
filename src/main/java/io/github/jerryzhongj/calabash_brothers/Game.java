package io.github.jerryzhongj.calabash_brothers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import javafx.application.Platform;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import lombok.Getter;


public class Game{
    @Getter
    private Pane canvas = new Pane();
    private ObservableMap<Integer, Element> elements = FXCollections.observableHashMap();
    private DoubleProperty anchoredX = new SimpleDoubleProperty();
    private DoubleProperty anchoredY = new SimpleDoubleProperty();
    private DoubleProperty camX = new SimpleDoubleProperty();
    private DoubleProperty camY = new SimpleDoubleProperty();
    @Getter
    private DoubleProperty width = new SimpleDoubleProperty(Settings.PREF_WIDTH);
    @Getter
    private DoubleProperty height = new SimpleDoubleProperty(Settings.PREF_HEIGHT);
    @Getter
    private BackEnd backEnd;
    @Getter
    private Navigator navigator;

    private Future<?> receiveData;
    
    private List<ImageView> backgroundImages = new LinkedList<>();
    
    public Game(Navigator navigator, BackEnd backEnd) {
        this.backEnd = backEnd;
        this.navigator = navigator;
        
        // Setting Camera
        anchoredX.addListener((observable, oldValue, newValue) -> setCamX());
        width.addListener((observable, oldValue, newValue) -> setCamX());
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> setCamX());

        anchoredY.addListener((observable, oldValue, newValue) -> setCamY());
        height.addListener((observable, oldValue, newValue) -> setCamY());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> setCamY());
        
        // Adding and removing elements
        elements.addListener((MapChangeListener.Change<? extends Integer, ? extends Element> change) ->{
                if(change.wasAdded()){
                    Element e = change.getValueAdded();
                    String location = "/images/"+e.elementType.getName()+".png";
                    Node node;
                    if(e instanceof Character){
                        Character c = (Character)e;
                        try {
                            
                            node = (VBox)FXMLLoader.load(getClass().getResource("/FXML/Character.fxml"));
                            ImageView imgView = (ImageView)node.lookup("#imgView");
                            Rectangle hpBar = (Rectangle)node.lookup("#hpBar");
                            Rectangle mpBar = (Rectangle)node.lookup("#mpBar");
                            Text name = (Text)node.lookup("#name");
                            // System.out.println(imgView.getFitWidth());
                            imgView.setImage(new Image(getClass().getResourceAsStream(location)));
                            
                            // System.out.println(imgView.getFitWidth());
                            // imgView.setFitWidth(10);
                            hpBar.widthProperty().bind(c.hp.divide(Settings.MAX_HP).multiply(Settings.BAR_WIDTH));
                            mpBar.widthProperty().bind(c.mp.divide(Settings.MAX_HP).multiply(Settings.BAR_WIDTH));
                            name.textProperty().bind(c.name);
                            imgView.scaleXProperty().bind(new When(c.facingRight).then(1.0).otherwise(-1.0));
                            Image superMode = new Image(getClass().getResourceAsStream("/images/"+e.elementType.getName()+" Super.png"));
                            Image normal = new Image(getClass().getResourceAsStream("/images/"+e.elementType.getName()+".png"));
                            imgView.imageProperty().bind(new When(c.superMode).then(superMode).otherwise(normal));
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            return;
                        }
                    }else{
                        node = new ImageView(getClass().getResource(location).toString());
                    }
                    
                    double offsetX = backEnd.getLoader().loadEntityOffsetX(e.elementType);
                    double offsetY = backEnd.getLoader().loadEntityOffsetY(e.elementType);

                    node.layoutXProperty().bind(e.x.subtract(camX).subtract(offsetX));
                    node.layoutYProperty().bind(camY.subtract(e.y).subtract(offsetY));
                    e.node = node;

                    canvas.getChildren().add(node);
                }

                if(change.wasRemoved()){
                    Element e = change.getValueRemoved();
                    canvas.getChildren().remove(e.node);
                }
                
            }
        );

        // Add key press handler
        canvas.setOnKeyPressed(event->{
            // System.out.println("pressed");
            DataOutputStream outputStream = backEnd.getOutput();
            try{
                switch(event.getCode()){
                    case A:
                    case LEFT:
                        outputStream.writeByte(RequestProtocol.MOVE_LEFT);
                        break;
                    case D:
                    case RIGHT:
                        outputStream.writeByte(RequestProtocol.MOVE_RIGHT);
                        break;
                    
                    default:
                        break;
                }
            }catch(IOException e){
                e.printStackTrace();
            }
            
        });

        canvas.setOnKeyReleased(event->{
            DataOutputStream outputStream = backEnd.getOutput();
            try{
                switch(event.getCode()){
                    case A:
                    case LEFT:
                    case D:
                    case RIGHT:
                        outputStream.writeByte(RequestProtocol.STOP);
                        break;
                    case SPACE:
                    case UP:
                        outputStream.writeByte(RequestProtocol.JUMP);
                        break;
                    case E:
                    case NUMPAD0:
                        outputStream.writeByte(RequestProtocol.PUNCH);
                        break;
                    case R:
                    case NUMPAD1:
                        outputStream.writeByte(RequestProtocol.SUPERMODE);
                    default:
                        break;
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        });

        // Setting canvas
        canvas.setPrefSize(Settings.PREF_WIDTH, Settings.PREF_HEIGHT);

        receiveData = ThreadPool.nonScheduled.submit(() -> {
            try {
                receiveData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    
    }

    private void setCamX(){
        double ax = anchoredX.get();
        double cx = camX.get(), newCx = cx;
        double canvasWidth = canvas.getWidth();
        double relative = (ax - cx) / canvasWidth;
        double w = width.get();
        // Place the character in the middle
        if(relative < 0.25)
            newCx = ax - 0.25 * canvasWidth;
        if(relative > 0.75)
            newCx = ax - 0.75 * canvasWidth;
        // 
        if(newCx < -w / 2)
            newCx = -w / 2;
        if(newCx > w / 2 - canvasWidth)
            newCx = w / 2 - canvasWidth;

        // System.out.printf("AnchoredX: %f\tCanvas Width:%f\tCamX:%f\n",ax,canvasWidth,newCx);
        if(newCx != cx)
            camX.set(newCx);
    }

    private void setCamY(){
        double ay = anchoredY.get();
        double cy = camY.get(), newCy = cy;
        double canvasHeight = canvas.getHeight();
        double relative = (cy - ay) / canvasHeight;
        double h = height.get();
        if(relative < 0.25)
            newCy = ay + 0.25 * canvasHeight;
        if(relative > 0.75)
            newCy = ay + 0.75 * canvasHeight;

        if(newCy < canvasHeight)
            newCy = canvasHeight;
        if(newCy > h)
            newCy = h;

        // System.out.printf("AnchoredY: %f\tCanvas Height:%f\tCamY:%f\n",ay,canvasHeight,newCy);
        if(newCy != cy)
            camY.set(newCy);
    }

    private static class Element{

        DoubleProperty x = new SimpleDoubleProperty(0);
        DoubleProperty y = new SimpleDoubleProperty(0);
        EntityType elementType;
        Node node;
        Element(EntityType type){
            elementType = type;
        }
        
    }

    private static class Character extends Element{
        StringProperty name = new SimpleStringProperty();
        DoubleProperty hp = new SimpleDoubleProperty();
        DoubleProperty mp = new SimpleDoubleProperty();
        BooleanProperty facingRight = new SimpleBooleanProperty();
        BooleanProperty superMode = new SimpleBooleanProperty();
        Character(EntityType type) {
            super(type);
        }
    }

    private void setBackground(String background){
        // Background must be placed in the middle
        canvas.getChildren().removeAll(backgroundImages);
        backgroundImages.clear();
        Image image = new Image(getClass().getResource("/Backgrounds/"+background).toString());
        double imageWidth = image.getWidth();
        double canvasWidth = width.get();
        int halfNum = (int)Math.ceil((canvasWidth - imageWidth) / 2 / imageWidth);
        // halfNum will not be negative
        for(int i = -halfNum;i <= halfNum;i++){
            ImageView imageView = new ImageView(image);
            DoubleProperty x = new SimpleDoubleProperty(imageWidth * (i - 0.5));
            DoubleProperty y = new SimpleDoubleProperty(image.getHeight());
            imageView.layoutXProperty().bind(x.subtract(camX));
            imageView.layoutYProperty().bind(camY.subtract(y));

            backgroundImages.add(imageView);
        }
        canvas.getChildren().addAll(backgroundImages);
        for(ImageView imageView : backgroundImages)
            imageView.toBack();

    }
    
    private String readString(DataInputStream in) throws IOException{
        int byte_len = in.readInt();
        byte[] bytes = new byte[byte_len];
        in.read(bytes);
        return new String(bytes);
    }

    private void receiveData() throws IOException{
        DataInputStream inputStream = backEnd.getInput();
        boolean gameIsOn = true;
        while(gameIsOn){
           
            byte command = inputStream.readByte();
            
            switch(command){
                case ResponseProtocol.ADD:
                    int id = inputStream.readInt();
                    EntityType type = EntityType.getType(inputStream.readInt());
                    
                    Platform.runLater(()->{
                        switch(type){
                            case CALABASH_BRO_I:   
                            case CALABASH_BRO_II: 
                            case CALABASH_BRO_III:
                            case CALABASH_BRO_VI:
                            case CALABASH_BRO_VII:
                                elements.put(id, new Character(type));
                                break;
                            default:
                                // System.out.println("Add " + type.getName());
                                elements.put(id, new Element(type));
                                break;
                        }
                        
                    });
                    break;
                case ResponseProtocol.SET_POS:
                    id = inputStream.readInt();
                    double x = inputStream.readDouble();
                    double y = inputStream.readDouble();
                    
                    Platform.runLater(()->{
                        Element e = elements.get(id);
                        if(e != null){
                            e.x.set(x);
                            e.y.set(y);
                        }
                    });
                    break;
                case ResponseProtocol.SET_NAME:
                    id = inputStream.readInt();
                    String name = readString(inputStream); 
                    Platform.runLater(()->{
                        Element e = elements.get(id);
                        if(e != null && e instanceof Character)
                            ((Character)e).name.set(name);       
                    });
                    break;
                case ResponseProtocol.SET_HP:
                    id = inputStream.readInt();
                    Double hp = inputStream.readDouble();
                    Platform.runLater(()->{
                        Element e = elements.get(id);
                        if(e != null && e instanceof Character)
                            ((Character)e).hp.set(hp);    
                    });
                    break;
                case ResponseProtocol.SET_MP:
                    id = inputStream.readInt();
                    Double mp = inputStream.readDouble();
                    
                    Platform.runLater(()->{
                        Element e = elements.get(id);
                        if(e != null && e instanceof Character)
                            ((Character)e).mp.set(mp);    
                    });
                    break;
                case ResponseProtocol.SET_FACING:
                    id = inputStream.readInt();
                    boolean facing = (inputStream.readInt() == 1? true:false);
                    
                    Platform.runLater(()->{
                        Element e = elements.get(id);
                        if(e != null && e instanceof Character)
                            ((Character)e).facingRight.set(facing);;    
                    });
                    break;
                case ResponseProtocol.SET_SUPERMODE:
                    id = inputStream.readInt();
                    boolean superMode = (inputStream.readInt() == 1? true:false);
                    
                    Platform.runLater(()->{
                        Element e = elements.get(id);
                        if(e != null && e instanceof Character)
                            ((Character)e).superMode.set(superMode);;    
                    });
                    break;
                case ResponseProtocol.CLEAR:
                    id = inputStream.readInt();
                    Platform.runLater(()->{
                        elements.remove(id);
                    });
                    break;
                case ResponseProtocol.ANCHOR:
                    id = inputStream.readInt();
                    Platform.runLater(()->{
                        Element e = elements.get(id);
                        if(e != null){
                            anchoredX.bind(e.x);
                            anchoredY.bind(e.y);
                        }
                    });
                    
                    
                    break;
                case ResponseProtocol.SET_SIZE:
                    double w = inputStream.readDouble();
                    double h = inputStream.readDouble();
                    Platform.runLater(()->{
                        width.set(w);
                        height.set(h);
                    });
                    break;
                case ResponseProtocol.SET_BACKGROUND:
                    name = readString(inputStream);
                    Platform.runLater(()->{
                        setBackground(name);
                    });
                    break;
                case ResponseProtocol.WINNER:
                    gameIsOn = false;
                    HBox winner = FXMLLoader.load(getClass().getResource("/FXML/Winner.fxml"));
                    Text nameText = (Text) winner.lookup("#name");
                    nameText.setText(readString(inputStream));
                    navigator.replace(winner);
                    break;

                case ResponseProtocol.LOSE:
                    gameIsOn = false;
                    HBox lose = FXMLLoader.load(getClass().getResource("/FXML/Lose.fxml"));
                    navigator.replace(lose);
                    break;
            }
        }
    }
}
