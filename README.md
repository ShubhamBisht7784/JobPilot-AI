# JobPilot AI Backend

Backend-only modular monolith for job discovery, resume analysis, matching, recruiter outreach, approvals and application tracking.

## Technology

- Java 21
- Spring Boot 4.1.2
- Spring AI 2.0.2
- Groq chat inference through Spring AI's OpenAI-compatible client
- PostgreSQL + pgvector
- Spring Security + JWT
- Gmail OAuth 2.0 / Gmail API
- SerpAPI + Hunter.io integrations
- PDFBox for PDF text extraction

Spring AI 2.0.x is the compatible line for Spring Boot 4.0.x/4.1.x. Spring AI 2.0.2 is managed through its BOM. See the official Spring AI getting-started and Groq references.

## Implemented backend modules

- Authentication: registration, login, JWT authentication and current-user endpoint.
- Jobs: CRUD, search, pagination and Google Jobs discovery through SerpAPI.
- Scheduler: recurring discovery and per-user resume matching; enabled by default but requires a SerpAPI key to perform searches.
- Resumes: PDF upload, text extraction, download and ownership checks.
- Matching: deterministic skill extraction, match score and persisted match history.
- AI: Groq-powered resume analysis, evidence-preserving resume tailoring drafts and application preparation drafts.
- RAG: resume indexing and question answering using PostgreSQL pgvector.
- Recruiters: CRUD, SerpAPI LinkedIn profile discovery, Hunter email lookup and bounded bulk discovery.
- Email: drafts, approval workflow, Gmail OAuth, encrypted token storage, token refresh and Gmail API send.
- Applications: tracking, status transitions, notes, statistics and follow-up dates.
- Agent: task lifecycle plus workflows for matching, AI resume analysis and recruiter discovery.

## Important AI / RAG design note

Groq is used for text generation. This build uses Groq for generation and does not configure a second embedding provider. To keep the project usable without another provider key, the initial pgvector index uses a local 384-dimensional feature-hashing vectorizer over words and adjacent word pairs. This is lexical retrieval, **not transformer semantic embeddings**. It provides a functional RAG baseline but is less semantically capable than a dedicated embedding model. The `ResumeKnowledgeService` is the replacement point if a separate embedding provider is added later.

The first request to `/api/ai/resumes/{id}/index` creates the pgvector extension/table/index. The PostgreSQL server must have the pgvector extension installed and the application database role must be allowed to create the extension. If extension creation is not permitted, ask the database administrator to run `database/enable-pgvector.sql`.

## Application submission and human approval

JobPilot prepares job-specific application material and tracks applications. It does not claim to submit applications to arbitrary third-party job portals. Job sites use different forms, authentication, MFA and CAPTCHA policies; bypassing access controls is not supported. The user must review the generated material and submit on the employer's official site. Only after the user has actually submitted should the application status be changed to `APPLIED`.

Recruiter emails are sent only after explicit approval. Gmail's external send operation cannot be made transactionally atomic with PostgreSQL. If a network failure occurs during send, verify the Gmail Sent folder before retrying to avoid a duplicate.

## Prerequisites

- JDK 21
- Maven 3.9+ (or included Maven Wrapper)
- PostgreSQL 16 with pgvector extension for RAG
- Groq API key for AI endpoints
- Google Cloud OAuth client and Gmail API only if recruiter email sending is required (activate the `gmail` Spring profile)
- SerpAPI key for job/recruiter discovery
- Hunter.io key for recruiter email lookup

## Configuration

No live secrets are committed. Copy `.env.example` to `.env`, then replace the database password and generate a valid Base64 JWT secret. Spring Boot imports the project-root `.env` file automatically. You can alternatively set environment variables directly. Configure the following values:

| Property / environment variable | Required for |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL |
| `APP_JWT_SECRET` | Login/JWT; Base64-encoded random 32-byte key |
| `APP_CRYPTO_ENCRYPTION_KEY` | Gmail token encryption; independent Base64-encoded random 32-byte key |
| `GROQ_API_KEY` | AI analysis, tailoring, RAG answers and application drafts |
| `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` | Gmail OAuth when the `gmail` profile is active |
| `SERPAPI_API_KEY` | Job and recruiter discovery |
| `HUNTER_API_KEY` | Recruiter email discovery |
| `JOB_DISCOVERY_ENABLED` | Set `true` to enable scheduled job discovery |

The project properties use Groq model `openai/gpt-oss-120b` by default. Set `GROQ_MODEL` to another model currently enabled for your Groq account if needed. Keep Groq reasoning-content replay disabled because Groq rejects that OpenAI-specific field.

To enable Gmail only when needed, configure `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, and `APP_CRYPTO_ENCRYPTION_KEY`, then start with `SPRING_PROFILES_ACTIVE=gmail`. The default profile does not require Google OAuth credentials.

Generate two independent Base64 keys on Windows PowerShell:

```powershell
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$bytes = New-Object byte[] 32
$rng.GetBytes($bytes)
[Convert]::ToBase64String($bytes)
$rng.Dispose()
```

Run twice: one value for `APP_JWT_SECRET`, another for `APP_CRYPTO_ENCRYPTION_KEY`. Do not reuse a key. Do not commit `application.properties` if it contains live credentials.

## Database setup

Option A — use the included pgvector container:

```bash
docker compose up -d postgres
```

Default compose credentials are development-only. Change them before use and make `DB_USERNAME` / `DB_PASSWORD` match.

Option B — use an existing PostgreSQL instance:

```sql
CREATE DATABASE jobpilot_db;
CREATE EXTENSION IF NOT EXISTS vector;
```

Run the extension command in the `jobpilot_db` database using a role with extension-creation privileges.

## Run

Windows:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
chmod +x mvnw
./mvnw clean test
./mvnw spring-boot:run
```

## Authentication

1. `POST /api/auth/register`
2. `POST /api/auth/login`
3. Send the returned JobPilot JWT in `Authorization: Bearer <token>` for protected `/api/**` routes.

Google OAuth browser login is a separate flow; a Google access token is not a JobPilot JWT.

## Main API routes

| Module | Method + route | Purpose |
|---|---|---|
| Auth | `POST /api/auth/register` | Register |
| Auth | `POST /api/auth/login` | Login |
| Auth | `GET /api/auth/me` | Current user |
| Jobs | `/api/jobs` | Job CRUD and list |
| Jobs | `GET /api/jobs/search...` | Search jobs |
| Jobs | `POST /api/jobs/discover` | Search Google Jobs via SerpAPI |
| Resumes | `/api/resumes` | Upload, list, get, delete, download |
| Matching | `/api/matching/analyze` | Resume/job skill match |
| AI | `POST /api/ai/resumes/{id}/analyze?jobId={jobId}` | AI resume analysis |
| AI | `POST /api/ai/resumes/{id}/tailor?jobId={jobId}` | Job-specific resume draft |
| AI | `POST /api/ai/resumes/{id}/index` | Index resume into pgvector |
| AI | `POST /api/ai/resumes/{id}/chat` | Ask a question about indexed resume |
| AI | `POST /api/ai/applications/{id}/prepare` | Generate application package draft |
| Recruiters | `/api/recruiters` | Recruiter CRUD |
| Recruiters | `POST /api/recruiters/bulk-discover` | Bounded recruiter discovery |
| Emails | `/api/emails` | Email drafts |
| Emails | `POST /api/emails/{id}/request-approval` | Request email approval |
| Approvals | `/api/approvals` | List/create approvals |
| Approvals | `PATCH /api/approvals/{id}/decision?decision=APPROVED` | Approve/reject |
| Gmail | `/api/gmail/connection` | Gmail connection status/disconnect |
| Applications | `/api/applications` | Application tracking |
| Agent | `/api/agent/tasks` | Create/list/get agent tasks |
| Agent | `POST /api/agent/tasks/{id}/execute/job-discovery` | Run job discovery as a task |
| Agent | `POST /api/agent/tasks/{id}/execute/resume-analysis` | Run AI resume analysis as a task |
| Agent | `POST /api/agent/tasks/{id}/execute/matching` | Run matching as a task |
| Agent | `POST /api/agent/tasks/{id}/execute/recruiter-discovery` | Run recruiter discovery as a task |

Protected routes require a JobPilot JWT. For importable request examples, import `docs/JobPilot-AI-Backend.postman_collection.json`. Set its `baseUrl`, `jwt`, and ID variables. For exact JSON fields, see the request/response DTO records.

## Example request bodies

Index a resume first:

```http
POST /api/ai/resumes/1/index
Authorization: Bearer <jobpilot-jwt>
```

Ask a question about the indexed resume:

```json
{
  "question": "Which backend technologies and projects are listed in my resume?"
}
```

Prepare an application package for a saved application:

```json
{
  "resumeId": 1,
  "additionalContext": "I am applying for an entry-level Java backend role."
}
```

Execute AI analysis through an Agent task:

```json
{
  "resumeId": 1,
  "jobId": 1
}
```

Discover jobs manually:

```json
{
  "query": "Java Backend Developer fresher India"
}
```

## Job discovery scheduler

The scheduler is enabled by default through `JOB_DISCOVERY_ENABLED=true`; it skips searches until `SERPAPI_API_KEY` is configured. Configure the fixed delay and baseline queries with `JOB_DISCOVERY_FIXED_DELAY_MS` and `JOB_DISCOVERY_QUERIES`. It stores discovered jobs in the shared `jobs` table and skips duplicate URLs/fingerprints. Manual discovery is available through `POST /api/jobs/discover`.

## Reference documentation

- Spring AI 2.0.x setup and Spring Boot compatibility: https://docs.spring.io/spring-ai/reference/getting-started.html
- Spring AI Groq-compatible OpenAI chat configuration: https://docs.spring.io/spring-ai/reference/api/chat/groq-chat.html
- Spring AI pgvector concepts and PostgreSQL extension requirements: https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html

## Known boundaries

- Resume matching is currently deterministic skill matching, not an LLM semantic score.
- PDFBox extracts selectable text; scanned/image-only PDFs need OCR and are not automatically OCR'd.
- Resume tailoring and application preparation return drafts; they do not overwrite the uploaded source resume.
- RAG retrieval currently uses lexical feature hashing. It is not a semantic embedding model.
- SerpAPI/Hunter/Gmail usage depends on provider quotas and account permissions.
- Playwright-based third-party portal submission is not included in this build. The backend generates an application package and the user reviews and submits it on the employer's site. A reliable browser workflow requires a separately scoped adapter for each supported portal and must stop at CAPTCHA/MFA or other access controls.
- Gmail send and PostgreSQL persistence cannot be guaranteed atomic across an external network call. For ambiguous send failures, reconcile against Gmail Sent before retry.

## Security checklist before deployment

- Rotate any credentials previously shared or committed.
- Set unique, random JWT and encryption keys.
- Use HTTPS and restrictive CORS origins.
- Keep uploads outside the public web root and enforce storage limits.
- Restrict database credentials and enable PostgreSQL backups.
- Keep provider API keys out of Git and logs.


## Automatic discovery and personalized recommendations

The backend now runs a scheduled discovery-and-matching pipeline when `JOB_DISCOVERY_ENABLED=true`:

1. Searches the configured baseline queries and each user's saved search keywords/locations.
2. Uses the SerpAPI Google Jobs engine as the supported aggregation provider.
3. Normalizes job details (title, company, location, description, provider label, salary/type/work-mode when returned, posted date when parseable, and apply URL).
4. Deduplicates using both the normalized apply URL and a SHA-256 fingerprint of title/company/location/canonical URL.
5. Scores newly discovered jobs against each user's latest uploaded resume. Groq is used for an AI relevance score when configured; deterministic extracted-skill scoring is the fallback if Groq is disabled, unavailable, or quota-limited.
6. Returns each user's recommendations ordered by match score, including matched/missing skills and the apply URL.

### Configure automatic discovery

Set these environment variables before starting the backend:

```properties
SERPAPI_API_KEY=your_serpapi_key
JOB_DISCOVERY_ENABLED=true
JOB_DISCOVERY_FIXED_DELAY_MS=21600000
JOB_DISCOVERY_INITIAL_DELAY_MS=60000
JOB_DISCOVERY_QUERIES=Java Backend Developer fresher India,Spring Boot Developer entry level India
GROQ_API_KEY=your_groq_key
```

The scheduler checks every six hours by default. The process must remain running; for production, deploy the backend as a continuously running service. Without a SerpAPI key, scheduled discovery is skipped and logged. Without a Groq key, matching continues with the transparent skill-based fallback.

### User search preferences

`GET /api/profile/job-preferences` returns the current user's saved preferences (or defaults).

`PUT /api/profile/job-preferences` accepts:

```json
{
  "searchKeywords": "Java Backend Developer fresher, Spring Boot Developer",
  "preferredLocations": "India, Remote",
  "remoteOnly": false,
  "autoDiscoveryEnabled": true,
  "minimumMatchScore": 45
}
```

Keywords are comma/semicolon-separated searches. The backend appends the preferred location text and `remote` when `remoteOnly=true`.

### Personalized job dashboard

`GET /api/jobs/recommended?minScore=40&page=0&size=20` returns a paginated list for the authenticated user, sorted by match score. `minScore` can be omitted to use the saved preference. `POST /api/jobs/recommended/refresh` scores any newly discovered jobs against the latest uploaded resume immediately. Each item includes the job details, provider label, score, matched/missing skills, matching method, and `applyUrl`. Upload a resume first; otherwise the endpoint returns a resume-not-found validation error.

`POST /api/jobs/discover` remains available for an authenticated manual discovery run:

```json
{ "query": "Java Backend Developer fresher India" }
```

### Job-source coverage and limitations

This integration queries Google Jobs through SerpAPI. Google Jobs may return listings whose provider/apply option is LinkedIn, Naukri, Indeed, a company site, or another publisher; the backend preserves the provider label and the returned apply link when present. This is **not** a direct LinkedIn, Naukri, or Indeed API integration and cannot guarantee every listing on those platforms. Coverage depends on Google Jobs indexing, SerpAPI account access/quotas, the query, and returned apply options. The backend does not bypass login, OTP, CAPTCHA, paywalls, or platform restrictions.

The Groq score is AI-assisted when the provider responds. If Groq is not configured or errors, the system explicitly stores `SKILL_BASED_FALLBACK` as the matching method rather than pretending an LLM scored the job.
