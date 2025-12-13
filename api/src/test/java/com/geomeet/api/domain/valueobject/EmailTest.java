package com.geomeet.api.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EmailTest {

    @Test
    void shouldCreateValidEmail() {
        Email email = new Email("test@example.com");
        assertNotNull(email);
        assertEquals("test@example.com", email.getValue());
    }

    @Test
    void shouldNormalizeEmailToLowerCase() {
        Email email = new Email("TEST@EXAMPLE.COM");
        assertEquals("test@example.com", email.getValue());
    }

    @Test
    void shouldTrimEmail() {
        assertThrows(IllegalArgumentException.class, () -> new Email("  test@example.com  "));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Email(null));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new Email(""));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Email("   "));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new Email("invalid-email"));
    }

    @Test
    void shouldThrowExceptionWhenEmailMissingAt() {
        assertThrows(IllegalArgumentException.class, () -> new Email("testexample.com"));
    }

    @Test
    void shouldThrowExceptionWhenEmailMissingDomain() {
        assertThrows(IllegalArgumentException.class, () -> new Email("test@"));
    }

    @Test
    void shouldThrowExceptionWhenEmailMissingLocalPart() {
        assertThrows(IllegalArgumentException.class, () -> new Email("@example.com"));
    }

    @Test
    void shouldAcceptValidEmailWithSubdomain() {
        Email email = new Email("test@mail.example.com");
        assertEquals("test@mail.example.com", email.getValue());
    }

    @Test
    void shouldAcceptValidEmailWithPlus() {
        Email email = new Email("test+tag@example.com");
        assertEquals("test+tag@example.com", email.getValue());
    }

    @Test
    void shouldBeEqualWhenEmailsAreSame() {
        Email email1 = new Email("test@example.com");
        Email email2 = new Email("test@example.com");
        assertEquals(email1, email2);
        assertEquals(email1.hashCode(), email2.hashCode());
    }

    @Test
    void shouldBeEqualWhenEmailsAreSameCaseInsensitive() {
        Email email1 = new Email("TEST@EXAMPLE.COM");
        Email email2 = new Email("test@example.com");
        assertEquals(email1, email2);
        assertEquals(email1.hashCode(), email2.hashCode());
    }
}

