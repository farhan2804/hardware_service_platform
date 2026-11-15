import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import api from "../../services/api";
import "./BookingForm.css";

const initialForm = {
  serviceCategoryId: "",
  serviceType: "REPAIR",
  issueDescription: "",
  preferredDateTime: "",
  preferredEndDateTime: "",
  serviceAddress: "",
  latitude: "",
  longitude: "",
};

function getLocalDateTimeMinimum() {
  const now = new Date();
  const localDate = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 16);
}

function BookingForm() {
  const navigate = useNavigate();

  const [categories, setCategories] = useState([]);
  const [formData, setFormData] = useState(initialForm);
  const [loadingCategories, setLoadingCategories] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        setLoadingCategories(true);
        const response = await api.get("/categories");
        setCategories(Array.isArray(response.data) ? response.data : []);
      } catch (err) {
        setError(
          err.response?.data?.message ||
            "Unable to load service categories. Please try again.",
        );
      } finally {
        setLoadingCategories(false);
      }
    };

    fetchCategories();
  }, []);

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
    setSuccess("");

    const start = new Date(formData.preferredDateTime);
    const end = new Date(formData.preferredEndDateTime);

    if (start <= new Date()) {
      setError("Please select a future appointment date and time.");
      return;
    }

    if (end <= start) {
      setError("Appointment end time must be after the start time.");
      return;
    }

    const payload = {
      serviceCategoryId: Number(formData.serviceCategoryId),
      serviceType: formData.serviceType,
      issueDescription: formData.issueDescription.trim(),
      preferredDateTime: formData.preferredDateTime,
      preferredEndDateTime: formData.preferredEndDateTime,
      serviceAddress: formData.serviceAddress.trim(),
      latitude: Number(formData.latitude),
      longitude: Number(formData.longitude),
    };

    try {
      setSubmitting(true);

      const response = await api.post("/bookings", payload);

      const bookingId = response.data?.bookingId;

      setSuccess(
        bookingId
          ? `Booking #${bookingId} created successfully!`
          : "Your service booking was created successfully!",
      );

      setFormData(initialForm);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          (typeof err.response?.data === "string"
            ? err.response.data
            : "Unable to create your booking. Please check your details and try again."),
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="booking-page">
      <div className="booking-container">
        <header className="booking-page-header">
          <Link to="/customer/dashboard" className="back-link">
            ← Back to Dashboard
          </Link>

          <div className="booking-heading">
            <h1>Book a Service</h1>
            <p>
              Tell us what needs repair and schedule a convenient appointment.
            </p>
          </div>
        </header>

        <form className="booking-form" onSubmit={handleSubmit}>
          <section className="booking-form-section">
            <div className="form-section-heading">
              <span className="section-number">01</span>
              <div>
                <h2>Service Details</h2>
                <p>Select the hardware service you need.</p>
              </div>
            </div>

            <div className="booking-field">
              <label htmlFor="serviceCategoryId">Service Category *</label>

              <select
                id="serviceCategoryId"
                name="serviceCategoryId"
                value={formData.serviceCategoryId}
                onChange={handleChange}
                required
                disabled={loadingCategories}
              >
                <option value="">
                  {loadingCategories
                    ? "Loading categories..."
                    : "Select a service category"}
                </option>

                {categories
                  .filter((category) => category.active !== false)
                  .map((category) => (
                    <option key={category.id} value={category.id}>
                      {category.name}
                    </option>
                  ))}
              </select>

              {!loadingCategories && categories.length === 0 && (
                <small className="field-hint">
                  No service categories are currently available.
                </small>
              )}
            </div>

            <div className="booking-field">
              <label htmlFor="serviceType">Service Type *</label>

              <select
                id="serviceType"
                name="serviceType"
                value={formData.serviceType}
                onChange={handleChange}
                required
              >
                <option value="REPAIR">Repair</option>
                <option value="INSTALLATION">Installation</option>
                <option value="MAINTENANCE">Maintenance</option>
                <option value="DIAGNOSIS">Diagnosis</option>
              </select>
            </div>

            <div className="booking-field">
              <label htmlFor="issueDescription">Describe Your Issue *</label>

              <textarea
                id="issueDescription"
                name="issueDescription"
                value={formData.issueDescription}
                onChange={handleChange}
                placeholder="Describe the hardware problem in detail..."
                rows={5}
                maxLength={1000}
                required
              />

              <small className="character-count">
                {formData.issueDescription.length}/1000 characters
              </small>
            </div>
          </section>

          <section className="booking-form-section">
            <div className="form-section-heading">
              <span className="section-number">02</span>
              <div>
                <h2>Appointment Schedule</h2>
                <p>Choose your preferred service time.</p>
              </div>
            </div>

            <div className="booking-fields-grid">
              <div className="booking-field">
                <label htmlFor="preferredDateTime">Start Date & Time *</label>

                <input
                  type="datetime-local"
                  id="preferredDateTime"
                  name="preferredDateTime"
                  value={formData.preferredDateTime}
                  onChange={handleChange}
                  min={getLocalDateTimeMinimum()}
                  required
                />
              </div>

              <div className="booking-field">
                <label htmlFor="preferredEndDateTime">End Date & Time *</label>

                <input
                  type="datetime-local"
                  id="preferredEndDateTime"
                  name="preferredEndDateTime"
                  value={formData.preferredEndDateTime}
                  onChange={handleChange}
                  min={formData.preferredDateTime || getLocalDateTimeMinimum()}
                  required
                />
              </div>
            </div>
          </section>

          <section className="booking-form-section">
            <div className="form-section-heading">
              <span className="section-number">03</span>
              <div>
                <h2>Service Location</h2>
                <p>Where should our engineer visit?</p>
              </div>
            </div>

            <div className="booking-field">
              <label htmlFor="serviceAddress">Complete Service Address *</label>

              <textarea
                id="serviceAddress"
                name="serviceAddress"
                value={formData.serviceAddress}
                onChange={handleChange}
                placeholder="House number, street, area, city, PIN code..."
                rows={3}
                maxLength={500}
                required
              />
            </div>

            <div className="booking-fields-grid">
              <div className="booking-field">
                <label htmlFor="latitude">Latitude *</label>
                <input
                  type="number"
                  id="latitude"
                  name="latitude"
                  value={formData.latitude}
                  onChange={handleChange}
                  placeholder="e.g. 22.8046"
                  min="-90"
                  max="90"
                  step="any"
                  required
                />
              </div>

              <div className="booking-field">
                <label htmlFor="longitude">Longitude *</label>
                <input
                  type="number"
                  id="longitude"
                  name="longitude"
                  value={formData.longitude}
                  onChange={handleChange}
                  placeholder="e.g. 86.2029"
                  min="-180"
                  max="180"
                  step="any"
                  required
                />
              </div>
            </div>

            <p className="location-hint">
              Coordinates help us find an available service engineer near your
              location.
            </p>
          </section>

          {error && (
            <div className="booking-alert booking-alert-error" role="alert">
              {error}
            </div>
          )}

          {success && (
            <div className="booking-alert booking-alert-success" role="status">
              {success}
              <button
                type="button"
                onClick={() => navigate("/customer/dashboard")}
              >
                View My Bookings
              </button>
            </div>
          )}

          <div className="booking-form-actions">
            <button
              type="button"
              className="cancel-booking-button"
              onClick={() => navigate("/customer/dashboard")}
            >
              Cancel
            </button>

            <button
              type="submit"
              className="submit-booking-button"
              disabled={
                submitting || loadingCategories || categories.length === 0
              }
            >
              {submitting ? "Submitting..." : "Submit Booking →"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default BookingForm;
