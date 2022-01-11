package io.github.jerryzhongj.calabash_brothers;

import java.io.DataOutputStream;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import lombok.Getter;

public class PlayerInfoController {
    @Getter
    private BackEnd backEnd;
    @Getter
    private Navigator navigator;
    
    @FXML
    private TextField ipTextField;

    @FXML
    private ChoiceBox chooseType;

    @FXML
    private TextField nameTextField;
    public PlayerInfoController(Navigator navigator, BackEnd backEnd) {
        this.backEnd = backEnd;
        this.navigator = navigator;
        
            
    }

    @FXML
    public void initialize(){
        if(backEnd.serverIsOn()){
            ipTextField.setText("localhost");
            ipTextField.setDisable(true);
        }
    }
    @FXML
    private void confirm(){

        try {
            backEnd.connect(ipTextField.getText());
        } catch (IOException e) {
            
            e.printStackTrace();
            return;
        }

        EntityType calabashType;
        switch((String)chooseType.getValue()){
            case "一娃":
                calabashType = EntityType.CALABASH_BRO_I;
                break;
            case "二娃":
                calabashType = EntityType.CALABASH_BRO_II;
                break;
            case "三娃":
                calabashType = EntityType.CALABASH_BRO_III;
                break;
            case "六娃":
                calabashType = EntityType.CALABASH_BRO_VI;
                break;
            case "七娃":
                calabashType = EntityType.CALABASH_BRO_VII;
                break;
            default:
                calabashType = EntityType.CALABASH_BRO_I;
                break;
        }

        DataOutputStream outputStream = backEnd.getOutput();
        try {
            outputStream.write(RequestProtocol.SET_NAME);
            byte[] bytes = nameTextField.getText().getBytes();
            outputStream.writeInt(bytes.length);
            outputStream.write(bytes);
            outputStream.write(RequestProtocol.SET_CALABASH);
            outputStream.writeInt(calabashType.getCode());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Room.fxml"));
        loader.setController(new RoomController(navigator, backEnd));
        try {
            navigator.replace(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

}
