import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.canvas.*;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.scene.image.*;
import java.net.URL;

public class StupidCanvasExample extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        double width;
        double height;
        double x;
        double y;

        stage.setTitle("StupidCanvasExample");
        stage.setResizable(false);
        Group root = new Group();
        Scene scene = new Scene(root);
        StackPane pane = new StackPane();
        URL url = this.getClass()
                      .getClassLoader()
                      .getResource("background.jpeg");
        if (url != null) {
          Image image = new Image(url.toString());
          width = image.getWidth();
          height = image.getHeight();
          ImageView iv = new ImageView(image);
          pane.getChildren().add(iv);

          final Canvas canvas = new Canvas(width, height);
          GraphicsContext gc = canvas.getGraphicsContext2D();
 
          gc.setStroke(Color.BLACK);
          gc.setLineWidth(height * 0.01);
          x = 0.42 * width - width / 36.0;
          y = 0.285 * height;
          gc.strokeArc(x, y,
                       width / 18.0, height / 40.0,
                       180, 180, ArcType.OPEN);
          x += width / 18.0;
          gc.strokeArc(x, y,
                       width / 18.0, height / 40.0,
                       180, 180, ArcType.OPEN);
          pane.getChildren().add(canvas);
          // Make canvas disappear when clicked
          canvas.setOnMouseClicked((e)->{
             canvas.setVisible(false);
            });
        }
        root.getChildren().add(pane);
        stage.setScene(scene);
        stage.show();
    }

}
