import React from "react";
import { LoaderCircle, Search, X } from "lucide-react";

export function Button({ children, variant = "primary", size = "md", loading = false, className = "", ...props }) {
  return <button className={`btn btn-${variant} btn-${size} ${className}`} disabled={loading || props.disabled} {...props}>
    {loading && <LoaderCircle size={15} className="spin" />}{children}
  </button>;
}
export function Input({ label, className = "", ...props }) {
  return <label className={`field ${className}`}>{label && <span>{label}</span>}<input {...props} /></label>;
}
export function Select({ label, children, className = "", ...props }) {
  return <label className={`field ${className}`}>{label && <span>{label}</span>}<select {...props}>{children}</select></label>;
}
export function Textarea({ label, className = "", ...props }) {
  return <label className={`field ${className}`}>{label && <span>{label}</span>}<textarea {...props} /></label>;
}
export function Card({ children, className = "" }) { return <section className={`card ${className}`}>{children}</section>; }
export function PageHeader({ eyebrow, title, subtitle, action }) {
  return <div className="page-header"><div><div className="eyebrow">{eyebrow}</div><h1>{title}</h1>{subtitle && <p>{subtitle}</p>}</div>{action && <div className="page-action">{action}</div>}</div>;
}
export function Empty({ title = "Nothing here yet", text = "Your data will appear here once you add it." }) {
  return <div className="empty"><div className="empty-icon">✦</div><b>{title}</b><span>{text}</span></div>;
}
export function Status({ value }) {
  const v = String(value || "UNKNOWN").toLowerCase().replaceAll("_", " ");
  return <span className={`status status-${v.replaceAll(" ", "-")}`}>{v}</span>;
}
export function SearchBox({ value, onChange, placeholder = "Search..." }) {
  return <div className="searchbox"><Search size={16}/><input value={value} onChange={e => onChange(e.target.value)} placeholder={placeholder}/>{value && <button onClick={() => onChange("")}><X size={14}/></button>}</div>;
}
export function Modal({ title, onClose, children, wide = false }) {
  return <div className="modal-backdrop" onMouseDown={e => e.target === e.currentTarget && onClose()}><div className={`modal ${wide ? "modal-wide" : ""}`}><div className="modal-head"><h3>{title}</h3><button className="icon-btn" onClick={onClose}><X size={18}/></button></div>{children}</div></div>;
}
export function Notice({ notice, onClose }) {
  if (!notice) return null;
  return <div className={`notice notice-${notice.type || "info"}`}><span>{notice.message}</span><button onClick={onClose}><X size={15}/></button></div>;
}
export const unwrapPage = (data) => Array.isArray(data) ? data : data?.content || data?.items || [];
export const dateText = (value) => value ? new Date(value).toLocaleDateString(undefined, { month: "short", day: "numeric", year: "numeric" }) : "—";
