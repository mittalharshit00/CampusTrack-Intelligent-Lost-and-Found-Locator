package com.campuslfp.repository;

import com.campuslfp.model.Flag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlagRepository extends JpaRepository<Flag, Long> {
    List<Flag> findByItemId(Long itemId);
    List<Flag> findByMessageId(Long messageId);
}
