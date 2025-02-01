package org.example.pdftool.view;

import javafx.embed.swing.SwingNode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.pdftool.controller.PDFController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;

public class PDFDocumentView extends Pane {
    private final SwingNode swingNode;
    private final PDFController pdfController;
    private final JScrollPane scrollPane;
    private PDFRenderer renderer;
    private JViewport viewport;
    private int currentPage = 0;
    private double scale = 1.0f;
    private double zoomLevel = 1;
    private boolean isFirstLoad = true;

    private static final double MIN_ZOOM = 0.1;
    private static final double MAX_ZOOM = 5.0;
    private static final double ZOOM_INCREMENT = 0.1;

    public PDFDocumentView(PDFController pdfController) {
        this.pdfController = pdfController;
        scrollPane = createPanel();
        swingNode = createSwingNode();
        setupLayout();
        setupEventHandlers();
    }

    private JScrollPane createPanel() {
        JPanel drawingSurface = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintPDFContent((Graphics2D) g, this);
            }
        };

        JScrollPane scrollPane = new JScrollPane(drawingSurface);
        viewport = scrollPane.getViewport();
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        return scrollPane;
    }

    private void paintPDFContent(Graphics2D g, JPanel contentPanel) {
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, contentPanel.getWidth(), contentPanel.getHeight());

        if (renderer != null) {
            PDPage page = pdfController.getDocument().getPage(currentPage);
            PDRectangle cropBox = page.getCropBox();

            Dimension contentSize = calculateDimension(cropBox);
            contentPanel.setPreferredSize(contentSize);

            // Calculate centre offset
            int xOffset = Math.max(0, (contentPanel.getWidth() - contentSize.width) / 2);
            int yOffset = Math.max(0, (contentPanel.getHeight() - contentSize.height) / 2);

            Graphics2D pdfGraphics = (Graphics2D) g.create();
            try {
                pdfGraphics.translate(xOffset, yOffset);
                renderer.renderPageToGraphics(currentPage, pdfGraphics, (float) scale);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                pdfGraphics.dispose();
            }
        }
    }

    private double calculateScale(PDRectangle cropBox) {
        // Calculate scale of panel
        double scaleX = getWidth() / cropBox.getWidth();
        double scaleY = getHeight() / cropBox.getHeight();
        double baseScale = Math.min(scaleX, scaleY);
        return baseScale * zoomLevel;
    }

    private Dimension calculateDimension(PDRectangle cropBox) {
        scale = calculateScale(cropBox);
        return new Dimension(
                (int) (cropBox.getWidth() * scale),
                (int) (cropBox.getHeight() * scale)
        );
    }

    private SwingNode createSwingNode() {
        SwingNode swingNode = new SwingNode();
        swingNode.setContent(scrollPane);
        getChildren().add(swingNode);
        return swingNode;
    }

    private void setupLayout() {
        swingNode.setLayoutX(0);
        swingNode.setLayoutY(0);
        setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
    }

    private void setupEventHandlers() {
        scrollPane.addMouseWheelListener(this::handleMouseWheel);
    }

    private void handleMouseWheel(MouseWheelEvent e) {
        if (e.isControlDown()) {
            handleZoom(e);
        } else {
            handlePageNavigation(e);
        }
    }

    private void handleZoom(MouseWheelEvent e) {
        System.out.println("\n=== Starting Zoom ===");
        Point mousePosition = e.getPoint();
        System.out.println("Mouse position: " + mousePosition);

        System.out.println("Current viewport size: " + viewport.getSize());
        System.out.println("Current view size: " + viewport.getView().getSize());
        System.out.println("Current preferred size: " + viewport.getView().getPreferredSize());
        System.out.println("Current view position: " + viewport.getViewPosition());

        double oldZoomLevel = zoomLevel;
        zoomLevel = calculateZoomLevel(e);
        System.out.println("Zoom changing from " + oldZoomLevel + " to " + zoomLevel);

        Point viewPosition = viewport.getViewPosition();
        double scaleFactor = zoomLevel / oldZoomLevel;
        Point newPosition = new Point(
                (int) ((viewPosition.x + mousePosition.x) * scaleFactor - mousePosition.x),
                (int) ((viewPosition.y + mousePosition.y) * scaleFactor - mousePosition.y)
        );
        System.out.println("Calculated new position: " + newPosition);

        viewport.setViewPosition(newPosition);
        viewport.getView().repaint();
    }

    private double calculateZoomLevel(MouseWheelEvent e) {
        double delta = e.getWheelRotation() < 0 ? ZOOM_INCREMENT : -ZOOM_INCREMENT;
        return Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoomLevel + delta));
    }

    private void handlePageNavigation(MouseWheelEvent e) {
        navigateToPage(currentPage + e.getWheelRotation());
    }

    private void navigateToPage(int newPage) {
        if (newPage >= 0 && newPage < pdfController.getPageCount()) {
            currentPage = newPage;
            scrollPane.repaint();
        }
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        swingNode.resize(getWidth(), getHeight());
        scrollPane.setSize((int) getWidth(), (int) getHeight());
    }

    // Create renderer
    public void setupRenderer() {
        renderer = pdfController.getRenderer();
    }

    public void displayCurrentPage() {
        scrollPane.repaint();
    }

    public void centreDocument() {
        isFirstLoad = true;
        scrollPane.repaint();
    }
}
