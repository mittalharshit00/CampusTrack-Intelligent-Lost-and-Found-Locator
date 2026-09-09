package com.campuslfp.service;

import com.campuslfp.dto.response.AnalyticsSummary;
import com.campuslfp.dto.response.UserAnalyticsSummary;
import com.campuslfp.enums.ItemStatus;
import com.campuslfp.model.Item;
import com.campuslfp.model.User;
import com.campuslfp.repository.ItemRepository;
import com.campuslfp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public AnalyticsSummary summary() {
        List<Item> items = itemRepository.findAll();
        long open = items.stream().filter(item -> item.getStatus() == ItemStatus.OPEN).count();
        long matched = items.stream()
                .filter(item -> item.getStatus() == ItemStatus.MATCHED || Boolean.TRUE.equals(item.getMatched()))
                .count();
        long closed = items.stream().filter(item -> item.getStatus() == ItemStatus.CLOSED).count();

        return new AnalyticsSummary(items.size(), open, matched, closed);
    }

    public UserAnalyticsSummary userSummary(Authentication authentication) {
        if (authentication == null) {
            return new UserAnalyticsSummary();
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return new UserAnalyticsSummary();
        }

        List<Item> items = itemRepository.findByPostedBy_Id(user.getId());
        long reported = items.size();
        long recovered = items.stream()
                .filter(item -> item.getStatus() == ItemStatus.MATCHED
                        || Boolean.TRUE.equals(item.getMatched())
                        || item.getStatus() == ItemStatus.CLOSED)
                .count();
        long notRecovered = items.stream().filter(item -> item.getStatus() == ItemStatus.OPEN).count();

        return new UserAnalyticsSummary(reported, recovered, notRecovered);
    }
}
