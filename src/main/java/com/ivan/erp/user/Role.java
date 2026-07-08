package com.ivan.erp.user;

import com.ivan.erp.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(
        name = "roles",
        indexes = {
                @Index(name = "idx_roles_name", columnList = "name", unique = true)
        }
)
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    protected Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
