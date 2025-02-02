package org.example.pdftool;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.example.pdftool.controller.PDFController;
import org.example.pdftool.view.PDFDocumentView;
import org.example.pdftool.view.PageCounter;

import java.io.File;
import java.io.IOException;

public class PDFToolApp extends Application {
    // Class variables
    private PDFController pdfController;
    private PDFDocumentView documentView;
    private PageCounter pageCounter;
    private final BorderPane root = new BorderPane();

    // Mode variables
    private boolean redactModeActive = false;

    // Menu variables
    Menu fileMenu = new Menu("File");
    Menu toolsMenu = new Menu("Tools");
    MenuItem openItem = new MenuItem("Open PDF...");
    MenuItem saveItem = new MenuItem("Save PDF...");
    MenuItem exitItem = new MenuItem("Exit");
    MenuItem redactTool = new MenuItem("Redact");
    MenuItem deleteLastRedaction = new MenuItem("Delete Last Redaction");
    MenuItem deleteAllRedaction = new MenuItem("Delete All Redactions");

    private void openPDF(Stage stage, BorderPane root) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                pdfController.loadPDFDocument(file);
                documentView.setupRenderer();
                documentView.displayCurrentPage();
                pageCounter.updateLabel();
            } catch (IOException e) {
                // How to handle error?
                e.printStackTrace();
            }
            System.out.println("Selected file: " + file.getAbsolutePath());
        }
    }

    private void savePDF(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        var file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                pdfController.savePDFDocument(file);

                // Success alert
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("PDF saved to " + file.getAbsolutePath());
                alert.showAndWait();
            } catch (IllegalStateException e) {
                // No document loaded alert
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("No Document Loaded");
                alert.setContentText("Please open a PDF before saving");
                alert.showAndWait();
            } catch (IOException e) {
                // Error alert
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Save Failed");
                alert.setContentText("Failed to save PDF to: " + file.getAbsolutePath());
                alert.showAndWait();
            }
        }
    }

    private void setupMenuBar() {
        // Create menu bar
        MenuBar menuBar = new MenuBar();

        // Add menus to menu bar
        fileMenu.getItems().addAll(openItem, saveItem, exitItem);
        toolsMenu.getItems().addAll(redactTool, deleteLastRedaction, deleteAllRedaction);
        menuBar.getMenus().addAll(fileMenu, toolsMenu);

        // Add menu bar to root
        root.setTop(menuBar);
    }

    @Override
    public void start(Stage stage){
        // Initialise controller
        pdfController = new PDFController();
        pageCounter = new PageCounter(pdfController);
        documentView = new PDFDocumentView(pdfController, pageCounter);

        // Create menu
        setupMenuBar();

        // Add document view to root
        root.setCenter(documentView);
        root.setBottom(pageCounter);
        // BorderPane.setAlignment(documentView, Pos.CENTER);
        BorderPane.setMargin(documentView, new Insets(2));

        // Event handlers
        openItem.setOnAction(event -> openPDF(stage, root));
        saveItem.setOnAction(event -> savePDF(stage));
        redactTool.setOnAction(event -> {
            redactModeActive = !redactModeActive;
            documentView.setRedactionModeActive(redactModeActive);
        });
        deleteLastRedaction.setOnAction(event -> {
            documentView.clearLastRedaction();
        });
        deleteLastRedaction.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
        deleteAllRedaction.setOnAction(event -> {
            documentView.clearAllRedactions();
        });
        exitItem.setOnAction(event -> Platform.exit());

        // Create the scene
        Scene scene = new Scene(root, 1200, 1000);

        // Show window
        stage.setTitle("PDF Tool");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}