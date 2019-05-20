import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.collections.*;

import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.BufferedReader;
import java.io.IOException;
 
public class BarChartExample extends Application {
    private final String dataFile = BarChartExample.class
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
         final CategoryAxis xAxis = new CategoryAxis();
         final NumberAxis yAxis = new NumberAxis();
         final BarChart<String,Number> bc = 
                            new BarChart<String,Number>(xAxis,yAxis);
         bc.setTitle("Database Waits");
         xAxis.setLabel("Event");       
         yAxis.setLabel("Percentage of Waits");
         bc.setLegendVisible(false);
                                                                                         
         loadData(dataFile);
         XYChart.Series<String,Number> series =
                   new XYChart.Series<String,Number>();
         series.setData(data);

         Scene scene  = new Scene(bc,1000,800);
         bc.getData().add(series);
         stage.setScene(scene);
         stage.show();
       }
                             
       public static void main(String[] args) {
          launch(args);
       }
}
