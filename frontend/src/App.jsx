import React, { useState } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import Shell from "./components/Shell";
import Auth from "./pages/Auth";
import Dashboard from "./pages/Dashboard";
import Resumes from "./pages/Resumes";
import Jobs from "./pages/Jobs";
import Applications from "./pages/Applications";
import Matching from "./pages/Matching";
import Assistant from "./pages/Assistant";
import Recruiters from "./pages/Recruiters";
import Emails from "./pages/Emails";
import Approvals from "./pages/Approvals";
import Agents from "./pages/Agents";
import Settings from "./pages/Settings";
import { useAuth } from "./context/AuthContext";
import { Notice } from "./components/UI";

function Protected({children}) {
  const {user,loading}=useAuth();
  if(loading)return <div className="boot-screen"><div className="boot-logo">✦</div><b>JobPilot AI</b><span>Preparing your workspace…</span></div>;
  return user?children:<Navigate to="/login" replace/>;
}
export default function App() {
 const [notice,setNotice]=useState(null);
 const showNotice=n=>{setNotice(n);window.setTimeout(()=>setNotice(null),5000)};
 return <><Notice notice={notice} onClose={()=>setNotice(null)}/><Routes>
  <Route path="/login" element={<Auth/>}/>
  <Route element={<Protected><Shell/></Protected>}>
   <Route index element={<Dashboard setNotice={showNotice}/>}/>
   <Route path="/resumes" element={<Resumes setNotice={showNotice}/>}/>
   <Route path="/jobs" element={<Jobs setNotice={showNotice}/>}/>
   <Route path="/applications" element={<Applications setNotice={showNotice}/>}/>
   <Route path="/matching" element={<Matching setNotice={showNotice}/>}/>
   <Route path="/assistant" element={<Assistant setNotice={showNotice}/>}/>
   <Route path="/recruiters" element={<Recruiters setNotice={showNotice}/>}/>
   <Route path="/emails" element={<Emails setNotice={showNotice}/>}/>
   <Route path="/approvals" element={<Approvals setNotice={showNotice}/>}/>
   <Route path="/agents" element={<Agents setNotice={showNotice}/>}/>
   <Route path="/settings" element={<Settings setNotice={showNotice}/>}/>
  </Route>
  <Route path="*" element={<Navigate to="/" replace/>}/>
 </Routes></>;
}
