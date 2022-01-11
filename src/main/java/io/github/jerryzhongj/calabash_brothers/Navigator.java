package io.github.jerryzhongj.calabash_brothers;

import java.util.Deque;
import java.util.LinkedList;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Navigator {
    
    // Does't include current root
    private Deque<Parent> stack = new LinkedList<>();
    private Scene scene;

    public void push(Parent parent){
        if(scene == null)
            scene = new Scene(parent);
        else{
            stack.push(scene.getRoot());
            scene.setRoot(parent);
            parent.requestFocus();
        }
        
    }

    public void pop(){
        Parent root = stack.pop();
        if(root == null)
            Platform.exit();
        else{
            scene.setRoot(root);
            root.requestFocus();
        }
            
    }

    public void replace(Parent parent){
        if(scene == null)
            throw new RuntimeException("Please add a initial parent first!");
        else{
            scene.setRoot(parent);
            parent.requestFocus();
        }
            
    }

    public Scene getScene(){
        if(scene == null)
            throw new RuntimeException("Please add a initial parent first!");
        else 
            return scene;

    }
}
