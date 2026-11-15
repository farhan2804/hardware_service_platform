
import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import api from "../../services/api";
import "./EngineerDashboard.css";

const ACTIVE_STATUSES = [
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
    ENGINEER_ASSIGNED: "Assigned",
    ACCEPTED: "Accepted",
    ON_THE_WAY: "On the Way",
    ARRIVED: "Arrived",
    QUOTATION_SHARED: "Quotation Shared",
    WORK_IN_PROGRESS: "Work in Progress",
    COMPLETED: "Completed",
    CANCELLED: "Cancelled",
};

function formatDate(dateTime) {
    if (!dateTime) return "Not scheduled";

    const date = new Date(dateTime);

    if (Number.isNaN(date.getTime())) return "Not scheduled";

    return date.toLocaleDateString("en-IN", {
        day: "numeric",
        month: "short",
        year: "numeric",
    });
}

function formatTime(dateTime) {
    if (!dateTime) return "";

    const date = new Date(dateTime);

    if (Number.isNaN(date.getTime())) return "";

    return date.toLocaleTimeString("en-IN", {
        hour: "2-digit",
        minute: "2-digit",
    });
}

function formatStatus(status) {
    return (
        STATUS_LABELS[status] ||
        status?.replaceAll("_", " ") ||
        "Unknown"
    );
}

function EngineerDashboard() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const [jobs, setJobs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [refreshing, setRefreshing] = useState(false);

    const [acceptingJobId, setAcceptingJobId] = useState(null);
    const [updatingJobId, setUpdatingJobId] = useState(null);
    const [actionError, setActionError] = useState("");

    const fetchJobs = useCallback(async (isRefresh = false) => {
        try {
            if (isRefresh) {
                setRefreshing(true);
            } else {
                setLoading(true);
            }

            setError("");

            const response = await api.get("/engineers/me/jobs");

            setJobs(
                Array.isArray(response.data) ? response.data : []
            );
        } catch (err) {
            console.error("Failed to fetch engineer jobs:", err);

            setError(
                err.response?.data?.message ||
                "Unable to load your assigned jobs. Please try again."
            );
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    }, []);

    useEffect(() => {
        fetchJobs();
    }, [fetchJobs]);

    // Accept an assigned booking.
    const handleAcceptJob = async (bookingId) => {
        try {
            setAcceptingJobId(bookingId);
            setActionError("");

            await api.post(
                `/engineers/me/jobs/${bookingId}/accept`
            );

            await fetchJobs(true);
        } catch (err) {
            console.error("Failed to accept job:", err);

            setActionError(
                err.response?.data?.message ||
                "Unable to accept this job. Please try again."
            );
        } finally {
            setAcceptingJobId(null);
        }
    };

    // Update an accepted booking's status.
    const handleUpdateStatus = async (bookingId, status) => {
        try {
            setUpdatingJobId(bookingId);
            setActionError("");

            await api.put(
                `/engineers/me/jobs/${bookingId}/status`,
                null,
                {
                    params: { status },
                }
            );

            await fetchJobs(true);
        } catch (err) {
            console.error("Failed to update job status:", err);

            setActionError(
                err.response?.data?.message ||
                "Unable to update job status. Please try again."
            );
        } finally {
            setUpdatingJobId(null);
        }
    };

    const handleLogout = () => {
        logout();
        navigate("/login");
    };

    const activeJobs = jobs.filter((job) =>
        ACTIVE_STATUSES.includes(job.status)
    );

    const completedJobs = jobs.filter(
        (job) => job.status === "COMPLETED"
    );

    const cancelledJobs = jobs.filter(
        (job) => job.status === "CANCELLED"
    );

    const displayName =
        user?.name ||
        user?.fullName ||
        user?.email ||
        "Service Engineer";

    const initials = displayName
        .split(/[\s@.]+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part[0].toUpperCase())
        .join("");

    return (
        <div className="engineer-dashboard">
            <aside className="engineer-sidebar">
                <div className="engineer-brand">
                    <div className="engineer-brand-icon">HC</div>
                    <span>HardwareCare</span>
                </div>

                <div className="engineer-sidebar-label">
                    WORKSPACE
                </div>

                <nav className="engineer-nav">
                    <div className="engineer-nav-item active">
                        <span className="nav-icon">▦</span>
                        Dashboard
                    </div>
                </nav>

                <div className="engineer-sidebar-bottom">
                    <div className="engineer-profile">
                        <div className="engineer-avatar">
                            {initials || "E"}
                        </div>

                        <div className="engineer-profile-info">
                            <strong>{displayName}</strong>
                            <span>Service Engineer</span>
                        </div>
                    </div>

                    <button
                        className="engineer-logout"
                        onClick={handleLogout}
                    >
                        <span>↪</span>
                        Logout
                    </button>
                </div>
            </aside>

            <main className="engineer-main">
                <header className="engineer-header">
                    <div>
                        <h1>Engineer Dashboard</h1>
                        <p>Manage your assigned service jobs.</p>
                    </div>

                    <button
                        className="engineer-refresh-button"
                        onClick={() => fetchJobs(true)}
                        disabled={refreshing}
                    >
                        <span
                            className={
                                refreshing ? "refresh-spinning" : ""
                            }
                        >
                            ↻
                        </span>

                        {refreshing ? "Refreshing..." : "Refresh"}
                    </button>
                </header>

                <section className="engineer-welcome">
                    <div>
                        <span className="engineer-welcome-tag">
                            ENGINEER WORKSPACE
                        </span>

                        <h2>
                            Welcome back, {displayName.split("@")[0]}!
                        </h2>

                        <p>
                            Here is an overview of your service
                            assignments and job progress.
                        </p>
                    </div>

                    <div className="engineer-welcome-icon">⚙</div>
                </section>

                <section className="engineer-stats-grid">
                    <div className="engineer-stat-card">
                        <div className="engineer-stat-top">
                            <span>Total Jobs</span>
                            <span className="engineer-stat-icon blue">
                                ▦
                            </span>
                        </div>

                        <strong>{jobs.length}</strong>
                        <small>All assigned bookings</small>
                    </div>

                    <div className="engineer-stat-card">
                        <div className="engineer-stat-top">
                            <span>Active Jobs</span>
                            <span className="engineer-stat-icon orange">
                                ◷
                            </span>
                        </div>

                        <strong>{activeJobs.length}</strong>
                        <small>Jobs currently in progress</small>
                    </div>

                    <div className="engineer-stat-card">
                        <div className="engineer-stat-top">
                            <span>Completed</span>
                            <span className="engineer-stat-icon green">
                                ✓
                            </span>
                        </div>

                        <strong>{completedJobs.length}</strong>
                        <small>Successfully completed jobs</small>
                    </div>

                    <div className="engineer-stat-card">
                        <div className="engineer-stat-top">
                            <span>Cancelled</span>
                            <span className="engineer-stat-icon red">
                                ×
                            </span>
                        </div>

                        <strong>{cancelledJobs.length}</strong>
                        <small>Cancelled bookings</small>
                    </div>
                </section>

                <section className="engineer-jobs-section">
                    <div className="engineer-section-header">
                        <div>
                            <h2>My Assigned Jobs</h2>
                            <p>
                                Bookings assigned to your engineer account.
                            </p>
                        </div>

                        <span className="engineer-job-count">
                            {jobs.length} {jobs.length === 1 ? "Job" : "Jobs"}
                        </span>
                    </div>

                    {loading ? (
                        <div className="engineer-state">
                            <div className="engineer-loader"></div>
                            <p>Loading your assigned jobs...</p>
                        </div>
                    ) : error ? (
                        <div className="engineer-state engineer-error">
                            <span>!</span>
                            <p>{error}</p>

                            <button onClick={() => fetchJobs()}>
                                Try Again
                            </button>
                        </div>
                    ) : jobs.length === 0 ? (
                        <div className="engineer-state">
                            <div className="engineer-empty-icon">▦</div>
                            <h3>No jobs assigned yet</h3>
                            <p>
                                New service assignments will appear here
                                when they are allocated to you.
                            </p>
                        </div>
                    ) : (
                        <div className="engineer-jobs-list">
                            {jobs.map((job) => (
                                <article
                                    className="engineer-job-card"
                                    key={job.bookingId}
                                >
                                    <div className="engineer-job-top">
                                        <div>
                                            <span className="engineer-booking-id">
                                                BOOKING #{job.bookingId}
                                            </span>

                                            <h3>{job.serviceCategory}</h3>
                                        </div>

                                        <span
                                            className={`engineer-status status-${job.status?.toLowerCase()}`}
                                        >
                                            {formatStatus(job.status)}
                                        </span>
                                    </div>

                                    <p className="engineer-job-description">
                                        {job.issueDescription}
                                    </p>

                                    <div className="engineer-job-details">
                                        <div>
                                            <span className="job-detail-icon">
                                                ▤
                                            </span>

                                            <div>
                                                <small>Service Type</small>
                                                <strong>
                                                    {job.serviceType?.replaceAll(
                                                        "_",
                                                        " "
                                                    )}
                                                </strong>
                                            </div>
                                        </div>

                                        <div>
                                            <span className="job-detail-icon">
                                                ▦
                                            </span>

                                            <div>
                                                <small>Appointment</small>

                                                <strong>
                                                    {formatDate(
                                                        job.preferredDateTime
                                                    )}
                                                </strong>

                                                <span className="job-detail-subtext">
                                                    {formatTime(
                                                        job.preferredDateTime
                                                    )}

                                                    {job.preferredEndDateTime
                                                        ? ` - ${formatTime(
                                                              job.preferredEndDateTime
                                                          )}`
                                                        : ""}
                                                </span>
                                            </div>
                                        </div>

                                        <div className="engineer-address-detail">
                                            <span className="job-detail-icon">
                                                ⌖
                                            </span>

                                            <div>
                                                <small>Service Address</small>
                                                <strong>
                                                    {job.serviceAddress}
                                                </strong>
                                            </div>
                                        </div>
                                    </div>

                                    {/* Accept assigned booking */}
                                    {job.status === "ENGINEER_ASSIGNED" && (
                                        <div className="engineer-job-actions">
                                            <button
                                                className="accept-job-button"
                                                onClick={() =>
                                                    handleAcceptJob(
                                                        job.bookingId
                                                    )
                                                }
                                                disabled={
                                                    acceptingJobId ===
                                                        job.bookingId ||
                                                    updatingJobId ===
                                                        job.bookingId
                                                }
                                            >
                                                {acceptingJobId ===
                                                job.bookingId
                                                    ? "Accepting..."
                                                    : "Accept Job"}
                                            </button>
                                        </div>
                                    )}

                                    {/* Start journey */}
                                    {job.status === "ACCEPTED" && (
                                        <div className="engineer-job-actions">
                                            <button
                                                className="accept-job-button"
                                                onClick={() =>
                                                    handleUpdateStatus(
                                                        job.bookingId,
                                                        "ON_THE_WAY"
                                                    )
                                                }
                                                disabled={
                                                    updatingJobId ===
                                                    job.bookingId
                                                }
                                            >
                                                {updatingJobId ===
                                                job.bookingId
                                                    ? "Updating..."
                                                    : "Start Journey"}
                                            </button>
                                        </div>
                                    )}

                                    {/* Mark arrival */}
                                    {job.status === "ON_THE_WAY" && (
                                        <div className="engineer-job-actions">
                                            <button
                                                className="accept-job-button"
                                                onClick={() =>
                                                    handleUpdateStatus(
                                                        job.bookingId,
                                                        "ARRIVED"
                                                    )
                                                }
                                                disabled={
                                                    updatingJobId ===
                                                    job.bookingId
                                                }
                                            >
                                                {updatingJobId ===
                                                job.bookingId
                                                    ? "Updating..."
                                                    : "Mark Arrived"}
                                            </button>
                                        </div>
                                    )}

                                    {/* Complete job */}
                                    {job.status === "WORK_IN_PROGRESS" && (
                                        <div className="engineer-job-actions">
                                            <button
                                                className="accept-job-button"
                                                onClick={() =>
                                                    handleUpdateStatus(
                                                        job.bookingId,
                                                        "COMPLETED"
                                                    )
                                                }
                                                disabled={
                                                    updatingJobId ===
                                                    job.bookingId
                                                }
                                            >
                                                {updatingJobId ===
                                                job.bookingId
                                                    ? "Updating..."
                                                    : "Complete Job"}
                                            </button>
                                        </div>
                                    )}

                                    {actionError && (
                                        <p className="engineer-action-error">
                                            {actionError}
                                        </p>
                                    )}
                                </article>
                            ))}
                        </div>
                    )}
                </section>
            </main>
        </div>
    );
}

export default EngineerDashboard;
