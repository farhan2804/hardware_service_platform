import { Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/auth/Login";
import CustomerDashboard from "./pages/customer/CustomerDashboard";
import BookingForm from "./pages/customer/BookingForm";

function EngineerDashboard() {
  return <h1>Engineer Dashboard</h1>;
}

function AdminDashboard() {
  return <h1>Admin Dashboard</h1>;
}

function RegisterPlaceholder() {
  return <h1>Registration Page</h1>;
}

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<RegisterPlaceholder />} />
      <Route path="/customer/dashboard" element={<CustomerDashboard />} />
      <Route path="/engineer/dashboard" element={<EngineerDashboard />} />
      <Route path="/admin/dashboard" element={<AdminDashboard />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
      <Route path="/customer/book-service" element={<BookingForm />} />
    </Routes>
  );
}

export default App;
