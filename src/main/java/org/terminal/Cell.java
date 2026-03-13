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
        this.styles = Set.copyOf(styles);
        this.isCellEmpty = true;
    }

    // Deep copy
    public Cell(Cell other) {
        if(other.isEmpty()) {
            this.isCellEmpty = true;
        }
        else {
            this.character = other.getCharacter();
            this.isCellEmpty = false;
        }
        this.fgColor = other.fgColor;
        this.bgColor = other.bgColor;
        this.styles = Set.copyOf(other.getStyles());
    }

    public void setCharacter(char character) {
        this.character = character;
        this.isCellEmpty = false;
    }

    public void setFgColor(Color fgColor) {
        this.fgColor = fgColor;
    }

    public void setBgColor(Color bgColor) {
        this.bgColor= bgColor;
    }

    public void setStyles(Set<Style> styles) {
        this.styles = Set.copyOf(styles);
    }

    public char getCharacter() {
        if(this.isCellEmpty) return '\0';
        return this.character;
    }
    
    public Color getFgColor() {
        return this.fgColor;
    }
    
    public Color getBgColor() {
        return this.bgColor;
    }

    public Set<Style> getStyles() {
        return Set.copyOf(this.styles);
    }

    public Boolean isEmpty() {
        return this.isCellEmpty;
    }
}

