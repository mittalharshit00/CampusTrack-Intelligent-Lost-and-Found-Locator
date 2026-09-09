import React, { useEffect, useState } from "react";
import api from "../services/api";
import { useNavigate } from "react-router-dom";
import "../styles/ItemsList.css";

const ItemsListPage = () => {
  const [items, setItems] = useState([]);
  const [type, setType] = useState("");
  const [category, setCategory] = useState("");
  const [location, setLocation] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchItems = async () => {
      setLoading(true);
      setError(null);
      try {
        let params = {};
        if (type) params.type = type;
        if (category) params.category = category;
        if (location) params.location = location;
        const response = await api.get("/items", { params });
        setItems(response.data);
      } catch (err) {
        setError("Failed to fetch items. Please try again.");
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchItems();
  }, [type, category, location]);

  return (
    <div className="items-page">
      <div className="items-header">
        <div>
          <h1>Lost & Found Items</h1>
          <p>Help your campus community find their belongings</p>
        </div>
        <button
          className="btn btn-primary btn-lg"
          onClick={() => navigate("/items/new")}
        >
          + Report Item
        </button>
      </div>

      <div className="items-filters">
        <select
          value={type}
          onChange={(e) => setType(e.target.value)}
          className="filter-input"
        >
          <option value="">All Items</option>
          <option value="LOST">Lost Items</option>
          <option value="FOUND">Found Items</option>
        </select>

        <input
          type="text"
          placeholder="Search by category..."
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          className="filter-input"
        />

        <input
          type="text"
          placeholder="Search by location..."
          value={location}
          onChange={(e) => setLocation(e.target.value)}
          className="filter-input"
        />
      </div>

      {error && <div className="error-message">{error}</div>}

      {loading ? (
        <div className="loading">Loading items...</div>
      ) : items.length === 0 ? (
        <div className="empty-state">
          <p>No items found. Try adjusting your filters or</p>
          <button className="btn btn-secondary" onClick={() => navigate("/items/new")}>
            report one now
          </button>
        </div>
      ) : (
        <div className="items-grid">
          {items.map((item) => (
            <div key={item.id} className="item-card">
              {item.imageUrl && (
                <div className="item-image">
                  <img src={item.imageUrl} alt={item.title} />
                </div>
              )}
              <div className="item-content">
                <div className="item-type-badge">
                  <span className={`badge badge-${item.type.toLowerCase()}`}>
                    {item.type}
                  </span>
                </div>
                <h3>{item.title}</h3>
                <p className="item-meta">
                  <span className="category">{item.category}</span>
                  <span className="location">📍 {item.location}</span>
                </p>
                <p className="item-description">{item.description}</p>
                {item.tags && (
                  <div className="item-tags">
                    {item.tags.split(",").map((tag, idx) => (
                      <span key={idx} className="tag">
                        {tag.trim()}
                      </span>
                    ))}
                  </div>
                )}
                <button
                  className="btn btn-secondary"
                  onClick={() => navigate(`/items/${item.id}`)}
                >
                  View Details
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ItemsListPage;
