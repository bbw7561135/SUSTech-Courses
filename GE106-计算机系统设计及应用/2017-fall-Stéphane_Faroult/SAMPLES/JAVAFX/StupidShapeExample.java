import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.scene.image.*;
import java.net.URL;
import java.util.Random;

public class StupidShapeExample extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        double width;
        double height;
        double x;
        double y;

        stage.setTitle("StupidShapeExample");
        stage.setResizable(false);
        Group root = new Group();
        Scene scene = new Scene(root);
        StackPane pane = new StackPane();
        Pane      shapePane = new Pane();
        URL url = this.getClass()
                      .getClassLoader()
                      .getResource("background.jpeg");
        if (url != null) {
          Image image = new Image(url.toString());
          width = image.getWidth();
          height = image.getHeight();
          ImageView iv = new ImageView(image);
          pane.getChildren().add(iv);
          shapePane.setPrefWidth(width);
          shapePane.setPrefHeight(height);
          pane.getChildren().add(shapePane);

          Arc arc = new Arc();
          arc.setCenterX(0.42 * width);
          arc.setCenterY(0.288 * height);
          arc.setRadiusX(width / 36.0);
          arc.setRadiusY(width / 36.0);
          arc.setStartAngle(180.0);
          arc.setLength(180.0);
          arc.setType(ArcType.OPEN);
          arc.setStroke(Color.BLACK);
          arc.setStrokeWidth(height * 0.01);
          arc.setFill(Color.rgb(255, 255, 255, 0.0));
          shapePane.getChildren().add(arc);
          Arc arc2 = new Arc();
          arc2.setCenterX(0.42 * width + width / 18.0);
          arc2.setCenterY(0.288 * height);
          arc2.setRadiusX(width / 36.0);
          arc2.setRadiusY(width / 36.0);
          arc2.setStartAngle(180.0);
          arc2.setLength(180.0);
          arc2.setType(ArcType.OPEN);
          arc2.setStroke(Color.BLACK);
          arc2.setStrokeWidth(height * 0.01);
          arc2.setFill(Color.rgb(255, 255, 255, 0.0));
          shapePane.getChildren().add(arc2);
          arc.setOnMouseClicked((e)->{
             Random rand = new Random();
             int r = rand.nextInt(256); 
             int g = rand.nextInt(256); 
             int b = rand.nextInt(256); 
             Color col = Color.rgb(r, g, b);
             arc.setStroke(col);
             arc2.setStroke(col);
            });
          arc2.setOnMouseClicked((e)->{
             Random rand = new Random();
             int r = rand.nextInt(256); 
             int g = rand.nextInt(256); 
             int b = rand.nextInt(256); 
             Color col = Color.rgb(r, g, b);
             arc.setStroke(col);
             arc2.setStroke(col);
            });
        }
        root.getChildren().add(pane);
        stage.setScene(scene);
        stage.show();
    }

}
