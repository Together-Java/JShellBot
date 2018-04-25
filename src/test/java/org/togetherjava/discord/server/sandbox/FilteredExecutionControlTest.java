package org.togetherjava.discord.server.sandbox;

import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilteredExecutionControlTest {

    @Test
    void testBlockPackage() {
        JShell jshell = getJShell(List.of("java.time"), List.of(), List.of());

        assertTrue(
                failed(jshell, "java.time.LocalDateTime.now()"),
                "Was able to access a method in the blocked package."
        );
        assertTrue(
                failed(jshell, "java.time.format.DateTimeFormatter.ISO_DATE"),
                "Was able to access a field in a subpackage of a blocked package."
        );
        assertTrue(
                failed(jshell, "java.time.format.DateTimeFormatter.ISO_DATE.getChronology()"),
                "Was able to access a method in a subpackage of a blocked package."
        );
        assertFalse(
                failed(jshell, "Math.PI"),
                "Was not able to access a field in another package."
        );
        assertFalse(
                failed(jshell, "Math.abs(5)"),
                "Was not able to access a field in another package."
        );
    }

    @Test
    void testBlockClass() {
        JShell jshell = getJShell(List.of(), List.of("java.time.LocalDate"), List.of());

        assertTrue(
                failed(jshell, "java.time.LocalDate.now()"),
                "Was able to access a method in the blocked class."
        );
        assertFalse(
                failed(jshell, "java.time.LocalDateTime.now()"),
                "Was not able to access another class with the same prefix."
        );
        assertFalse(
                failed(jshell, "java.time.format.DateTimeFormatter.ISO_DATE"),
                "Was not able to access a field in a not blocked class."
        );
        assertFalse(
                failed(jshell, "java.time.format.DateTimeFormatter.ISO_DATE.getChronology()"),
                "Was not able to access a method in a not blocked class."
        );
    }

    @Test
    void testBlockMethod() {
        JShell jshell = getJShell(
                List.of(), List.of(),
                List.of(
                        new ImmutablePair<>("java.time.LocalDate", "now")
                )
        );

        assertTrue(
                failed(jshell, "java.time.LocalDate.now()"),
                "Was able to access a blocked method."
        );
        assertFalse(
                failed(jshell, "java.time.LocalDateTime.now()"),
                "Was not able to access a method with the same name."
        );
        assertFalse(
                failed(jshell, "Math.abs(5)"),
                "Was not able to access a method in a not blocked class."
        );
        assertFalse(
                failed(jshell, "java.time.format.DateTimeFormatter.ISO_DATE.getChronology()"),
                "Was not able to access a method in a not blocked class."
        );
    }

    private JShell getJShell(Collection<String> blockedPackages, Collection<String> blockedClasses,
                             Collection<Pair<String, String>> blockedMethods) {
        return JShell.builder()
                .executionEngine(
                        new FilteredExecutionControlProvider(blockedPackages, blockedClasses, blockedMethods),
                        Map.of()
                )
                .build();
    }

    private boolean failed(JShell jshell, String command) {
        try {
            for (SnippetEvent event : jshell.eval(command)) {
                System.out.println("Got a value: " + event.value());
            }
        } catch (UnsupportedOperationException e) {
            return true;
        }
        return false;
    }
}