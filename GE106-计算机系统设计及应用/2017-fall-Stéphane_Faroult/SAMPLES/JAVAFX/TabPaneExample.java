//
//  TabPane demo program
//
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Screen;

public class TabPaneExample extends Application {

    private final String cssFile = TabPaneExample.class
                                   .getClassLoader()
                                   .getResource("gui.css")
                                   .toString();
                           
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        stage.setTitle("TabPane");
        Group root = new Group();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(cssFile);
        // Create a TabPane
        TabPane pane = new TabPane();
        pane.setPrefWidth(800);
        pane.setPrefHeight(600);
        root.getChildren().add(pane);
        Tab tab;
        // Create five tabs
        for (int i = 1; i <= 5; i++) {
          tab = new Tab();
          tab.setText("Option " + i);
          tab.setContent(new Label("Content of tab " + i));
          pane.getTabs().add(tab);
        }
        stage.setScene(scene);
        stage.show();
    }

}
