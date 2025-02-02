package org.example.pdftool.view;

import javafx.scene.shape.Rectangle;

import java.awt.event.MouseEvent;

public class RedactionTool extends Rectangle {
    int pdfX, pdfY;
    int pdfWidth, pdfHeight;
    int currentPage;

    public RedactionTool(double screenX, double screenY, int currentPage) {
        super(screenX, screenY, 0, 0);
        this.currentPage = currentPage;

        setFill(javafx.scene.paint.Color.BLACK);
        setStroke(javafx.scene.paint.Color.BLACK);
    }
}
