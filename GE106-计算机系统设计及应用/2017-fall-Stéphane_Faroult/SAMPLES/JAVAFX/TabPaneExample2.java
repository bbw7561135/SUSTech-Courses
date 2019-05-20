//
//  TabPane demo program, with padding
//
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Insets;

public class TabPaneExample2 extends Application {

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
        Tab  tab;
        VBox tabBox;
        // Create five tabs
        for (int i = 1; i <= 5; i++) {
          tab = new Tab();
          tabBox = new VBox();
          tabBox.setPadding(new Insets(20));
          tabBox.getChildren().add(new Label("Content of tab " + i));
          tab.setText("Option " + i);
          tab.setContent(tabBox);
          pane.getTabs().add(tab);
        }
        stage.setScene(scene);
        stage.show();
    }

}
