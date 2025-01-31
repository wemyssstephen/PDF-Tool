package org.example.pdftool.controller;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;


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

        java.util.logging.Logger.getLogger("org.apache.fontbox.cff.Type1CharString").setLevel(java.util.logging.Level.SEVERE);
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
