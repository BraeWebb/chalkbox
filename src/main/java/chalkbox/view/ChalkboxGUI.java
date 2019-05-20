package chalkbox.view;

import chalkbox.api.ChalkBox;
import chalkbox.api.annotations.Collector;
import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Processor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.reflections.Reflections;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ChalkboxGUI extends Application {
    private ChalkBox chalkbox;

    private GridPane collector;
    private Class collectorClazz;

    private GridPane processor;
    private Class processorClazz;

    private ErrorWindow log;
    private ObservableList<String> students;

    public void openCollectorSelector(ActionEvent event) {
        Reflections reflections = new Reflections("chalkbox");

        List<Class<?>> collectors = new ArrayList<>(reflections.getTypesAnnotatedWith(Collector.class));

        ChoiceDialog<Class<?>> dialog = new ChoiceDialog<>(collectors.get(0), collectors);
        dialog.setTitle("Pick a Collector");
        dialog.setHeaderText("Pick a Collector");
        dialog.setContentText("Choose a way to collect assignment submissions:");

        dialog.showAndWait().ifPresent(clazz -> {
            collectorClazz = clazz;
            openClassConfig(clazz, this::acceptCollector);
        });
    }

    public void openProcessorSelector(ActionEvent event) {
        Reflections reflections = new Reflections("chalkbox");

        List<Class<?>> collectors = new ArrayList<>(reflections.getTypesAnnotatedWith(Processor.class));

        ChoiceDialog<Class<?>> dialog = new ChoiceDialog<>(collectors.get(0), collectors);
        dialog.setTitle("Pick a Processor");
        dialog.setHeaderText("Pick a Processor");
        dialog.setContentText("Choose a process to run on submissions:");

        dialog.showAndWait().ifPresent(clazz -> {
            processorClazz = clazz;
            openClassConfig(clazz, this::acceptProcessor);
        });
    }

    private void openClassConfig(Class clazz, Callback accept) {
        // Create the custom dialog
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Collector Config");

        ButtonType acceptButton = new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(acceptButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        List<TextField> items = new ArrayList<>();
        int row = 0;
        for (Field field : clazz.getFields()) {
            if (field.isAnnotationPresent(ConfigItem.class)) {
                ConfigItem config = field.getAnnotation(ConfigItem.class);

                TextField item = new TextField();
                items.add(item);
                item.setPromptText(config.key());

                grid.add(new Label(config.key()), 0, row);
                grid.add(item, 1, row);
                row++;
                grid.add(new Label(config.description()), 0, row, 2, 1);
                row++;
            }
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == acceptButton) {
                Map<String, String> results = new HashMap<>();
                for (TextField item : items) {
                    results.put(item.getPromptText(), item.getText());
                }
                return results;
            }
            return null;
        });

        Optional<Map<String, String>> result = dialog.showAndWait();

        result.ifPresent(results -> {
            accept.accept(clazz, results);
        });
    }

    public int showConfig(GridPane pane, String header, Map<String, String> config) {
        pane.add(new Text(header), 0, 0);

        int row = 1;
        for (Map.Entry<String, String> configItem : config.entrySet()) {
            pane.add(new Text(configItem.getKey()), 0, row);
            pane.add(new Text(configItem.getValue()), 1, row);
            row++;
        }
        return row;
    }

    public void acceptCollector(Class<?> clazz, Map<String, String> config) {
        chalkbox.loadConfig(config);

        collector.getChildren().clear();
        int row = showConfig(collector, clazz.getName(), config);

        Button runCollector = new Button("Run Collector");
        collector.add(runCollector, 0, row);

        runCollector.setOnAction(this::runCollector);
    }

    public void acceptProcessor(Class<?> clazz, Map<String, String> config) {
        chalkbox.loadConfig(config);

        processor.getChildren().clear();
        int row = showConfig(processor, clazz.getName(), config);

        Button runProcessor = new Button("Run Processor");
        processor.add(runProcessor, 0, row);

        runProcessor.setOnAction(this::runProcessor);
    }

    public void runCollector(ActionEvent event) {
        Execution execution = new Execution() {
            @Override
            public void run() {
                chalkbox.executeCollection(collectorClazz);
            }
        };
        execution.start();
    }

    public void runProcessor(ActionEvent event) {
        Execution execution = new Execution() {
            @Override
            public void run() {
                chalkbox.executeProcess(processorClazz);
            }
        };
        execution.start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        chalkbox = new ChalkBox();

        BorderPane pane = new BorderPane();

        collector = new GridPane();
        Text collectorHeader = new Text("Collector");
        collectorHeader.setFont(new Font(30));
        Button collectorOpen = new Button("Load Collector");
        collectorOpen.setOnAction(this::openCollectorSelector);

        collector.add(collectorHeader, 0, 0);
        collector.add(collectorOpen, 0, 1);
        pane.setTop(collector);

        TextArea textOut = new TextArea();
        textOut.setEditable(false);
        log = new ErrorWindow(textOut);
        PrintStream ps = new PrintStream(log, true);
        chalkbox.setOutput(ps);
        pane.setCenter(textOut);

        ListView<String> students = new ListView<>();
        this.students = FXCollections.observableArrayList();
        students.setItems(this.students);
        pane.setRight(students);

        processor = new GridPane();
        Text processorHeader = new Text("Processor");
        processorHeader.setFont(new Font(30));
        Button processorOpen = new Button("Load Processor");
        processorOpen.setOnAction(this::openCollectorSelector);
        processorOpen.setOnAction(this::openProcessorSelector);

        processor.add(processorHeader, 0, 0);
        processor.add(processorOpen, 0, 1);
        pane.setBottom(processor);

        primaryStage.setTitle("Chalkbox Marking Tool");
        primaryStage.setScene(new Scene(pane));
        primaryStage.show();
    }
}


class ErrorWindow extends OutputStream {

    private TextArea output;

    public ErrorWindow(TextArea ta) {
        this.output = ta;
    }

    @Override
    public void write(int i) throws IOException {
        output.appendText(String.valueOf((char) i));
    }
}


interface Callback {
    void accept(Class<?> clazz, Map<String, String> config);
}

abstract class Execution extends Thread {
    @Override
    public abstract void run();
}