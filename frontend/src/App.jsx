import { Routes, Route, Navigate } from "react-router-dom";

import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";

import CustomerDashboard from "./pages/customer/CustomerDashboard";
import BookingForm from "./pages/customer/BookingForm";
import BookingDetails from "./pages/customer/BookingDetails";

import EngineerDashboard from "./pages/engineer/EngineerDashboard";
import AdminDashboard from "./pages/admin/AdminDashboard";

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/customer/dashboard" element={<CustomerDashboard />} />
      <Route path="/customer/book-service" element={<BookingForm />} />
      <Route
        path="/customer/bookings/:bookingId"
        element={<BookingDetails />}
      />
      <Route path="/engineer/dashboard" element={<EngineerDashboard />} />
      <Route path="/admin/dashboard" element={<AdminDashboard />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default App;
