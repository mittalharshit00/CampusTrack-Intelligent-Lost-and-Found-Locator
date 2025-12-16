package com.campuslfp.controller;

import com.campuslfp.model.Conversation;
import com.campuslfp.model.Message;
import com.campuslfp.service.ChatService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/conversations")
    public ResponseEntity<Conversation> startConversation(@RequestBody StartConvDto dto, Authentication auth) {
        Conversation conversation = chatService.startConversation(dto.getItemId(), dto.getOtherUserEmail(), auth.getName());
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<Conversation>> getConversations(Authentication auth) {
        return ResponseEntity.ok(chatService.getUserConversations(auth.getName()));
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(chatService.getConversationMessages(id));
    }

    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<Message> sendMessage(@PathVariable Long id, @RequestBody SendMsgDto dto, Authentication auth) {
        return ResponseEntity.ok(chatService.sendMessage(id, dto.getContent(), auth.getName()));
    }

    @PostMapping("/conversations/{id}/approve")
    public ResponseEntity<Conversation> approveConversation(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(chatService.approveConversation(id, auth.getName()));
    }

    @PostMapping("/conversations/{id}/block")
    public ResponseEntity<Conversation> blockConversation(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(chatService.blockInConversation(id, auth.getName()));
    }

    @PostMapping("/conversations/{id}/unblock")
    public ResponseEntity<Conversation> unblockConversation(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(chatService.unblockInConversation(id, auth.getName()));
    }

    @Data static class StartConvDto {
        private Long itemId;
        private String otherUserEmail;
    }
    @Data static class SendMsgDto {
        private String content;
    }
}
