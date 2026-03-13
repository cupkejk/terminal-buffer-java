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
}
