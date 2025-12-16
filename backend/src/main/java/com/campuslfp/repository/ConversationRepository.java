package com.campuslfp.repository;

import com.campuslfp.model.Conversation;
import com.campuslfp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUserAOrUserB(User userA, User userB);

    // Custom query because method name using itemId wouldn't map directly to item.id
    @Query("select c from Conversation c where c.item.id = :itemId and c.userA.id = :userAId and c.userB.id = :userBId")
    Optional<Conversation> findByItemIdAndUserA_IdAndUserB_Id(@Param("itemId") Long itemId, @Param("userAId") Long userAId, @Param("userBId") Long userBId);

    // Find conversation for the same item between two users in any order
    @Query("select c from Conversation c where c.item.id = :itemId and ((c.userA.id = :u1 and c.userB.id = :u2) or (c.userA.id = :u2 and c.userB.id = :u1))")
    Optional<Conversation> findByItemIdAndUsers(@Param("itemId") Long itemId, @Param("u1") Long user1Id, @Param("u2") Long user2Id);

    // (method above handles finding conversation between two users regardless of order)
}
