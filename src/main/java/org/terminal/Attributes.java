package org.terminal;

import java.util.Set;

public record Attributes(char character, Color fgColor, Color bgColor, Set<Style> styles, Boolean isCellEmpty) {}
