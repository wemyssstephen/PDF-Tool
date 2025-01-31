package org.example.pdftool.view;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.pdftool.controller.PDFController;

import java.awt.*;
import java.io.IOException;

public class PDFDocumentView extends VBox {
    private final ScrollPane scrollPane;
    private final VBox pagesContainer;
    private final PDFController pdfController;
    private PDFRenderer renderer;
    private int currentPage = 0;

    public PDFDocumentView(PDFController pdfController) {
        this.pdfController = pdfController;

        // Initialise Container for pages
        pagesContainer = new VBox(10);
        pagesContainer.setPadding(new Insets(10));

        scrollPane = new ScrollPane(pagesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinWidth(200);

        // Listener for lazy loading
        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            checkPageChange();
        });

        // Add scroll pane to VBox
        getChildren().add(scrollPane);
    }

    // Create renderer
    public void setupRenderer() {
        RenderingHints hints = new RenderingHints(null);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        renderer = pdfController.getRenderer();
        renderer.setRenderingHints(hints);
    }

    // Render Image
    public WritableImage renderPage(int pageIndex) throws IOException {
        return SwingFXUtils.toFXImage(
                renderer.renderImageWithDPI(pageIndex, 288),
                null
        );
    }

    public void displayPDFPages() throws IOException {
        // Clear existing pages
        pagesContainer.getChildren().clear();

        // Create placeholder for pages, but don't render yet
        for (int pageIndex = 0; pageIndex < pdfController.getPageCount(); pageIndex++) {
            PDFPageView pageView = new PDFPageView(pageIndex);
            pageView.fitWidthProperty().bind(scrollPane.widthProperty());
            pagesContainer.getChildren().add(pageView);
        }

        checkPageChange();
    }

    private void checkPageChange() {
        // Get middle of viewport
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double contentHeight = pagesContainer.getHeight();

        // Calculate current vertical offset in document
        double vmin = scrollPane.getVmin();
        double vmax = scrollPane.getVmax();
        double vvalue = scrollPane.getVvalue();

        double voffset = Math.max(0, contentHeight - viewportHeight) *
                        (vvalue - vmin) / (vmax - vmin);

        // Middle of viewport
        double viewportMiddle = voffset + (viewportHeight / 2);

        System.out.println("Current page: " + currentPage);
        System.out.println("viewportMiddle: " + viewportMiddle);

        if (currentPage < pagesContainer.getChildren().size() - 1) {
            PDFPageView nextPage = (PDFPageView) pagesContainer.getChildren().get(currentPage + 1);
            System.out.println("Next page: " + nextPage);
            System.out.println("Next page top: " + nextPage.getBoundsInParent().getMinY());
            if (nextPage.getBoundsInParent().getMinY() <= viewportMiddle) {
                currentPage++;
                try {loadPagesInRange();}
                catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        if (currentPage > 0) {
            PDFPageView previousPage = (PDFPageView) pagesContainer.getChildren().get(currentPage - 1);
            if (previousPage.getBoundsInParent().getMaxY() > viewportMiddle) {
                currentPage--;
                try {loadPagesInRange();}
                catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
    }

    private void loadPagesInRange() throws IOException {
        int startPage = Math.max(0, currentPage - 3);
        int endPage = Math.min(pdfController.getPageCount() - 1, currentPage + 3);

        // Kill previous pages
        if (startPage - 1 >= 0) {
            PDFPageView pageView = (PDFPageView) pagesContainer.getChildren().get(startPage - 1);
            if (pageView.isRendered()) {
                pageView.setImage(null);
                pageView.setRendered(false);
            }
        }
        if (endPage + 1 <= pagesContainer.getChildren().size()) {
            PDFPageView pageView = (PDFPageView) pagesContainer.getChildren().get(endPage + 1);
            if (pageView.isRendered()) {
                pageView.setImage(null);
                pageView.setRendered(false);
            }
        }

        // Load pages in range
        for (int i = startPage; i <= endPage; i++) {
            PDFPageView pageView = (PDFPageView) pagesContainer.getChildren().get(i);
            if (!pageView.isRendered()) {
                pageView.setImage(renderPage(i));
                pageView.setRendered(true);
            }
        }
    }
}
