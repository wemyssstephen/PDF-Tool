package org.example.pdftool.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.example.pdftool.controller.PDFController;
import org.example.pdftool.theme.Theme;

public class PageCounter extends HBox {
    private final Label pageLabel;
    private final PDFController pdfController;

    public PageCounter(PDFController pdfController) {
        this.pdfController = pdfController;

        pageLabel = new Label("No document loaded.");
        pageLabel.setStyle(String.format("""
                -fx-text-fill: %s;
                -fx-background-color: %s;
                -fx-background-radius: 4px;
                -fx-padding: 8px 12px;
                -fx-font-size: 13px;
                """,
                Theme.TEXT_PRIMARY,
                Theme.SURFACE
        ));

        this.getChildren().add(pageLabel);
        this.setAlignment(Pos.CENTER_RIGHT);
        this.setPadding(new Insets(5));
        this.setStyle("-fx-background-color: transparent;");
    }

    public void updateLabel() {
        int currentPage = pdfController.getCurrentPage() + 1;
        int totalPages = pdfController.getPageCount();
        pageLabel.setText(String.format("Page %d of %d", currentPage, totalPages));
    }
}
