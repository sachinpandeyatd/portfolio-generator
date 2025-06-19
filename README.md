# AI Portfolio Generator

![AI Portfolio Generator Banner](https://via.placeholder.com/1200x300.png?text=AI+Portfolio+Generator) <!-- Replace with an actual banner/screenshot -->

Generate a unique, personal portfolio website instantly by uploading your resume! This full-stack web application uses ReactJS for the frontend, Spring Boot for the backend, and integrates with Google's Gemini AI to parse your resume and create a self-contained HTML portfolio.

**Live Demo (Frontend):** [YOUR_VERCEL_FRONTEND_URL_HERE] (e.g., https://portfolio-generator.vercel.app)
**Backend API Host:** [YOUR_RENDER_BACKEND_URL_HERE] (e.g., https://ai-portfolio-backend.onrender.com)

---

## ✅ Features

*   **Easy Resume Upload:** Supports PDF and DOCX resume formats.
*   **AI-Powered Generation:** Leverages Google Gemini to understand resume content and generate HTML/CSS/JS portfolio code.
*   **Unique Portfolio Sites:** Each generated portfolio gets a unique, shareable public URL.
*   **Self-Contained Output:** Generates a single HTML file with all styles and scripts embedded, making it portable and easy to host anywhere or view offline.
*   **Downloadable HTML:** Users can download their generated portfolio file.
*   **Secure API:** Basic API key protection and rate limiting implemented on the backend.

---

## 🛠️ Technologies Used

**Frontend:**
*   ReactJS (with Vite or Create React App)
*   Axios (for API calls)
*   HTML5, CSS3, JavaScript

**Backend:**
*   Spring Boot 3.x
*   Java 17 / 21
*   Apache PDFBox (for PDF parsing)
*   Apache POI (for DOCX parsing)
*   Google Cloud Vertex AI SDK (for Gemini integration)
*   Bucket4j (for API rate limiting)
*   Maven (for build and dependency management)
*   Docker

**AI:**
*   Google Gemini (via Vertex AI)

**Deployment:**
*   **Frontend:** Vercel
*   **Backend:** Render.com (as a Dockerized Web Service with a Persistent Disk)

---

## 🚀 Getting Started

### Prerequisites

*   **Java Development Kit (JDK):** Version 17 or 21
*   **Apache Maven:** Version 3.6+
*   **Node.js & npm/yarn:** Latest LTS version for frontend development
*   **Docker & Docker Compose (Optional for local backend):** Latest version
*   **Google Cloud Platform (GCP) Account:**
    *   A GCP Project with the Vertex AI API enabled.
    *   A Service Account JSON key with "Vertex AI User" and "Storage Object Admin" (if using GCS for an alternative storage strategy) permissions.
*   **Render.com Account:** For backend deployment.
*   **Vercel Account:** For frontend deployment.
*   **API Keys/Secrets:**
    *   A self-generated secret API key for backend protection.
    *   Your GCP Service Account Key JSON content.

### Project Structure

```
portfolio-generator/
├── backend/      <-- Spring Boot (Java) application
│   ├── pom.xml
│   ├── Dockerfile
│   ├── src/
│   └── ...
├── frontend/     <-- ReactJS application
│   ├── package.json
│   ├── src/
│   └── ...
└── README.md
```


### Local Development Setup

**1. Backend (Spring Boot):**

   *   Navigate to the `backend/` directory:
     
     ```bash
     cd portfolio-generator/backend
     ```
   *   **Configuration:**
      *   Copy `src/main/resources/application.properties.example` to `src/main/resources/application.properties` (if you create an example file).
      *   Update `application.properties` with:
         *   `app.portfolio.storage-path.local`: Path for local HTML file storage (e.g., `./portfolio-sites-local`)
         *   `app.security.api-key`: A secret key for local API testing.
         *   `gemini.project-id`: Your GCP Project ID.
         *   `gemini.location`: Your GCP region (e.g., `us-central1`).
         *   `gemini.model-name`: (e.g., `gemini-1.0-pro-001`)
      *   **Google Cloud Authentication:** Ensure Application Default Credentials (ADC) are set up for local development:
        ```bash
        gcloud auth application-default login
        ```
        Alternatively, set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable to the path of your GCP service account JSON key file.
   *   **Run the application:**
     ```bash
     mvn spring-boot:run -Dspring.profiles.active=local
     ```
     The backend will be available at `http://localhost:8080`.

**2. Frontend (React):**

   *   Navigate to the `frontend/` directory:
     ```bash
     cd portfolio-generator/frontend
     ```
   *   Install dependencies:
     ```bash
     npm install
     # or
     # yarn install
     ```
   *   **Configuration:**
      *   Create a `.env` file in the `frontend/` directory (e.g., by copying `.env.example`).
      *   Set the following environment variables:
        ```env
        VITE_API_BASE_URL=http://localhost:8080/api/v1
        VITE_BACKEND_API_KEY=YOUR_LOCAL_BACKEND_API_KEY # Must match app.security.api-key in backend
        ```
        (Use `REACT_APP_...` prefixes if using Create React App).
   *   Run the development server:
     ```bash
     npm run dev
     # or
     # yarn dev
     ```
     The frontend will be available at `http://localhost:5173` (or another port specified by Vite/CRA).

### Running Backend with Docker (Locally - Optional)

1.  Navigate to `portfolio-generator/backend/`.
2.  Build the JAR: `mvn package -DskipTests`
3.  Build the Docker image (using the multi-stage Dockerfile provided in this project):
    ```bash
    docker build -t portfolio-backend .
    ```
4.  Run the Docker container:
    ```bash
    docker run -p 8080:8080 \
      -e SPRING_PROFILES_ACTIVE=local \
      -e APP_PORTFOLIO_STORAGE_PATH_LOCAL=/app/data/portfolio-sites \
      -e APP_SECURITY_API_KEY="YOUR_LOCAL_API_KEY" \
      -e GEMINI_PROJECT_ID="YOUR_GCP_PROJECT_ID" \
      -e GEMINI_LOCATION="us-central1" \
      -e GEMINI_MODEL_NAME="gemini-1.0-pro-001" \
      -e GOOGLE_APPLICATION_CREDENTIALS="/app/secrets/gcp-key.json" \
      -v $(pwd)/portfolio-sites-docker:/app/data/portfolio-sites \ # Mount local dir for storage
      -v /path/to/your/gcp-key.json:/app/secrets/gcp-key.json:ro \ # Mount GCP key
      portfolio-backend
    ```
    *Adjust paths and environment variables as needed.*

---

## ☁️ Deployment

This application is designed to be deployed with the frontend on Vercel and the backend on Render.com.

**1. Backend (Spring Boot on Render.com):**

   *   Push your `portfolio-generator/` monorepo (with the `backend/Dockerfile`) to GitHub/GitLab.
   *   On Render.com, create a new **Web Service**.
   *   Connect your Git repository.
   *   **Settings:**
      *   **Root Directory:** `backend`
      *   **Runtime:** Docker
      *   **Build Command:** Leave BLANK (the multi-stage Dockerfile handles the build).
      *   **Start Command:** Leave BLANK (handled by Dockerfile `ENTRYPOINT`).
   *   **Environment Variables:**
      *   `APP_PORTFOLIO_STORAGE_PATH`: `/var/data/portfolio-sites` (or your chosen disk mount path)
      *   `APP_PORTFOLIO_BASE_URL`: Your Render service URL (e.g., `https://your-backend.onrender.com`)
      *   `APP_SECURITY_API_KEY`: Your production API key.
      *   `GEMINI_PROJECT_ID`, `GEMINI_LOCATION`, `GEMINI_MODEL_NAME`.
      *   `GOOGLE_APPLICATION_CREDENTIALS`: Path to your GCP service account key, uploaded as a **Secret File** on Render (e.g., `/etc/secrets/gcp-key.json`).
      *   `SPRING_PROFILES_ACTIVE`: `production` (or a specific profile for Render if you have `application-production.properties`).
   *   **Persistent Disk:**
      *   Add a disk.
      *   **Mount Path:** `/var/data/portfolio-sites` (must match `APP_PORTFOLIO_STORAGE_PATH`).
      *   Set desired size.
   *   Deploy. Update `APP_PORTFOLIO_BASE_URL` if needed once you have the final URL.
   *   Configure CORS in `WebConfig.java` to allow your Vercel frontend domain and redeploy if necessary.

**2. Frontend (React on Vercel):**

   *   Push your `portfolio-generator/` monorepo to GitHub/GitLab.
   *   On Vercel, import the project from your Git repository.
   *   **Settings:**
      *   **Root Directory:** `frontend`
      *   Framework Preset, Build Command, Output Directory should be auto-detected for Vite/CRA.
   *   **Environment Variables:**
      *   `VITE_API_BASE_URL` (or `REACT_APP_API_BASE_URL`): The full API base URL of your deployed Render backend (e.g., `https://your-backend.onrender.com/api/v1`).
      *   `VITE_BACKEND_API_KEY` (or `REACT_APP_BACKEND_API_KEY`): The API key that matches the one on your Render backend.
   *   Deploy.

---

## 🔐 API Endpoints & Security

**Backend API Base:** `[YOUR_RENDER_BACKEND_URL]/api/v1`

**Protected Endpoints (require `X-API-Key` header):**

*   **`POST /portfolios/upload`**: Uploads a resume file.
    *   Body: `multipart/form-data` with a field `resumeFile`.
*   *(Other API endpoints if you add them for management)*

**Public Endpoints (do not require `X-API-Key`):**

*   **`GET /{portfolioId}`**: Serves the generated HTML portfolio page (e.g., `/wvvyz`).
*   **`GET /portfolios/{portfolioId}/download`**: Downloads the HTML file.
*   **`GET /health`**: Health check.

**Security Measures:**

*   **API Key Authentication:** Most API endpoints are protected by a secret API key sent in the `X-API-Key` header.
*   **CORS:** Configured to allow requests only from the deployed frontend and local development environment.
*   **Rate Limiting:** Implemented on the backend to prevent abuse (e.g., limits on uploads per IP per minute).
*   **Input Validation:** Basic validation for file types and sizes.

---

## 💡 Future Enhancements

*   More sophisticated resume parsing for structured data.
*   User accounts to save and manage multiple portfolios.
*   Template selection for generated portfolios.
*   Advanced AI prompting for diverse portfolio styles.
*   Option to edit generated portfolio content.
*   Custom domain support.
*   More robust WAF or edge security.

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request or open an Issue.
(Add guidelines if you have specific contribution rules).

---
