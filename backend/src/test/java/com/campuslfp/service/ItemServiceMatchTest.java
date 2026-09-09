package com.campuslfp.service;

import com.campuslfp.dto.response.ItemDto;
import com.campuslfp.enums.ItemStatus;
import com.campuslfp.enums.ItemType;
import com.campuslfp.mapper.ItemMapper;
import com.campuslfp.model.Item;
import com.campuslfp.model.User;
import com.campuslfp.repository.ItemRepository;
import com.campuslfp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceMatchTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemService itemService;

    @Test
    void shouldMarkLostAndFoundItemsAsMatched() {
        User owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@college.edu");

        Item lostItem = new Item();
        lostItem.setId(10L);
        lostItem.setType(ItemType.LOST);
        lostItem.setStatus(ItemStatus.OPEN);
        lostItem.setMatched(false);
        lostItem.setPostedBy(owner);
        lostItem.setDateReported(Instant.now());

        Item foundItem = new Item();
        foundItem.setId(11L);
        foundItem.setType(ItemType.FOUND);
        foundItem.setStatus(ItemStatus.OPEN);
        foundItem.setMatched(false);
        foundItem.setPostedBy(owner);
        foundItem.setDateReported(Instant.now());

        when(itemRepository.findById(10L)).thenReturn(Optional.of(lostItem));
        when(itemRepository.findById(11L)).thenReturn(Optional.of(foundItem));
        when(userRepository.findByEmail("owner@college.edu")).thenReturn(Optional.of(owner));
        when(itemRepository.save(lostItem)).thenReturn(lostItem);
        when(itemRepository.save(foundItem)).thenReturn(foundItem);

        Item updated = itemService.markItemsAsMatched(10L, 11L, "owner@college.edu");

        assertEquals(ItemStatus.MATCHED, updated.getStatus());
        assertTrue(updated.getMatched());
        assertEquals(ItemStatus.MATCHED, foundItem.getStatus());
        assertTrue(foundItem.getMatched());
    }
}
