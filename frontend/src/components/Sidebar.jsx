import React from "react";

const menuGroups = [
  [
    { icon: "🏠", label: "Home" },
    { icon: "🍳", label: "Recipe Generator", active: true },
    { icon: "⭐", label: "Favorites" },
    { icon: "🕒", label: "Recent Searches" },
  ],
  [{ icon: "🤖", label: "AI Suggestions" }],
  [
    { icon: "⚙️", label: "Settings" },
    { icon: "❓", label: "Help / How to Use" },
    { icon: "ℹ️", label: "About" },
  ],
];

function Sidebar({ collapsed, onToggleCollapse, mobileOpen, onCloseMobile }) {
  return (
    <aside className={`sidebar ${collapsed ? "collapsed" : ""} ${mobileOpen ? "mobile-open" : ""}`}>
      <div className="sidebar-top">
        <div className="brand-block">
          <span className="brand-icon">🍽️</span>
          {!collapsed && <h2>AI Recipe Generator</h2>}
        </div>
        <button type="button" className="sidebar-toggle" onClick={onToggleCollapse}>
          ☰
        </button>
      </div>

      <div className="sidebar-divider" />

      <nav className="sidebar-nav">
        {menuGroups.map((group, groupIndex) => (
          <div key={groupIndex} className="sidebar-group">
            {group.map((item) => (
              <button
                type="button"
                key={item.label}
                className={`sidebar-item ${item.active ? "active" : ""}`}
                onClick={onCloseMobile}
              >
                <span className="sidebar-item-icon">{item.icon}</span>
                {!collapsed && <span className="sidebar-item-label">{item.label}</span>}
                {item.active && !collapsed && <span className="active-dot" />}
              </button>
            ))}
            {groupIndex < menuGroups.length - 1 && <div className="sidebar-divider" />}
          </div>
        ))}
      </nav>

      <div className="sidebar-user">
        <div className="sidebar-avatar">U</div>
        {!collapsed && (
          <div className="sidebar-user-info">
            <strong>User</strong>
            <span>user@example.com</span>
          </div>
        )}
      </div>
    </aside>
  );
}

export default Sidebar;
