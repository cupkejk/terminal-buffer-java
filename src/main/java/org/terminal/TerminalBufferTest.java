package org.terminal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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
}