package org.example.pdftool;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.example.pdftool.controller.PDFController;
import org.example.pdftool.view.PDFDocumentView;

import java.io.File;
import java.io.IOException;

public class PDFToolApp extends Application {
    // Class variables
    private PDFController pdfController;
    private PDFDocumentView documentView;
    private final BorderPane root = new BorderPane();

    // Menu variables
    Menu fileMenu = new Menu("File");
    MenuItem openItem = new MenuItem("Open PDF...");
    MenuItem saveItem = new MenuItem("Save PDF...");
    MenuItem exitItem = new MenuItem("Exit");

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
            System.out.println("Selected file: " + file.getAbsolutePath());
        }
    }

    private void setupMenuBar() {
        // Create menu bar
        MenuBar menuBar = new MenuBar();

        // Add file menu to menu bar
        fileMenu.getItems().addAll(openItem, saveItem, exitItem);
        menuBar.getMenus().add(fileMenu);

        // Add menu bar to root
        root.setTop(menuBar);
    }

    @Override
    public void start(Stage stage){
        // Initialise controller
        pdfController = new PDFController();
        documentView = new PDFDocumentView(pdfController);

        // Create menu
        setupMenuBar();

        // Add document view to root
        root.setCenter(documentView);
        BorderPane.setAlignment(documentView, Pos.CENTER);

        // Event handlers
        openItem.setOnAction(event -> openPDF(stage, root));
        saveItem.setOnAction(event -> savePDF(stage));
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