# PrepGenius — AI Interview Preparation System

PrepGenius is a full-stack platform designed to help candidates prepare for technical and behavioral interviews. It leverages AI for intelligent question generation and answer evaluation, while providing comprehensive tracking of user progress, interview history, and administrative management.

## Project Structure

The project is organized into a monorepo structure:

- `/frontend`: A React-based web application.
- `/backend`: A Java-based Spring Boot REST API.

## Tech Stack

### Frontend
- **Framework:** React 18
- **Build Tool:** Vite
- **Styling:** Tailwind CSS
- **Visualization:** Recharts
- **Icons:** Lucide-react

### Backend
- **Language:** Java 17
- **Framework:** Spring Boot 3.3.2
- **Database:** MongoDB Atlas
- **Security:** Spring Security with JWT Authentication
- **AI Integration:** Google Gemini API

## Getting Started

You have three ways to run this. **Option A (Docker) is the fastest way to get everything working with zero local installs besides Docker.**

### Option A — Docker Compose (recommended, one command)

Requires only Docker installed.

```bash
docker compose up --build
```

This starts MongoDB, the backend (`http://localhost:8080`), and the frontend (`http://localhost:5173`) together. A default admin account and starter companies/categories are seeded automatically on first boot (see **Default login** below).

To point it at MongoDB Atlas instead of the bundled Mongo container, set `MONGODB_URI` before running compose:

```bash
MONGODB_URI="mongodb+srv://<user>:<pass>@<cluster>.mongodb.net/prepgenius?retryWrites=true&w=majority" docker compose up --build backend frontend
```

(Omit `mongo` from that command so it doesn't also start a local database you don't need.)

### Option B — Run locally without Docker

**Prerequisites:** Node.js 18+, Java JDK 17+, and either a local MongoDB instance or a MongoDB Atlas cluster.

#### 1. Backend

```bash
cd backend
cp .env.example .env   # already provided pre-filled for local Mongo — edit as needed
```

Edit `backend/.env`:
- For **local MongoDB**: leave `MONGODB_URI=mongodb://localhost:27017/prepgenius` as-is (make sure `mongod` is running, or run `docker compose up -d mongo` from the repo root to just start the database).
- For **MongoDB Atlas**: comment out the local line and uncomment/fill in the `mongodb+srv://...` line with your cluster credentials (get this from Atlas → Connect → Drivers).
- Add your `GEMINI_API_KEY` (free at https://aistudio.google.com/app/apikey) if you want AI question generation/evaluation to work. The app still runs fine without it — those specific endpoints will just return a clear "not configured" message.

Spring Boot automatically loads `backend/.env` on startup — no manual `export` needed.

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`, and Swagger docs at `http://localhost:8080/swagger-ui.html`.

#### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

The app will be available at `http://localhost:5173`.

### Default login

On first boot, a default admin account is seeded automatically:

- **Email:** `admin@prepgenius.com`
- **Password:** `Admin@123`

Override these via `ADMIN_EMAIL` / `ADMIN_PASSWORD` / `ADMIN_NAME` in `.env` (or as Docker Compose env vars). **Change the password after first login if this isn't just for local development.** Regular users can register normally through the Register page.

### MongoDB Atlas setup (if you don't already have a cluster)

1. Create a free cluster at https://www.mongodb.com/cloud/atlas
2. Under **Database Access**, create a user with a username/password
3. Under **Network Access**, add your IP (or `0.0.0.0/0` for local dev only)
4. Under **Database → Connect → Drivers**, copy the connection string and drop it into `MONGODB_URI` in `backend/.env`, replacing `<username>`, `<password>`, and adding `/prepgenius` before the `?` as the database name

## Key Features

### User Experience
- **Auth:** Secure registration and login using JWT.
- **Interviews:** Customizable mock interview sessions (by topic, company, difficulty).
- **AI Integration:** Real-time AI-driven question generation and answer evaluation.
- **Analytics:** Tracking progress, identifying weak spots, providing downloadable learning material for weak topics, and viewing performance trends.
- **History:** Reviewing past interview attempts.

### Admin Console
- **Dashboard:** Platform-wide statistics.
- **Management:** CRUD operations for Users, Companies and Categories.
- **Reports:** Advanced analytics on learner performance.
