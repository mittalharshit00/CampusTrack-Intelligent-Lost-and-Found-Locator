package com.campuslfp.repository;

import com.campuslfp.model.Item;
import com.campuslfp.model.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByType(ItemType type);
    List<Item> findByTypeAndCategory(ItemType type, String category);
    List<Item> findByLocation(String location);
    List<Item> findByStatus(String status);
    List<Item> findByPostedBy_Id(Long userId);
    List<Item> findByFlaggedTrue();
}
