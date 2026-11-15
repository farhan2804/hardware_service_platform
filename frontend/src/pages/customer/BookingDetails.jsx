import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../../services/api";
import "./BookingDetails.css";

function BookingDetails() {
  const { bookingId } = useParams();
  const navigate = useNavigate();

  const [booking, setBooking] = useState(null);
  const [history, setHistory] = useState([]);
  const [quotation, setQuotation] = useState(null);
  const [payment, setPayment] = useState(null);
  const [feedback, setFeedback] = useState(null);

  const [loading, setLoading] = useState(true);
  const [historyLoading, setHistoryLoading] = useState(true);
  const [quotationLoading, setQuotationLoading] = useState(true);

  const [error, setError] = useState("");
  const [historyError, setHistoryError] = useState("");
  const [quotationError, setQuotationError] = useState("");
  const [actionError, setActionError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const [paymentMethod, setPaymentMethod] = useState("UPI");
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");

  const [processing, setProcessing] = useState(false);

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

    const fetchQuotation = async () => {
      try {
        const response = await api.get(`/quotations/bookings/${bookingId}`);
        setQuotation(response.data);
      } catch (err) {
        // A quotation may not have been created yet.
        if (err.response?.status !== 404) {
          setQuotationError(
            err.response?.data?.message || "Unable to load quotation.",
          );
        }
      } finally {
        setQuotationLoading(false);
      }
    };

    const fetchFeedback = async () => {
      try {
        const response = await api.get("/feedback/my");
        const feedbackList = Array.isArray(response.data) ? response.data : [];

        const existingFeedback = feedbackList.find(
          (item) => Number(item.bookingId) === Number(bookingId),
        );

        if (existingFeedback) {
          setFeedback(existingFeedback);
        }
      } catch (err) {
        console.error("Unable to check existing feedback:", err);
      }
    };

    fetchBooking();
    fetchHistory();
    fetchQuotation();
    fetchFeedback();
  }, [bookingId]);

  // Load payment details after a quotation has been approved.
  useEffect(() => {
    if (quotation?.status !== "APPROVED") return;

    const fetchPayment = async () => {
      try {
        const response = await api.get(
          `/payments/quotations/${quotation.quotationId}`,
        );
        setPayment(response.data);
      } catch (err) {
        // No payment exists until the customer initiates one.
        if (err.response?.status !== 404) {
          setActionError(
            err.response?.data?.message || "Unable to initiate payment.",
          );
        }
      }
    };

    fetchPayment();
  }, [quotation]);

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

  const formatAmount = (amount) => {
    return Number(amount || 0).toLocaleString("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 2,
    });
  };

  const handleQuotationResponse = async (status) => {
    setProcessing(true);
    setActionError("");
    setSuccessMessage("");

    try {
      const response = await api.put(
        `/quotations/${quotation.quotationId}/respond`,
        { status },
      );

      setQuotation(response.data);
      setSuccessMessage(
        status === "APPROVED"
          ? "Quotation approved successfully."
          : "Quotation rejected.",
      );
    } catch (err) {
      setActionError(
        err.response?.data?.message || "Unable to update quotation.",
      );
    } finally {
      setProcessing(false);
    }
  };

  const handleCreatePayment = async () => {
    setProcessing(true);
    setActionError("");
    setSuccessMessage("");

    try {
      const response = await api.post(
        `/payments/quotations/${quotation.quotationId}`,
        { paymentMethod },
      );

      setPayment(response.data);
      setSuccessMessage("Payment initiated successfully.");
    } catch (err) {
      setActionError(
        err.response?.data?.message || "Unable to initiate payment.",
      );
    } finally {
      setProcessing(false);
    }
  };

  const handleSimulatePayment = async () => {
    if (!payment?.paymentId) return;

    setProcessing(true);
    setActionError("");
    setSuccessMessage("");

    try {
      const response = await api.post(
        `/payments/${payment.paymentId}/simulate`,
        { successful: true },
      );

      setPayment(response.data);
      setSuccessMessage("Payment completed successfully.");
    } catch (err) {
      setActionError(
        err.response?.data?.message || "Unable to process simulated payment.",
      );
    } finally {
      setProcessing(false);
    }
  };

  const handleFeedbackSubmit = async (event) => {
    event.preventDefault();

    setProcessing(true);
    setActionError("");
    setSuccessMessage("");

    try {
      const response = await api.post(`/feedback/bookings/${bookingId}`, {
        rating,
        comment,
      });

      setFeedback(response.data);
      setSuccessMessage("Thank you! Your feedback has been submitted.");
    } catch (err) {
      setActionError(
        err.response?.data?.message || "Unable to submit feedback.",
      );
    } finally {
      setProcessing(false);
    }
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

      {/* QUOTATION SECTION */}
      <section className="details-card workflow-card">
        <h2>Service quotation</h2>

        {quotationLoading ? (
          <p className="history-message">Checking quotation...</p>
        ) : quotationError ? (
          <p className="history-error">{quotationError}</p>
        ) : !quotation ? (
          <p className="history-message">
            The engineer has not shared a quotation yet. It will appear here
            when available.
          </p>
        ) : (
          <>
            <div className="detail-row">
              <span>Labour cost</span>
              <strong>{formatAmount(quotation.labourCost)}</strong>
            </div>

            <div className="detail-row">
              <span>Parts cost</span>
              <strong>{formatAmount(quotation.partsCost)}</strong>
            </div>

            <div className="detail-row">
              <span>Additional charges</span>
              <strong>{formatAmount(quotation.additionalCharges)}</strong>
            </div>

            <div className="detail-row quotation-total">
              <span>Total amount</span>
              <strong>{formatAmount(quotation.totalAmount)}</strong>
            </div>

            {quotation.remarks && (
              <p className="quotation-remarks">
                <strong>Engineer remarks:</strong> {quotation.remarks}
              </p>
            )}

            <p className="workflow-status">
              Quotation status:{" "}
              <strong>{formatStatus(quotation.status)}</strong>
            </p>

            {quotation.status === "PENDING" && (
              <div className="workflow-actions">
                <button
                  className="workflow-primary-button"
                  onClick={() => handleQuotationResponse("APPROVED")}
                  disabled={processing}
                >
                  {processing ? "Please wait..." : "Approve quotation"}
                </button>

                <button
                  className="workflow-secondary-button"
                  onClick={() => handleQuotationResponse("REJECTED")}
                  disabled={processing}
                >
                  Reject quotation
                </button>
              </div>
            )}
          </>
        )}
      </section>

      {/* PAYMENT SECTION */}
      {quotation?.status === "APPROVED" && (
        <section className="details-card workflow-card">
          <h2>Payment</h2>

          {!payment ? (
            <>
              <p>
                Your quotation has been approved. Initiate payment to proceed.
              </p>

              <label className="workflow-label">Payment method</label>

              <select
                className="workflow-select"
                value={paymentMethod}
                onChange={(event) => setPaymentMethod(event.target.value)}
              >
                <option value="UPI">UPI</option>
              </select>

              <button
                className="workflow-primary-button"
                onClick={handleCreatePayment}
                disabled={processing}
              >
                {processing ? "Please wait..." : "Initiate payment"}
              </button>
            </>
          ) : (
            <>
              <div className="detail-row">
                <span>Payment ID</span>
                <strong>#{payment.paymentId}</strong>
              </div>

              <div className="detail-row">
                <span>Amount</span>
                <strong>{formatAmount(payment.amount)}</strong>
              </div>

              <div className="detail-row">
                <span>Payment method</span>
                <strong>{payment.paymentMethod}</strong>
              </div>

              <div className="detail-row">
                <span>Transaction reference</span>
                <strong>
                  {payment.transactionReference || "Not available"}
                </strong>
              </div>

              <p className="workflow-status">
                Payment status: <strong>{formatStatus(payment.status)}</strong>
              </p>

              {payment.status === "PENDING" && (
                <button
                  className="workflow-primary-button"
                  onClick={handleSimulatePayment}
                  disabled={processing}
                >
                  {processing ? "Processing..." : "Simulate successful payment"}
                </button>
              )}

              {payment.status === "SUCCESSFUL" && (
                <p className="workflow-success">
                  Payment completed successfully.
                </p>
              )}
            </>
          )}
        </section>
      )}

      {/* FEEDBACK SECTION */}
      {booking.status === "COMPLETED" && (
        <section className="details-card workflow-card">
          <h2>Service feedback</h2>

          {feedback ? (
            <div className="feedback-submitted">
              <p className="workflow-success">
                Your feedback has been submitted.
              </p>

              <p>
                <strong>Rating:</strong> {feedback.rating}/5
              </p>

              <p>
                <strong>Comment:</strong>{" "}
                {feedback.comment || "No comment provided."}
              </p>
            </div>
          ) : (
            <form onSubmit={handleFeedbackSubmit}>
              <p>
                How was your service experience? Share your feedback with us.
              </p>

              <label className="workflow-label">Your rating</label>

              <div className="rating-options">
                {[1, 2, 3, 4, 5].map((value) => (
                  <button
                    type="button"
                    key={value}
                    className={
                      rating === value
                        ? "rating-button selected"
                        : "rating-button"
                    }
                    onClick={() => setRating(value)}
                  >
                    {value} ★
                  </button>
                ))}
              </div>

              <label className="workflow-label" htmlFor="feedback-comment">
                Your comments
              </label>

              <textarea
                id="feedback-comment"
                className="workflow-textarea"
                value={comment}
                onChange={(event) => setComment(event.target.value)}
                placeholder="Tell us about your experience..."
                rows={4}
              />

              <button
                type="submit"
                className="workflow-primary-button"
                disabled={processing}
              >
                {processing ? "Submitting..." : "Submit feedback"}
              </button>
            </form>
          )}
        </section>
      )}

      {actionError && <p className="workflow-error">{actionError}</p>}

      {successMessage && <p className="workflow-success">{successMessage}</p>}
    </div>
  );
}

export default BookingDetails;
