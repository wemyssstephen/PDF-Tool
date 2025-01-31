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
    private final JScrollPane panel;
    private PDFRenderer renderer;
    private int currentPage = 0;
    private double scale = 1.0f;
    private double zoomLevel = 1;
    private boolean isFirstLoad = true;

    private static final double MIN_ZOOM = 0.1;
    private static final double MAX_ZOOM = 5.0;
    private static final double ZOOM_INCREMENT = 0.1;

    public PDFDocumentView(PDFController pdfController) {
        this.pdfController = pdfController;
        panel = createPanel();
        swingNode = createSwingNode();
        setupLayout();
        setupEventHandlers();
    }

    private JScrollPane createPanel() {
        JPanel contentPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintPDFContent((Graphics2D) g, this);
            }
        };

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
    }

    private void paintPDFContent(Graphics2D g, JPanel contentPanel) {
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, contentPanel.getWidth(), contentPanel.getHeight());

        if (renderer != null) {
            // Get central position
            PDPage page = pdfController.getDocument().getPage(currentPage);
            PDRectangle cropBox = page.getCropBox();
            Dimension pdfSize = calculateDimension(cropBox);

            // Equal space on all sides
            int extraSpace = Math.max(pdfSize.width, pdfSize.height);
            contentPanel.setPreferredSize(new Dimension(pdfSize.width + extraSpace, pdfSize.height + extraSpace));

            // Centre PDF in the space only on first load
            int xCenter, yCenter;
            if (isFirstLoad) {
                xCenter = (contentPanel.getWidth() - pdfSize.width) / 2;
                yCenter = (contentPanel.getHeight() - pdfSize.height) / 2;
                isFirstLoad = false;
            } else {
                xCenter = extraSpace / 2;
                yCenter = extraSpace / 2;
            }

            g.translate(xCenter, yCenter);
            try {
                renderer.renderPageToGraphics(currentPage, g, (float) scale);
            } catch (IOException e) {
                e.printStackTrace();
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
        swingNode.setContent(panel);
        getChildren().add(swingNode);
        return swingNode;
    }

    private void setupLayout() {
        swingNode.setLayoutX(0);
        swingNode.setLayoutY(0);
        setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
    }

    private void setupEventHandlers() {
        panel.addMouseWheelListener(this::handleMouseWheel);
    }

    private void handleMouseWheel(MouseWheelEvent e) {
        if (e.isControlDown()) {
            handleZoom(e);
        } else {
            handlePageNavigation(e);
        }
    }

    private void handleZoom(MouseWheelEvent e) {
        Point mousePosition = e.getPoint();
        double oldZoomLevel = zoomLevel;

        // Debug first zoom
        if (isFirstLoad) {
            System.out.println("First zoom:");
            System.out.println("Mouse pos: " + mousePosition);
            System.out.println("View pos: " + ((JScrollPane) e.getComponent()).getViewport().getViewPosition());
            System.out.println("Old zoom: " + oldZoomLevel);
        }

        zoomLevel = calculateZoomLevel(e);

        JViewport viewport = ((JScrollPane) e.getComponent()).getViewport();
        JPanel contentPanel = (JPanel) viewport.getView();
        Point viewPosition = viewport.getViewPosition();
        Point panelPosition = contentPanel.getLocation();

        System.out.println("contentPanel: " + contentPanel);
        System.out.println("view position: " + viewPosition);

        double documentX = (mousePosition.x + viewPosition.x - panelPosition.x) / oldZoomLevel;
        double documentY = (mousePosition.y + viewPosition.y - panelPosition.y) / oldZoomLevel;

        Point newPosition = new Point(
            (int) (documentX * zoomLevel - mousePosition.x),
            (int) (documentY * zoomLevel - mousePosition.y)
        );

        viewport.setViewPosition(newPosition);
        contentPanel.revalidate();
        contentPanel.repaint();
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
            panel.repaint();
        }
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        swingNode.resize(getWidth(), getHeight());
        panel.setSize((int) getWidth(), (int) getHeight());
    }

    // Create renderer
    public void setupRenderer() {
        renderer = pdfController.getRenderer();
    }

    public void displayCurrentPage() {
        panel.repaint();
    }

    public void centreDocument() {
        isFirstLoad = true;
        panel.repaint();
    }
}
