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

  // Engineer directory state
  const [engineers, setEngineers] = useState([]);
  const [engineerSkills, setEngineerSkills] = useState({});
  const [engineersLoading, setEngineersLoading] = useState(true);
  const [engineersError, setEngineersError] = useState("");

  // Fetch all bookings
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

  // Fetch engineers and their assigned skills
  const fetchEngineers = useCallback(async () => {
    try {
      setEngineersLoading(true);
      setEngineersError("");

      const response = await api.get("/engineers");

      const engineerList = Array.isArray(response.data) ? response.data : [];

      setEngineers(engineerList);

      // Fetch skills for each engineer
      const skillResults = await Promise.all(
        engineerList.map(async (engineer) => {
          try {
            const skillResponse = await api.get(
              `/engineers/${engineer.engineerId}/skills`,
            );

            return {
              engineerId: engineer.engineerId,
              skills: Array.isArray(skillResponse.data)
                ? skillResponse.data
                : [],
            };
          } catch (err) {
            console.error(
              `Unable to load skills for engineer ${engineer.engineerId}`,
              err,
            );

            return {
              engineerId: engineer.engineerId,
              skills: [],
            };
          }
        }),
      );

      const skillsMap = {};

      skillResults.forEach((result) => {
        skillsMap[result.engineerId] = result.skills;
      });

      setEngineerSkills(skillsMap);
    } catch (err) {
      setEngineersError(
        err.response?.data?.message || "Unable to load engineer directory.",
      );
    } finally {
      setEngineersLoading(false);
    }
  }, []);

  // Initial dashboard loading
  useEffect(() => {
    fetchBookings();
    fetchEngineers();
  }, [fetchBookings, fetchEngineers]);

  // Booking statistics
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

  // Booking filters
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

  // Manual engineer assignment input
  const handleEngineerIdChange = (bookingId, value) => {
    setEngineerIds((previous) => ({
      ...previous,
      [bookingId]: value,
    }));
  };

  // Assign engineer to booking
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

  // Logout
  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/login");
  };

  return (
    <div className="admin-dashboard">
      {/* Sidebar */}
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

          <div className="admin-nav-item">
            <span>♙</span>
            <span>Engineers</span>
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

      {/* Main Content */}
      <main className="admin-main">
        {/* Header */}
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

        {/* Alerts */}
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

        {/* Booking Statistics */}
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

        {/* Booking Management */}
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

          {/* Booking Filters */}
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

          {/* Booking Table */}
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

        {/* Engineer Directory */}
        <section className="admin-bookings-section admin-engineers-section">
          <div className="admin-section-heading">
            <div>
              <h2>Engineer Directory</h2>
              <p>View engineer profiles, skills and current workload.</p>
            </div>

            <button
              className="admin-refresh-button"
              onClick={fetchEngineers}
              disabled={engineersLoading}
            >
              {engineersLoading ? "Loading..." : "↻ Refresh Engineers"}
            </button>
          </div>

          {engineersError && (
            <div className="admin-alert error" role="alert">
              {engineersError}
            </div>
          )}

          {engineersLoading ? (
            <div className="admin-state">Loading engineers...</div>
          ) : engineers.length === 0 ? (
            <div className="admin-state">No engineers found.</div>
          ) : (
            <div className="admin-engineer-grid">
              {engineers.map((engineer) => {
                const activeWorkload = bookings.filter(
                  (booking) =>
                    booking.assignedEngineerName === engineer.fullName &&
                    booking.status !== "COMPLETED" &&
                    booking.status !== "CANCELLED",
                ).length;

                const skills = engineerSkills[engineer.engineerId] || [];

                return (
                  <article
                    className="admin-engineer-card"
                    key={engineer.engineerId}
                  >
                    <div className="admin-engineer-card-header">
                      <div>
                        <h3>{engineer.fullName}</h3>

                        <span className="admin-cell-subtext">
                          Engineer ID: #{engineer.engineerId}
                        </span>
                      </div>

                      <span
                        className={`admin-availability ${
                          engineer.available ? "available" : "unavailable"
                        }`}
                      >
                        {engineer.available ? "Available" : "Unavailable"}
                      </span>
                    </div>

                    <div className="admin-engineer-details">
                      <p>
                        <strong>Email:</strong>{" "}
                        {engineer.email || "Not provided"}
                      </p>

                      <p>
                        <strong>Phone:</strong>{" "}
                        {engineer.phone || "Not provided"}
                      </p>

                      <p>
                        <strong>Service Area:</strong>{" "}
                        {engineer.serviceArea || "Not specified"}
                      </p>

                      <p>
                        <strong>Service Radius:</strong>{" "}
                        {engineer.serviceRadiusKm ?? "N/A"} km
                      </p>

                      <p>
                        <strong>Working Hours:</strong>{" "}
                        {engineer.workingHoursStart || "N/A"} -{" "}
                        {engineer.workingHoursEnd || "N/A"}
                      </p>
                    </div>

                    <div className="admin-engineer-skills">
                      <strong>Skills</strong>

                      <div className="admin-skill-tags">
                        {skills.length > 0 ? (
                          skills.map((skill) => (
                            <span
                              className="admin-skill-tag"
                              key={skill.skillId}
                            >
                              {skill.serviceCategory} · {skill.serviceType}
                            </span>
                          ))
                        ) : (
                          <span className="admin-cell-subtext">
                            No skills assigned
                          </span>
                        )}
                      </div>
                    </div>

                    <div className="admin-engineer-workload">
                      <span>Current Workload</span>

                      <strong>{activeWorkload}</strong>

                      <small>Non-completed assigned bookings</small>
                    </div>
                  </article>
                );
              })}
            </div>
          )}
        </section>
      </main>
    </div>
  );
}

export default AdminDashboard;
