package org.example.pdftool.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.example.pdftool.controller.PDFController;
import org.example.pdftool.theme.Theme;

import java.io.IOException;
import java.util.List;

public class SearchBar extends HBox {
    private final TextField searchField;
    private final PDFController pdfController;
    private final PDFDocumentView documentView;
    private PageCounter pageCounter;
    private final Label resultCount;
    private boolean isVisible = false;

    public SearchBar(PDFController pdfController, PDFDocumentView documentView, PageCounter pageCounter) {
        this.pdfController = pdfController;
        this.documentView = documentView;
        this.pageCounter = pageCounter;

        // Create search field
        searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(200);
        searchField.setStyle(String.format("""
                        
                        -fx-text-fill: %s;
                        -fx-background-color: %s;
                        -fx-background-radius: 4px;
                        -fx-font-size: 13px;
                        -fx-padding: 8px;
                        -fx-border-color: %s;
                        -fx-border-radius: 4px;
                        -fx-border-width: 1px
                        
                        """,
                Theme.TEXT_PRIMARY,
                Theme.BACKGROUND,
                Theme.SURFACE
        ));

        // Label to show number of results
        resultCount = new Label("");
        resultCount.setStyle(String.format("""
                        -fx-text-fill: %s;
                        -fx-background-color: %s;
                        -fx-background-radius: 4px;
                        -fx-font-size: 13px;
                        -fx-padding: 8px;
                        """,
                Theme.TEXT_SECONDARY,
                Theme.BACKGROUND
        ));
        resultCount.setPrefWidth(80);
        resultCount.setVisible(false);

        // Create next/previous buttons
        Button prevButton = new Button("↑");
        Button nextButton = new Button("↓");

        String buttonStyle = String.format("""
                        -fx-text-fill: %s;
                        -fx-background-color: %s;
                        -fx-background-radius: 4px;
                        -fx-font-size: 14px;
                        -fx-padding: 8px 12px;
                        -fx-cursor: hand;
                        -fx-border-color: transparent;
                        """,
                Theme.TEXT_PRIMARY,
                Theme.PRIMARY
        );

        String buttonHoverStyle = buttonStyle + String.format("""
                        -fx-background-color: %s;
                        """,
                Theme.SURFACE
        );

        prevButton.setStyle(buttonStyle);
        nextButton.setStyle(buttonStyle);

        // Add hover effects
        prevButton.setOnMouseEntered(e -> prevButton.setStyle(buttonHoverStyle));
        prevButton.setOnMouseExited(e -> prevButton.setStyle(buttonStyle));
        nextButton.setOnMouseEntered(e -> nextButton.setStyle(buttonHoverStyle));
        nextButton.setOnMouseExited(e -> nextButton.setStyle(buttonStyle));

        this.setSpacing(8);
        this.setAlignment(Pos.CENTER_RIGHT);
        this.setPadding(new Insets(8));
        this.setStyle("-fx-background-color: transparent;");

        // Hide by default
        this.setVisible(false);

        // Search functionality
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("Performing search...");
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
            List<PDFController.PDFSearchResult> results = pdfController.searchText(searchTerm);

            if (results.isEmpty()) {
                resultCount.setText("No results found");
                resultCount.setVisible(true);
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        Platform.runLater(() -> resultCount.setVisible(false));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                resultCount.setText("1/" + results.size());
                resultCount.setVisible(true);
                showNextResult();
            }
        } catch (IOException e) {
            e.printStackTrace();
            resultCount.setText("No results found");
            resultCount.setVisible(true);
        }
    }

    private void showNextResult() {
        PDFController.PDFSearchResult result = pdfController.getNextSearchResult();
        if (result != null) {
            pdfController.setCurrentPage(result.pageNumber());
            updateResultCount();
            documentView.displayCurrentPage();
            pageCounter.updateLabel();
        }
    }

    private void showPreviousResult() {
        PDFController.PDFSearchResult result = pdfController.getPreviousSearchResult();
        if (result != null) {
            pdfController.setCurrentPage(result.pageNumber());
            updateResultCount();
            documentView.displayCurrentPage();
            pageCounter.updateLabel();
        }
    }

    private void updateResultCount() {
        List<PDFController.PDFSearchResult> results = pdfController.getCurrentSearchResults();
        if (!results.isEmpty()) {
            int current = pdfController.getCurrentSearchIndex() + 1;
            resultCount.setText(current + "/" + results.size());
        }
    }

    public void toggle() {
        isVisible = !isVisible;
        this.setVisible(isVisible);
        if (isVisible) {
            searchField.requestFocus();
        }
    }
}
