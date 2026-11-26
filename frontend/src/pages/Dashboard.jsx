import React, { useEffect, useState } from "react";
import { ArrowUpRight, BriefcaseBusiness, CalendarClock, CheckCircle2, FileText, Plus, Sparkles, Target, TrendingUp, Users } from "lucide-react";
import { Link } from "react-router-dom";
import { applicationApi, jobApi, resumeApi, recruiterApi, gmailApi, unwrapPage, messageOf } from "../api";
import { Button, Card, Empty, Status, dateText } from "../components/UI";
import { ResponsiveContainer, AreaChart, Area, XAxis, Tooltip } from "recharts";


export default function Dashboard({ setNotice }) {
  const [stats,setStats]=useState(null),[apps,setApps]=useState([]),[jobs,setJobs]=useState([]),[resumes,setResumes]=useState([]),[gmail,setGmail]=useState(null),[loading,setLoading]=useState(true);
  useEffect(()=>{(async()=>{try{
    const results=await Promise.allSettled([applicationApi.stats(),applicationApi.list({page:0,size:5}),jobApi.list({page:0,size:5}),resumeApi.list(),gmailApi.status()]);
    if(results[0].status==="fulfilled")setStats(results[0].value.data);
    if(results[1].status==="fulfilled")setApps(unwrapPage(results[1].value.data));
    if(results[2].status==="fulfilled")setJobs(unwrapPage(results[2].value.data));
    if(results[3].status==="fulfilled")setResumes(results[3].value.data||[]);
    if(results[4].status==="fulfilled")setGmail(results[4].value.data);
  }finally{setLoading(false)}})()},[]);
  const chartData=[{day:"Saved",value:stats?.saved??0},{day:"Applied",value:stats?.applied??0},{day:"Interview",value:stats?.interviews??0},{day:"Offers",value:stats?.offers??0},{day:"Rejected",value:stats?.rejected??0},{day:"Withdrawn",value:stats?.withdrawn??0}];
  const cards=[
    {label:"Applications",value:stats?.totalApplications??apps.length,sub:`${stats?.applied??0} submitted`,icon:BriefcaseBusiness,color:"blue"},
    {label:"Interviews",value:stats?.interviews??0,sub:"Keep the momentum",icon:CalendarClock,color:"violet"},
    {label:"Offers",value:stats?.offers??0,sub:"Your next chapter",icon:TrendingUp,color:"green"},
    {label:"Saved resumes",value:resumes.length,sub:"Ready to share",icon:FileText,color:"amber"},
  ];
  return <div className="page-stack">
    <div className="welcome-row"><div><div className="eyebrow">MONDAY MOTIVATION, EVERY DAY</div><h1>Your career cockpit.</h1><p>Small, consistent moves add up. Here's where things stand.</p></div><Link to="/jobs"><Button><Plus size={16}/> Explore jobs</Button></Link></div>
    <div className="stats-grid">{cards.map(({label,value,sub,icon:Icon,color})=><Card className="stat-card" key={label}><div className={`stat-icon ${color}`}><Icon size={19}/></div><div className="stat-label">{label}</div><div className="stat-value">{loading?"—":value}</div><div className="stat-sub">{sub}</div></Card>)}</div>
    <div className="dashboard-grid">
      <Card className="chart-card"><div className="card-heading"><div><h3>Application pipeline</h3><p>Live breakdown from your application tracker</p></div><span className="period-tag">LIVE STATS</span></div><div className="chart-wrap"><ResponsiveContainer width="100%" height="100%"><AreaChart data={chartData}><defs><linearGradient id="activityFill" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#6d7cff" stopOpacity={0.23}/><stop offset="100%" stopColor="#6d7cff" stopOpacity={0}/></linearGradient></defs><XAxis dataKey="day" axisLine={false} tickLine={false} tick={{fill:"#8b93a7",fontSize:11}}/><Tooltip contentStyle={{borderRadius:12,border:"1px solid #e8ebf2",boxShadow:"0 8px 24px #1a244010"}}/><Area type="monotone" dataKey="value" stroke="#6574ee" strokeWidth={2.5} fill="url(#activityFill)" /></AreaChart></ResponsiveContainer></div><div className="chart-foot"><span><i className="legend-dot"/> Applications by status</span><b>Keep moving forward.</b></div></Card>
      <Card className="quick-card"><div className="card-heading"><div><h3>Quick actions</h3><p>Pick up where you need to</p></div><Sparkles size={19} className="muted-icon"/></div>
        <Link className="quick-action" to="/resumes"><span className="quick-icon q-blue"><FileText size={17}/></span><div><b>Upload a resume</b><small>Keep your profile current</small></div><ArrowUpRight size={16}/></Link>
        <Link className="quick-action" to="/assistant"><span className="quick-icon q-purple"><Sparkles size={17}/></span><div><b>Ask your AI assistant</b><small>Get tailored career guidance</small></div><ArrowUpRight size={16}/></Link>
        <Link className="quick-action" to="/recruiters"><span className="quick-icon q-orange"><Users size={17}/></span><div><b>Find recruiters</b><small>Build your network</small></div><ArrowUpRight size={16}/></Link>
        <div className={`integration-mini ${gmail?.connected?"connected":""}`}><span className="gmail-g">G</span><div><b>{gmail?.connected?"Gmail connected":"Connect Gmail"}</b><small>{gmail?.gmailAddress || "Enable email outreach"}</small></div>{gmail?.connected?<CheckCircle2 size={17}/>:<Link to="/settings">Connect</Link>}</div>
      </Card>
    </div>
    <div className="dashboard-grid lower-grid">
      <Card><div className="card-heading"><div><h3>Recent applications</h3><p>Track every opportunity</p></div><Link to="/applications" className="text-link">View all <ArrowUpRight size={14}/></Link></div>
        {apps.length? <div className="table-list">{apps.slice(0,5).map(a=><div className="table-row" key={a.id}><div className="company-logo">{(a.companyName||"C").slice(0,1).toUpperCase()}</div><div className="row-main"><b>{a.jobTitle||"Untitled role"}</b><small>{a.companyName||"Company"} · {a.location||"Location not set"}</small></div><Status value={a.status}/></div>)}</div>:<Empty title="No applications yet" text="Save a job and start building your pipeline."/>}
      </Card>
      <Card><div className="card-heading"><div><h3>Fresh opportunities</h3><p>Recently added to your workspace</p></div><Link to="/jobs" className="text-link">Browse jobs <ArrowUpRight size={14}/></Link></div>
        {jobs.length?<div className="table-list">{jobs.slice(0,5).map(j=><div className="table-row" key={j.id}><div className="company-logo">{(j.companyName||"J").slice(0,1).toUpperCase()}</div><div className="row-main"><b>{j.title}</b><small>{j.companyName} · {j.location||"Remote / not specified"}</small></div><a className="external-mini" href={j.jobUrl} target="_blank" rel="noreferrer"><ArrowUpRight size={15}/></a></div>)}</div>:<Empty title="No jobs in your feed" text="Discover new jobs to get started."/>}
      </Card>
    </div>
  </div>;
}
