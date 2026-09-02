import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext.jsx'

import ProtectedRoute from './components/ProtectedRoute.jsx'
import UserLayout from './layouts/UserLayout.jsx'
import AdminLayout from './layouts/AdminLayout.jsx'

import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import ForgotPassword from './pages/ForgotPassword.jsx'
import NotFound from './pages/NotFound.jsx'

import Dashboard from './pages/user/Dashboard.jsx'
import StartInterview from './pages/user/StartInterview.jsx'
import InterviewSession from './pages/user/InterviewSession.jsx'
import Results from './pages/user/Results.jsx'
import ProgressAnalysis from './pages/user/ProgressAnalysis.jsx'
import InterviewHistory from './pages/user/InterviewHistory.jsx'
import Profile from './pages/user/Profile.jsx'

import AdminDashboard from './pages/admin/AdminDashboard.jsx'
import ManageUsers from './pages/admin/ManageUsers.jsx'
import ManageCompanies from './pages/admin/ManageCompanies.jsx'
import ManageCategories from './pages/admin/ManageCategories.jsx'
import Reports from './pages/admin/Reports.jsx'

export default function App() {

  const { currentUser, loading } = useAuth()

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-surface">
        <div className="text-center">
          <div className="text-lg font-semibold">
            Loading PrepGenius...
          </div>
        </div>
      </div>
    )
  }

  return (
    <Routes>

      {

}

      <Route
        path="/"
        element={
          <Navigate
            to={
              currentUser
                ? currentUser.role === 'ADMIN'
                  ? '/admin'
                  : '/dashboard'
                : '/login'
            }
            replace
          />
        }
      />

      {

}

      <Route
        path="/login"
        element={
          currentUser ? (
            <Navigate
              to={
                currentUser.role === 'ADMIN'
                  ? '/admin'
                  : '/dashboard'
              }
              replace
            />
          ) : (
            <Login />
          )
        }
      />

      <Route
        path="/register"
        element={
          currentUser ? (
            <Navigate
              to={
                currentUser.role === 'ADMIN'
                  ? '/admin'
                  : '/dashboard'
              }
              replace
            />
          ) : (
            <Register />
          )
        }
      />

      <Route
        path="/forgot-password"
        element={<ForgotPassword />}
      />

      {

}

      <Route
        element={
          <ProtectedRoute role="USER">
            <UserLayout />
          </ProtectedRoute>
        }
      >

        {}

        <Route
          path="/dashboard"
          element={<Dashboard />}
        />

        {}

        <Route
          path="/interview/setup"
          element={<StartInterview />}
        />

        {}

        <Route
          path="/interview/session"
          element={<InterviewSession />}
        />

        {}

        <Route
          path="/interview/results/:id"
          element={<Results />}
        />

        {}

        <Route
          path="/progress"
          element={<ProgressAnalysis />}
        />

        {}

        <Route
          path="/history"
          element={<InterviewHistory />}
        />

        {}

        <Route
          path="/profile"
          element={<Profile />}
        />

      </Route>

      {

}

      <Route
        element={
          <ProtectedRoute role="ADMIN">
            <AdminLayout />
          </ProtectedRoute>
        }
      >

        <Route
          path="/admin"
          element={<AdminDashboard />}
        />

        <Route
          path="/admin/users"
          element={<ManageUsers />}
        />

        <Route
          path="/admin/companies"
          element={<ManageCompanies />}
        />

        <Route
          path="/admin/categories"
          element={<ManageCategories />}
        />

        <Route
          path="/admin/reports"
          element={<Reports />}
        />

      </Route>

      {

}

      <Route
        path="*"
        element={<NotFound />}
      />

    </Routes>
  )
}