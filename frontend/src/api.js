import axios from "axios";

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 45000,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("jobpilot_token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !String(error.config?.url || "").includes("/api/auth/login")) {
      // Keep the login screen in control; a user can sign in again without losing the error.
      window.dispatchEvent(new CustomEvent("jobpilot:unauthorized"));
    }
    return Promise.reject(error);
  }
);

// Normalize Spring Data Page responses and simple array responses.
// Call this with response.data (not the full Axios response).
export const unwrapPage = (data) => {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  if (Array.isArray(data?.items)) return data.items;
  if (Array.isArray(data?.data)) return data.data;
  if (Array.isArray(data?.results)) return data.results;
  return [];
};

export const messageOf = (error) => {
  const data = error?.response?.data;
  if (typeof data === "string" && data.trim()) return data;
  if (data?.message) return data.message;
  if (data?.error) return data.error;
  if (data?.errors && Array.isArray(data.errors)) return data.errors.map(x => x.defaultMessage || x.message).join(", ");
  return error?.message || "Something went wrong. Please try again.";
};

export const authApi = {
  register: (payload) => api.post("/api/auth/register", payload),
  login: (payload) => api.post("/api/auth/login", payload),
  me: () => api.get("/api/auth/me"),
};
export const resumeApi = {
  list: () => api.get("/api/resumes"),
  upload: (file) => { const fd = new FormData(); fd.append("file", file); return api.post("/api/resumes/upload", fd); },
  remove: (id) => api.delete(`/api/resumes/${id}`),
  download: (id) => api.get(`/api/resumes/${id}/download`, { responseType: "blob" }),
};
export const jobApi = {
  list: (params = {}) => api.get("/api/jobs", { params }),
  search: (params = {}) => api.get("/api/jobs/search", { params }),
  discover: (query) => api.post("/api/jobs/discover", { query }),
  get: (id) => api.get(`/api/jobs/${id}`),
};
export const applicationApi = {
  list: (params = {}) => api.get("/api/applications", { params }),
  stats: () => api.get("/api/applications/stats"),
  create: (jobId, notes = "") => api.post("/api/applications", { jobId: Number(jobId), status: "SAVED", notes }),
  updateStatus: (id, status) => api.patch(`/api/applications/${id}/status`, { status }),
  updateNotes: (id, notes) => api.patch(`/api/applications/${id}/notes`, { notes }),
  followUp: (id, nextFollowUpAt) => api.patch(`/api/applications/${id}/follow-up`, { nextFollowUpAt }),
  due: () => api.get("/api/applications/follow-ups/due"),
  upcoming: () => api.get("/api/applications/follow-ups/upcoming"),
  remove: (id) => api.delete(`/api/applications/${id}`),
};
export const matchingApi = {
  analyze: (resumeId, jobId) => api.post("/api/matching/analyze", { resumeId: Number(resumeId), jobId: Number(jobId) }),
  history: (params = {}) => api.get("/api/matching/history", { params }),
  remove: (id) => api.delete(`/api/matching/history/${id}`),
};
export const aiApi = {
  analyzeResume: (resumeId, jobId) => api.post(`/api/ai/resumes/${resumeId}/analyze`, null, { params: jobId ? { jobId } : {} }),
  tailorResume: (resumeId, jobId) => api.post(`/api/ai/resumes/${resumeId}/tailor`, null, { params: { jobId } }),
  indexResume: (resumeId) => api.post(`/api/ai/resumes/${resumeId}/index`),
  chatResume: (resumeId, question) => api.post(`/api/ai/resumes/${resumeId}/chat`, { question }),
  prepareApplication: (applicationId, payload) => api.post(`/api/ai/applications/${applicationId}/prepare`, payload),
};
export const recruiterApi = {
  list: (params = {}) => api.get("/api/recruiters", { params }),
  create: (payload) => api.post("/api/recruiters", payload),
  update: (id, payload) => api.put(`/api/recruiters/${id}`, payload),
  remove: (id) => api.delete(`/api/recruiters/${id}`),
  search: (payload) => api.post("/api/recruiters/search", payload),
  findEmail: (linkedinUrl) => api.post("/api/recruiters/find-email", { linkedinUrl }),
  findAndSave: (linkedinUrl) => api.post("/api/recruiters/find-email/save", { linkedinUrl }),
  bulkDiscover: (payload) => api.post("/api/recruiters/bulk-discover", payload),
};
export const emailApi = {
  list: (params = {}) => api.get("/api/emails", { params }),
  create: (payload) => api.post("/api/emails", payload),
  update: (id, payload) => api.put(`/api/emails/${id}`, payload),
  remove: (id) => api.delete(`/api/emails/${id}`),
  requestApproval: (id) => api.post(`/api/emails/${id}/request-approval`),
};
export const preferencesApi = {
  get: () => api.get("/api/profile/job-preferences"),
  save: (payload) => api.put("/api/profile/job-preferences", payload),
};
export const gmailApi = {
  status: () => api.get("/api/gmail/connection"),
  disconnect: () => api.delete("/api/gmail/connection"),
  connectUrl: `${API_BASE_URL}/oauth2/authorization/google`,
};
export const approvalApi = {
  list: (params = {}) => api.get("/api/approvals", { params }),
  decide: (id, decision) => api.patch(`/api/approvals/${id}/decision`, null, { params: { decision } }),
  retry: (id) => api.post(`/api/approvals/${id}/retry`),
};
export const agentApi = {
  list: (params = {}) => api.get("/api/agent/tasks", { params }),
  create: (payload) => api.post("/api/agent/tasks", payload),
  cancel: (id) => api.patch(`/api/agent/tasks/${id}/cancel`),
  executeJobs: (id, query) => api.post(`/api/agent/tasks/${id}/execute/job-discovery`, { query }),
  executeResume: (id, resumeId, jobId) => api.post(`/api/agent/tasks/${id}/execute/resume-analysis`, { resumeId: Number(resumeId), jobId: jobId ? Number(jobId) : null }),
  executeMatch: (id, resumeId, jobId) => api.post(`/api/agent/tasks/${id}/execute/matching`, { resumeId: Number(resumeId), jobId: Number(jobId) }),
  executeRecruiters: (id, payload) => api.post(`/api/agent/tasks/${id}/execute/recruiter-discovery`, payload),
};
export default api;
