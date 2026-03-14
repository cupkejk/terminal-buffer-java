# Java Terminal Buffer

A high-performance, memory-efficient terminal buffer implementation in Java. This project simulates a grid-based text terminal with support for text wrapping, automatic scrolling, and a persistent scrollback history.

## Architecture & Design Decisions

### Cell Management
I chose a "Fixed Grid" approach where each cell in the terminal is an instance of a Cell class. To optimize performance, the system avoids re-instantiating objects during standard write operations. Instead, it performs in-place updates of the character, colors, and styles within existing Cell objects. This stabilizes memory usage and reduces GC pressure during high-frequency updates.

### Coordinate System & Performance
The buffer implements a 2D-to-1D coordinate translation logic. This allows the system to treat the entire screen as a continuous stream of data when necessary. For text insertion, I implemented a block-shift strategy. Instead of shifting characters one-by-one, the system calculates the total required space and shifts the existing data block in a single pass, ensuring O(N) complexity relative to the screen size.

### Scrollback and Overflow
The terminal supports a configurable scrollback limit. When the screen is full or an insertion causes an overflow, the top-most lines are deep-copied and archived into a history list. I implemented an "overflow buffer" logic for insertions, which ensures that text pushed off the screen during a mid-buffer insertion is safely captured and moved into the scrollback rather than being deleted.

## Features
- Standard Write: Overwrites existing content and advances the cursor with automatic line wrapping and scrolling.
- Atomic Insertion: Pushes existing text forward to insert new strings, with waterfall effects across line boundaries.
- Scrollback History: Stores lines pushed off the top of the screen for later retrieval.
- Styling: Support for foreground colors, background colors, and text styles (Bold, Italic, etc.) per cell.
- Line Operations: Bulk fill and clear operations for efficient screen management.

## Prerequisites
- Java 21 or higher (Uses Math.clamp and modern Record features)
- Maven or Gradle (for building and testing)

## Getting Started

### Build
To compile the project, run:
```
mvn clean compile
```

### Run Tests
To execute the unit tests and verify the buffer logic:
```
mvn test
```

## Future Improvements
- Circular Buffer: Implementing the screen as a circular array of Lines to make vertical scrolling an O(1) pointer swap.
- ANSI Parser: Adding a sequence scanner in the write method to interpret standard ANSI escape codes for colors and styling automatically.
- Thread Safety: Implementing concurrent locks to allow a background logic thread to write to the buffer while a UI thread renders the output.
