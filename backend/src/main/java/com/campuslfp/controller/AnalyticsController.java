package com.campuslfp.controller;

import com.campuslfp.model.Item;
import com.campuslfp.model.ItemStatus;
import com.campuslfp.repository.ItemRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import com.campuslfp.model.User;
import com.campuslfp.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @GetMapping("/summary")
    public Summary summary() {
        List<Item> items = itemRepository.findAll();
        long open = items.stream().filter(i -> i.getStatus() == ItemStatus.OPEN).count();
    long matched = items.stream().filter(i -> (i.getStatus() == ItemStatus.MATCHED) || Boolean.TRUE.equals(i.getMatched())).count();
        long closed = items.stream().filter(i -> i.getStatus() == ItemStatus.CLOSED).count();

        Summary s = new Summary();
        s.total = items.size();
        s.open = open;
        s.matched = matched;
        s.closed = closed;
        return s;
    }

    @GetMapping("/user")
    public UserSummary userSummary(Authentication authentication) {
        if (authentication == null) return new UserSummary();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return new UserSummary();

        java.util.List<Item> items = itemRepository.findByPostedBy_Id(user.getId());
        long reported = items.size();
        long recovered = items.stream().filter(i -> (i.getStatus() == ItemStatus.MATCHED) || Boolean.TRUE.equals(i.getMatched()) || i.getStatus() == ItemStatus.CLOSED).count();
        long notRecovered = items.stream().filter(i -> i.getStatus() == ItemStatus.OPEN).count();

        UserSummary us = new UserSummary();
        us.reported = reported;
        us.recovered = recovered;
        us.notRecovered = notRecovered;
        return us;
    }

    @Data
    public static class Summary {
        public long total;
        public long open;
        public long matched;
        public long closed;
    }

    @Data
    public static class UserSummary {
        public long reported = 0;
        public long recovered = 0;
        public long notRecovered = 0;
    }
}
