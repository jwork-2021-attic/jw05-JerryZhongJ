package io.github.jerryzhongj.calabash_brothers;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("疯狂葫芦娃乱斗");
        primaryStage.setHeight(GlobalSettings.WINDOWS_HEIGHT);
        primaryStage.setWidth(GlobalSettings.WINDOWS_WIDTH);
        primaryStage.setResizable(false);

        primaryStage.setOnCloseRequest((event)->{
            // TODO: 
        });
        
    }
}
