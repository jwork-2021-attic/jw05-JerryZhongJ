package io.github.jerryzhongj.calabash_brothers;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import lombok.Getter;

public class MenuController{
    @Getter
    private BackEnd backEnd;
    @Getter
    private Navigator navigator;
    public MenuController(Navigator navigator, BackEnd backEnd){
        this.navigator = navigator;
        this.backEnd = backEnd;
    }
    @FXML
    private void createRoom(){
       
        try {
            backEnd.startServer();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        FXMLLoader playerInfo = new FXMLLoader(getClass().getResource("/FXML/PlayerInfo.fxml"));
        PlayerInfoController controller = new PlayerInfoController(navigator, backEnd);
        playerInfo.setController(controller);
        
        try {
            navigator.push(playerInfo.load());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }   

    @FXML
    private void joinRoom(){
        FXMLLoader playerInfo = new FXMLLoader(getClass().getResource("/FXML/PlayerInfo.fxml"));
        PlayerInfoController controller = new PlayerInfoController(navigator, backEnd);
        playerInfo.setController(controller);
        
        try {
            navigator.push(playerInfo.load());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    @FXML
    private void quitGame(){
        Platform.exit();
    }
    
}
