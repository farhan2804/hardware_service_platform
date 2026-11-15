import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import "./Login.css";

function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");

    if (!formData.email.trim() || !formData.password) {
      setError("Please enter your email and password.");
      return;
    }

    try {
      setLoading(true);

      const user = await login({
        email: formData.email.trim(),
        password: formData.password,
      });

      const role = user.role?.toUpperCase();

      if (role === "CUSTOMER") {
        navigate("/customer/dashboard");
      } else if (role === "ENGINEER") {
        navigate("/engineer/dashboard");
      } else if (role === "ADMIN") {
        navigate("/admin/dashboard");
      } else {
        setError("Your account has an unrecognized role.");
      }
    } catch (err) {
      if (!err.response) {
        setError(
          "Unable to connect to the server. Please make sure the backend is running.",
        );
      } else {
        setError(
          err.response.data?.message ||
            "Login failed. Please check your credentials and try again.",
        );
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-brand">
          <div className="brand-icon">HS</div>
          <span>HardwareCare</span>
        </div>

        <div className="auth-heading">
          <h1>Welcome back</h1>
          <p>Sign in to manage your hardware services.</p>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="email">Email address</label>
            <input
              id="email"
              type="email"
              name="email"
              placeholder="Enter your email"
              value={formData.email}
              onChange={handleChange}
              autoComplete="email"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              name="password"
              placeholder="Enter your password"
              value={formData.password}
              onChange={handleChange}
              autoComplete="current-password"
              required
            />
          </div>

          {error && (
            <div className="auth-error" role="alert">
              {error}
            </div>
          )}

          <button type="submit" className="auth-button" disabled={loading}>
            {loading ? "Signing in..." : "Sign in"}
          </button>
        </form>

        <p className="auth-footer">
          Don't have an account? <Link to="/register">Create an account</Link>
        </p>
      </div>
    </div>
  );
}

export default Login;
