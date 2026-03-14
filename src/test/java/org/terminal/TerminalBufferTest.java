package org.terminal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.w3c.dom.Attr;
import org.jspecify.annotations.NonNull;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TerminalBufferTest {

    @Test
    @DisplayName("Cursor should lock at (0,0) when moving into negative coordinates")
    void cursorShouldNotGoBelowZero() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 100);

        buffer.moveCursor(-10, -10);

        assertAll("Top-left boundaries",
                () -> assertEquals(0, buffer.getCursorX()),
                () -> assertEquals(0, buffer.getCursorY())
        );
    }

    @Test
    @DisplayName("Cursor should lock at (width-1, height-1) when moving beyond bounds")
    void cursorShouldNotGoBeyondMaxBounds() {
        TerminalBuffer buffer = new TerminalBuffer(10, 10, 100);

        buffer.moveCursor(50, 50);

        assertAll("Bottom-right boundaries",
                () -> assertEquals(9, buffer.getCursorX()),
                () -> assertEquals(9, buffer.getCursorY())
        );
    }

    @Test
    @DisplayName("moveCursor should handle relative movement correctly")
    void cursorRelativeMovement() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 100);

        buffer.moveCursor(5, 5); // To (5,5)
        buffer.moveCursor(-2, 3); // 5-2, 5+3 -> (3,8)

        assertEquals(3, buffer.getCursorX());
        assertEquals(8, buffer.getCursorY());
    }

    @Test
    @DisplayName("getCursorPosition should return a valid Position record")
    void getPositionRecordTest() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 100);
        buffer.moveCursor(10, 12);

        Position pos = buffer.getCursorPosition();

        assertEquals(10, pos.column());
        assertEquals(12, pos.row());
    }

    @Test
    @DisplayName("moving the cursor up, down, left and right once, should bring it to the starting position")
    void movingCursorOnceInEachDirectionTest() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 100);
        buffer.setCursorPosition(1, 1);

        buffer.moveCursorUp(1);
        buffer.moveCursorRight(1);
        buffer.moveCursorDown(1);
        buffer.moveCursorLeft(1);

        Position pos = buffer.getCursorPosition();

        assertEquals(1, pos.column());
        assertEquals(1, pos.row());
    }

    @Test
    @DisplayName("moving the cursor up, down, left and right once, should bring it to a different position than the starting one because it cannot be outside of the bounds")
    void movingCursorOnceInEachDirectionFromZeroZeroTest() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 100);
        buffer.setCursorPosition(0, 0);

        buffer.moveCursorUp(1);
        buffer.moveCursorRight(1);
        buffer.moveCursorDown(1);
        buffer.moveCursorLeft(1);

        Position pos = buffer.getCursorPosition();

        assertEquals(0, pos.column());
        assertEquals(1, pos.row());
    }

    @Test
    @DisplayName("basic writing test")
    void writingTest() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 2);

        buffer.write("Hello");
        buffer.setCursorPosition(0, 0);
        buffer.write("Hi");

        String testString = "Hillo\n     \n     \n";

        assertEquals(testString, buffer.getScreenText());
    }

    @Test
    @DisplayName("basic writing test with styles added")
    void writingStylesTest() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 2);

        buffer.setFgColor(Color.BLACK);
        buffer.setBgColor(Color.WHITE);
        buffer.setStyles(Style.ITALIC, Style.UNDERLINE);
        buffer.write("Hello");
        Attributes attr = new Attributes(Color.BLACK, Color.WHITE, Set.of(Style.ITALIC, Style.UNDERLINE), false);

        assertEquals(attr, buffer.getCharacterAttributes(0, 0).get());
    }

    @Test
    @DisplayName("basic inserting test with styles added")
    void insertingStylesTest() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 2);

        buffer.setFgColor(Color.BLACK);
        buffer.setBgColor(Color.WHITE);
        buffer.setStyles(Style.ITALIC, Style.UNDERLINE);
        buffer.insert("Hjdoiasjidja");
        Attributes attr = new Attributes(Color.BLACK, Color.WHITE, Set.of(Style.ITALIC, Style.UNDERLINE), false);

        assertEquals(attr, buffer.getCharacterAttributes(0, 0).get());
    }

    @Test
    @DisplayName("basic writing scrollback test")
    void writingScrollbackTest() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 2);

        buffer.write("Hello");
        buffer.setCursorPosition(0, 0);
        buffer.write("Hi");
        buffer.write("llo1234512345");

        String testString = "Hillo\n12345\n12345\n     \n";

        assertEquals(testString, buffer.getAllText());
    }

    @Test
    @DisplayName("scrollback deletion test")
    void writingScrollbackDeletionTest() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 2);

        buffer.write("Hello");
        buffer.setCursorPosition(0, 0);
        buffer.write("Hi");
        buffer.write("llo123451234554321Hello");

        String testString = "12345\n12345\n54321\nHello\n     \n";

        assertEquals(testString, buffer.getAllText());
    }

    @Test
    @DisplayName("basic insertion test")
    void insertionTest() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 5);

        buffer.write("Hiii2Hiii3");
        buffer.setCursorPosition(0, 0);
        buffer.insert("Hiii1");
        String testString = "Hiii1\nHiii2\nHiii3\n";

        assertEquals(testString, buffer.getAllText());
    }

    @Test
    @DisplayName("advanced insertion test")
    void advancedInsertionTest() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 5);

        buffer.write("Hiii2Hiii3");
        buffer.setCursorPosition(0, 0);
        buffer.insert("Hiii1");
        buffer.setCursorPosition(0, 0);
        buffer.insert("Hiii0");
        String testString = "Hiii0\nHiii9\nHiii8\nHiii7\nHiii1\nHiii2\nHiii3\n";
        buffer.setCursorPosition(0, 0);
        buffer.insert("Hiii9Hiii8Hiii7");

        assertEquals(testString, buffer.getAllText());
    }

    @Test
    @DisplayName("get line from screen")
    void lineFromScreenTest() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 5);

        buffer.write("Hello");
        String testString = "Hello\n";

        assertEquals(testString, buffer.getLineAsString(0));
    }

    @Test
    @DisplayName("get line from scrollback")
    void lineFromScrollbackTest() {
        TerminalBuffer buffer = new TerminalBuffer(6, 3, 5);

        buffer.write("Hello1");
        buffer.write("Hello2");
        buffer.write("Hello3");
        String testString = "Hello1\n";

        assertEquals(testString, buffer.getLineAsString(0));
    }

    @Test
    @DisplayName("line filling test")
    void fillingLineTest() {
        TerminalBuffer buffer = new TerminalBuffer(6, 3, 5);

        buffer.setFgColor(Color.BLUE);
        buffer.write("Hello1");
        buffer.fillLine('H');
        buffer.moveCursorDown(1);
        buffer.fillLine();

        String testString = "Hello1\nHHHHHH\n      \n";
        assertEquals(testString, buffer.getAllText());

        Attributes attr = new Attributes(Color.BLUE, Color.DEFAULT, Set.of(), false);
        assertEquals(attr, buffer.getCharacterAttributes(0, 0).get());
        assertEquals(attr, buffer.getCharacterAttributes(0, 1).get());
        Attributes attr2 = new Attributes(Color.BLUE, Color.DEFAULT, Set.of(), true);
        assertEquals(attr2, buffer.getCharacterAttributes(0, 2).get());
    }

    @Test
    @DisplayName("screen clearing")
    void clearTest() {
        TerminalBuffer buffer = new TerminalBuffer(6, 3, 5);

        buffer.setFgColor(Color.BLUE);
        buffer.write("Hello");
        buffer.clearScreen();
        Attributes attr = new Attributes(Color.BLUE, Color.DEFAULT, Set.of(), true);
        assertEquals(attr, buffer.getCharacterAttributes(0, 0).get());
        assertEquals('\0', buffer.getCharacter(0, 0));

        String text = "      \n      \n      \n";
        assertEquals(text, buffer.getScreenText());

        buffer.write("G");
        buffer.moveCursorDown(1);
        buffer.moveCursorLeft(1);
        buffer.write("H");
        buffer.clearScreenScrollback();

        String text2 = "G     \nH     \n      \n      \n      \n";
        assertEquals(text2, buffer.getAllText());
    }


}