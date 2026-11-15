
import { createContext, useContext, useState } from "react";
import api from "../services/api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [token, setToken] = useState(
        () => localStorage.getItem("token")
    );

    const [user, setUser] = useState(() => {
        const savedUser = localStorage.getItem("user");
        return savedUser ? JSON.parse(savedUser) : null;
    });

    // Login
    const login = async (credentials) => {
        const response = await api.post("/auth/login", credentials);

        const data = response.data;

        const userData = {
            userId: data.userId,
            fullName: data.fullName,
            email: data.email,
            role: data.role,
        };

        localStorage.setItem("token", data.token);
        localStorage.setItem("user", JSON.stringify(userData));

        setToken(data.token);
        setUser(userData);

        return userData;
    };

    // Register customer
    const register = async (userData) => {
        const response = await api.post("/auth/register", userData);
        return response.data;
    };

    // Logout
    const logout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("user");

        setToken(null);
        setUser(null);
    };

    const value = {
        user,
        token,
        isAuthenticated: Boolean(token),
        login,
        register,
        logout,
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}

// Custom hook
export function useAuth() {
    return useContext(AuthContext);
}
