package com.campuslfp.repository;

import com.campuslfp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    java.util.List<com.campuslfp.model.User> findByApprovedFalse();
    // pending and not ignored
    java.util.List<com.campuslfp.model.User> findByApprovedFalseAndIgnoredFalse();
    // find pending users where ignored is false or null
    @Query("SELECT u FROM User u WHERE u.approved = false AND (u.ignored = false OR u.ignored IS NULL)")
    java.util.List<com.campuslfp.model.User> findPending();
}
