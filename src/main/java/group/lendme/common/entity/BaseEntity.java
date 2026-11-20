package group.lendme.common.entity;


import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Parent of every entity in LendMe (User, Loan, Repayment...).
 *
 * OOP:
 *  - Abstraction:   abstract, so it can never be saved on its own.
 *  - Inheritance:   entities "extend BaseEntity" and get id, audit dates and version for free.
 *  - Encapsulation: fields are private with getters only. Nobody can set an id or
 *                   createdAt by hand; JPA and Spring auditing manage them.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Optimistic locking. If two requests update the same loan at the same time,
     * the second one fails instead of silently overwriting the balance.
     */
    @Version
    private Long version;

    public boolean isNew() {
        return id == null;
    }

    /*
     * Two entities are equal when they are the same type and have the same id.
     * Uses getId() (not the field) so it also works with Hibernate lazy proxies.
     */
    @Override
    public final boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (effectiveClass(this) != effectiveClass(other)) return false;
        BaseEntity that = (BaseEntity) other;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public final int hashCode() {
        return effectiveClass(this).hashCode();
    }

    @Override
    public String toString() {
        return effectiveClass(this).getSimpleName() + "(id=" + getId() + ")";
    }

    private static Class<?> effectiveClass(Object o) {
        return o instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
    }
}
