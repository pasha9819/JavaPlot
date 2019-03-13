package graphics;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;

import java.util.Formatter;
import java.util.Set;

public class GraphPlotter extends Application {
    private volatile Stage stage;
    private Stage ownerStage;

    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private LineChart<Number, Number> chart;

    private volatile boolean wait;
    private static volatile boolean synchronised = true;

    public GraphPlotter(){
        if (!isLaunch){
            launch();
        }
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        StringConverter<Number> converter = new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                double d = object.doubleValue();
                if (d < 1e-3) {
                    Formatter f = new Formatter();
                    String s = f.format("%3.2e", d).toString();
                    s = s.replace(",00", "");
                    s = s.replace("e+00", "");
                    s = s.replace("0e", "e");
                    s = s.replace("+0", "+");
                    s = s.replace("-0", "-");
                    return s;
                } else if (xAxis.getUpperBound() - xAxis.getLowerBound() < 1e-3) {
                    Formatter f = new Formatter();
                    String s = f.format("%5.4e", d).toString();
                    while (s.contains("0e")) {
                        s = s.replace("0e", "e");
                    }
                    s = s.replace("e+00", "");
                    return s;
                } else {
                    d = Math.round(d * 1000) / 1000.;
                    return String.valueOf(d);
                }
            }

            @Override
            public Number fromString(String string) {
                return null;
            }
        };
        yAxis.setTickLabelFormatter(converter);

        xAxis.setTickLabelFormatter(converter);

        chart = new LineChart<>(xAxis, yAxis);
        chart.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
        chart.setCreateSymbols(false);
    }

    private GraphPlotter(Stage ownerStage){
        this();
        if (ownerStage == null) {
            throw new NullPointerException("Stage must not be null");
        }
        this.ownerStage = ownerStage;
    }

    public void start(Stage primaryStage) {
        stage = primaryStage;
        Scene scene = new Scene(chart);
        scene.getStylesheets().add("chart.css");
        scene.getStylesheets().add("chart2.css");
        stage.setScene(scene);
        if (ownerStage != null) {
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(ownerStage);
        }
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                wait = false;
                stage.close();
                event.consume();
            }
        });
    }

    public static void plot(String xLabel, String yLabel, Graphic... graphics){
        if (graphics.length == 0){
            throw new IllegalArgumentException("Count of graphics must be > 0");
        }
        GraphPlotter plotter = new GraphPlotter();
        plotter.xAxis.setLabel(xLabel);
        plotter.yAxis.setLabel(yLabel);

        double x,y, minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        int maxCount = 1;

        for (int i = 0; i < graphics.length; i++) {
            Graphic graphic = graphics[i];
            maxCount = Math.max(maxCount, graphic.getX().length);
            XYChart.Series<Number, Number> s = new XYChart.Series<>();
            String nameOfSeries = graphic.getName();
            s.setName(nameOfSeries/*.replace(" ", "\n")*/);
            int size = graphic.getX().length;
            for (int j = 0; j < size; j++) {
                x = graphic.getX()[j];
                y = graphic.getY()[j];
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                s.getData().add(new XYChart.Data<>(x, y));
            }
            if (graphic.getColor() != null) {
                String style = plotter.chart.getStyle() +  " CHART_COLOR_" + (i+1) + ": " + graphic.getColor() + ";";
                plotter.chart.setStyle(style);
            }
            plotter.chart.getData().add(s);


            /*if (graphic.getWidth() != null){
                Node line = s.getNode().lookup(".chart-series-line");
                line.setStyle("-fx-stroke-width: 5px;");
            }*/
            if (graphic.getWidth() != null){
                Node line = s.getNode().lookup(".default-color" + i + ".chart-series-line");
                line.setStyle("-fx-stroke-width: " + graphic.getWidth() + "px;");
            }

            /*Set<Node> line = plotter.chart.lookupAll(".chart-legend");
            for (Node n : line){
                System.out.println(n.getClass());
            }*/
        }
        plotter.xAxis.setAutoRanging(false);
        plotter.xAxis.setUpperBound(maxX);
        plotter.xAxis.setLowerBound(minX);
        double scale = maxX - minX;
        double count = (double)maxCount;
        System.out.println("count = " + count);
        plotter.xAxis.setTickUnit(scale / 3);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                plotter.start(new Stage());
                plotter.stage.show();
            }
        });

        plotter.wait = true;
        Thread t = Thread.currentThread();
        String name = t.getName();
        if (name == null) {
            name = "";
        }
        while (plotter.wait && synchronised && !name.equals("JavaFX Application Thread")){
            Thread.yield();
        }

    }

    public static void plot(Graphic... graphics){
        plot("", "", graphics);
    }

    public static void synchronisedModOn(){
        synchronised = true;
    }

    public static void synchronisedModOff(){
        synchronised = false;
    }

    private static Thread launcherThread = new GraphLauncherThread();
    private static volatile boolean isLaunch = false;

    private static void launch(){
        Platform.setImplicitExit(false);
        launcherThread.start();
        isLaunch = true;
    }
}
