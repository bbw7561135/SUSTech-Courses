//
//  Accordion demo program
//
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Screen;

public class AccordionExample extends Application {

    private final String cssFile = AccordionExample.class
                                   .getClassLoader()
                                   .getResource("gui.css")
                                   .toString();
                           
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        stage.setTitle("Accordion");
        Group root = new Group();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(cssFile);
        // Create an Accordion
        Accordion accordion = new Accordion();
        root.getChildren().add(accordion);
        TitledPane pane;
        // Create five titled panes
        for (int i = 1; i <= 5; i++) {
          pane = new TitledPane();
          pane.setText("Option " + i);
          pane.setContent(new Label("Content of pane " + i));
          accordion.getPanes().add(pane);
        }
        stage.setScene(scene);
        stage.show();
    }

}
