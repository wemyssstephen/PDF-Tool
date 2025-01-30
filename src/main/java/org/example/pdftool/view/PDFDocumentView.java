package org.example.pdftool.view;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Node;
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
    private static final int PRELOAD_PAGES = 1;

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
            try {
                loadVisiblePages();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        // Load visible pages
        loadVisiblePages();
    }

    private void loadVisiblePages() throws IOException {
        // Measure view window
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        // Measure how far we have scrolled
        double scrollY = scrollPane.getVvalue() * (pagesContainer.getHeight() - viewportHeight);

        for (Node node : pagesContainer.getChildren()) {
            // Case Node to PDFPageView, trust me Java!
            PDFPageView pageView = (PDFPageView) node;

            // Calculate if page is visible
            double pageTop = pageView.getBoundsInParent().getMinY();
            double pageBottom = pageView.getBoundsInParent().getMaxY();

            boolean isVisible = (
                    pageTop <= scrollY + viewportHeight + viewportHeight * PRELOAD_PAGES
                    &&
                    pageBottom >= scrollY - viewportHeight * PRELOAD_PAGES);

            if (isVisible && !pageView.isRendered()) {
                pageView.setImage(renderPage(pageView.getPageIndex()));
                pageView.setRendered(true);
            } else if (!isVisible && pageView.isRendered()) {
                pageView.setImage(null);
                pageView.setRendered(false);
            }
        }
    }
}
