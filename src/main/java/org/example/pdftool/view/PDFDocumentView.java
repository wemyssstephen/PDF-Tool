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
    private final JPanel panel;
    private PDFRenderer renderer;
    private int currentPage = 0;
    private double scale = 1.0f;

    public PDFDocumentView(PDFController pdfController) {
        this.pdfController = pdfController;
        panel = createPanel();
        swingNode = createSwingNode();
        setupLayout();
        setupEventHandlers();
    }

    private JPanel createPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                renderPage(g);
            }
        };
        panel.setBackground(Color.GRAY);
        return panel;
    }

    private void renderPage(Graphics g) {
        if (renderer != null) {
            try {
                PDPage page = pdfController.getDocument().getPage(currentPage);
                PDRectangle cropBox = page.getCropBox();

                scale = calculateScale(cropBox);
                Point offset = calculateOffset(cropBox);

                Graphics2D g2d = (Graphics2D) g;
                g2d.translate(offset.x, offset.y);
                renderer.renderPageToGraphics(currentPage, g2d, (float) scale);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private double calculateScale(PDRectangle cropBox) {
        // Calculate scale of panel
        double scaleX = getWidth() / cropBox.getWidth();
        double scaleY = getHeight() / cropBox.getHeight();
        return Math.min(scaleX, scaleY);
    }

    private Point calculateOffset(PDRectangle cropBox) {
        // Calculate centre offset
        int xOffset = (int) ((getWidth() - (cropBox.getWidth()) * scale) / 2);
        int yOffset = (int) ((getHeight() - (cropBox.getHeight()) * scale) / 2);
        return new Point(xOffset, yOffset);
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
        if (e.getWheelRotation() < 0) {navigateToPage(currentPage - 1);}
        else {navigateToPage(currentPage + 1);}
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
}
