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
    private LinkedList<Line> scrollback;
    private LinkedList<Line> overflow;

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
        this.scrollback = new LinkedList<>();
        this.overflow = new LinkedList<>();
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
        this.screen.get(this.cursorY).setCellAt(this.cursorX, character, this.fgColor, this.bgColor, this.styles, false);
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
        }
    }

    private void scrollbackOnce() {
        Line firstLine = new Line(this.screen.getFirst());

        this.screen.removeFirst();
        if(this.overflow.isEmpty()) {
            this.screen.add(new Line(this.screenWidth));
        }
        else {
            this.screen.add(new Line(this.overflow.getFirst()));
            this.overflow.removeFirst();
        }

        if(this.scrollback.size() == this.maxScrollbackLines) {
            this.scrollback.removeFirst();
        }
        this.scrollback.addLast(firstLine);
        this.moveCursorUp(1);
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

    private int translateToNumber(int x, int y) {
        return x + y*screenWidth;
    }

    private int[] translateToCoordinates(int num) {
        int[] coords = new int[2];
        int x = num%this.screenWidth;
        int y = num/this.screenWidth;
        coords[0] = x;
        coords[1] = y;
        return coords;
    }

    private void moveChar(int from, int to) {
        if(to < this.screenWidth*this.screenHeight) {
            int[] coordsFrom = translateToCoordinates(from);
            int[] coordsTo = translateToCoordinates(to);
            Cell cellFrom = this.screen.get(coordsFrom[1]).getCellAt(coordsFrom[0]);

            this.screen.get(coordsTo[1]).setCellAt(coordsTo[0], cellFrom.getCharacter(), cellFrom.getFgColor(), cellFrom.getBgColor(), cellFrom.getStyles(), cellFrom.isEmpty());
        }
        else if(to >= this.screenWidth*this.screenHeight && from < this.screenWidth*this.screenHeight) {
            to -= this.screenWidth*this.screenHeight;
            int[] coordsFrom = translateToCoordinates(from);
            int[] coordsTo = translateToCoordinates(to);
            Cell cellFrom = this.screen.get(coordsFrom[1]).getCellAt(coordsFrom[0]);

            this.overflow.get(coordsTo[1]).setCellAt(coordsTo[0], cellFrom.getCharacter(), cellFrom.getFgColor(), cellFrom.getBgColor(), cellFrom.getStyles(), cellFrom.isEmpty());
        }
        else {
            to -= this.screenWidth*this.screenHeight;
            from -= this.screenWidth*this.screenHeight;
            int[] coordsFrom = translateToCoordinates(from);
            int[] coordsTo = translateToCoordinates(to);
            Cell cellFrom = this.overflow.get(coordsFrom[1]).getCellAt(coordsFrom[0]);

            this.overflow.get(coordsTo[1]).setCellAt(coordsTo[0], cellFrom.getCharacter(), cellFrom.getFgColor(), cellFrom.getBgColor(), cellFrom.getStyles(), cellFrom.isEmpty());
        }
    }

    private void moveBlock(int spaces, int from, int to) {
        for(int i = spaces-1; i >= 0; i--) {
            moveChar(from + i, to + i);
        }
    }

    public void insert(String text) {
        int cursorPosition = translateToNumber(this.cursorX, this.cursorY);
        Boolean needToMove = false;
        int textLength = text.length();
        int spaceNeeded = textLength;
        int spaceFound = 0;
        int moveFrom = 0;
        int moveTo = 0;
        int nCharacters = 0;
        int additionalSpaceNeeded = 0;

        // Finding out if some of the text has to be moved
        for(int i = cursorPosition; i < cursorPosition+textLength; i++) {
            int[] currPos = translateToCoordinates(i);
            if(i == screenHeight*screenWidth) break;
            if(!this.screen.get(currPos[1]).getCellAt(currPos[0]).isEmpty()) {
                moveFrom = i;
                needToMove = true;
                break;
            }
            spaceNeeded--;
        }

        if(needToMove) {
            for(int i = moveFrom; spaceNeeded > spaceFound; i++) {
                int[] currPos = translateToCoordinates(i);
                if(i < this.screenWidth*this.screenHeight) {
                    if(this.screen.get(currPos[1]).getCellAt(currPos[0]).isEmpty()) {
                        spaceFound++;
                        nCharacters++;
                    }
                    else {
                        spaceFound = 0;
                        nCharacters++;
                    }
                }
                else {
                    spaceFound++;
                    nCharacters++;
                }

            }
            nCharacters = nCharacters - spaceFound;
            moveTo = moveFrom + spaceNeeded;

            int newLinesNeeded = (moveTo+nCharacters-this.screenWidth*this.screenHeight)/this.screenWidth;

            if(newLinesNeeded > 0) {
                insertAdditionalLines(newLinesNeeded);
            }

            moveBlock(nCharacters, moveFrom, moveTo);
        }

        write(text);

        while(!this.overflow.isEmpty()) {
            scrollbackOnce();
        }
    }

    private void insertAdditionalLines(int n) {
        for(int i = 0; i < n; i++) {
            this.overflow.add(new Line(this.screenWidth));
        }
    }

    public void fillLineAt(char character, int y) {
        this.screen.get(cursorY).fill(character, fgColor, bgColor, styles);
    }

    public void fillLineAt(int y) {
        this.screen.get(cursorY).fillEmpty(fgColor, bgColor, styles);
    }

    public void fillLine(char character) {
        this.fillLineAt(character, this.cursorY);
    }

    public void fillLine() {
        this.fillLineAt(this.cursorY);
    }

    public void insertLine() {
        this.scrollbackOnce();
    }

    public void clearScreen() {
        for(int i = 0; i < this.screenHeight; i++) {
            fillLineAt(i);
        }
    }

    public void clearScreenScrollback() {
        int linesToScroll = 0;
        for(int i = 0; i < this.screenHeight; i++) {
            for(int j = 0; j < this.screenWidth; j++) {
                if (this.screen.get(i).getCellAt(i).isEmpty()) linesToScroll = i;
            }
        }
        for(int i = 0; i < linesToScroll; i++) {
            scrollbackOnce();
        }
    }

    public String getLineAsString(int lineNum) {
        if(!this.scrollback.isEmpty()) {
            if(lineNum >= 0 && lineNum <= this.scrollback.size()) {
                return this.scrollback.get(lineNum).getAsString();
            }
            else if(lineNum > this.scrollback.size() && lineNum < this.scrollback.size()+this.screen.size()) {
                return this.screen.get(lineNum - this.scrollback.size()).getAsString();
            }
            else {
                return "";
            }
        }
        else {
            if(lineNum >= 0 && lineNum < this.screen.size()) {
                return this.screen.get(lineNum).getAsString();
            }
            else {
                return "";
            }
        }
    }

    public char getCharacter(int x, int y) {
        if(!this.scrollback.isEmpty()) {
            if (x >= 0 && x < this.screenWidth) {
                if (y >= 0 && y <= this.scrollback.size()) {
                    return this.screen.get(y).getCellAt(x).getCharacter();
                } else if (y > this.scrollback.size() && y < this.scrollback.size() + this.screen.size()) {
                    return this.screen.get(y).getCellAt(x).getCharacter();
                } else return '\0';
            } else return '\0';
        }
        else {
            if (x >= 0 && x < this.screenWidth) {
                if (y >= 0 && y <= this.screen.size()) {
                    return this.screen.get(y).getCellAt(x).getCharacter();
                } else return '\0';
            } else return '\0';
        }
    }

    public Optional<Attributes> getCharacterAttributes(int x, int y) {
        if(!this.scrollback.isEmpty()) {
            if (x >= 0 && x < this.screenWidth) {
                if (y >= 0 && y <= this.scrollback.size()) {
                    return Optional.of(this.screen.get(y).getCellAt(x).getAttributes());
                } else if (y > this.scrollback.size() && y < this.scrollback.size() + this.screen.size()) {
                    return Optional.of(this.screen.get(y).getCellAt(x).getAttributes());
                } else return Optional.empty();
            } else return Optional.empty();
        }
        else {
            if (x >= 0 && x < this.screenWidth) {
                if (y >= 0 && y <= this.screen.size()) {
                    return Optional.of(this.screen.get(y).getCellAt(x).getAttributes());
                } else return Optional.empty();
            } else return Optional.empty();
        }
    }
}
