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
  const [paymentLoading, setPaymentLoading] = useState(false);

  const [error, setError] = useState("");
  const [historyError, setHistoryError] = useState("");
  const [quotationError, setQuotationError] = useState("");
  const [actionError, setActionError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const [paymentMethod, setPaymentMethod] = useState("UPI");
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");
  const [processing, setProcessing] = useState(false);

  const getErrorMessage = (err, fallback) => {
    const data = err?.response?.data;

    if (typeof data === "string" && data.trim()) {
      return data;
    }

    return data?.message || data?.error || err?.message || fallback;
  };

  const getQuotationId = (value) => value?.quotationId ?? value?.id ?? null;

  const isNotFoundError = (err) => {
    const status = err?.response?.status;
    const message = String(
      err?.response?.data?.message || err?.response?.data?.error || "",
    ).toLowerCase();

    return status === 404 || message.includes("payment not found");
  };

  const loadPayment = async (quotationId) => {
    if (!quotationId) {
      setPayment(null);
      return null;
    }

    try {
      const response = await api.get(`/payments/quotations/${quotationId}`);

      setPayment(response.data);
      return response.data;
    } catch (err) {
      if (isNotFoundError(err)) {
        setPayment(null);
        return null;
      }

      throw err;
    }
  };

  // Load booking, history, quotation and feedback.
  useEffect(() => {
    let isActive = true;

    const loadBookingDetails = async () => {
      setLoading(true);
      setHistoryLoading(true);
      setQuotationLoading(true);

      setError("");
      setHistoryError("");
      setQuotationError("");
      setActionError("");
      setSuccessMessage("");

      setBooking(null);
      setHistory([]);
      setQuotation(null);
      setPayment(null);
      setFeedback(null);

      try {
        const bookingResponse = await api.get(`/bookings/${bookingId}`);

        if (!isActive) return;

        setBooking(bookingResponse.data);

        const [historyResult, quotationResult, feedbackResult] =
          await Promise.allSettled([
            api.get(`/bookings/${bookingId}/history`),
            api.get(`/quotations/bookings/${bookingId}`),
            api.get("/feedback/my"),
          ]);

        if (!isActive) return;

        // Booking history.
        if (historyResult.status === "fulfilled") {
          setHistory(
            Array.isArray(historyResult.value.data)
              ? historyResult.value.data
              : [],
          );
          setHistoryError("");
        } else {
          setHistoryError(
            getErrorMessage(
              historyResult.reason,
              "Unable to load booking history.",
            ),
          );
        }

        // Quotation.
        if (quotationResult.status === "fulfilled") {
          setQuotation(quotationResult.value.data);
          setQuotationError("");
        } else if (quotationResult.reason?.response?.status === 404) {
          // No quotation exists yet. This is normal.
          setQuotation(null);
          setQuotationError("");
        } else {
          setQuotationError(
            getErrorMessage(
              quotationResult.reason,
              "Unable to load quotation.",
            ),
          );
        }

        // Existing customer feedback.
        if (feedbackResult.status === "fulfilled") {
          const feedbackList = Array.isArray(feedbackResult.value.data)
            ? feedbackResult.value.data
            : [];

          const existingFeedback = feedbackList.find(
            (item) => Number(item.bookingId) === Number(bookingId),
          );

          setFeedback(existingFeedback || null);
        } else {
          console.error(
            "Unable to load existing feedback:",
            feedbackResult.reason,
          );
        }
      } catch (err) {
        if (!isActive) return;

        setError(getErrorMessage(err, "Unable to load booking details."));
      } finally {
        if (isActive) {
          setLoading(false);
          setHistoryLoading(false);
          setQuotationLoading(false);
        }
      }
    };

    if (bookingId) {
      loadBookingDetails();
    } else {
      setError("Invalid booking ID.");
      setLoading(false);
      setHistoryLoading(false);
      setQuotationLoading(false);
    }

    return () => {
      isActive = false;
    };
  }, [bookingId]);

  // Fetch payment only when the quotation has been approved.
  useEffect(() => {
    let isActive = true;

    const fetchPayment = async () => {
      const quotationId = getQuotationId(quotation);

      if (quotation?.status !== "APPROVED" || !quotationId) {
        setPayment(null);
        setPaymentLoading(false);
        return;
      }

      setPaymentLoading(true);

      try {
        const response = await api.get(`/payments/quotations/${quotationId}`);

        if (isActive) {
          setPayment(response.data);
          setActionError("");
        }
      } catch (err) {
        if (!isActive) return;

        if (isNotFoundError(err)) {
          // Payment has not been initiated yet.
          setPayment(null);
        } else {
          setActionError(
            getErrorMessage(err, "Unable to load payment details."),
          );
        }
      } finally {
        if (isActive) {
          setPaymentLoading(false);
        }
      }
    };

    fetchPayment();

    return () => {
      isActive = false;
    };
  }, [quotation]);

  const formatDate = (dateTime) => {
    if (!dateTime) return "Not available";

    const date = new Date(dateTime);

    if (Number.isNaN(date.getTime())) {
      return "Not available";
    }

    return date.toLocaleDateString("en-IN", {
      day: "numeric",
      month: "long",
      year: "numeric",
    });
  };

  const formatTime = (dateTime) => {
    if (!dateTime) return "Not available";

    const date = new Date(dateTime);

    if (Number.isNaN(date.getTime())) {
      return "Not available";
    }

    return date.toLocaleTimeString("en-IN", {
      hour: "numeric",
      minute: "2-digit",
      hour12: true,
    });
  };

  const formatStatus = (status) => {
    if (!status) return "Unknown";

    return status.replaceAll("_", " ");
  };

  const formatAmount = (amount) =>
    Number(amount || 0).toLocaleString("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 2,
    });

  // Customer approves or rejects the quotation.
  const handleQuotationResponse = async (status) => {
    const quotationId = getQuotationId(quotation);

    if (!quotationId || processing) return;

    setProcessing(true);
    setActionError("");
    setSuccessMessage("");

    try {
      const response = await api.put(`/quotations/${quotationId}/respond`, {
        status,
      });

      setQuotation(response.data);

      setSuccessMessage(
        status === "APPROVED"
          ? "Quotation approved successfully."
          : "Quotation rejected successfully.",
      );
    } catch (err) {
      setActionError(
        getErrorMessage(err, "Unable to update quotation. Please try again."),
      );
    } finally {
      setProcessing(false);
    }
  };

  // Create a payment for an approved quotation.
  const handleCreatePayment = async () => {
    const quotationId = getQuotationId(quotation);

    if (!quotationId || processing) return;

    setProcessing(true);
    setActionError("");
    setSuccessMessage("");

    try {
      const response = await api.post(`/payments/quotations/${quotationId}`, {
        paymentMethod: paymentMethod,
      });

      setPayment(response.data);
      setSuccessMessage("Payment initiated successfully.");
    } catch (err) {
      console.error("Payment initiation failed:", err);

      /*
       * If the payment was already created by a previous request,
       * retrieve that existing payment instead of leaving the
       * customer stuck on the Initiate Payment screen.
       */
      try {
        const existingPayment = await loadPayment(quotationId);

        if (existingPayment) {
          setSuccessMessage("An existing payment record was found and loaded.");
          setActionError("");
        } else {
          setActionError(
            getErrorMessage(
              err,
              "Unable to initiate payment. Please try again.",
            ),
          );
        }
      } catch (fetchError) {
        console.error("Unable to retrieve existing payment:", fetchError);

        setActionError(
          getErrorMessage(err, "Unable to initiate payment. Please try again."),
        );
      }
    } finally {
      setProcessing(false);
    }
  };

  // Simulate successful payment.
  const handleSimulatePayment = async () => {
    if (!payment?.paymentId || processing) return;

    setProcessing(true);
    setActionError("");
    setSuccessMessage("");

    try {
      const response = await api.post(
        `/payments/${payment.paymentId}/simulate`,
        {
          successful: true,
        },
      );

      setPayment(response.data);
      setSuccessMessage("Payment completed successfully.");
    } catch (err) {
      setActionError(
        getErrorMessage(err, "Unable to process simulated payment."),
      );
    } finally {
      setProcessing(false);
    }
  };

  // Submit customer feedback after completion.
  const handleFeedbackSubmit = async (event) => {
    event.preventDefault();

    if (processing) return;

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
      setActionError(getErrorMessage(err, "Unable to submit feedback."));
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

        <span
          className={`booking-status ${booking.status?.toLowerCase() || ""}`}
        >
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
                      {formatDate(item.changedAt)}
                      {" at "}
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

      {/* QUOTATION */}
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

      {/* PAYMENT */}
      {quotation?.status === "APPROVED" && (
        <section className="details-card workflow-card">
          <h2>Payment</h2>

          {paymentLoading ? (
            <p className="history-message">Loading payment details...</p>
          ) : !payment ? (
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

              {payment.status === "FAILED" && (
                <p className="workflow-error">
                  Payment failed. Please contact support.
                </p>
              )}
            </>
          )}
        </section>
      )}

      {/* FEEDBACK */}
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
