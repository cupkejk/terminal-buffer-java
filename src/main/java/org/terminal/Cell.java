package org.terminal;

import java.util.Set;

public class Cell {
    char character;
    private Color fgColor;
    private Color bgColor;
    private Set<Style> styles;
    private Boolean isCellEmpty;

    public Cell() {
        this.fgColor = Color.DEFAULT;
        this.bgColor = Color.DEFAULT;
        this.styles = Set.of();
        this.isCellEmpty = true;
    }

    public Cell(Color fgColor, Color bgColor, Set<Style> styles) {
        this.fgColor = fgColor;
        this.bgColor = bgColor;
        this.styles = styles;
        this.isCellEmpty = true;
    }
}

