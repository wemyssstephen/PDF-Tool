package org.example.pdftool.view;

import javafx.scene.image.ImageView;

public class PDFPageView extends ImageView {
    private int pageIndex;
    private boolean isRendered;

    public PDFPageView(int pageIndex) {
        this.pageIndex = pageIndex;
        this.isRendered = false;
        setPreserveRatio(true);
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public boolean isRendered() {
        return isRendered;
    }

    public void setRendered(boolean rendered) {
        isRendered = rendered;
    }
}
