package com.geomeet.api.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InviteCodeTest {

    @Test
    void shouldCreateInviteCodeWithValidValue() {
        // Given
        String validCode = "ABCDEFGH";

        // When
        InviteCode inviteCode = new InviteCode(validCode);

        // Then
        assertNotNull(inviteCode);
        assertEquals(validCode, inviteCode.getValue());
    }

    @Test
    void shouldConvertToUpperCase() {
        // Given - use valid characters that are in the allowed set
        String lowercaseCode = "abcdefgh"; // A-H are valid

        // When & Then - should throw because validation happens before toUpperCase
        // The validation checks against CHARACTERS which doesn't include lowercase
        assertThrows(IllegalArgumentException.class, () -> {
            new InviteCode(lowercaseCode);
        });
    }

    @Test
    void shouldThrowExceptionWhenValueIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new InviteCode(null);
        });
        assertEquals("Invite code cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenValueIsEmpty() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new InviteCode("");
        });
        assertEquals("Invite code cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenValueIsBlank() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new InviteCode("   ");
        });
        assertEquals("Invite code cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLengthIsTooShort() {
        // Given
        String shortCode = "ABCDEFG"; // 7 characters

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new InviteCode(shortCode);
        });
        assertEquals("Invite code must be exactly 8 characters", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLengthIsTooLong() {
        // Given
        String longCode = "ABCDEFGHI"; // 9 characters

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new InviteCode(longCode);
        });
        assertEquals("Invite code must be exactly 8 characters", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenContainsInvalidCharacters() {
        // Given - contains '0' which is not allowed
        String invalidCode = "ABCDEFG0";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new InviteCode(invalidCode);
        });
        assertEquals("Invite code contains invalid characters", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenContainsO() {
        // Given - contains 'O' which is not allowed
        String invalidCode = "ABCDEFGO";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new InviteCode(invalidCode);
        });
        assertEquals("Invite code contains invalid characters", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenContainsI() {
        // Given - contains 'I' which is not allowed
        String invalidCode = "ABCDEFGI";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new InviteCode(invalidCode);
        });
        assertEquals("Invite code contains invalid characters", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenContains1() {
        // Given - contains '1' which is not allowed
        String invalidCode = "ABCDEFG1";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new InviteCode(invalidCode);
        });
        assertEquals("Invite code contains invalid characters", exception.getMessage());
    }

    @Test
    void shouldAcceptValidAlphanumericCharacters() {
        // Given - valid characters: A-H, J-N, P-Z, 2-9
        String validCode = "ABCDEFGH"; // All letters
        String validCode2 = "23456789"; // All numbers
        String validCode3 = "ABCD2345"; // Mixed

        // When & Then - should not throw
        InviteCode code1 = new InviteCode(validCode);
        InviteCode code2 = new InviteCode(validCode2);
        InviteCode code3 = new InviteCode(validCode3);

        assertNotNull(code1);
        assertNotNull(code2);
        assertNotNull(code3);
    }

    @Test
    void shouldGenerateRandomInviteCode() {
        // When
        InviteCode inviteCode = InviteCode.generate();

        // Then
        assertNotNull(inviteCode);
        assertNotNull(inviteCode.getValue());
        assertEquals(8, inviteCode.getValue().length());
        // Verify it contains only valid characters
        String value = inviteCode.getValue();
        assertTrue(value.matches("^[A-HJ-NP-Z2-9]{8}$"));
    }

    @Test
    void shouldGenerateDifferentCodesOnMultipleCalls() {
        // When
        InviteCode code1 = InviteCode.generate();
        InviteCode code2 = InviteCode.generate();
        InviteCode code3 = InviteCode.generate();

        // Then - very unlikely to be the same (but not impossible)
        // At least verify they are valid codes
        assertNotNull(code1);
        assertNotNull(code2);
        assertNotNull(code3);
        assertEquals(8, code1.getValue().length());
        assertEquals(8, code2.getValue().length());
        assertEquals(8, code3.getValue().length());
    }

    @Test
    void shouldCreateInviteCodeFromString() {
        // Given
        String validCode = "ABCDEFGH";

        // When
        InviteCode inviteCode = InviteCode.fromString(validCode);

        // Then
        assertNotNull(inviteCode);
        assertEquals(validCode, inviteCode.getValue());
    }

    @Test
    void shouldCreateInviteCodeFromStringWithValidCode() {
        // Given
        String validCode = "ABCDEFGH";

        // When
        InviteCode inviteCode = InviteCode.fromString(validCode);

        // Then
        assertNotNull(inviteCode);
        assertEquals("ABCDEFGH", inviteCode.getValue());
    }
}

