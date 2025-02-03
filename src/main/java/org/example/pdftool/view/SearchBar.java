package org.example.pdftool.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.example.pdftool.controller.PDFController;

import java.io.IOException;
import java.util.List;

public class SearchBar extends HBox {
    private final TextField searchField;
    private final PDFController pdfController;
    private final PDFDocumentView documentView;
    private final Label resultCount;
    private boolean isVisible = false;

    public SearchBar(PDFController pdfController, PDFDocumentView documentView) {
        this.pdfController = pdfController;
        this.documentView = documentView;

        // Create search field
        searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(200);

        // Label to show number of results
        resultCount = new Label("");
        resultCount.setStyle("-fx-text-fill: white; -fx-background-color: rgb(30,30,30); -fx-font-size: 12px;");
        resultCount.setPrefWidth(50);

        // Create next/previous buttons
        Button prevButton = new Button("↑");
        Button nextButton = new Button("↓");

        // Style components
        searchField.setStyle("""
                -fx-text-fill: white;
                -fx-background-color: rgb(30,30,30);
                -fx-font-size: 12px;
                -fx-padding: 5px
                """);
        String buttonStyle ="""
                -fx-text-fill: white;
                -fx-background-color: rgb(30,30,30);
                -fx-font-size: 14px;
                -fx-padding: 5px 10px;
                """;
        prevButton.setStyle(buttonStyle);
        nextButton.setStyle(buttonStyle);

        this.setSpacing(5);
        this.setAlignment(Pos.CENTER_RIGHT);
        this.setPadding(new Insets(5));
        this.setStyle("-fx-background-color: rgb(50,50,50);");

        // Hide by default
        this.setVisible(false);
        // this.setManaged(false);

        // Search functionality
        searchField.setOnKeyPressed(event -> {
            System.out.println("Key pressed: " + event.getCode());
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("Enter pressed - performing search");
                performSearch();
            }
        });

        prevButton.setOnAction(event -> showPreviousResult());
        nextButton.setOnAction(event -> showNextResult());

        this.getChildren().addAll(searchField, prevButton, nextButton, resultCount);
    }

    private void performSearch() {
        try {
            String searchTerm = searchField.getText();
            System.out.println("Search term: " + searchTerm);
            List<PDFController.PDFSearchResult> results = pdfController.searchText(searchTerm);
            System.out.println("Search results: " + results.size());

            if (results.isEmpty()) {
                resultCount.setText("No results found");
            } else {
                resultCount.setText("1 of " + results.size());
                showNextResult();
            }
        } catch (IOException e) {
            e.printStackTrace();
            resultCount.setText("No results found");
        }
    }

    private void showNextResult() {
        PDFController.PDFSearchResult result = pdfController.getNextSearchResult();
        if (result != null) {
            System.out.println("Moving to result on page " + result.getPageNumber());
            pdfController.setCurrentPage(result.getPageNumber());
            updateResultCount();
            documentView.displayCurrentPage();
        }
    }

    private void showPreviousResult() {
        PDFController.PDFSearchResult result = pdfController.getPreviousSearchResult();
        if (result != null) {
            System.out.println("Moving to result on page " + result.getPageNumber());
            pdfController.setCurrentPage(result.getPageNumber());
            updateResultCount();
            documentView.displayCurrentPage();
        }
    }

    private void updateResultCount() {
        List<PDFController.PDFSearchResult> results = pdfController.getCurrentSearchResults();
        if (!results.isEmpty()) {
            System.out.println("Updating result count.");
            int current = pdfController.getCurrentSearchIndex() + 1;
            resultCount.setText(current + " of " + results.size());
        }
    }


    public void toggle() {
        isVisible = !isVisible;
        this.setVisible(isVisible);
        // this.setManaged(isVisible);
        if (isVisible) {
            searchField.requestFocus();
        }
    }
}
