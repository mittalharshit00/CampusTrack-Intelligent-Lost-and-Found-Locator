import React, { useEffect, useState, useRef } from "react";
import api from "../services/api";
import "../styles/Admin.css";

// Material UI icons and button
import IconButton from '@mui/material/IconButton';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import FlagIcon from '@mui/icons-material/Flag';

const AdminDashboardPage = () => {
  const [items, setItems] = useState([]);
  const [flaggedItems, setFlaggedItems] = useState([]);
  const [pendingUsers, setPendingUsers] = useState([]);
  const [allUsers, setAllUsers] = useState([]);
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeSection, setActiveSection] = useState("items");
  const [showEngagement, setShowEngagement] = useState(false);
  const [sidebarTop, setSidebarTop] = useState(140);
  const [isDragging, setIsDragging] = useState(false);
  const dragStartY = useRef(0);
  const sidebarRef = useRef(null);
  const [deleteConfirm, setDeleteConfirm] = useState(null);
  const [editingItem, setEditingItem] = useState(null);
  const [editFormData, setEditFormData] = useState({});
  const [editLoading, setEditLoading] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [itemsRes, analyticsRes, flaggedRes] = await Promise.all([
        api.get("/admin/items"),
        api.get("/analytics/summary"),
        api.get("/items/flagged"),
      ]);
      setItems(itemsRes.data);
      setAnalytics(analyticsRes.data);
      setFlaggedItems(flaggedRes.data || []);
      // fetch pending user approvals
      try {
        const pendingRes = await api.get('/admin/users/pending');
        setPendingUsers(pendingRes.data || []);
      } catch (e) {
        // ignore if endpoint not available or no permission
        setPendingUsers([]);
      }
      // fetch all users for management
      try {
        const usersRes = await api.get('/admin/users');
        setAllUsers(usersRes.data || []);
      } catch (e) {
        setAllUsers([]);
      }
      setError(null);
    } catch (err) {
      setError("Failed to load admin data. Please check your permissions.");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const blockUser = async (id) => {
    try {
      await api.post(`/admin/users/${id}/block`);
      setAllUsers((prev) => prev.map(u => u.id === id ? { ...u, blocked: true } : u));
      // also reflect in pending/all lists if present
    } catch (err) {
      console.error('Failed to block user', err);
      setError('Failed to block user');
    }
  };

  const unblockUser = async (id) => {
    try {
      await api.post(`/admin/users/${id}/unblock`);
      setAllUsers((prev) => prev.map(u => u.id === id ? { ...u, blocked: false } : u));
    } catch (err) {
      console.error('Failed to unblock user', err);
      setError('Failed to unblock user');
    }
  };

  // handle dragging the sidebar vertically
  const onSidebarMouseDown = (e) => {
    setIsDragging(true);
    dragStartY.current = e.clientY;
    e.preventDefault();
  };

  useEffect(() => {
    const onMouseMove = (e) => {
      if (!isDragging) return;
      const delta = e.clientY - dragStartY.current;
      dragStartY.current = e.clientY;
      setSidebarTop((t) => Math.max(60, t + delta));
    };
    const onMouseUp = () => setIsDragging(false);
    window.addEventListener("mousemove", onMouseMove);
    window.addEventListener("mouseup", onMouseUp);
    return () => {
      window.removeEventListener("mousemove", onMouseMove);
      window.removeEventListener("mouseup", onMouseUp);
    };
  }, [isDragging]);

  const scrollToSection = (id) => {
    const el = document.getElementById(id);
    if (el) {
      el.scrollIntoView({ behavior: "smooth", block: "start" });
      const short = id.replace(/-section$/, "");
      setActiveSection(short);
    }
  };
  // scrollToSection is currently unused but kept for possible future navigation helpers
  // eslint-disable-next-line no-unused-vars

  const deleteItem = async (id) => {
    try {
      await api.delete(`/admin/items/${id}`);
      setItems(items.filter((i) => i.id !== id));
      setDeleteConfirm(null);
    } catch (err) {
      setError("Failed to delete item. Please try again.");
      console.error(err);
    }
  };

  const flagItem = async (id) => {
    try {
      await api.post(`/items/${id}/flag`);
      alert("Item flagged successfully");
      // Refresh flagged list
      const flaggedRes = await api.get('/items/flagged');
      setFlaggedItems(flaggedRes.data || []);
    } catch (err) {
      setError("Failed to flag item. Please try again.");
      console.error(err);
    }
  };

  const unflagItem = async (id) => {
    try {
      await api.delete(`/items/${id}/flag`);
      // remove from flaggedItems locally
      setFlaggedItems(flaggedItems.filter(i => i.id !== id));
      // Also refresh main items list to reflect flagged change
      const itemsRes = await api.get('/admin/items');
      setItems(itemsRes.data || []);
      alert('Item unflagged');
    } catch (err) {
      console.error('Failed to unflag item', err);
      setError('Failed to unflag item');
    }
  };

  const openEditDialog = (item) => {
    setEditingItem(item);
    setEditFormData({
      title: item.title || "",
      description: item.description || "",
      location: item.location || "",
      category: item.category || "",
      status: item.status || "OPEN",
      matched: item.matched || false,
    });
  };

  const closeEditDialog = () => {
    setEditingItem(null);
    setEditFormData({});
  };

  const handleEditFormChange = (e) => {
    const { name, value, type, checked } = e.target;
    setEditFormData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const saveEditedItem = async () => {
    if (!editingItem) return;

    try {
      setEditLoading(true);
      setError(null);
  console.log("Sending PUT request to /items/" + editingItem.id, editFormData);
      // remember previous matched state so we can adjust analytics locally
      const prevMatched = !!editingItem.matched;

      // api has baseURL set to http://localhost:8080/api so use `/items/...` here
      const response = await api.put(`/items/${editingItem.id}`, editFormData);
      console.log("Update response:", response.data);
      
      // Update the items list with the updated item
      setItems(items.map((item) =>
        item.id === editingItem.id ? response.data : item
      ));
      // If matched state changed, update analytics locally to reflect it immediately
      try {
        const newMatched = !!response.data.matched;
        if (prevMatched !== newMatched) {
          setAnalytics((a) => {
            if (!a) return a;
            const prev = a.matched || 0;
            const next = newMatched ? prev + 1 : Math.max(0, prev - 1);
            return { ...a, matched: next };
          });
        }
      } catch (e) {
        // non-fatal, continue
        console.warn('Failed to update analytics locally', e);
      }
      
      closeEditDialog();
      alert("Item updated successfully!");
    } catch (err) {
      console.error("Error updating item:", err);
      const errorMsg = err.response?.data?.message || 
                       err.response?.data?.error || 
                       err.message || 
                       "Failed to update item. Please try again.";
      setError(errorMsg);
      alert("Failed to update item: " + errorMsg);
    } finally {
      setEditLoading(false);
    }
  };

  if (loading) {
    return <div className="admin-loading">Loading admin dashboard...</div>;
  }

  return (
    <div className="admin-container">
      {/* Left movable sidebar */}
      <div
        ref={sidebarRef}
        className={`admin-sidebar ${isDragging ? "dragging" : ""}`}
        style={{ top: sidebarTop }}
        onMouseDown={onSidebarMouseDown}
      >
        <div className="sidebar-handle" title="Drag to move">☰</div>
        <div
          className={`sidebar-item ${activeSection === "items" ? "active" : ""}`}
          onClick={() => { setActiveSection("items"); scrollToSection('items-section'); }}
        >
          <div>Items</div>
          <div className="item-count">{items.length}</div>
        </div>
        <div
          className={`sidebar-item ${activeSection === "flagged" ? "active" : ""}`}
          onClick={() => { setActiveSection("flagged"); scrollToSection('flagged-section'); }}
        >
          <div>Flagged</div>
          <div className="item-count">{flaggedItems.length}</div>
        </div>
        <div
          className={`sidebar-item ${activeSection === "pending" ? "active" : ""}`}
          onClick={() => { setActiveSection("pending"); scrollToSection('pending-section'); }}
        >
          <div>Pending</div>
          <div className="item-count">{pendingUsers.length}</div>
        </div>
        <div
          className={`sidebar-item ${activeSection === "users" ? "active" : ""}`}
          onClick={() => { setActiveSection("users"); scrollToSection('users-section'); }}
        >
          <div>Users</div>
          <div className="item-count">{allUsers.length}</div>
        </div>
        <div
          className={`sidebar-item ${showEngagement ? "active" : ""}`}
          onClick={() => { setShowEngagement((s) => !s); if (!showEngagement) scrollToSection('engagement-section'); }}
        >
          <div>Engagement</div>
          <div className="item-count">{analytics?.total || 0}</div>
        </div>
      </div>
      <div className="admin-header">
        <h1>Admin Dashboard</h1>
        <button onClick={fetchData} className="refresh-btn">
          Refresh Data
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

  <div className="section-wrapper">
  {/* Analytics Section (fixed below header) */}
  <div id="analytics-section" className="analytics-section">
        <h2>Platform Analytics</h2>
        {analytics ? (
          <div className="analytics-grid">
            <div className="stat-card">
              <div className="stat-value">{analytics.total || 0}</div>
              <div className="stat-label">Total Items</div>
            </div>
            <div className="stat-card">
              <div className="stat-value" style={{ color: "#e74c3c" }}>
                {analytics.open || 0}
              </div>
              <div className="stat-label">Open</div>
            </div>
            <div className="stat-card">
              <div className="stat-value" style={{ color: "#27ae60" }}>
                {analytics.matched || 0}
              </div>
              <div className="stat-label">Matched</div>
            </div>
            <div className="stat-card">
              <div className="stat-value" style={{ color: "#95a5a6" }}>
                {analytics.closed || 0}
              </div>
              <div className="stat-label">Closed</div>
            </div>
          </div>
        ) : (
          <p className="no-data">No analytics data available</p>
        )}
      </div>

      {/* Engagement Pie Chart Section (sticky, toggled via sidebar) */}
      {showEngagement && (
        <div id="engagement-section" className="engagement-section">
        <h2>User Engagement</h2>
        {analytics ? (
          (() => {
            const reported = analytics.total || 0;
            const recovered = (analytics.matched || 0) + (analytics.closed || 0);
            const notRecovered = analytics.open || 0;
            // total is not used directly in the pie (we compute slices from recovered/notRecovered)

            const slices = [
              { label: 'Recovered', value: recovered, color: '#27ae60' },
              { label: 'Not Recovered', value: notRecovered, color: '#e74c3c' },
            ];

            // compute angles for pie
            const sum = slices.reduce((s, it) => s + Math.max(0, it.value), 0) || 1;
            let acc = 0;
            const paths = slices.map((s, i) => {
              const start = acc / sum;
              acc += Math.max(0, s.value);
              const end = acc / sum;
              return { ...s, start, end };
            });

            const cx = 100, cy = 100, r = 80;
            const polar = (t) => {
              const angle = 2 * Math.PI * t - Math.PI / 2;
              return { x: cx + r * Math.cos(angle), y: cy + r * Math.sin(angle) };
            };

            const arcPath = (start, end) => {
              const p1 = polar(start);
              const p2 = polar(end);
              const large = end - start > 0.5 ? 1 : 0;
              return `M ${cx} ${cy} L ${p1.x} ${p1.y} A ${r} ${r} 0 ${large} 1 ${p2.x} ${p2.y} Z`;
            };

            return (
              <div className="engagement-card">
                <div className="engagement-pie-container">
                  <svg viewBox="0 0 200 200" width="220" height="220" aria-hidden>
                    {paths.map((p, idx) => (
                      <path key={idx} d={arcPath(p.start, p.end)} fill={p.color} stroke="#fff" strokeWidth="1" />
                    ))}
                    {/* center label */}
                    <circle cx={cx} cy={cy} r={r - 40} fill="#ffffff" />
                    <text x={cx} y={cy - 6} textAnchor="middle" fontSize="14" fontWeight="700" fill="#2c3e50">{reported}</text>
                    <text x={cx} y={cy + 14} textAnchor="middle" fontSize="12" fill="#95a5a6">Total Reports</text>
                  </svg>

                  <div className="engagement-legend">
                    {paths.map((p, i) => (
                      <div className="legend-row" key={i}>
                        <span className="legend-color" style={{ background: p.color }} />
                        <span className="legend-label">{p.label}</span>
                        <span className="legend-value">{(p.value || 0)}</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            );
          })()
        ) : (
          <p className="no-data">No engagement data available</p>
        )}
        </div>
      )}

      {/* Items Management Section */}
      {activeSection === "items" && (
        <div id="items-section" className="items-section">
          <h2>Manage Items ({items.length})</h2>
        {items.length === 0 ? (
          <p className="no-data">No items found</p>
        ) : (
          <div className="table-responsive">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Title</th>
                  <th>Type</th>
                  <th>Category</th>
                  <th>Status</th>
                  <th>Matched</th>
                  <th>Posted By</th>
                  <th>Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {items.map((item) => (
                  <tr key={item.id} className={`item-row status-${item.status?.toLowerCase()}`}>
                    <td className="id-cell">{item.id}</td>
                    <td className="title-cell">
                      <div className="title-content">{item.title}</div>
                      <div className="description">
                        {item.description?.substring(0, 50)}
                        {item.description?.length > 50 ? "..." : ""}
                      </div>
                    </td>
                    <td>
                      <span
                        className={`type-badge ${item.type?.toLowerCase()}`}
                      >
                        {item.type}
                      </span>
                    </td>
                    <td>{item.category || "N/A"}</td>
                    <td>
                      <span
                        className={`status-badge status-${item.status?.toLowerCase()}`}
                      >
                        {item.status}
                      </span>
                    </td>
                    <td>
                      <span
                        className={`matched-badge ${item.matched ? "matched-yes" : "matched-no"}`}
                      >
                        {item.matched ? "Yes" : "No"}
                      </span>
                    </td>
                    <td>{item.postedBy?.name || item.postedBy?.email || 'N/A'}</td>
                    <td className="actions-cell">
                      <IconButton
                        className="action-btn edit-btn"
                        onClick={() => openEditDialog(item)}
                        title="Edit item"
                        size="small"
                        aria-label={`edit-${item.id}`}
                      >
                        <EditIcon fontSize="small" />
                      </IconButton>

                      <IconButton
                        className="action-btn flag-btn"
                        onClick={() => flagItem(item.id)}
                        title="Flag item"
                        size="small"
                        aria-label={`flag-${item.id}`}
                      >
                        <FlagIcon fontSize="small" />
                      </IconButton>

                      <IconButton
                        className="action-btn delete-btn"
                        onClick={() => setDeleteConfirm(item.id)}
                        title="Delete item"
                        size="small"
                        aria-label={`delete-${item.id}`}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        </div>
      )}

      {/* Flagged Items Section (for moderator review) */}
      {activeSection === "flagged" && (
        <div id="flagged-section" className="flagged-section">
          <h2>Flagged Items ({flaggedItems.length})</h2>
        {flaggedItems.length === 0 ? (
          <p className="no-data">No flagged items</p>
        ) : (
          <div className="flagged-list">
            <table className="admin-table flagged-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Title</th>
                  <th>Posted By</th>
                  <th>Description</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {flaggedItems.map((f) => (
                  <tr key={f.id} className="flag-row">
                    <td>{f.id}</td>
                    <td className="title-cell">{f.title}</td>
                    <td>{f.postedBy?.name || f.postedBy?.email || 'N/A'}</td>
                    <td>{f.description?.substring(0, 60) || ''}</td>
                    <td>
                      <IconButton
                        size="small"
                        title="Unflag"
                        onClick={() => unflagItem(f.id)}
                        aria-label={`unflag-${f.id}`}
                      >
                        <FlagIcon fontSize="small" />
                      </IconButton>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        </div>
      )}

      {activeSection === "users" && (
        <div id="users-section" className="pending-section">{/* reuse pending styles */}
          <h2>All Users ({allUsers.length})</h2>
          {allUsers.length === 0 ? (
            <p className="no-data">No users found</p>
          ) : (
            <div className="table-responsive">
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>Department</th>
                    <th>Contact</th>
                    <th>Approved</th>
                    <th>Blocked</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {allUsers.map((u) => (
                    <tr key={u.id}>
                      <td>{u.id}</td>
                      <td>{u.name}</td>
                      <td>{u.email}</td>
                      <td>{u.role}</td>
                      <td>{u.department || 'N/A'}</td>
                      <td>{u.contactNo || 'N/A'}</td>
                      <td>{u.approved ? 'Yes' : 'No'}</td>
                      <td>{u.blocked ? 'Yes' : 'No'}</td>
                      <td>
                        {u.blocked ? (
                          <button className="btn-approve" onClick={() => unblockUser(u.id)}>Unblock</button>
                        ) : (
                          <button className="btn-ignore" onClick={() => blockUser(u.id)}>Block</button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Pending Accounts Section */}
      {activeSection === "pending" && (
        <div id="pending-section" className="pending-section">
          <h2>Pending Account Approvals ({pendingUsers.length})</h2>
        {pendingUsers.length === 0 ? (
          <p className="no-data">No pending accounts</p>
        ) : (
          <div className="table-responsive">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Department</th>
                  <th>Contact</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {pendingUsers.map((u) => (
                  <tr key={u.id}>
                    <td>{u.id}</td>
                    <td>{u.name}</td>
                    <td>{u.email}</td>
                    <td>{u.department || 'N/A'}</td>
                    <td>{u.contactNo || 'N/A'}</td>
                    <td>
                      <button
                        className="btn-approve"
                        onClick={async () => {
                          try {
                            await api.post(`/admin/users/${u.id}/approve`);
                            // remove locally
                            setPendingUsers((prev) => prev.filter(p => p.id !== u.id));
                            alert('User approved');
                          } catch (err) {
                            console.error(err);
                            alert('Failed to approve user');
                          }
                        }}
                      >
                        Approve
                      </button>
                      <button
                        className="btn-ignore"
                        style={{ marginLeft: '8px' }}
                        onClick={async () => {
                          try {
                            await api.post(`/admin/users/${u.id}/ignore`);
                            // remove locally
                            setPendingUsers((prev) => prev.filter(p => p.id !== u.id));
                            alert('User ignored and removed from pending list');
                          } catch (err) {
                            console.error(err);
                            alert('Failed to ignore user');
                          }
                        }}
                      >
                        Ignore
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
  </div>
  )}
  </div>

      {/* Delete Confirmation Modal */}
      {deleteConfirm && (
        <div className="modal-overlay" onClick={() => setDeleteConfirm(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>Confirm Delete</h3>
            <p>
              Are you sure you want to delete this item? This action cannot be
              undone.
            </p>
            <div className="modal-actions">
              <button
                className="btn-cancel"
                onClick={() => setDeleteConfirm(null)}
              >
                Cancel
              </button>
              <button
                className="btn-delete"
                onClick={() => deleteItem(deleteConfirm)}
              >
                Delete Item
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Edit Item Modal */}
      {editingItem && (
        <div className="modal-overlay" onClick={closeEditDialog}>
          <div className="modal-content edit-modal" onClick={(e) => e.stopPropagation()}>
            <h3>Edit Item Details</h3>
            
            <div className="form-group">
              <label htmlFor="edit-title">Title</label>
              <input
                id="edit-title"
                type="text"
                name="title"
                value={editFormData.title}
                onChange={handleEditFormChange}
                placeholder="Item title"
              />
            </div>

            <div className="form-group">
              <label htmlFor="edit-description">Description</label>
              <textarea
                id="edit-description"
                name="description"
                value={editFormData.description}
                onChange={handleEditFormChange}
                placeholder="Item description"
                rows="4"
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="edit-location">Location</label>
                <input
                  id="edit-location"
                  type="text"
                  name="location"
                  value={editFormData.location}
                  onChange={handleEditFormChange}
                  placeholder="Item location"
                />
              </div>

              <div className="form-group">
                <label htmlFor="edit-category">Category</label>
                <input
                  id="edit-category"
                  type="text"
                  name="category"
                  value={editFormData.category}
                  onChange={handleEditFormChange}
                  placeholder="Item category"
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="edit-status">Status</label>
                <select
                  id="edit-status"
                  name="status"
                  value={editFormData.status}
                  onChange={handleEditFormChange}
                >
                  <option value="OPEN">OPEN</option>
                  <option value="CLOSED">CLOSED</option>
                </select>
              </div>

              <div className="form-group checkbox-group">
                <label htmlFor="edit-matched">
                  <input
                    id="edit-matched"
                    type="checkbox"
                    name="matched"
                    checked={editFormData.matched}
                    onChange={handleEditFormChange}
                  />
                  <span>Mark as Matched</span>
                </label>
              </div>
            </div>

            <div className="modal-actions">
              <button
                className="btn-cancel"
                onClick={closeEditDialog}
                disabled={editLoading}
              >
                Cancel
              </button>
              <button
                className="btn-save"
                onClick={saveEditedItem}
                disabled={editLoading}
              >
                {editLoading ? "Saving..." : "Save Changes"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminDashboardPage;
