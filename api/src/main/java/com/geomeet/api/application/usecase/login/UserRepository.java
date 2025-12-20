package com.geomeet.api.application.usecase.login;

import com.geomeet.api.domain.entity.User;
import java.util.Optional;

/**
 * Repository interface for User aggregate.
 * This port is defined in the application layer (use case layer).
 * It defines what the use cases need from the infrastructure.
 */
public interface UserRepository {

    /**
     * Finds a user by username.
     *
     * @param username the username
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by email.
     *
     * @param email the email
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by username or email.
     *
     * @param usernameOrEmail username or email
     * @return Optional containing the user if found
     */
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);

    /**
     * Saves a user (creates or updates).
     *
     * @param user the user aggregate root to save
     * @return the saved user
     */
    User save(User user);

    /**
     * Finds a user by ID.
     *
     * @param id the user ID
     * @return Optional containing the user if found
     */
    Optional<User> findById(Long id);
}

