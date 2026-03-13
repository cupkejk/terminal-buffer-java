package org.terminal;

import org.jspecify.annotations.NonNull;

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

    private ArrayList<Line> screen;
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

    public void setFgColor(Color fgColor) {
        this.fgColor = fgColor;
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    public void setStyles(Style first) {
        this.styles = Set.of(first);
    }

    public void setStyles(Style first, Style second) {
        this.styles = Set.of(first, second);
    }

    public void setStyles(Style first, Style second, Style third) {
        this.styles = Set.of(first, second, third);
    }

    public void resetStyles() {
        this.styles = Set.of();
    }

    public void write(String text) {
        for(int i = 0; i < text.length(); i++) {
            write(text.charAt(i));
        }
    }

    private void write(char character) {
        this.screen.get(this.cursorY).setCellAt(this.cursorX, character, this.fgColor, this.bgColor, this.styles);
        advanceCursor();
    }

    private void advanceCursor() {
        this.cursorX++;
        if(this.cursorX == this.screenWidth) {
            this.cursorX = 0;
            this.cursorY++;
        }
        if(this.cursorY == this.screenHeight) {
            this.scrollbackOnce();
            this.cursorY--;
        }
    }

    private void scrollbackOnce() {
        Line firstLine = new Line(this.screen.getFirst());

        this.screen.removeFirst();
        this.screen.add(new Line(this.screenWidth));

        if(this.scrollback.size() == this.maxScrollbackLines) {
            this.scrollback.removeFirst();
        }
        this.scrollback.addLast(firstLine);
    }

    public String getScreenText() {
        StringBuilder text = new StringBuilder();
        for(Line line : this.screen) {
            text.append(line.getAsString());
        }
        return text.toString();
    }

    public String getAllText() {
        StringBuilder text = new StringBuilder();
        for(Line line : this.scrollback) {
            text.append(line.getAsString());
        }
        text.append(this.getScreenText());
        return text.toString();
    }
}
