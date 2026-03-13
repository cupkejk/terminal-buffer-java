package org.terminal;

import java.lang.*;
import java.util.*;

public class TerminalBuffer {
    private final int screenWidth;
    private final int screenHeight;
    private final int maxScrollbackLines;

    private int cursorX;
    private int cursorY;

    private Color fgColor;
    private Color bgColor;
    private Set<Style> styles;

    private List<Line> screen;
    private Deque<Line> scrollback;

    public TerminalBuffer(int width, int height, int scrollbackLines) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.maxScrollbackLines = scrollbackLines;

        this.cursorX = 0;
        this.cursorY = 0;

        this.fgColor = Color.DEFAULT;
        this.bgColor = Color.DEFAULT;
        this.styles = Set.of();

        this.screen = new ArrayList<>();
        for(int i = 0; i < height; i++) {
            this.screen.add(new Line(width));
        }
        this.scrollback = new ArrayDeque<>();
    }

    public void moveCursor(int x, int y) {
        setCursorPosition(this.cursorX + x, this.cursorY + y);
    }

    public void moveCursorUp(int n) {
        if (n < 0) return;
        moveCursor(0, -n); // Reuse your existing relative movement logic
    }

    public void moveCursorDown(int n) {
        if (n < 0) return;
        moveCursor(0, n);
    }

    public void moveCursorLeft(int n) {
        if (n < 0) return;
        moveCursor(-n, 0);
    }

    public void moveCursorRight(int n) {
        if (n < 0) return;
        moveCursor(n, 0);
    }

    public Position getCursorPosition() {
        return new Position(this.cursorX, this.cursorY);
    }

    public void setCursorPosition(int x, int y) {
        this.cursorX = Math.clamp(x, 0, screenWidth - 1);
        this.cursorY = Math.clamp(y, 0, screenHeight - 1);
    }

    public int getCursorX() {
        return cursorX;
    }

    public int getCursorY() {
        return cursorY;
    }
}
