package org.example.pdftool.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class PDFController {
    private PDDocument document;

    public PDFRenderer getRenderer() {
        if (document == null) {throw new IllegalStateException("No document has been loaded");}
        return new PDFRenderer(document);
    }

    public void loadPDFDocument(File file) throws IOException {
        if (document != null) {document.close();}
        document = Loader.loadPDF(file);
    }

    public int getPageCount() {
        return document != null ? document.getNumberOfPages() : 0;
    }

    public void closeDocument() throws IOException {
        if (document != null) {
            document.close();
        }
    }
}
