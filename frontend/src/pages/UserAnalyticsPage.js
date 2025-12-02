import React, { useEffect, useState } from "react";
import api from "../services/api";
import "../styles/Admin.css";

const UserAnalyticsPage = () => {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        setLoading(true);
        // Fetch platform summary first and show it even if user endpoint fails
        try {
          const plat = await api.get('/analytics/summary');
          setAnalytics(plat.data);
        } catch (e) {
          console.error('Failed to load platform analytics', e);
          setAnalytics(null);
        }

        // Fetch user-specific summary but do not block platform analytics on failure
        try {
          await api.get('/analytics/user');
          // we don't render per-user engagement here, but requesting it
          // ensures any permission issues are logged without blocking platform data
        } catch (e) {
          console.debug('User analytics not available or permission denied', e?.response?.status);
        }
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, []);

  if (loading) return <div className="admin-loading">Loading analytics...</div>;

  // per-user engagement removed from this view; we only show platform analytics

  const cx = 100, cy = 100, r = 80;
  const polar = (t) => {
    const angle = 2 * Math.PI * t - Math.PI / 2;
    return { x: cx + r * Math.cos(angle), y: cy + r * Math.sin(angle) };
  };
  

  return (
    <div className="section-wrapper">
      <div className="analytics-section">
        <h2>Platform Analytics</h2>
        {analytics ? (
          <div className="analytics-grid">
            <div className="stat-card">
              <div className="stat-value">{analytics.total || 0}</div>
              <div className="stat-label">Total Items</div>
            </div>
            <div className="stat-card">
              <div className="stat-value" style={{ color: "#e74c3c" }}>{analytics.open || 0}</div>
              <div className="stat-label">Open</div>
            </div>
            <div className="stat-card">
              <div className="stat-value" style={{ color: "#27ae60" }}>{analytics.matched || 0}</div>
              <div className="stat-label">Matched</div>
            </div>
            <div className="stat-card">
              <div className="stat-value" style={{ color: "#95a5a6" }}>{analytics.closed || 0}</div>
              <div className="stat-label">Closed</div>
            </div>
          </div>
        ) : (
          <p className="no-data">No analytics available</p>
        )}
      </div>
      {/* Platform Engagement (same as admin panel) */}
      <div className="engagement-section">
        <h2>Platform Engagement</h2>
        {analytics ? (
          (() => {
            const reportedPlat = analytics.total || 0;
            const recoveredPlat = (analytics.matched || 0) + (analytics.closed || 0);
            const notRecoveredPlat = analytics.open || 0;

            const slicesPlat = [
              { label: 'Recovered', value: recoveredPlat, color: '#27ae60' },
              { label: 'Not Recovered', value: notRecoveredPlat, color: '#e74c3c' },
            ];

            const sumPlat = slicesPlat.reduce((s, it) => s + Math.max(0, it.value), 0) || 1;
            let accPlat = 0;
            const pathsPlat = slicesPlat.map((s) => {
              const start = accPlat / sumPlat;
              accPlat += Math.max(0, s.value);
              const end = accPlat / sumPlat;
              return { ...s, start, end };
            });

            const arcPathPlat = (start, end) => {
              const p1 = polar(start);
              const p2 = polar(end);
              const large = end - start > 0.5 ? 1 : 0;
              return `M ${cx} ${cy} L ${p1.x} ${p1.y} A ${r} ${r} 0 ${large} 1 ${p2.x} ${p2.y} Z`;
            };

            return (
              <div className="engagement-card">
                <div className="engagement-pie-container">
                  <svg viewBox="0 0 200 200" width="220" height="220" aria-hidden>
                    {pathsPlat.map((p, idx) => (
                      <path key={idx} d={arcPathPlat(p.start, p.end)} fill={p.color} stroke="#fff" strokeWidth="1" />
                    ))}
                    <circle cx={cx} cy={cy} r={r - 40} fill="#ffffff" />
                    <text x={cx} y={cy - 6} textAnchor="middle" fontSize="14" fontWeight="700" fill="#2c3e50">{reportedPlat}</text>
                    <text x={cx} y={cy + 14} textAnchor="middle" fontSize="12" fill="#95a5a6">Total Reports</text>
                  </svg>

                  <div className="engagement-legend">
                    {pathsPlat.map((p, i) => (
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

      {/* per-user engagement removed - only platform analytics and engagement are shown */}
    </div>
  );
};

export default UserAnalyticsPage;
