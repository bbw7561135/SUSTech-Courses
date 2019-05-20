//
//  Basic javafx program 
//
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.geometry.*;  // Insets and Pos

public class BasicGUI extends Application {
    private String directory = BasicGUI.class
                               .getProtectionDomain()
                               .getCodeSource()
                               .getLocation()
                               .toString();

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        stage.setTitle("Basic GUI");
        Group root = new Group();
        Scene scene = new Scene(root);
        // Will just generate a warning if not found
       // scene.getStylesheets().add(directory + "stylesheet.css");
        // Create a VBox (for centering)
        VBox vbox = new VBox();
        vbox.setPrefWidth(400);
        vbox.setPrefHeight(300);
        vbox.setAlignment(Pos.CENTER);
        root.getChildren().add(vbox);
        // Create a button
        Button button = new Button("Say 'Hi'");
        vbox.getChildren().add(button);
        //  
        button.setOnAction((e)-> { System.out.println("Hi!"); });
        // Everything is ready, display
        stage.setScene(scene);
        stage.show();
    }

}
