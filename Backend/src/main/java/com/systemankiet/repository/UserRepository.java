package com.systemankiet.repository;

import com.systemankiet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repozytorium użytkowników — wyszukiwanie po emailu case-insensitive
 * (Spring Data generuje zapytania z metod findBy*/existsBy*).
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
