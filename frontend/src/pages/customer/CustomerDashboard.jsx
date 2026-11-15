import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import api from "../../services/api";
import "./CustomerDashboard.css";

const ACTIVE_STATUSES = [
  "REQUEST_RECEIVED",
  "AWAITING_ASSIGNMENT",
  "ENGINEER_ASSIGNED",
  "ACCEPTED",
  "ON_THE_WAY",
  "ARRIVED",
  "QUOTATION_SHARED",
  "WORK_IN_PROGRESS",
];

const STATUS_LABELS = {
  REQUEST_RECEIVED: "Request Received",
  AWAITING_ASSIGNMENT: "Awaiting Assignment",
  ENGINEER_ASSIGNED: "Engineer Assigned",
  ACCEPTED: "Accepted",
  ON_THE_WAY: "On The Way",
  ARRIVED: "Arrived",
  QUOTATION_SHARED: "Quotation Shared",
  WORK_IN_PROGRESS: "Work In Progress",
  COMPLETED: "Completed",
  CANCELLED: "Cancelled",
};

function formatDate(dateString) {
  if (!dateString) return "Not scheduled";

  return new Date(dateString).toLocaleDateString("en-IN", {
    day: "numeric",
    month: "short",
    year: "numeric",
  });
}

function formatTime(dateString) {
  if (!dateString) return "";

  return new Date(dateString).toLocaleTimeString("en-IN", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: true,
  });
}

function formatStatus(status) {
  return STATUS_LABELS[status] || status?.replaceAll("_", " ") || "Unknown";
}

function CustomerDashboard() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const fetchBookings = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/bookings/my");

      setBookings(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      if (err.response?.status === 401 || err.response?.status === 403) {
        setError("Your session may have expired. Please sign in again.");
      } else {
        setError(
          err.response?.data?.message ||
            "Unable to load your bookings. Please try again.",
        );
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchBookings();
  }, [fetchBookings]);

  const totalBookings = bookings.length;

  const activeBookings = bookings.filter((booking) =>
    ACTIVE_STATUSES.includes(booking.status),
  ).length;

  const completedBookings = bookings.filter(
    (booking) => booking.status === "COMPLETED",
  ).length;

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div className="customer-dashboard">
      {/* Sidebar */}
      <aside className="dashboard-sidebar">
        <div className="dashboard-brand">
          <div className="brand-icon">HC</div>
          <span>HardwareCare</span>
        </div>

        <nav className="sidebar-navigation">
          <p className="sidebar-label">MENU</p>

          <button className="sidebar-link active">
            <span>▦</span>
            Dashboard
          </button>

          <button
            className="sidebar-link"
            onClick={() => navigate("/customer/book-service")}
          >
            <span>＋</span>
            Book a Service
          </button>
        </nav>

        <div className="sidebar-bottom">
          <div className="sidebar-user">
            <div className="user-avatar">
              {user?.fullName?.charAt(0)?.toUpperCase() || "C"}
            </div>
            <div className="sidebar-user-info">
              <strong>{user?.fullName || "Customer"}</strong>
              <span>Customer</span>
            </div>
          </div>

          <button className="logout-button" onClick={handleLogout}>
            ↪ Sign out
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="dashboard-main">
        <header className="dashboard-header">
          <div>
            <h1>Dashboard</h1>
            <p>Manage your hardware repair services.</p>
          </div>

          <button
            className="header-book-button"
            onClick={() => navigate("/customer/book-service")}
          >
            + Book a Service
          </button>
        </header>

        <section className="welcome-section">
          <div>
            <h2>
              Welcome back, {user?.fullName?.split(" ")[0] || "Customer"}!
            </h2>
            <p>Here's an overview of your service bookings.</p>
          </div>
          <div className="welcome-icon">⚙</div>
        </section>

        {/* Statistics */}
        <section className="stats-grid">
          <div className="stat-card">
            <div className="stat-icon total-icon">▦</div>
            <div>
              <p>Total Bookings</p>
              <h3>{loading ? "—" : totalBookings}</h3>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon active-icon">◷</div>
            <div>
              <p>Active Bookings</p>
              <h3>{loading ? "—" : activeBookings}</h3>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon completed-icon">✓</div>
            <div>
              <p>Completed</p>
              <h3>{loading ? "—" : completedBookings}</h3>
            </div>
          </div>
        </section>

        {/* Bookings */}
        <section className="bookings-section">
          <div className="section-heading">
            <div>
              <h2>Recent Bookings</h2>
              <p>Track your hardware service requests.</p>
            </div>

            <button
              className="refresh-button"
              onClick={fetchBookings}
              disabled={loading}
            >
              ↻ Refresh
            </button>
          </div>

          {loading && (
            <div className="dashboard-message">Loading your bookings...</div>
          )}

          {!loading && error && (
            <div className="dashboard-error">
              <p>{error}</p>
              <button onClick={fetchBookings}>Try Again</button>
            </div>
          )}

          {!loading && !error && bookings.length === 0 && (
            <div className="empty-state">
              <div className="empty-icon">⌕</div>
              <h3>No bookings yet</h3>
              <p>
                You haven't requested any hardware services. Book your first
                service to get started.
              </p>
              <button
                className="header-book-button"
                onClick={() => navigate("/customer/book-service")}
              >
                Book Your First Service
              </button>
            </div>
          )}

          {!loading && !error && bookings.length > 0 && (
            <div className="booking-list">
              {bookings.map((booking) => (
                <article className="booking-card" key={booking.bookingId}>
                  <div className="booking-card-top">
                    <div className="booking-service">
                      <div className="booking-service-icon">⚙</div>
                      <div>
                        <h3>{booking.serviceCategory}</h3>
                        <span>Booking #{booking.bookingId}</span>
                      </div>
                    </div>

                    <span
                      className={`status-badge status-${booking.status?.toLowerCase()}`}
                    >
                      {formatStatus(booking.status)}
                    </span>
                  </div>

                  <p className="booking-description">
                    {booking.issueDescription}
                  </p>

                  <div className="booking-details">
                    <div>
                      <span>Appointment</span>
                      <strong>{formatDate(booking.preferredDateTime)}</strong>
                      <small>
                        {formatTime(booking.preferredDateTime)}
                        {" - "}
                        {formatTime(booking.preferredEndDateTime)}
                      </small>
                    </div>

                    <div>
                      <span>Service Engineer</span>
                      <strong>
                        {booking.assignedEngineerName || "Not assigned yet"}
                      </strong>
                    </div>

                    <div>
                      <span>Service Type</span>
                      <strong>{booking.serviceType}</strong>
                    </div>
                  </div>

                  <div className="booking-card-footer">
                    <span className="booking-address">
                      📍 {booking.serviceAddress}
                    </span>

                    <span className="booking-created">
                      Created {formatDate(booking.createdAt)}
                    </span>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
      </main>
    </div>
  );
}

export default CustomerDashboard;
