package group.lendme.user;

import group.lendme.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A person on LendMe. Borrower/lender are not stored here - they are the
 * positions a user takes on a specific Loan.
 *
 * Inheritance:   extends BaseEntity (id, createdAt, updatedAt, version).
 * Encapsulation: no setters; state changes only through business methods.
 */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA needs it; nobody else should use it
public class User extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 75)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 75)
    private String lastName;

    /** E.164 format, e.g. +254712345678 */
    @Column(name = "phone_number", nullable = false, unique = true, length = 13)
    private String phoneNumber;

    /** Optional. Stored lower-case. */
    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** Platform permissions (USER, ADMIN). Stored in the user_roles table. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Set<Role> roles = new HashSet<>();

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    public User(String firstName, String lastName, String phoneNumber,
                String email, String passwordHash) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles.add(Role.USER);   // everyone starts as a normal user
        this.enabled = true;
        this.failedLoginAttempts = 0;
    }

    // ---------- roles ----------

    /** Read-only view so nobody can modify roles directly. */
    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public void grantRole(Role role) {
        roles.add(role);
    }

    public void revokeRole(Role role) {
        if (role == Role.USER) {
            throw new IllegalStateException("The USER role cannot be removed; disable the account instead");
        }
        roles.remove(role);
    }

    // ---------- login state ----------

    public boolean isLocked(Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    /** @return true if this failed attempt caused the account to be locked */
    public boolean recordFailedLogin(int maxAttempts, Duration lockDuration, Instant now) {
        failedLoginAttempts++;
        if (failedLoginAttempts >= maxAttempts) {
            lockedUntil = now.plus(lockDuration);
            failedLoginAttempts = 0;
            return true;
        }
        return false;
    }

    public void recordSuccessfulLogin(Instant now) {
        failedLoginAttempts = 0;
        lockedUntil = null;
        lastLoginAt = now;
    }

    // ---------- account management ----------

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void changeName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void disable() {
        this.enabled = false;
    }

    public void enable() {
        this.enabled = true;
    }

    public void unlock() {
        this.lockedUntil = null;
        this.failedLoginAttempts = 0;
    }
}