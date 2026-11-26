import React, { useState } from "react";
import { NavLink, Outlet, useLocation } from "react-router-dom";
import {
  Activity, BriefcaseBusiness, BrainCircuit, CheckCheck, ChevronLeft, ChevronRight,
  CircleHelp, FileText, LayoutDashboard, LogOut, Mail, Menu, Search, Settings,
  ShieldCheck, Sparkles, Users, X, Zap
} from "lucide-react";
import { useAuth } from "../context/AuthContext";

const nav = [
  { label: "Workspace", items: [
    ["Overview", "/", LayoutDashboard],
    ["My resumes", "/resumes", FileText],
    ["Discover jobs", "/jobs", BriefcaseBusiness],
    ["Applications", "/applications", Activity],
    ["Skill matching", "/matching", CheckCheck],
  ]},
  { label: "AI & outreach", items: [
    ["AI assistant", "/assistant", BrainCircuit],
    ["Recruiters", "/recruiters", Users],
    ["Email drafts", "/emails", Mail],
    ["Approvals", "/approvals", ShieldCheck],
    ["Agent tasks", "/agents", Zap],
  ]},
];
const titles = Object.fromEntries(nav.flatMap(g => g.items.map(([label, path]) => [path, label])));

export default function Shell() {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const { user, logout } = useAuth();
  const location = useLocation();
  const title = titles[location.pathname] || "Workspace";
  const initials = (user?.name || user?.email || "JP").split(" ").map(s => s[0]).join("").slice(0, 2).toUpperCase();

  return <div className={`app-shell ${collapsed ? "sidebar-collapsed" : ""}`}>
    <aside className={`sidebar ${mobileOpen ? "mobile-open" : ""}`}>
      <div className="brand"><div className="brand-mark"><Sparkles size={19}/></div>{!collapsed && <div><b>jobpilot<span>.ai</span></b><small>CAREER WORKSPACE</small></div>}<button className="mobile-close" onClick={() => setMobileOpen(false)}><X size={18}/></button></div>
      <div className="workspace-switch"><div className="workspace-avatar">J</div>{!collapsed && <div><b>Personal workspace</b><small>Free workspace</small></div>}<ChevronRight size={15}/></div>
      <nav className="nav-list">
        {nav.map(group => <div className="nav-group" key={group.label}><div className="nav-label">{!collapsed && group.label}</div>
          {group.items.map(([label, path, Icon]) => <NavLink end={path === "/"} to={path} key={path} title={collapsed ? label : undefined} onClick={() => setMobileOpen(false)} className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}><Icon size={18}/>{!collapsed && <span>{label}</span>}{!collapsed && path === "/approvals" && <i className="nav-dot"/>}</NavLink>)}
        </div>)}
      </nav>
      <div className="sidebar-bottom">
        <NavLink to="/settings" className={({isActive}) => `nav-link ${isActive ? "active" : ""}`}><Settings size={18}/>{!collapsed && <span>Settings</span>}</NavLink>
        <div className="profile-mini"><div className="avatar">{initials}</div>{!collapsed && <div className="profile-copy"><b>{user?.name || "JobPilot user"}</b><small>{user?.email || ""}</small></div>}<button className="logout-btn" title="Sign out" onClick={logout}><LogOut size={16}/></button></div>
      </div>
      <button className="collapse-btn" onClick={() => setCollapsed(!collapsed)}>{collapsed ? <ChevronRight size={15}/> : <ChevronLeft size={15}/>}</button>
    </aside>
    <div className="main-area">
      <header className="topbar"><button className="mobile-menu" onClick={() => setMobileOpen(true)}><Menu size={20}/></button><div className="breadcrumbs"><span>JobPilot</span><b>/</b><strong>{title}</strong></div><div className="topbar-right"><div className="online-pill"><i/> All systems ready</div><div className="avatar top-avatar">{initials}</div></div></header>
      <main className="page-content"><Outlet/></main>
      <footer className="footer"><span>© 2026 JobPilot AI</span><span><CircleHelp size={13}/> Your career, in motion.</span></footer>
    </div>
  </div>;
}
