package com.campuslfp.service;

import com.campuslfp.exception.ResourceNotFoundException;
import com.campuslfp.model.Flag;
import com.campuslfp.model.Item;
import com.campuslfp.model.User;
import com.campuslfp.repository.FlagRepository;
import com.campuslfp.repository.ItemRepository;
import com.campuslfp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ItemRepository itemRepository;
    private final FlagRepository flagRepository;
    private final UserRepository userRepository;

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item not found");
        }
        itemRepository.deleteById(id);
    }

    public List<Flag> getAllFlags() {
        return flagRepository.findAll();
    }

    public List<User> getPendingUsers() {
        return userRepository.findPending();
    }

    public void approveUser(Long id) {
        User user = findUser(id);
        user.setApproved(true);
        userRepository.save(user);
    }

    public void ignoreUser(Long id) {
        User user = findUser(id);
        user.setIgnored(true);
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void blockUser(Long id) {
        User user = findUser(id);
        user.setBlocked(true);
        userRepository.save(user);
    }

    public void unblockUser(Long id) {
        User user = findUser(id);
        user.setBlocked(false);
        userRepository.save(user);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
