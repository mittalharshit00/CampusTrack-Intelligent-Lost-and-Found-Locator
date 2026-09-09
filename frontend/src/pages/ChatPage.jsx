import React, { useEffect, useState, useContext } from "react";
import api from "../services/api";
import { AuthContext } from "../context/AuthContext";
import "../styles/Chat.css";

const ChatPage = () => {
  const [conversations, setConversations] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [messageBox, setMessageBox] = useState("");
  const { user } = useContext(AuthContext);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  

  // Fetch conversations on mount
  useEffect(() => {
    fetchConversations();
  }, []);

  const fetchConversations = async () => {
    try {
      setLoading(true);
      const res = await api.get("/chat/conversations");
      setConversations(res.data);
      setError(null);
    } catch (err) {
      setError("Failed to load conversations. Please try again.");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // Fetch messages when selected conversation changes
  useEffect(() => {
    const fetchMessages = async () => {
      try {
        const res = await api.get(`/chat/conversations/${selectedId}/messages`);
        setMessages(res.data);
      } catch (err) {
        console.error("Failed to fetch messages:", err);
      }
    };

    if (selectedId) {
      fetchMessages();
      // Poll for new messages every 2 seconds
      const interval = setInterval(fetchMessages, 2000);
      return () => clearInterval(interval);
    }
  }, [selectedId]);

  const sendMessage = async () => {
    if (!messageBox.trim()) return;

    try {
      await api.post(`/chat/conversations/${selectedId}/messages`, {
        content: messageBox,
      });
      setMessageBox("");
      // Fetch messages again to show the sent message
      const res = await api.get(`/chat/conversations/${selectedId}/messages`);
      setMessages(res.data);
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data || err.message || 'Failed to send message';
      setError(msg);
      console.error('Send message error', err);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  if (loading) {
    return <div className="chat-loading">Loading conversations...</div>;
  }

  return (
    <div className="chat-container">
      <div className="chat-header">
        <h2>Messages</h2>
      </div>

      <div className="chat-main">
        {/* Conversations List */}
        <div className="conversations-panel">
          <h3>Conversations</h3>
          {error && <div className="error-message">{error}</div>}
          {conversations.length === 0 ? (
            <div className="empty-state">
              <p>No conversations yet.</p>
              <p className="hint">Contact someone about an item to start chatting.</p>
            </div>
          ) : (
            <ul className="conversations-list">
              {conversations.map((conversation) => (
                <li
                  key={conversation.id}
                  className={`conversation-item ${
                    selectedId === conversation.id ? "active" : ""
                  }`}
                  onClick={() => setSelectedId(conversation.id)}
                >
                  <div className="conv-header">
                    <strong>{conversation.item?.title || "Unknown Item"}</strong>
                  </div>
                  <div className="conv-users">
                    {conversation.userA?.name} & {conversation.userB?.name}
                  </div>
                  <div className="conv-item-type">
                    <span className={`badge ${conversation.item?.type?.toLowerCase()}`}>
                      {conversation.item?.type}
                    </span>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* Messages Panel */}
        <div className="messages-panel">
          {selectedId ? (
            <>
              <div className="messages-header">
                <h3>
                  {conversations
                    .find((c) => c.id === selectedId)
                    ?.item?.title || "Chat"}
                </h3>
                <div className="conv-controls">
                  {/* Approve button: show only if current user is userB (recipient) and conversation not approved */}
                  {(() => {
                    const conv = conversations.find((c) => c.id === selectedId);
                    if (!conv) return null;
                    const isUserA = conv.userA?.email === user?.email;
                    const isUserB = conv.userB?.email === user?.email;
                    return (
                      <>
                        {!conv.approved && isUserB && (
                          <button
                            className="btn-approve"
                            onClick={async () => {
                              try {
                                await api.post(`/chat/conversations/${selectedId}/approve`);
                                // refresh conversations and messages
                                await fetchConversations();
                                const res = await api.get(`/chat/conversations/${selectedId}/messages`);
                                setMessages(res.data);
                              } catch (e) {
                                console.error('Approve failed', e);
                                setError('Failed to approve conversation');
                              }
                            }}
                          >
                            Approve
                          </button>
                        )}

                        {/* Block/Unblock for current user */}
                        {isUserA && (
                          conv.blockedByA ? (
                            <button className="btn-ignore" onClick={async () => { try { await api.post(`/chat/conversations/${selectedId}/unblock`); await fetchConversations(); } catch(e){console.error(e); setError('Failed to unblock');} }}>Unblock</button>
                          ) : (
                            <button className="btn-ignore" onClick={async () => { try { await api.post(`/chat/conversations/${selectedId}/block`); await fetchConversations(); } catch(e){console.error(e); setError('Failed to block');} }}>Block</button>
                          )
                        )}
                        {isUserB && (
                          conv.blockedByB ? (
                            <button className="btn-ignore" onClick={async () => { try { await api.post(`/chat/conversations/${selectedId}/unblock`); await fetchConversations(); } catch(e){console.error(e); setError('Failed to unblock');} }}>Unblock</button>
                          ) : (
                            <button className="btn-ignore" onClick={async () => { try { await api.post(`/chat/conversations/${selectedId}/block`); await fetchConversations(); } catch(e){console.error(e); setError('Failed to block');} }}>Block</button>
                          )
                        )}
                      </>
                    );
                  })()}
                </div>
              </div>

              <div className="messages-list">
                {messages.length === 0 ? (
                  <div className="empty-chat">
                    <p>No messages yet. Start the conversation!</p>
                  </div>
                ) : (
                  messages.map((msg) => (
                    <div
                      key={msg.id}
                      className={`message ${
                        msg.sender?.email === user?.email ? "sent" : "received"
                      }`}
                    >
                      <div className="message-header">
                        <strong>{msg.sender?.name}</strong>
                        <span className="message-time">
                          {new Date(msg.sentAt).toLocaleTimeString([], {
                            hour: "2-digit",
                            minute: "2-digit",
                          })}
                        </span>
                      </div>
                      <div className="message-content">{msg.content}</div>
                    </div>
                  ))
                )}
              </div>

              

              <div className="message-input-area">
                <textarea
                  value={messageBox}
                  onChange={(e) => setMessageBox(e.target.value)}
                  onKeyPress={handleKeyPress}
                  placeholder="Type your message... (Press Enter to send)"
                  className="message-input"
                  rows="3"
                />
                <button onClick={sendMessage} className="send-btn">
                  Send
                </button>
              </div>
            </>
          ) : (
            <div className="no-selection">
              <p>Select a conversation to start messaging</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ChatPage;
