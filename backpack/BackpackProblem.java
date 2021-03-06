/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backpack;


import backpack.impl.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Optional;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.swing.*;

/*TODO

-polepszyć menu -> obsługa podania złej nazwy pliku
 */

/**
 * @author piotr
 */
public class BackpackProblem extends Application {

    Stage window;
    Scene mainScene;
    boolean isInstanceLoaded = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setTitle("Problem plecakowy");

        BackpackImpl backpack = new BackpackImpl();


        ///////////////////////////////////////////////////////
        //TOP PANE
        ////////////////////////////////////////////////////////
        TextField fileNameTextField = new TextField();
        fileNameTextField.setPromptText("Nazwa pliku");

        Button loadFileButton = new Button("Załaduj istniejący plik");
        loadFileButton.setAlignment(Pos.BASELINE_RIGHT);

        VBox topPane = new VBox();
        topPane.setSpacing(10);
        topPane.setPadding(new Insets(10, 10, 10, 10));
        topPane.setAlignment(Pos.CENTER);
        topPane.getChildren().addAll(fileNameTextField, loadFileButton);
        topPane.setStyle("-fx-border-style: dotted");
        ///////////////////////////////////////////////////////

        ///////////////////////////////////////////////////////
        //MIDDLE PANE
        ////////////////////////////////////////////////////////
        TextField instanceSizeTextField = new TextField();
        instanceSizeTextField.setPromptText("Rozmiar instancji");

        TextField maxItemSizeTextField = new TextField();
        maxItemSizeTextField.setPromptText("Maksymalny rozmiar przedmiotów");

        TextField maxItemValueTextField = new TextField();
        maxItemValueTextField.setPromptText("Maksymalna wartość przedmiotów");

        Button loadRandInstanceButton = new Button("Załaduj losową instancję");
        Button generateRandInstanceFileButton = new Button("Utwórz plik z losową instancją");

        TextField errorCoefficientTextField = new TextField();
        errorCoefficientTextField.setPromptText("Współczynnik błędu (dla FPTAS)");

        VBox middlePane = new VBox();
        middlePane.setSpacing(10);
        middlePane.setPadding(new Insets(10, 10, 10, 10));
        middlePane.setAlignment(Pos.CENTER);
        middlePane.getChildren().addAll(instanceSizeTextField, maxItemSizeTextField, maxItemValueTextField,
                generateRandInstanceFileButton, loadRandInstanceButton, errorCoefficientTextField);
        middlePane.setStyle("-fx-border-style: dotted");
        ////////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////
        //BOTTOM PANE
        ////////////////////////////////////////////////////////
        Label instanceLoadStatusLabel = new Label("Nie załadowano żadnej instancji problemu");
        instanceLoadStatusLabel.setTextFill(Color.RED);

        TextField backpackSizeTextField = new TextField();
        backpackSizeTextField.setPromptText("Rozmiar plecaka");

        TextArea algorithmResultsTextArea = new TextArea("");
        algorithmResultsTextArea.setMinHeight(200);

        ChoiceBox<String> algorithmChoiceBox = new ChoiceBox<>();
        algorithmChoiceBox.getItems().add("Branch&Bound");
        algorithmChoiceBox.getItems().add("Dynamic programming");
        algorithmChoiceBox.getItems().add("Brute force");
        algorithmChoiceBox.getItems().add("FPTAS schema");
        algorithmChoiceBox.setValue(algorithmChoiceBox.getItems().get(0));

        Button executeButton = new Button("Wykonaj algorytm");
        Button clearTextAreaButton = new Button("Wyczyść pole tesktowe");
        clearTextAreaButton.setOnAction(e -> algorithmResultsTextArea.clear());

        GridPane bottomPane = new GridPane();
        bottomPane.setHgap(10);
        bottomPane.setVgap(10);
        bottomPane.setPadding(new Insets(10, 10, 10, 10));
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.add(instanceLoadStatusLabel, 0, 0, 3, 1);
        bottomPane.add(algorithmChoiceBox, 0, 1);
        bottomPane.add(backpackSizeTextField, 1, 1);
        bottomPane.add(executeButton, 2, 1);
        bottomPane.add(algorithmResultsTextArea, 0, 2, 3, 1);
        bottomPane.add(clearTextAreaButton, 0, 3);
        bottomPane.setStyle("-fx-border-style: dotted");
        //////////////////////////////////////////////////////////

        /////////////////////////////////
        //Button control
        /////////////////////////////////
        //Przycisk załadowania pliku
        loadFileButton.setOnAction(loadFileAction(backpack, fileNameTextField, instanceLoadStatusLabel, algorithmResultsTextArea));

        //Przycisk generowania pliku z losową instancją
        generateRandInstanceFileButton.setOnAction(generateFileAction(fileNameTextField, instanceSizeTextField, maxItemSizeTextField, maxItemValueTextField, algorithmResultsTextArea));

        //Przycisk ładowania losowej instancji
        loadRandInstanceButton.setOnAction(loadRandomFileAction(backpack, instanceSizeTextField, maxItemSizeTextField, maxItemValueTextField, instanceLoadStatusLabel, algorithmResultsTextArea));

        //Przycisk wykonania algorytmu
        executeButton.setOnAction(executeAlgorithmAction(backpack, errorCoefficientTextField, backpackSizeTextField, algorithmResultsTextArea, algorithmChoiceBox));
        //////////////////////////////////

        VBox mainLayout = new VBox();
        mainLayout.setSpacing(10);
        mainLayout.getChildren().addAll(topPane, middlePane, bottomPane);
        mainLayout.setPadding(new Insets(10, 10, 10, 10));

        mainScene = new Scene(mainLayout);
        window.setScene(mainScene);
        window.show();
    }

    private EventHandler<ActionEvent> executeAlgorithmAction(BackpackImpl backpack, TextField errorCoefficientTextField, TextField backpackSizeTextField, TextArea algorithmResultsTextArea, ChoiceBox<String> algorithmChoiceBox) {
        return e -> {
            String backpackSizeInput = backpackSizeTextField.getText();
            if (backpackSizeInput.equals("") || !isInstanceLoaded) {
                algorithmResultsTextArea.setStyle("-fx-text-fill: red");
                algorithmResultsTextArea.appendText("Aby wykonać algorytm, musisz załadować instancję\n" +
                        "problemu i podać rozmiar plecaka!\n");
            } else {
                int backpack_size;
                try {
                    backpack_size = Integer.parseInt(backpackSizeInput);
                    String algorithmChoice = algorithmChoiceBox.getValue();

                    switch (algorithmChoice) {
                        case "Branch&Bound": {
                            BranchAndBoundAlgorithm algorithm = new BranchAndBoundAlgorithm(backpack.getItemsToPut(), (double) backpack_size);
                            algorithm.startBranchAndBound();
                            algorithmResultsTextArea.setStyle("-fx-text-fill: black");
                            algorithmResultsTextArea.appendText("Wyniki wykonania algorytmu:\n" +
                                    "Zapakowano: " + algorithm.getBestSize() + "/" + algorithm.getBackpackSize() + "\n" +
                                    "Wartość zapakowanych przedmiotów: " + algorithm.getBestValue() + "\n" +
                                    "Zapakowane przedmioty:\n" +
                                    algorithm.getBestItems().toString() + "\n"
                            );
                            break;
                        }

                        case "Dynamic programming": {
                            DynamicProgrammingAlgorithm algorithm = new DynamicProgrammingAlgorithm(backpack.getItemsToPut(), backpack_size);
                            algorithm.startDynamicProgramming();
                            algorithmResultsTextArea.setStyle("-fx-text-fill: black");
                            algorithmResultsTextArea.appendText("Wyniki wykonania algorytmu:\n" +
                                    "Zapakowano: " + algorithm.getBestSize() + "/" + algorithm.getBackpackSize() + "\n" +
                                    "Wartość zapakowanych przedmiotów: " + algorithm.getBestValue() + "\n" +
                                    "Zapakowane przedmioty:\n" +
                                    algorithm.getBestItems().toString() + "\n"
                            );
                            break;
                        }
                        case "Brute force": {
                            if (backpack.getItemsToPut().size() > BruteForceAlgorithm.MAX_COUNT_OF_ITEMS_FOR_BRUTEFORCE) {
                                algorithmResultsTextArea.setStyle("-fx-text-fill: red");
                                algorithmResultsTextArea.appendText("Zbyt duży rozmiar instancji! Max to 30!\n");
                            } else {
                                BruteForceAlgorithm algorithm = new BruteForceAlgorithm(backpack.getItemsToPut(), backpack_size);
                                algorithm.startBruteForce();
                                algorithmResultsTextArea.setStyle("-fx-text-fill: black");
                                algorithmResultsTextArea.appendText("Wyniki wykonania algorytmu:\n" +
                                        "Zapakowano: " + algorithm.getBestSize() + "/" + algorithm.getBackpackSize() + "\n" +
                                        "Wartość zapakowanych przedmiotów: " + algorithm.getBestValue() + "\n" +
                                        "Zapakowane przedmioty:\n" +
                                        algorithm.getBestItems().toString() + "\n"
                                );
                            }
                            break;
                        }
                        case "FPTAS schema": {
                            FPTASSchema algorithm = new FPTASSchema(backpack_size, backpack.getItemsToPut(), Float.valueOf(errorCoefficientTextField.getText()));
                            algorithm.startFPTASSchema();
                            algorithmResultsTextArea.setStyle("-fx-text-fill: black");
                            algorithmResultsTextArea.appendText("Wyniki wykonania algorytmu:\n" +
                                    "Zapakowano: " + algorithm.getBestSize() + "/" + algorithm.getBackpackSize() + "\n" +
                                    "Wartość zapakowanych przedmiotów: " + algorithm.getBestValue() + "\n" +
                                    "Zapakowane przedmioty:\n" +
                                    algorithm.getBestItems().toString() + "\n"
                            );
                            break;
                        }
                    }

                } catch (NumberFormatException ex) {
                    algorithmResultsTextArea.setStyle("-fx-text-fill: red");
                    algorithmResultsTextArea.appendText("Rozmiar plecaka musi być liczbą całkowitą!\n");
                }
            }
        };
    }

    private EventHandler<ActionEvent> loadRandomFileAction(BackpackImpl backpack, TextField instanceSizeTextField, TextField maxItemSizeTextField, TextField maxItemValueTextField, Label instanceLoadStatusLabel, TextArea algorithmResultsTextArea) {
        return e -> {
            String instanceSize = instanceSizeTextField.getText();
            String maxItemSize = maxItemSizeTextField.getText();
            String maxItemsValue = maxItemValueTextField.getText();

            if (instanceSize.equals("") || maxItemSize.equals("") || maxItemsValue.equals("")) {
                algorithmResultsTextArea.setStyle("-fx-text-fill: red");
                algorithmResultsTextArea.appendText("Podaj wszystkie parametry instancji!\n");
            } else {
                int instance_size, max_size, max_value;
                try {
                    instance_size = Integer.parseInt(instanceSize);
                    max_size = Integer.parseInt(maxItemSize);
                    max_value = Integer.parseInt(maxItemsValue);

                    ArrayList<Pair<Integer, Integer>> problemInstance;
                    problemInstance = ItemsListGenerator.generateInstance(instance_size, max_size, max_value);

                    if (problemInstance == null) {
                        algorithmResultsTextArea.setStyle("-fx-text-fill: red");
                        algorithmResultsTextArea.appendText("Nieoczekiwany błąd przy generowaniu danych!\n");
                    } else {
                        backpack.setItemsToPut(problemInstance);
                        isInstanceLoaded = true;
                        algorithmResultsTextArea.setStyle("-fx-text-fill: green");
                        algorithmResultsTextArea.appendText("Załadowano losową instancję problemu!\n");
                        instanceLoadStatusLabel.setTextFill(Color.GREEN);
                        instanceLoadStatusLabel.setText("Załadowano instancję problemu.");
                    }
                } catch (NumberFormatException ex) {
                    algorithmResultsTextArea.setStyle("-fx-text-fill: red");
                    algorithmResultsTextArea.appendText("Parametry muszą być liczbami całkowitymi!\n");
                }
            }
        };
    }

    private EventHandler<ActionEvent> generateFileAction(TextField fileNameTextField, TextField instanceSizeTextField, TextField maxItemSizeTextField, TextField maxItemValueTextField, TextArea algorithmResultsTextArea) {
        return e -> {
            String instanceSize = instanceSizeTextField.getText();
            String maxItemSize = maxItemSizeTextField.getText();
            String maxItemsValue = maxItemValueTextField.getText();

            if (instanceSize.equals("") || maxItemSize.equals("") || maxItemsValue.equals("")) {
                algorithmResultsTextArea.setStyle("-fx-text-fill: red");
                algorithmResultsTextArea.appendText("Podaj wszystkie parametry instancji!\n");
            } else {
                int instance_size, max_size, max_value;
                try {
                    instance_size = Integer.parseInt(instanceSize);
                    max_size = Integer.parseInt(maxItemSize);
                    max_value = Integer.parseInt(maxItemsValue);

                    ArrayList<Pair<Integer, Integer>> problemInstance;
                    problemInstance = ItemsListGenerator.generateInstance(instance_size, max_size, max_value);

                    if (problemInstance == null) {
                        algorithmResultsTextArea.setStyle("-fx-text-fill: red");
                        algorithmResultsTextArea.appendText("Nieoczekiwany błąd przy generowaniu danych!\n");
                    } else {
                        String fileName = fileNameTextField.getText();
                        if (fileName.equals("")) {
                            algorithmResultsTextArea.setStyle("-fx-text-fill: red");
                            algorithmResultsTextArea.appendText("Podaj nazwę pliku dla generowanej instancji!\n");
                        } else {
                            ItemsListFileSaver saver = new ItemsListFileSaver(problemInstance);
                            saver.saveToFile(fileName);
                            algorithmResultsTextArea.setStyle("-fx-text-fill: green");
                            algorithmResultsTextArea.appendText("Utworzono nową instancję problemu w pliku " + fileName + "\n");
                        }
                    }
                } catch (NumberFormatException ex) {
                    algorithmResultsTextArea.setStyle("-fx-text-fill: red");
                    algorithmResultsTextArea.appendText("Parametry muszą być liczbami całkowitymi!\n");
                }
            }
        };
    }

    private EventHandler<ActionEvent> loadFileAction(BackpackImpl backpack, TextField fileNameTextField, Label instanceLoadStatusLabel, TextArea algorithmResultsTextArea) {
        return e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.addChoosableFileFilter(new FileFilter().getFilter());
            fileChooser.setAcceptAllFileFilterUsed(true);
            fileChooser.showOpenDialog(null);
            File file = null;
            if ((file = fileChooser.getSelectedFile()) != null) {
                fileNameTextField.setText(file.getAbsolutePath());
            } else
                JOptionPane.showMessageDialog(null, "Nie znaleziono pliku");

            String fileName = fileNameTextField.getText();
            if (fileName.equals(""))

            {
                algorithmResultsTextArea.setStyle("-fx-text-fill: red");
                algorithmResultsTextArea.setText("Podaj nazwę pliku!\n");
            } else

            {
                ArrayList<Pair<Integer, Integer>> problemInstance;
                try {
                    problemInstance = ItemsListFileLoader.loadFromFile(fileName);
                    if (!problemInstance.isEmpty()) {
                        backpack.setItemsToPut(problemInstance);
                        isInstanceLoaded = true;
                        algorithmResultsTextArea.setStyle("-fx-text-fill: green");
                        algorithmResultsTextArea.appendText("Załadowano instancję problemu z pliku " + fileName + "\n");
                        instanceLoadStatusLabel.setTextFill(Color.GREEN);
                        instanceLoadStatusLabel.setText("Załadowano instancję problemu.");
                    } else {
                        algorithmResultsTextArea.setStyle("-fx-text-fill: red");
                        algorithmResultsTextArea.appendText("Plik jest pusty!\n");
                    }
                } catch (FileNotFoundException ex) {
                    algorithmResultsTextArea.setStyle("-fx-text-fill: red");
                    algorithmResultsTextArea.appendText("Nie ma takiego pliku!\n");
                }
            }
        };
    }

    private class FileFilter {
        public javax.swing.filechooser.FileFilter getFilter() {
            return new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File file) {
                    String ext = getExtension(file.getName());
                    return ext.equals("") || (ext.equals(".txt"));
                }

                @Override
                public String getDescription() {
                    return ".txt";
                }

                private String getExtension(String a) {
                    if (a.contains("."))
                        return Optional.of(a.substring(a.lastIndexOf('.'), a.length()).toLowerCase()).orElse("");
                    return "";
                }

            };
        }
    }
}

























