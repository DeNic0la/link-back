package ch.denic0la.db.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;

@Entity
public class SecuredLink extends PanacheEntity {
    @Column(nullable = false, unique = true)
    public String accessKey;

    @Column(nullable = false)
    public String secondFactorKey;

    @Column(nullable = false)
    public String targetLink;

    @Column(nullable = false)
    public boolean hasBeenAccessed = false;
}
