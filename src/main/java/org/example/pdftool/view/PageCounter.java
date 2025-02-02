package org.example.pdftool.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.example.pdftool.controller.PDFController;

public class PageCounter extends HBox {
    private final Label pageLabel;
    private final PDFController pdfController;

    public PageCounter(PDFController pdfController) {
        this.pdfController = pdfController;

        pageLabel = new Label("No document loaded.");
        pageLabel.setStyle("-fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.5); -fx-padding: 5px");

        this.getChildren().add(pageLabel);
        this.setAlignment(Pos.CENTER_RIGHT);
        this.setPadding(new Insets(5));
        this.setStyle("-fx-background-color: rgba(50,50,50);");
    }

    public void updateLabel() {
        int currentPage = pdfController.getCurrentPage() + 1;
        int totalPages = pdfController.getPageCount();
        pageLabel.setText(String.format("Page %d of %d", currentPage, totalPages));
    }
}
