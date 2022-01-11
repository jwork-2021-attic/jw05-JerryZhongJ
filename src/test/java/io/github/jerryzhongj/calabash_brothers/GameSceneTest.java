package io.github.jerryzhongj.calabash_brothers;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import io.github.jerryzhongj.calabash_brothers.server.Server;
import javafx.application.Application;
import javafx.stage.Stage;

public class GameSceneTest extends ApplicationTest{

    private Stage stage;
    private Server server;
    @Override
    public void start(Stage stage){
        this.stage = stage;
        stage.setTitle("Test");
        stage.setResizable(true);
        Loader loader = new Loader();
        try {
            server = new Server(loader);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Game game1 = new Game(stage, loader);
        Game game2 = new Game(stage, loader);
        try {
            game1.connect("localhost", "Player 1", EntityType.CALABASH_BRO_I);
            game2.connect("localhost", "Player 2", EntityType.CALABASH_BRO_I);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        stage.setScene(game1.getScene());
        stage.show();
    }

    @Test
    public void testClient() throws InterruptedException{
        Thread.sleep(2000);
        server.startWorld("test");
        while(true);
    }
    
}
