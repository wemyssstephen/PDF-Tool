package org.example.pdftool.view;

import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
;

public class RedactionTool extends Rectangle {
    private Point2D pdfTopLeft;
    private Point2D pdfBottomRight;
    private final int pageNumber;

    public RedactionTool(double screenX, double screenY, int pageNumber) {
        super(screenX, screenY, 0, 0);
        this.pageNumber = pageNumber;
        setFill(javafx.scene.paint.Color.BLACK);
        setStroke(javafx.scene.paint.Color.BLACK);
        setOpacity(0.5);
    }

    public void updatePDFCoordinates(Point2D pdfTopLeft, Point2D pdfBottomRight) {
        this.pdfTopLeft = pdfTopLeft;
        this.pdfBottomRight = pdfBottomRight;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public PDRectangle getPDFRectangle() {
        double minX = Math.min(pdfTopLeft.getX(), pdfBottomRight.getX());
        double maxX = Math.max(pdfTopLeft.getX(), pdfBottomRight.getX());
        double minY = Math.min(pdfTopLeft.getY(), pdfBottomRight.getY());
        double maxY = Math.max(pdfTopLeft.getY(), pdfBottomRight.getY());

        return new PDRectangle(
                (float)minX,
                (float)minY,
                (float)(maxX - minX),
                (float)(maxY - minY)
        );
    }
}
