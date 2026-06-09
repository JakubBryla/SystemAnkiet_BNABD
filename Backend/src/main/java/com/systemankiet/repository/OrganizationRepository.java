package com.systemankiet.repository;

import com.systemankiet.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
