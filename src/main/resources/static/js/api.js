const API_BASE = '/api';

const Api = {
  // Cars API
  async getCars(params = {}) {
    const query = new URLSearchParams();
    if (params.category) query.append('category', params.category);
    if (params.status) query.append('status', params.status);
    if (params.fuelType) query.append('fuelType', params.fuelType);
    if (params.transmission) query.append('transmission', params.transmission);
    if (params.maxRate) query.append('maxRate', params.maxRate);
    if (params.search) query.append('search', params.search);

    const url = `${API_BASE}/cars${query.toString() ? '?' + query.toString() : ''}`;
    const res = await fetch(url);
    const json = await res.json();
    if (!res.ok) throw new Error(json.message || 'Failed to fetch cars');
    return json.data;
  },

  async getCarById(id) {
    const res = await fetch(`${API_BASE}/cars/${id}`);
    const json = await res.json();
    if (!res.ok) throw new Error(json.message || 'Failed to fetch car details');
    return json.data;
  },

  async createCar(carData) {
    const res = await fetch(`${API_BASE}/cars`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(carData),
    });
    const json = await res.json();
    if (!res.ok) throw new Error(json.message || 'Failed to create car');
    return json.data;
  },

  async updateCar(id, carData) {
    const res = await fetch(`${API_BASE}/cars/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(carData),
    });
    const json = await res.json();
    if (!res.ok) throw new Error(json.message || 'Failed to update car');
    return json.data;
  },

  async updateCarStatus(id, status) {
    const res = await fetch(`${API_BASE}/cars/${id}/status?status=${status}`, {
      method: 'PATCH',
    });
    const json = await res.json();
    if (!res.ok) throw new Error(json.message || 'Failed to update car status');
    return json.data;
  },

  async deleteCar(id) {
    const res = await fetch(`${API_BASE}/cars/${id}`, {
      method: 'DELETE',
    });
    const json = await res.json();
    if (!res.ok) throw new Error(json.message || 'Failed to delete car');
    return json.data;
  },

  // Customers API
  async getCustomers() {
    const res = await fetch(`${API_BASE}/customers`);
    const json = await res.json();
    if (!res.ok) throw new Error(json.message || 'Failed to fetch customers');
    return json.data;
  },

  async createCustomer(customerData) {
    const res = await fetch(`${API_BASE}/customers`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(customerData),
    });
    const json = await res.json();
    if (!res.ok) throw new Error(json.message || 'Failed to register customer');
    return json.data;
  },

  // Bookings API
  async getBookings(status) {
    const url = status ? `${API_BASE}/bookings?status=${status}` : `${API_BASE}/bookings`;
    const res = await fetch(url);
    const json = await res.json();
    if (!res.ok) throw new Error(json.message || 'Failed to fetch bookings');
    return json.data;
  },

  async getBookingByRef(ref) {
    const res = await fetch(`${API_BASE}/bookings/reference/${ref}`);
    const json = await res.json();
    if (!res.ok) throw new Error(json.message || 'Booking not found');
    return json.data;
  },

  async createBooking(bookingData) {
    const res = await fetch(`${API_BASE}/bookings`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(bookingData),
    });
    const json = await res.json();
    if (!res.ok) {
      if (json.details && json.details.length > 0) {
        throw new Error(json.details.join(', '));
      }
      throw new Error(json.message || 'Failed to create booking');
    }
    return json.data;
  },

  async startRental(id) {
    const res = await fetch(`${API_BASE}/bookings/${id}/start`, {
      method: 'PATCH',
    });
    const json = await res.json();
    if (!res.ok) throw new Error(json.message || 'Failed to start rental');
    return json.data;
  },

  async completeRental(id, returnData) {
    const res = await fetch(`${API_BASE}/bookings/${id}/return`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(returnData || {}),
    });
    const json = await res.json();
    if (!res.ok) throw new Error(json.message || 'Failed to complete rental');
    return json.data;
  },

  async cancelBooking(id, reason) {
    const url = reason ? `${API_BASE}/bookings/${id}/cancel?reason=${encodeURIComponent(reason)}` : `${API_BASE}/bookings/${id}/cancel`;
    const res = await fetch(url, {
      method: 'PATCH',
    });
    const json = await res.json();
    if (!res.ok) throw new Error(json.message || 'Failed to cancel booking');
    return json.data;
  },

  // Dashboard Stats
  async getDashboardStats() {
    const res = await fetch(`${API_BASE}/dashboard/stats`);
    const json = await res.json();
    if (!res.ok) throw new Error(json.message || 'Failed to fetch dashboard metrics');
    return json.data;
  },
};
