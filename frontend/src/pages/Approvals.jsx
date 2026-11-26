import React, { useEffect, useState } from "react";
import { Check, Clock3, RefreshCw, ShieldCheck, X } from "lucide-react";
import { approvalApi, messageOf, unwrapPage } from "../api";
import { Button, Card, Empty, PageHeader, Status, dateText } from "../components/UI";

export default function Approvals({setNotice}) {
 const [items,setItems]=useState([]),[filter,setFilter]=useState("ALL"),[busy,setBusy]=useState(false);
 const load=()=>approvalApi.list({page:0,size:50,...(filter!=="ALL"?{status:filter}:{})}).then(r=>setItems(unwrapPage(r.data))).catch(e=>setNotice({type:"error",message:messageOf(e)}));
 useEffect(()=>{load()},[filter]);
 const decide=async(id,decision)=>{setBusy(true);try{await approvalApi.decide(id,decision);setNotice({type:"success",message:`Approval ${decision.toLowerCase()}.`});load()}catch(e){setNotice({type:"error",message:messageOf(e)})}finally{setBusy(false)}};
 const retry=async id=>{setBusy(true);try{await approvalApi.retry(id);setNotice({type:"success",message:"Execution retry requested."});load()}catch(e){setNotice({type:"error",message:messageOf(e)})}finally{setBusy(false)}};
 return <div className="page-stack"><PageHeader eyebrow="HUMAN IN THE LOOP" title="Approvals" subtitle="Review actions before JobPilot executes them. You stay in control."/>
 <div className="approval-note"><ShieldCheck size={20}/><div><b>Your approval matters</b><p>Pending actions are held until you approve them. Review the description and execution state before deciding.</p></div></div>
 <div className="toolbar-row"><div><h3>Approval queue</h3><p>{items.length} item{items.length===1?"":"s"} in this view</p></div><select className="compact-select" value={filter} onChange={e=>setFilter(e.target.value)}><option value="ALL">All statuses</option><option>PENDING</option><option>APPROVED</option><option>REJECTED</option></select></div>
 {items.length?<div className="approval-list">{items.map(a=><Card className="approval-card" key={a.id}><div className="approval-card-head"><div className="approval-type"><span><ShieldCheck size={17}/></span><div><b>{String(a.type||"ACTION").replaceAll("_"," ")}</b><small>Requested {dateText(a.createdAt)}</small></div></div><Status value={a.status}/></div><p>{a.description}</p><div className="approval-meta"><span>Execution: <b>{a.executionStatus||"NOT_STARTED"}</b></span><span>Attempts: <b>{a.executionAttempts??0}</b></span></div>{a.executionError&&<div className="error-inline">{a.executionError}</div>}<div className="approval-actions">{a.status==="PENDING"?<><Button size="sm" onClick={()=>decide(a.id,"APPROVED")} loading={busy}><Check size={14}/> Approve</Button><Button size="sm" variant="danger" onClick={()=>decide(a.id,"REJECTED")}><X size={14}/> Reject</Button></>:a.status==="APPROVED"&&a.executionStatus==="FAILED"?<Button size="sm" variant="secondary" onClick={()=>retry(a.id)} loading={busy}><RefreshCw size={14}/> Retry execution</Button>:<span className="muted-copy">Decision recorded</span>}</div></Card>)}</div>:<Card><Empty title="No approvals waiting" text="When an email or agent action needs your approval, it will appear here."/></Card>}
 </div>;
}
