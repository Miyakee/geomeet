package com.geomeet.api.domain.entity;

import com.geomeet.api.domain.valueobject.Email;
import com.geomeet.api.domain.valueobject.PasswordHash;
import com.geomeet.api.domain.valueobject.Username;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * User aggregate root.
 * This is the main entity in the User aggregate.
 */
@Getter
@Setter(AccessLevel.PACKAGE)
public class User {

    private Long id;
    private Username username;
    private Email email;
    private PasswordHash passwordHash;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Private constructor for JPA reconstruction
    private User() {
    }

    /**
     * Factory method to create a new User.
     */
    public static User create(Username username, Email email, PasswordHash passwordHash) {
        User user = new User();
        user.username = username;
        user.email = email;
        user.passwordHash = passwordHash;
        user.active = true;
        LocalDateTime now = LocalDateTime.now();
        user.createdAt = now;
        user.updatedAt = now;
        return user;
    }

    /**
     * Factory method to reconstruct User from persistence.
     */
    public static User reconstruct(
        Long id,
        Username username,
        Email email,
        PasswordHash passwordHash,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
    ) {
        User user = new User();
        user.id = id;
        user.username = username;
        user.email = email;
        user.passwordHash = passwordHash;
        user.active = active;
        user.createdAt = createdAt;
        user.updatedAt = updatedAt;
        user.createdBy = createdBy;
        user.updatedBy = updatedBy;
        return user;
    }

    /**
     * Business method: Deactivate user account.
     */
    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("User is already inactive");
        }
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Business method: Activate user account.
     */
    public void activate() {
        if (this.active) {
            throw new IllegalStateException("User is already active");
        }
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Business method: Update password.
     */
    public void changePassword(PasswordHash newPasswordHash) {
        if (newPasswordHash == null) {
            throw new IllegalArgumentException("Password hash cannot be null");
        }
        this.passwordHash = newPasswordHash;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Business method: Check if user can authenticate.
     */
    public boolean canAuthenticate() {
        return this.active != null && this.active;
    }
}
