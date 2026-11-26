import React, { useEffect, useState } from "react";
import { CheckCircle2, ExternalLink, Link2, LogOut, Save, ShieldCheck, Unplug } from "lucide-react";
import { gmailApi, messageOf, preferencesApi } from "../api";
import { Button, Card, PageHeader } from "../components/UI";
import { useAuth } from "../context/AuthContext";

const defaults = { searchKeywords: "Java Backend Developer fresher India", preferredLocations: "India", remoteOnly: false, autoDiscoveryEnabled: true, minimumMatchScore: 60 };

export default function Settings({ setNotice }) {
  const [gmail, setGmail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [prefs, setPrefs] = useState(defaults);
  const { user, logout } = useAuth();

  useEffect(() => {
    Promise.allSettled([gmailApi.status(), preferencesApi.get()]).then(([g, p]) => {
      if (g.status === "fulfilled") setGmail(g.value.data);
      else setNotice({ type: "error", message: `Gmail status: ${messageOf(g.reason)}` });
      if (p.status === "fulfilled") setPrefs({ ...defaults, ...p.value.data });
      else if (p.reason?.response?.status !== 404) setNotice({ type: "error", message: `Job preferences: ${messageOf(p.reason)}` });
    }).finally(() => setLoading(false));
    const params = new URLSearchParams(window.location.search);
    if (params.has("connected")) setNotice({ type: "success", message: "Gmail connection flow returned to the app. Refresh connection status." });
  }, []);

  const savePrefs = async (e) => {
    e.preventDefault(); setBusy(true);
    try {
      const { data } = await preferencesApi.save({ ...prefs, minimumMatchScore: Number(prefs.minimumMatchScore) });
      setPrefs({ ...defaults, ...data });
      setNotice({ type: "success", message: "Job preferences saved." });
    } catch (e) { setNotice({ type: "error", message: messageOf(e) }); }
    finally { setBusy(false); }
  };

  const refreshGmail = () => gmailApi.status().then(r => setGmail(r.data)).catch(e => setNotice({ type: "error", message: messageOf(e) }));
  const disconnect = async () => {
    setBusy(true);
    try { await gmailApi.disconnect(); setNotice({ type: "success", message: "Gmail disconnected." }); await refreshGmail(); }
    catch (e) { setNotice({ type: "error", message: messageOf(e) }); }
    finally { setBusy(false); }
  };

  return <div className="page-stack">
    <PageHeader eyebrow="YOUR WORKSPACE" title="Settings" subtitle="Manage your profile, job search preferences, integrations, and account access." />
    <Card className="settings-profile"><div className="settings-avatar">{(user?.name || "J").slice(0, 1).toUpperCase()}</div><div><h3>{user?.name || "JobPilot user"}</h3><p>{user?.email}</p><span className="role-pill">{user?.role || "USER"}</span></div></Card>

    <Card className="settings-card">
      <div className="settings-card-head"><div className="settings-icon secure"><ShieldCheck size={19} /></div><div><h3>Job search preferences</h3><p>These preferences are stored in your JobPilot profile and can guide discovery.</p></div></div>
      {loading ? <div className="loading-line">Loading preferences…</div> : <form className="settings-form" onSubmit={savePrefs}>
        <label className="field"><span>Search keywords</span><textarea rows="3" value={prefs.searchKeywords || ""} onChange={e => setPrefs(v => ({ ...v, searchKeywords: e.target.value }))} required maxLength={2000} placeholder="Java Backend Developer, Spring Boot, fresher" /></label>
        <label className="field"><span>Preferred locations</span><input value={prefs.preferredLocations || ""} onChange={e => setPrefs(v => ({ ...v, preferredLocations: e.target.value }))} maxLength={1000} placeholder="India, Bangalore, Remote" /></label>
        <label className="field"><span>Minimum match score: <b>{prefs.minimumMatchScore}%</b></span><input type="range" min="0" max="100" value={prefs.minimumMatchScore} onChange={e => setPrefs(v => ({ ...v, minimumMatchScore: Number(e.target.value) }))} /></label>
        <label className="settings-check"><input type="checkbox" checked={!!prefs.remoteOnly} onChange={e => setPrefs(v => ({ ...v, remoteOnly: e.target.checked }))} /><span>Prefer remote-only roles</span></label>
        <label className="settings-check"><input type="checkbox" checked={!!prefs.autoDiscoveryEnabled} onChange={e => setPrefs(v => ({ ...v, autoDiscoveryEnabled: e.target.checked }))} /><span>Enable automatic job discovery preference</span></label>
        <Button type="submit" loading={busy}><Save size={15} /> Save preferences</Button>
      </form>}
    </Card>

    <Card className="settings-card"><div className="settings-card-head"><div className="settings-icon google">G</div><div><h3>Google / Gmail integration</h3><p>Connect your Google account to enable email sending with the approval workflow.</p></div><span className={`connection-state ${gmail?.connected ? "on" : ""}`}><i />{loading ? "Checking…" : gmail?.connected ? "Connected" : "Not connected"}</span></div>
      {gmail?.connected ? <div className="integration-details"><div><small>Connected account</small><b>{gmail.gmailAddress}</b></div><div><small>Connected since</small><b>{gmail.connectedAt ? new Date(gmail.connectedAt).toLocaleString() : "—"}</b></div><div><small>Granted scopes</small><b>{gmail.scopes || "—"}</b></div><Button variant="danger" size="sm" onClick={disconnect} loading={busy}><Unplug size={14} /> Disconnect Gmail</Button></div> : <div className="integration-connect"><p>You'll be redirected to Google to authorize JobPilot. Make sure your Google account email matches the email on your JobPilot account.</p><a href={gmailApi.connectUrl}><Button><Link2 size={15} /> Connect Google account <ExternalLink size={14} /></Button></a></div>}
    </Card>

    <Card className="settings-card"><div className="settings-card-head"><div className="settings-icon secure"><ShieldCheck size={19} /></div><div><h3>Security & privacy</h3><p>Your session uses a JWT token. Sign out when using a shared device.</p></div></div><div className="settings-bottom"><span><CheckCircle2 size={16} /> Authenticated session active</span><Button variant="secondary" size="sm" onClick={logout}><LogOut size={14} /> Sign out</Button></div></Card>
    <div className="settings-footnote">API endpoint: <code>{import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"}</code> · Change it in the frontend <code>.env</code> file.</div>
  </div>;
}
