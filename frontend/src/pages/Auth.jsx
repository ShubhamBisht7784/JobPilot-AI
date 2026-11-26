import React, { useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { ArrowRight, BriefcaseBusiness, Eye, EyeOff, Sparkles } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { Button, Input, Notice } from "../components/UI";
import { messageOf } from "../api";

export default function Auth() {
  const { user, login, register } = useAuth();
  const [mode, setMode] = useState("login");
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [show, setShow] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();
  if (user) return <Navigate to="/" replace/>;

  const submit = async (e) => {
    e.preventDefault(); setError(""); setBusy(true);
    try {
      if (mode === "register") await register(name.trim(), email.trim(), password);
      else await login(email.trim(), password);
      navigate("/", { replace: true });
    } catch (err) { setError(messageOf(err)); }
    finally { setBusy(false); }
  };

  return <div className="auth-screen">
    <div className="auth-art"><div className="auth-art-grid"/><div className="auth-logo"><div className="brand-mark"><Sparkles size={19}/></div><b>jobpilot<span>.ai</span></b></div>
      <div className="auth-pitch"><div className="eyebrow light">YOUR NEXT CHAPTER STARTS HERE</div><h1>Make your next<br/><em>move</em> your best one.</h1><p>One intelligent workspace to discover opportunities, sharpen your story, and move your career forward.</p>
        <div className="auth-feature"><span><BriefcaseBusiness size={17}/></span><div><b>Find the right opportunities</b><small>Bring your job search into focus.</small></div></div>
        <div className="auth-feature"><span><Sparkles size={17}/></span><div><b>Let AI do the heavy lifting</b><small>Turn preparation into momentum.</small></div></div>
      </div><div className="auth-art-foot">BUILT FOR WHAT'S NEXT <span>✳</span></div>
    </div>
    <div className="auth-panel"><div className="auth-mobile-logo"><div className="brand-mark"><Sparkles size={18}/></div><b>jobpilot<span>.ai</span></b></div><div className="auth-form-wrap">
      <div className="eyebrow">{mode === "login" ? "WELCOME BACK" : "GET STARTED"}</div><h2>{mode === "login" ? "Sign in to JobPilot" : "Create your account"}</h2><p className="auth-sub">{mode === "login" ? "Pick up right where your career journey left off." : "Build a smarter job search, starting today."}</p>
      {error && <div className="form-error">{error}</div>}
      <form onSubmit={submit} className="auth-form">
        {mode === "register" && <Input label="Full name" placeholder="Your name" value={name} onChange={e=>setName(e.target.value)} required/>}
        <Input label="Email address" type="email" placeholder="you@example.com" value={email} onChange={e=>setEmail(e.target.value)} required/>
        <label className="field"><span>Password</span><div className="password-wrap"><input type={show ? "text" : "password"} placeholder="At least 8 characters" value={password} onChange={e=>setPassword(e.target.value)} minLength={mode==="register"?8:1} required/><button type="button" onClick={()=>setShow(!show)}>{show?<EyeOff size={17}/>:<Eye size={17}/>}</button></div></label>
        <Button loading={busy} className="auth-submit">{mode==="login"?"Sign in":"Create account"} <ArrowRight size={16}/></Button>
      </form>
      <div className="auth-switch">{mode==="login"?"New to JobPilot?":"Already have an account?"} <button onClick={()=>{setMode(mode==="login"?"register":"login");setError("");}}>{mode==="login"?"Create an account":"Sign in"}</button></div>
      <div className="auth-legal">By continuing, you agree to use JobPilot responsibly and keep your account credentials secure.</div>
    </div><div className="auth-panel-foot">© 2026 JobPilot AI <span>Made for your next move</span></div></div>
  </div>;
}
