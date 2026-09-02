import React, {
  createContext,
  useContext,
  useState,
  useCallback,
  useEffect
} from 'react';

import api from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');

    if (!token) {
      setCurrentUser(null);
      setLoading(false);
      return;
    }

    const loadCurrentUser = async () => {
      try {
        const response = await api.get('/api/auth/me');

        const data = response.data;

        const user = {
          id: data.userId ?? data.id,
          name: data.name,
          email: data.email,
          role: data.role
            ? data.role.toUpperCase()
            : 'USER',
          createdAt: data.createdAt
        };

        setCurrentUser(user);

      } catch (error) {
        console.error(
          'Failed to load current user:',
          error
        );

        localStorage.removeItem('token');
        setCurrentUser(null);

      } finally {
        setLoading(false);
      }
    };

    loadCurrentUser();

  }, []);

  const login = useCallback(async (email, password) => {
    try {
      const response = await api.post(
        '/api/auth/login',
        {
          email,
          password
        }
      );

      const data = response.data;

      if (!data || !data.token) {
        return {
          ok: false,
          error: 'Login failed. No authentication token received.'
        };
      }

      const user = {
        id: data.userId ?? data.id,
        name: data.name,
        email: data.email,
        role: data.role
          ? data.role.toUpperCase()
          : 'USER',
        createdAt: data.createdAt
      };

      localStorage.setItem('token', data.token);

      setCurrentUser(user);

      return {
        ok: true,
        user
      };

    } catch (error) {
      console.error('Login error:', error);

      return {
        ok: false,
        error:
          error.response?.data?.message ||
          error.response?.data?.error ||
          'No account found for this email or the password is incorrect.'
      };
    }
  }, []);

  const register = useCallback(
    async (name, email, password) => {
      try {
        const response = await api.post(
          '/api/auth/register',
          {
            name,
            email,
            password
          }
        );

        const data = response.data;

        return {
          ok: true,
          message:
            data?.message ||
            'Account created successfully.'
        };

      } catch (error) {
        console.error(
          'Registration error:',
          error
        );

        return {
          ok: false,
          error:
            error.response?.data?.message ||
            error.response?.data?.error ||
            'Registration failed. Please try again.'
        };
      }
    },
    []
  );

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    setCurrentUser(null);
  }, []);

  const updateProfile = useCallback(
    async (name, email) => {
      try {
        const response = await api.put(
          '/api/auth/me',
          {
            name,
            email
          }
        );

        const data = response.data;

        const user = {
          id: data.userId ?? data.id,
          name: data.name,
          email: data.email,
          role: data.role
            ? data.role.toUpperCase()
            : 'USER',
          createdAt: data.createdAt
        };

        setCurrentUser(user);

        return {
          ok: true,
          user
        };

      } catch (error) {
        console.error(
          'Update profile error:',
          error
        );

        return {
          ok: false,
          error:
            error.response?.data?.message ||
            error.response?.data?.error ||
            'Failed to update profile.'
        };
      }
    },
    []
  );

  const changePassword = useCallback(
    async (currentPassword, newPassword) => {
      try {
        await api.put(
          '/api/auth/me/password',
          {
            currentPassword,
            newPassword
          }
        );

        return {
          ok: true
        };

      } catch (error) {
        console.error(
          'Change password error:',
          error
        );

        return {
          ok: false,
          error:
            error.response?.data?.message ||
            error.response?.data?.error ||
            'Failed to change password.'
        };
      }
    },
    []
  );

  const value = {
    currentUser,
    login,
    register,
    logout,
    updateProfile,
    changePassword,
    loading
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);

  if (!ctx) {
    throw new Error(
      'useAuth must be used inside AuthProvider'
    );
  }

  return ctx;
}