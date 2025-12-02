import React, { useState } from "react";
import api from "../services/api";
import { useNavigate } from "react-router-dom";
import "../styles/Form.css";

const NewItemPage = () => {
  const [form, setForm] = useState({
    title: "",
    description: "",
    type: "LOST",
    category: "",
    tags: "",
    location: "",
    color: "",
  });
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    // Validate file type (JPG/JPEG only)
    const validTypes = ["image/jpeg", "image/jpg"];
    if (!validTypes.includes(file.type)) {
      setError("Only JPG/JPEG format is allowed. Please select a JPG or JPEG image.");
      e.target.value = ""; // Reset input
      setImageFile(null);
      setImagePreview(null);
      return;
    }

    // Validate file size (max 5MB)
    const maxSize = 5 * 1024 * 1024; // 5MB
    if (file.size > maxSize) {
      setError("Image size must be less than 5MB.");
      e.target.value = ""; // Reset input
      setImageFile(null);
      setImagePreview(null);
      return;
    }

    setError(null);
    setImageFile(file);

    // Create preview
    const reader = new FileReader();
    reader.onloadend = () => {
      setImagePreview(reader.result);
    };
    reader.readAsDataURL(file);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    // Validate form
    if (!form.title.trim()) {
      setError("Title is required");
      return;
    }
    if (!form.description.trim()) {
      setError("Description is required");
      return;
    }
    if (!form.location.trim()) {
      setError("Location is required");
      return;
    }

    // Image is required
    if (!imageFile) {
      setError("Image is required. Please upload a JPG/JPEG image.");
      return;
    }

    setLoading(true);

    try {
      // Create FormData to handle file upload
      const formData = new FormData();
      formData.append("title", form.title);
      formData.append("description", form.description);
      formData.append("type", form.type);
      formData.append("category", form.category);
      formData.append("tags", form.tags);
      formData.append("location", form.location);
      formData.append("color", form.color);

      // Image is required (we validated above)
      formData.append("image", imageFile);

      await api.post("/items", formData);

      setSuccess("Item reported successfully! Redirecting...");
      setTimeout(() => {
        navigate("/items");
      }, 1500);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to create item. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="form-page">
      <div className="form-container">
        <h1>Report a Lost or Found Item</h1>
        <p className="form-subtitle">
          Help your campus community by reporting lost or found items
        </p>

        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        <form onSubmit={handleSubmit} className="item-form">
          <div className="form-group">
            <label htmlFor="type">Item Type *</label>
            <select
              id="type"
              name="type"
              value={form.type}
              onChange={handleChange}
              className="form-input"
            >
              <option value="LOST">Lost Item</option>
              <option value="FOUND">Found Item</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="title">Title *</label>
            <input
              id="title"
              type="text"
              name="title"
              placeholder="e.g., Blue Backpack, Black Wallet"
              value={form.title}
              onChange={handleChange}
              className="form-input"
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="description">Description *</label>
            <textarea
              id="description"
              name="description"
              placeholder="Provide details about the item (color, brand, distinguishing features, etc.)"
              value={form.description}
              onChange={handleChange}
              className="form-input form-textarea"
              required
              disabled={loading}
              rows="4"
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="category">Category</label>
              <input
                id="category"
                type="text"
                name="category"
                placeholder="e.g., Electronics, Accessories"
                value={form.category}
                onChange={handleChange}
                className="form-input"
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="location">Location *</label>
              <input
                id="location"
                type="text"
                name="location"
                placeholder="e.g., Library, Main Gate, Cafeteria"
                value={form.location}
                onChange={handleChange}
                className="form-input"
                required
                disabled={loading}
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="tags">Tags</label>
            <input
              id="tags"
              type="text"
              name="tags"
              placeholder="Add tags separated by commas (e.g., urgent, reward)"
              value={form.tags}
              onChange={handleChange}
              className="form-input"
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="color">Color</label>
            <input
              id="color"
              type="text"
              name="color"
              placeholder="e.g., Blue, Red, Black, etc."
              value={form.color}
              onChange={handleChange}
              className="form-input"
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="image">Image * (JPG/JPEG only)</label>
            <input
              id="image"
              type="file"
              name="image"
              accept="image/jpeg,image/jpg"
              onChange={handleImageChange}
              className="form-input"
              disabled={loading}
              required
            />
            <p className="form-helper-text">
              Accepted formats: JPG, JPEG | Maximum size: 5MB
            </p>
            {imagePreview && (
              <div className="image-preview-container">
                <img
                  src={imagePreview}
                  alt="Preview"
                  className="image-preview"
                />
                <p className="image-preview-info">Preview: {imageFile?.name}</p>
              </div>
            )}
          </div>

          <div className="form-actions">
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? "Reporting..." : "Report Item"}
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => navigate("/items")}
              disabled={loading}
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default NewItemPage;
