package com.geomeet.api.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.geomeet.api.domain.valueobject.Email;
import com.geomeet.api.domain.valueobject.PasswordHash;
import com.geomeet.api.domain.valueobject.Username;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserTest {

    private Username username;
    private Email email;
    private PasswordHash passwordHash;

    @BeforeEach
    void setUp() {
        username = new Username("testuser");
        email = new Email("test@example.com");
        passwordHash = new PasswordHash("$2a$12$hashedpassword");
    }

    @Test
    void shouldCreateUserWithFactoryMethod() {
        User user = User.create(username, email, passwordHash);

        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(passwordHash, user.getPasswordHash());
        assertTrue(user.getActive());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void shouldSetActiveToTrueWhenCreatingUser() {
        User user = User.create(username, email, passwordHash);
        assertTrue(user.getActive());
    }

    @Test
    void shouldSetCreatedAtAndUpdatedAtWhenCreatingUser() {
        LocalDateTime before = LocalDateTime.now();
        User user = User.create(username, email, passwordHash);
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertTrue(user.getCreatedAt().isAfter(before.minusSeconds(1)));
        assertTrue(user.getCreatedAt().isBefore(after.plusSeconds(1)));
        assertTrue(user.getUpdatedAt().isAfter(before.minusSeconds(1)));
        assertTrue(user.getUpdatedAt().isBefore(after.plusSeconds(1)));
    }

    @Test
    void shouldReconstructUserFromPersistence() {
        Long id = 1L;
        Boolean active = true;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        String createdBy = "admin";
        String updatedBy = "admin";

        User user = User.reconstruct(
            id, username, email, passwordHash, active, createdAt, updatedAt, createdBy, updatedBy
        );

        assertNotNull(user);
        assertEquals(id, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(passwordHash, user.getPasswordHash());
        assertEquals(active, user.getActive());
        assertEquals(createdAt, user.getCreatedAt());
        assertEquals(updatedAt, user.getUpdatedAt());
        assertEquals(createdBy, user.getCreatedBy());
        assertEquals(updatedBy, user.getUpdatedBy());
    }

    @Test
    void shouldDeactivateUser() {
        User user = User.create(username, email, passwordHash);
        assertTrue(user.getActive());

        user.deactivate();

        assertFalse(user.getActive());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void shouldThrowExceptionWhenDeactivatingAlreadyInactiveUser() {
        User user = User.create(username, email, passwordHash);
        user.deactivate();

        assertThrows(IllegalStateException.class, user::deactivate);
    }

    @Test
    void shouldActivateUser() {
        User user = User.create(username, email, passwordHash);
        user.deactivate();
        assertFalse(user.getActive());

        user.activate();

        assertTrue(user.getActive());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void shouldThrowExceptionWhenActivatingAlreadyActiveUser() {
        User user = User.create(username, email, passwordHash);

        assertThrows(IllegalStateException.class, user::activate);
    }

    @Test
    void shouldChangePassword() {
        User user = User.create(username, email, passwordHash);
        PasswordHash newPasswordHash = new PasswordHash("$2a$12$newhashedpassword");

        user.changePassword(newPasswordHash);

        assertEquals(newPasswordHash, user.getPasswordHash());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void shouldThrowExceptionWhenChangingPasswordWithNull() {
        User user = User.create(username, email, passwordHash);

        assertThrows(IllegalArgumentException.class, () -> user.changePassword(null));
    }

    @Test
    void shouldReturnTrueWhenUserCanAuthenticate() {
        User user = User.create(username, email, passwordHash);
        assertTrue(user.canAuthenticate());
    }

    @Test
    void shouldReturnFalseWhenUserCannotAuthenticate() {
        User user = User.create(username, email, passwordHash);
        user.deactivate();
        assertFalse(user.canAuthenticate());
    }

    @Test
    void shouldReturnFalseWhenUserIsNull() {
        User user = User.reconstruct(
            1L, username, email, passwordHash, null, LocalDateTime.now(), LocalDateTime.now(), null, null
        );
        assertFalse(user.canAuthenticate());
    }
}

