import React, { useEffect, useState } from "react";
import api from "../services/api";
import { useParams, useNavigate } from "react-router-dom";
// Optional: use Material UI IconButton and FlagIcon if available
import IconButton from '@mui/material/IconButton';
import FlagIcon from '@mui/icons-material/Flag';
import "../styles/ItemDetail.css";

const ItemDetailPage = () => {
  const { id } = useParams();
  const [item, setItem] = useState(null);
  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [contactLoading, setContactLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      setError(null);
      try {
        const itemRes = await api.get(`/items/${id}`);
        setItem(itemRes.data);

        const matchesRes = await api.get(`/items/${id}/matches`);
        setMatches(matchesRes.data);
      } catch (err) {
        setError(err.response?.data?.message || "Failed to load item details");
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [id]);

  const handleContactOwner = async (match) => {
    setContactLoading(true);
    try {
      // Create a new conversation with the matched item owner
      const response = await api.post("/chat/conversations", {
        itemId: id,
        otherUserEmail: match.postedBy.email,
      });
      // Navigate to chat page
      navigate(`/chat/${response.data.id}`);
    } catch (err) {
      console.error("Failed to start conversation:", err);
      alert("Failed to start conversation. Please try again.");
    } finally {
      setContactLoading(false);
    }
  };

  const flagItem = async () => {
    try {
      await api.post(`/items/${id}/flag`);
      alert("Item flagged for review. Thank you.");
    } catch (err) {
      console.error("Failed to flag item:", err);
      const msg = err.response?.data?.message || err.message || "Failed to flag item";
      // If user is not authenticated, suggest login
      if (err.response?.status === 401) {
        if (window.confirm("You need to be logged in to flag items. Go to login?")) {
          navigate('/login');
        }
      } else {
        alert(msg);
      }
    }
  };

  if (loading) {
    return <div className="loading">Loading item details...</div>;
  }

  if (error) {
    return (
      <div className="error-container">
        <p className="error-message">{error}</p>
        <button className="btn btn-secondary" onClick={() => navigate("/items")}>
          Back to Items
        </button>
      </div>
    );
  }

  if (!item) {
    return (
      <div className="error-container">
        <p className="error-message">Item not found</p>
        <button className="btn btn-secondary" onClick={() => navigate("/items")}>
          Back to Items
        </button>
      </div>
    );
  }

  return (
    <div className="item-detail-page">
      <button className="back-button" onClick={() => navigate("/items")}>
        ← Back to Items
      </button>

      <div className="item-detail-container">
        <div className="item-detail-main">
          {item.imageUrl && (
            <div className="item-image-container">
              <img src={item.imageUrl} alt={item.title} className="item-image" />
            </div>
          )}

          <div className="item-info">
            <div className="item-header">
              <h1>{item.title}</h1>
              <div style={{display: 'flex', alignItems: 'center', gap: '8px'}}>
                <span className={`badge badge-${item.type.toLowerCase()}`}>
                  {item.type}
                </span>
                {/* Flag button for users to report this item */}
                <IconButton
                  aria-label="flag-item"
                  title="Flag this item for review"
                  size="small"
                  onClick={flagItem}
                >
                  <FlagIcon fontSize="small" />
                </IconButton>
              </div>
            </div>

            <div className="item-metadata">
              <div className="metadata-item">
                <strong>Category:</strong>
                <span>{item.category || "Not specified"}</span>
              </div>
              <div className="metadata-item">
                <strong>📍 Location:</strong>
                <span>{item.location}</span>
              </div>
              <div className="metadata-item">
                <strong>Status:</strong>
                <span>{item.status || "Active"}</span>
              </div>
            </div>

            <div className="item-description-section">
              <h3>Description</h3>
              <p>{item.description}</p>
            </div>

            {item.tags && item.tags.trim() && (
              <div className="item-tags-section">
                <h4>Tags</h4>
                <div className="tags">
                  {item.tags.split(",").map((tag, idx) => (
                    <span key={idx} className="tag">
                      {tag.trim()}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="item-sidebar">
          <div className="matches-section">
            <h2>Suggested Matches</h2>
            {matches.length === 0 ? (
              <div className="no-matches">
                <p>No matching items found yet.</p>
                <p className="text-muted">
                  Check back later as more items are reported.
                </p>
              </div>
            ) : (
              <div className="matches-list">
                {matches.map((match) => (
                  <div key={match.id} className="match-card">
                    {match.imageUrl && (
                      <div className="match-image">
                        <img src={match.imageUrl} alt={match.title} />
                      </div>
                    )}
                    <div className="match-info">
                      <h4>{match.title}</h4>
                      <p className="match-type">
                        <span className={`badge badge-sm badge-${match.type.toLowerCase()}`}>
                          {match.type}
                        </span>
                        <span className="match-category">{match.category}</span>
                      </p>
                      <p className="match-location">📍 {match.location}</p>
                      <button
                        className="btn btn-sm btn-primary"
                        onClick={() => handleContactOwner(match)}
                        disabled={contactLoading}
                      >
                        {contactLoading ? "Starting..." : "Contact Owner"}
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ItemDetailPage;
