package com.geomeet.api.domain.valueobject;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Invite Code value object.
 * Represents a short, random code required to join a session.
 * This is separate from SessionId to prevent enumeration attacks.
 */
@Getter
@EqualsAndHashCode
@ToString
public class InviteCode {

    // Excluding confusing characters (0, O, I, 1)
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String value;

    public InviteCode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Invite code cannot be null or empty");
        }
        if (value.length() != CODE_LENGTH) {
            throw new IllegalArgumentException("Invite code must be exactly " + CODE_LENGTH + " characters");
        }
        // Validate that code contains only allowed characters
        if (!value.chars().allMatch(c -> CHARACTERS.indexOf(c) >= 0)) {
            throw new IllegalArgumentException("Invite code contains invalid characters");
        }
        this.value = value.toUpperCase();
    }

    /**
     * Factory method to generate a new random invite code.
     * Uses secure random to prevent predictability.
     */
    public static InviteCode generate() {
        String code = IntStream.range(0, CODE_LENGTH)
            .mapToObj(i -> String.valueOf(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length()))))
            .collect(Collectors.joining());
        return new InviteCode(code);
    }

    /**
     * Factory method to create InviteCode from a string value.
     * @param value the string value of the invite code
     * @return an InviteCode instance
     */
    public static InviteCode fromString(String value) {
        return new InviteCode(value);
    }
}

