//
//  ListView example
//
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.collections.*;

public class ListViewExample extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        stage.setTitle("ListViews");
        Group root = new Group();
        Scene scene = new Scene(root);
        Pane pane = new Pane();
        VBox box = new VBox();
        pane.setPrefWidth(800);
        pane.setPrefHeight(600);
        root.getChildren().add(pane);
        pane.getChildren().add(box);
        ObservableList<String> choices =
                         FXCollections.observableArrayList("Tea",
                                                           "Coffee",
                                                           "Chocolate",
                                                           "Milk" );
        ListView<String> lv1 = new ListView<String>(choices);
        lv1.setPrefWidth(100);
        lv1.setPrefHeight(100);
        box.getChildren().add(lv1);
        Button button1 = new Button("Show Choices");
        button1.setOnAction((e)->{System.out.println(lv1.getFocusModel()
                                                       .getFocusedItem());});
        box.getChildren().add(button1);
        // Everything is ready, display
        stage.setScene(scene);
        stage.show();
    }

}
