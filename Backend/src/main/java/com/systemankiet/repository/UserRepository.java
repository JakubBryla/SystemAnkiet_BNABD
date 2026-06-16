package com.systemankiet.repository;

import com.systemankiet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repozytorium użytkowników — wyszukiwanie po emailu case-insensitive.
 * Spring Data generuje zapytania SQL automatycznie na podstawie nazw metod (findBy..., existsBy...).
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    // Daty rejestracji od podanego momentu — używane przez StatisticsService do grupowania po miesiącach
    @Query("SELECT u.createdAt FROM User u WHERE u.createdAt >= :since")
    List<LocalDateTime> findCreatedAtSince(@Param("since") LocalDateTime since);
}
