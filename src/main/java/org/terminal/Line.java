package org.terminal;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;

public class Line {
    private List<Cell> cells;

    public Line(int width, Color fgColor, Color bgColor, Set<Style> styles) {
        cells = new ArrayList<>();
        for(int i = 0; i < width; i++) {
            cells.add(new Cell(fgColor, bgColor, styles));
        }
    }

    public Line(int width) {
        cells = new ArrayList<>();
        for(int i = 0; i < width; i++) {
            cells.add(new Cell());
        }
    }

    // Deep copy
    public Line(Line other) {
        this.cells = new ArrayList<>();
        for(int i = 0; i < other.getCells().size(); i++) {
            this.cells.add(new Cell(other.getCellAt(i)));
        }
    }

    public void setCellAt(int index, char character, Color fgColor, Color bgColor, Set<Style> styles, Boolean isCellEmpty) {
        Cell current = this.cells.get(index);
        current.setCharacter(character);
        current.setFgColor(fgColor);
        current.setBgColor(bgColor);
        current.setStyles(styles);
        current.setIsCellEmpty(isCellEmpty);
    }

    public Cell getCellAt(int index) {
        return this.cells.get(index);
    }

    public List<Cell> getCells() {
        return cells;
    }

    public String getAsString() {
        StringBuilder text = new StringBuilder();
        for(Cell cell : this.cells) {
            char character = cell.getCharacter();
            if(character == '\0') {
                text.append(' ');
            }
            else {
                text.append(character);
            }
        }
        return text.toString() + '\n';
    }
}
