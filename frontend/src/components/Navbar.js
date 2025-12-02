import React, { useContext } from "react";
import { Link, useNavigate } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";
import "../styles/Navbar.css";

const Navbar = () => {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  if (!user) {
    return null; // Don't show navbar if user is not logged in
  }

  const isAdmin = user.role === "ROLE_ADMIN";

  return (
    <nav className="navbar">
      <div className="navbar-container">
        {/* Logo/Brand */}
        <Link to="/items" className="navbar-brand">
          <span className="logo-icon">🎓</span>
          <span className="logo-text">Campus LFP</span>
        </Link>

        {/* Navigation Links */}
        <ul className="nav-links">
          <li>
            <Link to="/items" className="nav-link">
              Items
            </Link>
          </li>
          <li>
            <Link to="/chat" className="nav-link">
              <span className="nav-icon">💬</span>
              Messages
            </Link>
          </li>
          <li>
            <Link to="/analytics" className="nav-link">
              <span className="nav-icon">📊</span>
              Analytics
            </Link>
          </li>
          {isAdmin && (
            <li>
              <Link to="/admin" className="nav-link admin-link">
                <span className="nav-icon">⚙️</span>
                Admin
              </Link>
            </li>
          )}
        </ul>

        {/* User Section */}
        <div className="nav-user">
          <div className="user-info">
            <span className="user-name">{user.name}</span>
            {isAdmin && <span className="admin-badge">ADMIN</span>}
          </div>
          <button onClick={handleLogout} className="logout-btn">
            Logout
          </button>
        </div>

        {/* Mobile Menu Toggle */}
        <div className="mobile-menu-icon">
          <input type="checkbox" id="menu-toggle" />
          <label htmlFor="menu-toggle" className="menu-icon">
            <span></span>
            <span></span>
            <span></span>
          </label>
        </div>
      </div>

      {/* Mobile Menu */}
      <input type="checkbox" id="menu-toggle" style={{ display: "none" }} />
      <div className="mobile-menu">
        <Link to="/items" className="mobile-nav-link">
          Items
        </Link>
        <Link to="/chat" className="mobile-nav-link">
          Messages
        </Link>
        {isAdmin && (
          <Link to="/admin" className="mobile-nav-link admin-link">
            Admin
          </Link>
        )}
        <button onClick={handleLogout} className="mobile-logout-btn">
          Logout ({user.name})
        </button>
      </div>
    </nav>
  );
};

export default Navbar;
