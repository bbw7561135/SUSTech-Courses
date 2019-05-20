import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.collections.*;

import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
 
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import javafx.stage.FileChooser;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class SaveChartExample extends Application {
    private final String dataFile = SaveChartExample.class
                                      .getClassLoader()
                                      .getResource("data.txt")
                                      .toString().replace("file:", "");

    private static ObservableList<XYChart.Data<String,Number>> data =
                                   FXCollections.observableArrayList();

    static void loadData(String file) {
        // Here it's loaded from of a file, it could
        // as well be queried from a database (this type
        // of data is obtained by querying system tables).
        // We are only interested by the first and third fields
        // from each line (event name and percentage)
        try (BufferedReader reader
                = Files.newBufferedReader(Paths.get(file))) {
          String   line = null;
          String[] fields;
          while ((line = reader.readLine()) != null) {
            if (!line.startsWith("#")) {
              fields = line.split(",");
              data.add(new XYChart.Data<String,Number>(fields[0].replace("\"",
                                                                         ""),
                                                      new Float(fields[2])));
            }
          }
        } catch (IOException x) {
          System.err.format("IOException: %s%n", x);
        }
    }

    @Override
    public void start(Stage stage) {
         stage.setTitle("Technical Bar Chart");
         VBox  box = new VBox();
         final CategoryAxis xAxis = new CategoryAxis();
         final NumberAxis yAxis = new NumberAxis();
         final BarChart<String,Number> bc = 
                            new BarChart<String,Number>(xAxis,yAxis);
         bc.setTitle("Database Waits");
         xAxis.setLabel("Event");       
         yAxis.setLabel("Percentage of Waits");
         bc.setLegendVisible(false);
         bc.setAnimated(false); // IMPORTANT. Must be done before
                                // you start plotting.
                                                                                         
         loadData(dataFile);
         XYChart.Series<String,Number> series
                      = new XYChart.Series<String,Number>();
         series.setData(data);
         bc.getData().add(series);
         bc.setPrefWidth(800);
         bc.setPrefHeight(700);
         box.setPadding(new Insets(10));
         box.setAlignment(Pos.CENTER);
         box.getChildren().add(bc);

         HBox hbox = new HBox();
         hbox.setPadding(new Insets(5));
         hbox.setAlignment(Pos.CENTER);
         Button saveButton = new Button("Save Chart");
         hbox.getChildren().add(saveButton);
         box.getChildren().add(hbox);

         saveButton.setOnAction((e)->{
             FileChooser fileChooser = new FileChooser();
             fileChooser.setTitle("Save Chart");
             fileChooser.setInitialFileName("barchart.png");
             File selectedFile = fileChooser.showSaveDialog(stage);
             if (selectedFile != null) {
               try {
                 WritableImage snap = bc.snapshot(null, null);
                 ImageIO.write(SwingFXUtils.fromFXImage(snap, null),
                         "png", selectedFile);
               } catch (IOException exc) {
                 System.err.println(exc.getMessage());
               }
             }
           });

         Scene scene  = new Scene(box);
         stage.setScene(scene);
         stage.show();
       }
                             
       public static void main(String[] args) {
          launch(args);
       }
}
