import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../services/api";
import "./AdminDashboard.css";

const FILTERS = [
  { label: "All Bookings", value: "ALL" },
  { label: "Awaiting Assignment", value: "AWAITING_ASSIGNMENT" },
  { label: "Active Jobs", value: "ACTIVE" },
  { label: "Completed", value: "COMPLETED" },
  { label: "Cancelled", value: "CANCELLED" },
];

const ACTIVE_STATUSES = [
  "ENGINEER_ASSIGNED",
  "ACCEPTED",
  "ON_THE_WAY",
  "ARRIVED",
  "QUOTATION_SHARED",
  "WORK_IN_PROGRESS",
];

function formatStatus(status = "") {
  return status
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function formatDate(dateTime) {
  if (!dateTime) return "Not scheduled";

  return new Date(dateTime).toLocaleString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function AdminDashboard() {
  const navigate = useNavigate();

  const [bookings, setBookings] = useState([]);
  const [filter, setFilter] = useState("ALL");
  const [engineerIds, setEngineerIds] = useState({});
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [assigningId, setAssigningId] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const fetchBookings = useCallback(async (isRefresh = false) => {
    try {
      setError("");
      if (isRefresh) {
        setRefreshing(true);
      } else {
        setLoading(true);
      }

      const response = await api.get("/admin/bookings");
      setBookings(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Unable to load bookings. Please check your admin login.",
      );
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    fetchBookings();
  }, [fetchBookings]);

  const stats = useMemo(
    () => ({
      total: bookings.length,
      awaiting: bookings.filter(
        (booking) => booking.status === "AWAITING_ASSIGNMENT",
      ).length,
      active: bookings.filter((booking) =>
        ACTIVE_STATUSES.includes(booking.status),
      ).length,
      completed: bookings.filter((booking) => booking.status === "COMPLETED")
        .length,
    }),
    [bookings],
  );

  const filteredBookings = useMemo(() => {
    switch (filter) {
      case "AWAITING_ASSIGNMENT":
        return bookings.filter(
          (booking) => booking.status === "AWAITING_ASSIGNMENT",
        );
      case "ACTIVE":
        return bookings.filter((booking) =>
          ACTIVE_STATUSES.includes(booking.status),
        );
      case "COMPLETED":
        return bookings.filter((booking) => booking.status === "COMPLETED");
      case "CANCELLED":
        return bookings.filter((booking) => booking.status === "CANCELLED");
      default:
        return bookings;
    }
  }, [bookings, filter]);

  const handleEngineerIdChange = (bookingId, value) => {
    setEngineerIds((previous) => ({
      ...previous,
      [bookingId]: value,
    }));
  };

  const handleAssignEngineer = async (bookingId) => {
    const engineerId = engineerIds[bookingId]?.trim();

    if (!engineerId || !/^\d+$/.test(engineerId)) {
      setError("Please enter a valid numeric engineer ID.");
      setSuccess("");
      return;
    }

    try {
      setAssigningId(bookingId);
      setError("");
      setSuccess("");

      await api.put(`/admin/bookings/${bookingId}/assign/${engineerId}`);

      setSuccess(`Engineer assigned successfully to Booking #${bookingId}.`);

      setEngineerIds((previous) => ({
        ...previous,
        [bookingId]: "",
      }));

      await fetchBookings(true);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Engineer assignment failed. Please verify the engineer ID.",
      );
    } finally {
      setAssigningId(null);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/login");
  };

  return (
    <div className="admin-dashboard">
      <aside className="admin-sidebar">
        <div className="admin-brand">
          <div className="admin-brand-icon">H</div>
          <span>ServiceDesk</span>
        </div>

        <div className="admin-sidebar-label">MANAGEMENT</div>

        <nav className="admin-nav">
          <div className="admin-nav-item active">
            <span>▦</span>
            <span>Bookings</span>
          </div>
        </nav>

        <div className="admin-sidebar-bottom">
          <div className="admin-profile">
            <div className="admin-avatar">A</div>
            <div>
              <strong>Administrator</strong>
              <span>Admin Account</span>
            </div>
          </div>

          <button className="admin-logout" onClick={handleLogout}>
            ↪ Logout
          </button>
        </div>
      </aside>

      <main className="admin-main">
        <header className="admin-header">
          <div>
            <span className="admin-header-label">ADMINISTRATION</span>
            <h1>Booking Management</h1>
            <p>Monitor service requests and manage engineer assignments.</p>
          </div>

          <button
            className="admin-refresh-button"
            onClick={() => fetchBookings(true)}
            disabled={refreshing}
          >
            {refreshing ? "Refreshing..." : "↻ Refresh"}
          </button>
        </header>

        {error && (
          <div className="admin-alert error" role="alert">
            {error}
          </div>
        )}

        {success && (
          <div className="admin-alert success" role="status">
            {success}
          </div>
        )}

        <section className="admin-stats-grid">
          <div className="admin-stat-card">
            <span>Total Bookings</span>
            <strong>{stats.total}</strong>
            <small>All service requests</small>
          </div>

          <div className="admin-stat-card">
            <span>Awaiting Assignment</span>
            <strong>{stats.awaiting}</strong>
            <small>Require an engineer</small>
          </div>

          <div className="admin-stat-card">
            <span>Active Jobs</span>
            <strong>{stats.active}</strong>
            <small>Currently in progress</small>
          </div>

          <div className="admin-stat-card">
            <span>Completed</span>
            <strong>{stats.completed}</strong>
            <small>Successfully completed</small>
          </div>
        </section>

        <section className="admin-bookings-section">
          <div className="admin-section-heading">
            <div>
              <h2>Service Bookings</h2>
              <p>Review customer requests and manage assignments.</p>
            </div>
            <span className="admin-booking-count">
              {filteredBookings.length} bookings
            </span>
          </div>

          <div className="admin-filters">
            {FILTERS.map((item) => (
              <button
                key={item.value}
                className={`admin-filter-button ${
                  filter === item.value ? "selected" : ""
                }`}
                onClick={() => setFilter(item.value)}
              >
                {item.label}
              </button>
            ))}
          </div>

          {loading ? (
            <div className="admin-state">Loading bookings...</div>
          ) : filteredBookings.length === 0 ? (
            <div className="admin-state">
              No bookings found for this filter.
            </div>
          ) : (
            <div className="admin-table-wrapper">
              <table className="admin-bookings-table">
                <thead>
                  <tr>
                    <th>Booking</th>
                    <th>Customer</th>
                    <th>Service</th>
                    <th>Appointment</th>
                    <th>Engineer</th>
                    <th>Status</th>
                    <th>Assignment</th>
                  </tr>
                </thead>

                <tbody>
                  {filteredBookings.map((booking) => (
                    <tr key={booking.bookingId}>
                      <td>
                        <strong>#{booking.bookingId}</strong>
                        <span className="admin-cell-subtext">
                          {formatDate(booking.createdAt)}
                        </span>
                      </td>

                      <td>
                        <strong>{booking.customerName}</strong>
                        <span className="admin-cell-subtext">
                          {booking.customerEmail}
                        </span>
                        <span className="admin-cell-subtext">
                          {booking.customerPhone}
                        </span>
                      </td>

                      <td>
                        <strong>{booking.serviceCategory}</strong>
                        <span className="admin-cell-subtext">
                          {formatStatus(booking.serviceType)}
                        </span>
                      </td>

                      <td>
                        <strong>{formatDate(booking.appointmentStart)}</strong>
                        <span className="admin-cell-subtext">
                          To {formatDate(booking.appointmentEnd)}
                        </span>
                      </td>

                      <td>
                        {booking.assignedEngineerName || (
                          <span className="admin-unassigned">Not assigned</span>
                        )}
                      </td>

                      <td>
                        <span
                          className={`admin-status status-${booking.status?.toLowerCase()}`}
                        >
                          {formatStatus(booking.status)}
                        </span>
                      </td>

                      <td>
                        {booking.status !== "COMPLETED" &&
                        booking.status !== "CANCELLED" ? (
                          <div className="admin-assignment">
                            <input
                              type="number"
                              min="1"
                              placeholder="Engineer ID"
                              value={engineerIds[booking.bookingId] || ""}
                              onChange={(event) =>
                                handleEngineerIdChange(
                                  booking.bookingId,
                                  event.target.value,
                                )
                              }
                            />
                            <button
                              onClick={() =>
                                handleAssignEngineer(booking.bookingId)
                              }
                              disabled={assigningId === booking.bookingId}
                            >
                              {assigningId === booking.bookingId
                                ? "Saving..."
                                : "Assign"}
                            </button>
                          </div>
                        ) : (
                          <span className="admin-cell-subtext">No action</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>
    </div>
  );
}

export default AdminDashboard;
