# Personal Finance Tracker Pro 🚀

> **Production-ready, full-stack personal finance management platform built with Spring Boot 3 (Java 21), MongoDB Atlas, React, Tailwind CSS, and Recharts.**

![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)
![Spring%20Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)
![React](https://img.shields.io/badge/React-19-blue.svg)
![Tailwind%20CSS](https://img.shields.io/badge/Tailwind%20CSS-4.x-teal.svg)

---

## 🌟 Key Architectural Highlights

- **Enterprise Layered Architecture:** Strict Controller → Service → Repository pattern with zero data leaks or cross-tenant exposure.
- **Stateless JWT Security & BCrypt:** Comprehensive authentication lifecycle using Spring Security, authorization filters, and secure context principals.
- **Accurate Financial Arithmetic:** Exact `BigDecimal` representation mapped to MongoDB `Decimal128` to completely prevent floating-point rounding errors.
- **MongoDB Compound Indexing:** Optimized indexing on `(userId, transactionDate)`, `(userId, type, transactionDate)`, and `(userId, category, month, year)`.
- **Dynamic KPI & Analytics Engine:** Real-time net balance calculations, monthly income vs. expense bar charts, category breakdown donut charts, and spending trend tracking with Recharts.
- **Threshold-Driven Budget Controls:** Overall monthly budgets and category budgets with real-time utilization % and warning trigger alerts (75%, 80%, 100%).
- **Financial Planning & Automation:** Dedicated savings goals with progress tracking and contributions, recurring transactions engine, and subscription manager.
- **Data Mobility & Statements:** CSV export, safe bulk CSV import with validation, and monthly PDF statement reports powered by OpenPDF.

---

## 📁 Monorepo Structure

```
personal-finance-tracker-pro/
├── client/                     # React + Vite + Tailwind CSS frontend
│   ├── src/
│   │   ├── components/         # Modals, layout, navigation, charts
│   │   ├── context/            # AuthContext & session handling
│   │   ├── pages/              # Dashboard, Transactions, Budgets, Goals, etc.
│   │   ├── routes/             # ProtectedRoute guards
│   │   └── services/           # Axios interceptors & HTTP clients
├── server/                     # Spring Boot 3 + Java 21 backend
│   ├── src/main/java/com/financetracker/pro/
│   │   ├── config/             # SecurityConfig, CORS
│   │   ├── controller/         # REST Controllers
│   │   ├── document/           # MongoDB Models (@Document)
│   │   ├── dto/                # Request & Response DTOs
│   │   ├── exception/          # GlobalExceptionHandler (@ControllerAdvice)
│   │   ├── repository/         # Spring Data MongoDB Repositories
│   │   ├── security/           # JWT Provider, CustomUserDetailsService
│   │   └── service/            # Business Logic & Scheduled tasks
├── docs/                       # Architecture diagrams & design specs
├── screenshots/                # UI previews
└── FEATURE_CHECKLIST.md        # Comprehensive requirement matrix
```

---

## 🛠️ Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Frontend** | React 19, Vite, Tailwind CSS 4, React Router DOM, Recharts, React Hook Form, Zod, Lucide React, date-fns, React Hot Toast |
| **Backend** | Java 21 LTS, Spring Boot 3.2.5, Spring Security 6, Spring Data MongoDB, JJWT 0.12.5, OpenPDF, Apache Commons CSV, SpringDoc OpenAPI |
| **Database** | MongoDB Atlas (Cloud) / Local MongoDB 7.0+ |
| **CI/CD** | GitHub Actions |

---

## 🚀 Local Quickstart Guide

### 1. Prerequisites
- **JDK 21** installed and configured on your `PATH`.
- **Apache Maven 3.9+** installed.
- **Node.js (v20+) & npm** installed.
- **MongoDB** instance (Local or Atlas URI).

### 2. Configure Backend (`server`)
Navigate to `server/src/main/resources/application.yml` or copy `server/.env.example`.
Update your MongoDB connection URI:
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/finance_tracker_pro
```

Run backend:
```bash
cd personal-finance-tracker-pro/server
mvn clean spring-boot:run
```
Backend API will boot at `http://localhost:8080`.
Interactive Swagger UI: `http://localhost:8080/swagger-ui.html`.

### 3. Configure Frontend (`client`)
```bash
cd personal-finance-tracker-pro/client
npm install --legacy-peer-deps
npm run dev
```
Client dev server will launch at `http://localhost:5173`.

---

## 🔒 Security Best Practices

1. **Owner-Derivation Only:** Every financial mutation explicitly derives the `userId` from the authenticated JWT token stored in the `SecurityContext`.
2. **Strict CORS Policy:** Restricted to designated trusted frontend origins with credential negotiation support.
3. **Stateless Sessions:** Sessions are disabled (`SessionCreationPolicy.STATELESS`), rejecting session hijacking vectors.
4. **BCrypt Hashing:** Passwords hashed with salt factor before persisting to MongoDB.
5. **Soft Delete Safety:** Soft delete ensures data integrity with no accidental cascading losses.

---

## 📊 Comprehensive Feature Verification Matrix
See [FEATURE_CHECKLIST.md](./FEATURE_CHECKLIST.md) for individual verification states of every single requirement.
