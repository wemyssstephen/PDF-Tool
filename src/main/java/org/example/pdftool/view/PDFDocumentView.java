package org.example.pdftool.view;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.pdftool.controller.PDFController;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class PDFDocumentView extends Pane {
    private final PDFController pdfController;
    private final ZoomableScrollPane scrollPane;
    private PDFRenderer renderer;

    private final ImageView pdfView;
    private final StackPane centrePane;
    private PageCounter pageCounter;

    private final Pane redactionLayer;
    private RedactionTool currentRedaction;
    private boolean redactionModeActive;
    private double AnchorX;
    private double AnchorY;

    public class ZoomableScrollPane extends ScrollPane {
        private double scaleValue = 1.0;
        private double zoomIntensity = 0.02;
        private Node target;
        private Node zoomNode;

        public ZoomableScrollPane(Node target) {
            super();
            this.target = target;
            this.zoomNode = new Group(target);
            setContent(outerNode(zoomNode));

            setPannable(true);
            setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            setFitToHeight(true); //center
            setFitToWidth(true); //center

            this.setStyle("-fx-background: rgb(50,50,50); -fx-background-color: rgb(50,50,50);");

            updateScale();
        }

        private Node outerNode(Node node) {
            StackPane stackPane = new StackPane(node);
            stackPane.setAlignment(Pos.CENTER);
            stackPane.setPrefSize(ScrollPane.USE_COMPUTED_SIZE, ScrollPane.USE_COMPUTED_SIZE);

            stackPane.setOnScroll(e -> {
                e.consume();
                if (e.isControlDown()) {
                    onScroll(e.getTextDeltaY(), new Point2D(e.getX(), e.getY()));
                } else {
                    if (e.getDeltaY() < 0) {
                        pdfController.nextPage();
                    } else {
                        pdfController.previousPage();
                    }
                PDFDocumentView.this.displayCurrentPage();
                pageCounter.updateLabel();
                }

            });
            return stackPane;
        }

        private Node centeredNode(Node node) {
            VBox vBox = new VBox(node);
            vBox.setAlignment(Pos.CENTER);
            return vBox;
        }

        private void updateScale() {
            target.setScaleX(scaleValue);
            target.setScaleY(scaleValue);
        }

        private void onScroll(double wheelDelta, Point2D mousePoint) {
            double zoomFactor = Math.exp(wheelDelta * zoomIntensity);

            Bounds innerBounds = zoomNode.getLayoutBounds();
            Bounds viewportBounds = getViewportBounds();

            // calculate pixel offsets from [0, 1] range
            double valX = this.getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
            double valY = this.getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());

            scaleValue = scaleValue * zoomFactor;
            updateScale();
            this.layout(); // refresh ScrollPane scroll positions & target bounds

            // convert target coordinates to zoomTarget coordinates
            Point2D posInZoomTarget = target.parentToLocal(zoomNode.parentToLocal(mousePoint));

            // calculate adjustment of scroll position (pixels)
            Point2D adjustment = target.getLocalToParentTransform().deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

            // convert back to [0, 1] range
            // (too large/small values are automatically corrected by ScrollPane)
            Bounds updatedInnerBounds = zoomNode.getBoundsInLocal();
            this.setHvalue((valX + adjustment.getX()) / (updatedInnerBounds.getWidth() - viewportBounds.getWidth()));
            this.setVvalue((valY + adjustment.getY()) / (updatedInnerBounds.getHeight() - viewportBounds.getHeight()));
        }
    }

    public PDFDocumentView(PDFController pdfController, PageCounter pageCounter) {
        this.pdfController = pdfController;
        this.pageCounter = pageCounter;

        // Create image view
        pdfView = new ImageView();
        pdfView.setPreserveRatio(true);

        // Create layout structure
        centrePane = new StackPane();
        centrePane.setAlignment(Pos.CENTER);
        centrePane.getChildren().add(pdfView);

        // Create redaction layer
        redactionLayer = new Pane();
        centrePane.getChildren().add(redactionLayer);

        // Mouse handlers for redaction layer
        redactionLayer.setOnMousePressed(this::handleMousePressed);
        redactionLayer.setOnMouseDragged(this::handleMouseDragged);
        redactionLayer.setOnMouseReleased(this::handleMouseReleased);

        // Add layout to zoomable scroll pane
        scrollPane = new ZoomableScrollPane(centrePane);

        // Ensure PDFDocumentView (Pane) resizes with parent container
        this.widthProperty().addListener((obs, oldVal, newVal)
                -> scrollPane.setPrefWidth(newVal.doubleValue()));
        this.heightProperty().addListener((obs, oldVal, newVal)
                -> scrollPane.setPrefHeight(newVal.doubleValue()));

        getChildren().add(scrollPane);
    }

    private void renderPage() {
        if (renderer == null) return;

        try {
            PDPage page = pdfController.getDocument().getPage(pdfController.getCurrentPage());
            PDRectangle cropBox = page.getCropBox();

            // Calculate dimensions
            double width = cropBox.getWidth();
            double height = cropBox.getHeight();

            // Render PDF page to BufferedImage
            BufferedImage pdfImage = renderer.renderImageWithDPI(pdfController.getCurrentPage(), 144);

            // Convert to JavaFX Image
            WritableImage fxImage = SwingFXUtils.toFXImage(pdfImage, null);

            // Update ImageView
            pdfView.setImage(fxImage);
            pdfView.setFitHeight(scrollPane.getViewportBounds().getHeight());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setupRenderer() {
        renderer = pdfController.getRenderer();
    }

    public void displayCurrentPage() {
        renderPage();
    }

    public void setRedactionModeActive(boolean active) {
        this.redactionModeActive = active;
    }

    public void clearAllRedactions() {
        redactionLayer.getChildren().clear();
    }

    public void clearLastRedaction() {
        if (!redactionLayer.getChildren().isEmpty()) {
            redactionLayer.getChildren().removeLast();
        }
    }

    private void handleMousePressed(MouseEvent event) {
        System.out.println("Mouse pressed, redactionModeActive: " + redactionModeActive);
        if (!redactionModeActive) return;

        AnchorX = event.getX();
        AnchorY = event.getY();

        // Create new redaction rectangle at click position
        System.out.println("Creating rectangle at: " + event.getX() + ", " + event.getY());
        currentRedaction = new RedactionTool(
                event.getX(),
                event.getY(),
                pdfController.getCurrentPage()
        );
        // Add to redaction layer
        redactionLayer.getChildren().add(currentRedaction);
    }

    private void handleMouseDragged(MouseEvent event) {
        if (!redactionModeActive || currentRedaction == null) return;

        double recX = Math.min(AnchorX, event.getX());
        double recY = Math.min(AnchorY, event.getY());

        double width = Math.abs(event.getX() - AnchorX);
        double height = Math.abs(event.getY() - AnchorY);

        currentRedaction.setX(recX);
        currentRedaction.setY(recY);
        currentRedaction.setWidth(width);
        currentRedaction.setHeight(height);
    }

    private void handleMouseReleased(MouseEvent event) {
        if (!redactionModeActive || currentRedaction == null) return;

        // If the rectangle is too small, remove it
        if (currentRedaction.getWidth() < 5 || currentRedaction.getHeight() < 5) {
            redactionLayer.getChildren().remove(currentRedaction);
        }
        currentRedaction = null;
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
    }
}