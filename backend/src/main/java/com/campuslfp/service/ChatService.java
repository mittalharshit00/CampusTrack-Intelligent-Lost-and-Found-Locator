package com.campuslfp.service;

import com.campuslfp.exception.BadRequestException;
import com.campuslfp.exception.ResourceNotFoundException;
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
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        User otherUser = userRepository.findByEmail(otherUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        return conversationRepository.findByItemIdAndUsers(itemId, currentUser.getId(), otherUser.getId())
                .orElseGet(() -> conversationRepository.save(
                        Conversation.builder()
                                .userA(currentUser)
                                .userB(otherUser)
                                .item(item)
                                .createdAt(Instant.now())
                                .approved(false)
                                .blockedByA(false)
                                .blockedByB(false)
                                .build()));
    }

    public List<Conversation> getUserConversations(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return conversationRepository.findByUserAOrUserB(user, user);
    }

    public List<Message> getConversationMessages(Long conversationId) {
        return messageRepository.findByConversation_IdOrderBySentAtAsc(conversationId);
    }

    public Message sendMessage(Long conversationId, String text, String senderEmail) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!isParticipant(sender, conversation)) {
            throw new BadRequestException("Sender not part of conversation");
        }
        if (isBlockedByOtherUser(sender, conversation)) {
            throw new BadRequestException("You are blocked");
        }
        if (!conversation.isApproved() && isApprovalBlocked(sender, conversationId)) {
            throw new BadRequestException("Conversation pending approval");
        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(text)
                .sentAt(Instant.now())
                .isRead(false)
                .build();
        return messageRepository.save(message);
    }

    public Conversation approveConversation(Long conversationId, String approverEmail) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        User approver = userRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!approver.getId().equals(conversation.getUserB().getId())) {
            throw new BadRequestException("Only the recipient can approve the conversation");
        }
        conversation.setApproved(true);
        return conversationRepository.save(conversation);
    }

    public Conversation blockInConversation(Long conversationId, String blockerEmail) {
        Conversation conversation = findConversation(conversationId);
        User blocker = findUser(blockerEmail);

        updateBlockState(conversation, blocker, true);
        return conversationRepository.save(conversation);
    }

    public Conversation unblockInConversation(Long conversationId, String blockerEmail) {
        Conversation conversation = findConversation(conversationId);
        User blocker = findUser(blockerEmail);

        updateBlockState(conversation, blocker, false);
        return conversationRepository.save(conversation);
    }

    private Conversation findConversation(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private boolean isParticipant(User sender, Conversation conversation) {
        return sender.getId().equals(conversation.getUserA().getId())
                || sender.getId().equals(conversation.getUserB().getId());
    }

    private boolean isBlockedByOtherUser(User sender, Conversation conversation) {
        boolean senderIsInitiator = sender.getId().equals(conversation.getUserA().getId());
        boolean senderIsRecipient = sender.getId().equals(conversation.getUserB().getId());
        return (senderIsInitiator && conversation.isBlockedByB())
                || (senderIsRecipient && conversation.isBlockedByA());
    }

    private boolean isApprovalBlocked(User sender, Long conversationId) {
        boolean senderIsInitiator = sender.getId().equals(conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found")).getUserA().getId());
        if (!senderIsInitiator) {
            return true;
        }
        return messageRepository.countByConversation_IdAndSender_Id(conversationId, sender.getId()) >= 1;
    }

    private void updateBlockState(Conversation conversation, User blocker, boolean blocked) {
        if (blocker.getId().equals(conversation.getUserA().getId())) {
            conversation.setBlockedByA(blocked);
            return;
        }
        if (blocker.getId().equals(conversation.getUserB().getId())) {
            conversation.setBlockedByB(blocked);
            return;
        }
        throw new BadRequestException("User not part of conversation");
    }
}
