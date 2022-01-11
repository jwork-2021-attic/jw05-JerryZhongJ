package io.github.jerryzhongj.calabash_brothers;

import java.io.IOException;

import io.github.jerryzhongj.calabash_brothers.server.Server;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("疯狂葫芦娃乱斗");
        primaryStage.setWidth(Settings.PREF_WIDTH);
        primaryStage.setHeight(Settings.PREF_HEIGHT);
        primaryStage.setResizable(false);

        BackEnd backEnd = new BackEnd();
        FXMLLoader menu = new FXMLLoader(getClass().getResource("/FXML/Menu.fxml"));
        Navigator navigator = new Navigator();
        menu.setController(new MenuController(navigator, backEnd));
        navigator.push(menu.load());
        
        primaryStage.setScene(navigator.getScene());
        primaryStage.show();
    }
}
