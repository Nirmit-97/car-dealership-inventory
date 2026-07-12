package com.dealership.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for User entity.
 * JpaRepository provides: save(), findById(), findAll(), delete(), count(), etc.
 * We add findByEmail() for the login/register flow.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Find a user by their email address.
     * Used in: login authentication, duplicate-email check on register.
     *
     * @param email the email to search for
     * @return Optional<User> — empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if an email is already registered.
     * Used in register() to detect duplicates before saving.
     *
     * @param email the email to check
     * @return true if email exists in DB
     */
    boolean existsByEmail(String email);
}
