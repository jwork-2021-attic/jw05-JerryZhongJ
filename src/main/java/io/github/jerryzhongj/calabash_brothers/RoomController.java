package io.github.jerryzhongj.calabash_brothers;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import lombok.Getter;

public class RoomController {
    @Getter
    private BackEnd backEnd;
    @Getter
    private Navigator navigator;
    
    @FXML
    private FlowPane playerList;

    @FXML
    private ChoiceBox<String> chooseMap;

    @FXML
    private Button startGameButton;

    private Future<?> receiveData;

    public RoomController(Navigator navigator, BackEnd backEnd) {
        this.backEnd = backEnd;
        this.navigator = navigator;

        receiveData = ThreadPool.nonScheduled.submit(()->{
            receiveData();
        });
    }

    @FXML
    public void initialize(){
        if(!backEnd.serverIsOn()){
            chooseMap.setDisable(true);
            startGameButton.setDisable(true);
        }
    }

    @FXML
    private void startGame(){
        backEnd.serverStartWorld((String)chooseMap.getValue());
    }

    @FXML
    private void back(){
        receiveData.cancel(true);
        backEnd.closeServer();
        backEnd.disconnect();
        navigator.pop();
    }
    
    private String readString(DataInputStream in) throws IOException{
        int byte_len = in.readInt();
        byte[] bytes = new byte[byte_len];
        in.read(bytes);
        return new String(bytes);
    }

    private void receiveData(){
        DataInputStream inputStream = backEnd.getInput();
        try{
            while(true){
                byte command = inputStream.readByte();
                if(command == ResponseProtocol.PLAYER_LIST){
                    int len = inputStream.readInt();
                    List<Pair<String, EntityType>> list = new ArrayList<>();
                    for(int i = 0;i < len;i++){
                        list.add(new Pair(readString(inputStream), EntityType.getType(inputStream.readInt())));
                    }
                    Platform.runLater(()->{
                        playerList.getChildren().clear();
                        try {
                            for(Pair<String, EntityType> pair : list){

                                VBox character = (VBox)FXMLLoader.load(getClass().getResource("/FXML/Character.fxml"));
                                Text name = (Text) character.lookup("#name");
                                ImageView imgView = (ImageView) character.lookup("#imgView");
                                name.setText(pair.getKey());
                                imgView.setImage(new Image(getClass().getResourceAsStream("/images/"+pair.getValue().getName()+".png")));
                                playerList.getChildren().add(character);
                                
                            }
                        } catch (IOException e) {
                                    
                            e.printStackTrace();
                        }     
                    });
                }

                if(command == ResponseProtocol.START_GAME){
                    Game game = new Game(navigator, backEnd);
                    navigator.push(game.getCanvas());
                    break;
                }

            }
        }catch(IOException e){
            e.printStackTrace();
        }
        
    }

}
