# GeoMeet UI

React + TypeScript + Vite frontend application with Material UI.

## Prerequisites

- Node.js 18+
- npm or yarn

## Setup

### 1. Install Dependencies

```bash
npm install
```

### 2. Configure Environment Variables

Create a `.env` file in the `ui` directory:

```env
VITE_API_BASE_URL=http://localhost:8080
```

### 3. Start Development Server

```bash
npm run dev
```

The application will start on `http://localhost:3000`

## Features

- **Login Page**: Material UI login form with username/email and password
- **Dashboard**: Protected route showing user information after login
- **Authentication**: JWT token-based authentication with localStorage
- **Protected Routes**: Automatic redirect to login if not authenticated
- **Error Handling**: User-friendly error messages

## Project Structure

```
src/
├── components/          # Reusable components
│   └── ProtectedRoute.tsx
├── contexts/            # React contexts
│   └── AuthContext.tsx
├── pages/               # Page components
│   ├── LoginPage.tsx
│   └── DashboardPage.tsx
├── services/            # API services
│   └── api.ts
├── App.tsx              # Main app component
└── main.tsx             # Entry point
```

## Default Test Users

- **Admin**: `admin` / `admin123`
- **Test User**: `testuser` / `test123`

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

## Technologies

- **React 18** - UI library
- **TypeScript** - Type safety
- **Material UI (MUI)** - Component library
- **React Router** - Routing
- **Axios** - HTTP client
- **Vite** - Build tool
