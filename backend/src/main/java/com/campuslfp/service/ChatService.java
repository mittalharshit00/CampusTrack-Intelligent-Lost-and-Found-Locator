package com.campuslfp.service;

import com.campuslfp.model.*;
import com.campuslfp.repository.ConversationRepository;
import com.campuslfp.repository.ItemRepository;
import com.campuslfp.repository.MessageRepository;
import com.campuslfp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public Conversation startConversation(Long itemId, String otherUserEmail, String currentUserEmail) {
        User userA = userRepository.findByEmail(currentUserEmail).orElseThrow();
        User userB = userRepository.findByEmail(otherUserEmail).orElseThrow();
        Item item = itemRepository.findById(itemId).orElseThrow();
        // Check if conversation already exists (in any order)
        return conversationRepository.findByItemIdAndUsers(itemId, userA.getId(), userB.getId())
            .orElseGet(() -> conversationRepository.save(
                Conversation.builder()
                    .userA(userA)
                    .userB(userB)
                    .item(item)
                    .createdAt(Instant.now())
                    .approved(false)
                    .blockedByA(false)
                    .blockedByB(false)
                    .build()
            ));
    }

    public List<Conversation> getUserConversations(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        return conversationRepository.findByUserAOrUserB(user, user);
    }

    public List<Message> getConversationMessages(Long conversationId) {
        return messageRepository.findByConversation_IdOrderBySentAtAsc(conversationId);
    }

    public Message sendMessage(Long conversationId, String text, String senderEmail) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();
        User sender = userRepository.findByEmail(senderEmail).orElseThrow();
        // Check blocking: if recipient has blocked the sender, prevent sending
        User userA = conversation.getUserA();
        User userB = conversation.getUserB();
        boolean senderIsA = sender.getId().equals(userA.getId());
        boolean senderIsB = sender.getId().equals(userB.getId());
        if (!senderIsA && !senderIsB) throw new RuntimeException("Sender not part of conversation");

        // If the other party has blocked this sender, stop
        if (senderIsA && conversation.isBlockedByB()) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "You are blocked");
        }
        if (senderIsB && conversation.isBlockedByA()) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "You are blocked");
        }

        // Approval rules: if conversation not approved yet
        if (!conversation.isApproved()) {
            // Only allow the initiator (userA) to send the first single message
            if (senderIsA) {
                long sentCount = messageRepository.countByConversation_IdAndSender_Id(conversationId, sender.getId());
                if (sentCount >= 1) {
                    throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Conversation pending approval");
                }
                // allow the first message
            } else {
                // recipient cannot send until they approve
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Conversation pending approval");
            }
        }

        Message msg = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(text)
                .sentAt(Instant.now())
                .isRead(false)
                .build();
        return messageRepository.save(msg);
    }

    public Conversation approveConversation(Long conversationId, String approverEmail) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();
        User approver = userRepository.findByEmail(approverEmail).orElseThrow();
        // Only allow the recipient (userB) to approve if they are userB
        if (!approver.getId().equals(conversation.getUserB().getId())) {
            throw new RuntimeException("Only the recipient can approve the conversation");
        }
        conversation.setApproved(true);
        return conversationRepository.save(conversation);
    }

    public Conversation blockInConversation(Long conversationId, String blockerEmail) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();
        User blocker = userRepository.findByEmail(blockerEmail).orElseThrow();
        if (blocker.getId().equals(conversation.getUserA().getId())) {
            conversation.setBlockedByA(true);
        } else if (blocker.getId().equals(conversation.getUserB().getId())) {
            conversation.setBlockedByB(true);
        } else {
            throw new RuntimeException("User not part of conversation");
        }
        return conversationRepository.save(conversation);
    }

    public Conversation unblockInConversation(Long conversationId, String blockerEmail) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();
        User blocker = userRepository.findByEmail(blockerEmail).orElseThrow();
        if (blocker.getId().equals(conversation.getUserA().getId())) {
            conversation.setBlockedByA(false);
        } else if (blocker.getId().equals(conversation.getUserB().getId())) {
            conversation.setBlockedByB(false);
        } else {
            throw new RuntimeException("User not part of conversation");
        }
        return conversationRepository.save(conversation);
    }
}
