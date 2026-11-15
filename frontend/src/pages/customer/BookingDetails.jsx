import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../../services/api";
import "./BookingDetails.css";

function BookingDetails() {
  const { bookingId } = useParams();
  const navigate = useNavigate();

  const [booking, setBooking] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [historyLoading, setHistoryLoading] = useState(true);
  const [error, setError] = useState("");
  const [historyError, setHistoryError] = useState("");

  useEffect(() => {
    const fetchBooking = async () => {
      try {
        const response = await api.get(`/bookings/${bookingId}`);
        setBooking(response.data);
      } catch (err) {
        setError(
          err.response?.data?.message || "Unable to load booking details.",
        );
      } finally {
        setLoading(false);
      }
    };

    const fetchHistory = async () => {
      try {
        const response = await api.get(`/bookings/${bookingId}/history`);
        setHistory(response.data);
      } catch (err) {
        setHistoryError(
          err.response?.data?.message || "Unable to load booking history.",
        );
      } finally {
        setHistoryLoading(false);
      }
    };

    fetchBooking();
    fetchHistory();
  }, [bookingId]);

  const formatDate = (dateTime) => {
    if (!dateTime) return "Not available";

    return new Date(dateTime).toLocaleDateString("en-IN", {
      day: "numeric",
      month: "long",
      year: "numeric",
    });
  };

  const formatTime = (dateTime) => {
    if (!dateTime) return "Not available";

    return new Date(dateTime).toLocaleTimeString("en-IN", {
      hour: "numeric",
      minute: "2-digit",
      hour12: true,
    });
  };

  const formatStatus = (status) => {
    if (!status) return "Unknown";

    return status.replaceAll("_", " ");
  };

  if (loading) {
    return <div className="booking-details-message">Loading booking...</div>;
  }

  if (error || !booking) {
    return (
      <div className="booking-details-message">
        <p>{error || "Booking not found."}</p>
        <button onClick={() => navigate("/customer/dashboard")}>
          Back to Dashboard
        </button>
      </div>
    );
  }

  return (
    <div className="booking-details-page">
      <header className="details-header">
        <button
          className="back-button"
          onClick={() => navigate("/customer/dashboard")}
        >
          ← Back to Dashboard
        </button>

        <span className="booking-reference">Booking #{booking.bookingId}</span>
      </header>

      <section className="details-heading">
        <div>
          <p className="details-eyebrow">SERVICE BOOKING</p>
          <h1>{booking.serviceCategory}</h1>
          <p>Track your repair request and booking information.</p>
        </div>

        <span className={`booking-status ${booking.status?.toLowerCase()}`}>
          {formatStatus(booking.status)}
        </span>
      </section>

      <div className="details-grid">
        <section className="details-card">
          <h2>Service information</h2>

          <div className="detail-row">
            <span>Booking ID</span>
            <strong>#{booking.bookingId}</strong>
          </div>

          <div className="detail-row">
            <span>Service category</span>
            <strong>{booking.serviceCategory}</strong>
          </div>

          <div className="detail-row">
            <span>Service type</span>
            <strong>{booking.serviceType}</strong>
          </div>

          <div className="detail-row">
            <span>Issue description</span>
            <strong>{booking.issueDescription}</strong>
          </div>

          <div className="detail-row">
            <span>Booking created</span>
            <strong>{formatDate(booking.createdAt)}</strong>
          </div>
        </section>

        <section className="details-card">
          <h2>Appointment details</h2>

          <div className="detail-row">
            <span>Appointment date</span>
            <strong>{formatDate(booking.preferredDateTime)}</strong>
          </div>

          <div className="detail-row">
            <span>Start time</span>
            <strong>{formatTime(booking.preferredDateTime)}</strong>
          </div>

          <div className="detail-row">
            <span>End time</span>
            <strong>{formatTime(booking.preferredEndDateTime)}</strong>
          </div>

          <div className="detail-row">
            <span>Service address</span>
            <strong>{booking.serviceAddress}</strong>
          </div>
        </section>

        <section className="details-card engineer-card">
          <h2>Assigned engineer</h2>

          {booking.assignedEngineerName ? (
            <strong>{booking.assignedEngineerName}</strong>
          ) : (
            <div>
              <strong>Not assigned yet</strong>
              <p>
                Your request is waiting for an eligible service engineer. Please
                check again later.
              </p>
            </div>
          )}
        </section>

        <section className="details-card tracking-card">
          <h2>Booking tracking</h2>

          {historyLoading ? (
            <p className="history-message">Loading booking history...</p>
          ) : historyError ? (
            <p className="history-error">{historyError}</p>
          ) : history.length === 0 ? (
            <p className="history-message">
              No status history is available yet.
            </p>
          ) : (
            <div className="booking-timeline">
              {history.map((item, index) => (
                <div
                  className="timeline-item"
                  key={`${item.newStatus}-${item.changedAt}-${index}`}
                >
                  <div className="timeline-indicator">
                    <span className="timeline-dot" />
                    {index !== history.length - 1 && (
                      <span className="timeline-line" />
                    )}
                  </div>

                  <div className="timeline-content">
                    <strong>{formatStatus(item.newStatus)}</strong>

                    <span className="timeline-date">
                      {formatDate(item.changedAt)} at{" "}
                      {formatTime(item.changedAt)}
                    </span>

                    {item.note && <p>{item.note}</p>}

                    {item.changedByName && (
                      <span className="timeline-actor">
                        Updated by {item.changedByName}
                      </span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}

export default BookingDetails;
