package org.terminal;

import java.util.Set;

public record Attributes(Color fgColor, Color bgColor, Set<Style> styles, Boolean isCellEmpty) {}
