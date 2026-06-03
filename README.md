# FlexZone Asansol - Gym Management Web Application

**FlexZone Asansol** is a production-grade Gym Management System built to facilitate administrative controls, booking schedulers, digital check-ins, and online supplement purchases.

---

## Technical Stack
- **Frontend**: Angular 20 SPA (Responsive, styled with Tailwind CSS)
- **Backend**: Java Spring Boot 3.4 (Stateless REST API, JWT Authentication, Spring Data JPA)
- **Database**: MySQL

---

## Directory Structure
```text
FlexZone/
├── database/
│   └── schema.sql          # MySQL Schema Definitions & Data Seeding
├── backend/
│   ├── pom.xml             # Maven dependencies
│   ├── mvnw.cmd            # Windows Maven wrapper
│   └── src/                # Spring Boot source code
└── frontend/
    ├── package.json        # Node dependencies
    ├── tailwind.config.js  # Tailwind settings
    └── src/                # Angular application source code
```

---

## Setup & Deployment Instructions

### Phase 1: Database Setup (MySQL)
1. Ensure a MySQL database server is running locally on port `3306`.
2. Connect to your MySQL server and run:
   ```sql
   CREATE DATABASE IF NOT EXISTS flexzone_db;
   ```
3. Import the schema script to create tables and seed default plans, products, time-slots, and admin accounts:
   ```bash
   mysql -u root -p flexzone_db < database/schema.sql
   ```

### Phase 2: Configuration & Startup
1. Edit `backend/src/main/resources/application.properties` to specify your MySQL credentials:
   ```properties
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```
2. From the root directory (`FlexZone/`), run the installer (only needed the first time):
   ```bash
   npm run install-all
   ```
3. Start both the Spring Boot Backend and the Angular Frontend concurrently:
   ```bash
   npm start
   ```
4. Open your browser and navigate to `http://localhost:4200` to view the website. You will see both server logs in your VS Code terminal window.

---

## Credentials for Testing
You can use the default administrator account seeded in the database:
- **Role**: Admin (Gym Owner)
- **Username**: `admin`
- **Password**: `admin123`

You can also sign up for new accounts directly from the UI to test Member or Trainer flows.
- **Member Flow**: Register, sign in, log daily weight/height to calculate BMI, browse the E-Shop catalog, renew membership subscriptions, checkout orders via mock payment gateways, and book workout slots or fitness classes.
- **Trainer/Admin Flow**: Scan member IDs at the scan terminal to log daily attendance, view analytics, and manage plans/trainers.
