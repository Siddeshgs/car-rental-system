const App = {
  currentFilterCategory: null,
  selectedCarForBooking: null,
  editingCarId: null,

  async init() {
    this.bindEvents();
    this.setDefaultBookingDates();
    await this.loadCars();
    await this.loadDashboardStats();
  },

  bindEvents() {
    // Navigation tabs
    document.querySelectorAll('.nav-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const tab = e.currentTarget.dataset.tab;
        this.switchTab(tab);
      });
    });

    // Category chips
    document.querySelectorAll('.chip').forEach(chip => {
      chip.addEventListener('click', (e) => {
        document.querySelectorAll('.chip').forEach(c => c.classList.remove('active'));
        e.currentTarget.classList.add('active');
        this.currentFilterCategory = e.currentTarget.dataset.category || null;
        this.loadCars();
      });
    });

    // Search filter bar
    document.getElementById('search-btn')?.addEventListener('click', () => {
      this.loadCars();
    });

    document.getElementById('search-input')?.addEventListener('keyup', (e) => {
      if (e.key === 'Enter') this.loadCars();
    });

    // Booking modal calculation triggers
    document.getElementById('booking-start-date')?.addEventListener('change', () => this.updateBookingCalculation());
    document.getElementById('booking-end-date')?.addEventListener('change', () => this.updateBookingCalculation());
    document.getElementById('booking-insurance')?.addEventListener('change', () => this.updateBookingCalculation());
    document.getElementById('booking-promo-btn')?.addEventListener('click', () => this.updateBookingCalculation());
  },

  setDefaultBookingDates() {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const returnDate = new Date(today);
    returnDate.setDate(returnDate.getDate() + 4);

    const startInput = document.getElementById('search-start-date');
    const endInput = document.getElementById('search-end-date');
    if (startInput) startInput.value = tomorrow.toISOString().split('T')[0];
    if (endInput) endInput.value = returnDate.toISOString().split('T')[0];
  },

  switchTab(tabId) {
    document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
    document.querySelector(`[data-tab="${tabId}"]`)?.classList.add('active');

    document.querySelectorAll('.tab-content').forEach(c => c.style.display = 'none');
    const target = document.getElementById(`tab-${tabId}`);
    if (target) target.style.display = 'block';

    if (tabId === 'fleet') this.loadCars();
    if (tabId === 'dashboard') {
      this.loadDashboardStats();
      this.loadAdminBookings();
      this.loadAdminFleet();
      this.loadAdminCustomers();
    }
    if (tabId === 'bookings') this.loadUserBookings();
  },

  async loadCars() {
    try {
      const search = document.getElementById('search-input')?.value;
      const fuelType = document.getElementById('filter-fuel')?.value;
      const transmission = document.getElementById('filter-transmission')?.value;
      const maxRate = document.getElementById('filter-max-price')?.value;

      const cars = await Api.getCars({
        category: this.currentFilterCategory,
        fuelType: fuelType || null,
        transmission: transmission || null,
        maxRate: maxRate || null,
        search: search || null,
      });

      const grid = document.getElementById('cars-grid');
      if (!grid) return;

      if (!cars || cars.length === 0) {
        grid.innerHTML = `
          <div style="grid-column: 1 / -1; text-align:center; padding: 4rem 1rem;">
            <p style="color:var(--text-secondary); font-size:1.2rem; margin-bottom:1rem;">No vehicles found matching your criteria.</p>
            <button class="btn btn-secondary" onclick="App.resetFilters()">Clear Filters</button>
          </div>
        `;
        return;
      }

      grid.innerHTML = cars.map(car => UI.renderCarCard(car)).join('');
    } catch (err) {
      UI.toast(err.message, 'error');
    }
  },

  resetFilters() {
    document.getElementById('search-input').value = '';
    document.getElementById('filter-fuel').value = '';
    document.getElementById('filter-transmission').value = '';
    document.getElementById('filter-max-price').value = '';
    this.currentFilterCategory = null;
    document.querySelectorAll('.chip').forEach(c => c.classList.remove('active'));
    document.querySelector('.chip[data-category=""]')?.classList.add('active');
    this.loadCars();
  },

  async openBookingModal(carId) {
    try {
      const car = await Api.getCarById(carId);
      this.selectedCarForBooking = car;

      document.getElementById('modal-car-name').textContent = `${car.make} ${car.model} (${car.year})`;
      document.getElementById('modal-car-rate').textContent = `${UI.formatCurrency(car.dailyRate)} / day`;
      document.getElementById('modal-car-image').src = car.imageUrl || 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80';

      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      const returnDate = new Date(tomorrow);
      returnDate.setDate(returnDate.getDate() + 3);

      document.getElementById('booking-start-date').value = tomorrow.toISOString().split('T')[0];
      document.getElementById('booking-end-date').value = returnDate.toISOString().split('T')[0];

      this.updateBookingCalculation();
      UI.openModal('booking-modal');
    } catch (err) {
      UI.toast(err.message, 'error');
    }
  },

  updateBookingCalculation() {
    if (!this.selectedCarForBooking) return;

    const startDateStr = document.getElementById('booking-start-date')?.value;
    const endDateStr = document.getElementById('booking-end-date')?.value;
    const insuranceChecked = document.getElementById('booking-insurance')?.checked;
    const promoCode = document.getElementById('booking-promo')?.value?.trim().toUpperCase();

    if (!startDateStr || !endDateStr) return;

    const start = new Date(startDateStr);
    const end = new Date(endDateStr);
    const diffTime = end - start;
    let days = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    if (days <= 0) days = 1;

    const dailyRate = this.selectedCarForBooking.dailyRate;
    const baseTotal = dailyRate * days;
    const insuranceFee = insuranceChecked ? 15.00 * days : 0.00;

    let discount = 0;
    if (promoCode === 'SAVE10') discount = baseTotal * 0.10;
    else if (promoCode === 'DRIVE20') discount = Math.min(20, baseTotal);
    else if (promoCode === 'WEEKEND' && days >= 3) discount = baseTotal * 0.15;
    else if (promoCode === 'SPECIAL') discount = Math.min(50, baseTotal);

    const total = Math.max(0, baseTotal + insuranceFee - discount);

    document.getElementById('calc-days').textContent = `${days} Days`;
    document.getElementById('calc-base-total').textContent = UI.formatCurrency(baseTotal);
    document.getElementById('calc-insurance').textContent = UI.formatCurrency(insuranceFee);
    document.getElementById('calc-discount').textContent = `-${UI.formatCurrency(discount)}`;
    document.getElementById('calc-final-total').textContent = UI.formatCurrency(total);
  },

  async handleBookingSubmit(event) {
    event.preventDefault();
    if (!this.selectedCarForBooking) return;

    const requestData = {
      carId: this.selectedCarForBooking.id,
      customerFirstName: document.getElementById('cust-first-name').value,
      customerLastName: document.getElementById('cust-last-name').value,
      customerEmail: document.getElementById('cust-email').value,
      customerPhone: document.getElementById('cust-phone').value,
      customerDriverLicense: document.getElementById('cust-license').value,
      customerAddress: document.getElementById('cust-address').value,
      startDate: document.getElementById('booking-start-date').value,
      endDate: document.getElementById('booking-end-date').value,
      includeInsurance: document.getElementById('booking-insurance').checked,
      promoCode: document.getElementById('booking-promo').value,
      pickupLocation: document.getElementById('pickup-loc').value,
      dropoffLocation: document.getElementById('dropoff-loc').value,
      paymentMethod: document.getElementById('payment-method').value,
      notes: document.getElementById('booking-notes').value,
    };

    try {
      const booking = await Api.createBooking(requestData);
      UI.closeModal('booking-modal');
      UI.toast('Booking confirmed successfully!', 'success');
      this.showInvoiceModal(booking);
      this.loadCars();
      this.loadDashboardStats();
    } catch (err) {
      UI.toast(err.message, 'error');
    }
  },

  showInvoiceModal(booking) {
    const container = document.getElementById('invoice-container');
    if (container) {
      container.innerHTML = UI.renderInvoice(booking);
    }
    UI.openModal('invoice-modal');
  },

  // Dashboard Stats & Admin Operations
  async loadDashboardStats() {
    try {
      const stats = await Api.getDashboardStats();
      if (!stats) return;

      document.getElementById('kpi-total-cars').textContent = stats.totalCars;
      document.getElementById('kpi-available-cars').textContent = stats.availableCars;
      document.getElementById('kpi-active-rentals').textContent = stats.activeBookings;
      document.getElementById('kpi-revenue').textContent = UI.formatCurrency(stats.totalRevenue);
      document.getElementById('kpi-utilization').textContent = `${stats.fleetUtilizationRate}%`;
    } catch (err) {
      console.error(err);
    }
  },

  async loadAdminBookings() {
    try {
      const bookings = await Api.getBookings();
      const tbody = document.getElementById('admin-bookings-tbody');
      if (!tbody) return;

      tbody.innerHTML = bookings.map(b => `
        <tr>
          <td><strong>${b.bookingReference}</strong></td>
          <td>${b.carMake} ${b.carModel}</td>
          <td>${b.customerName}<br><small style="color:var(--text-muted)">${b.customerPhone}</small></td>
          <td>${UI.formatDate(b.startDate)} &rarr; ${UI.formatDate(b.endDate)}</td>
          <td><strong>${UI.formatCurrency(b.totalAmount)}</strong></td>
          <td><span class="badge badge-${b.status.toLowerCase()}">${b.status}</span></td>
          <td>
            <div style="display:flex; gap:0.4rem;">
              ${b.status === 'CONFIRMED' ? `<button class="btn btn-primary btn-sm" onclick="App.startRental(${b.id})">Handover</button>` : ''}
              ${b.status === 'ACTIVE' ? `<button class="btn btn-success btn-sm" onclick="App.openReturnModal(${b.id})">Return</button>` : ''}
              ${b.status !== 'COMPLETED' && b.status !== 'CANCELLED' ? `<button class="btn btn-danger btn-sm" onclick="App.cancelBooking(${b.id})">Cancel</button>` : ''}
              <button class="btn btn-secondary btn-sm" onclick="App.viewBookingInvoice('${b.bookingReference}')">Invoice</button>
            </div>
          </td>
        </tr>
      `).join('');
    } catch (err) {
      console.error(err);
    }
  },

  async loadAdminFleet() {
    try {
      const cars = await Api.getCars();
      const tbody = document.getElementById('admin-fleet-tbody');
      if (!tbody) return;

      tbody.innerHTML = cars.map(car => `
        <tr>
          <td>
            <div style="display:flex; align-items:center; gap:0.75rem;">
              <img src="${car.imageUrl}" style="width:45px; height:32px; object-fit:cover; border-radius:4px;"/>
              <strong>${car.make} ${car.model} (${car.year})</strong>
            </div>
          </td>
          <td>${car.category}</td>
          <td><code>${car.registrationNumber}</code></td>
          <td>${UI.formatCurrency(car.dailyRate)}/day</td>
          <td><span class="badge badge-${car.status.toLowerCase()}">${car.status}</span></td>
          <td>
            <div style="display:flex; gap:0.4rem;">
              <button class="btn btn-secondary btn-sm" onclick="App.openEditCarModal(${car.id})">Edit</button>
              <button class="btn btn-secondary btn-sm" onclick="App.toggleCarStatus(${car.id}, '${car.status}')">
                ${car.status === 'AVAILABLE' ? 'Set Maint.' : 'Set Avail.'}
              </button>
              <button class="btn btn-danger btn-sm" onclick="App.deleteCar(${car.id})">Delete</button>
            </div>
          </td>
        </tr>
      `).join('');
    } catch (err) {
      console.error(err);
    }
  },

  async loadAdminCustomers() {
    try {
      const customers = await Api.getCustomers();
      const tbody = document.getElementById('admin-customers-tbody');
      if (!tbody) return;

      tbody.innerHTML = customers.map(c => `
        <tr>
          <td><strong>${c.firstName} ${c.lastName}</strong></td>
          <td>${c.email}</td>
          <td>${c.phone}</td>
          <td><code>${c.driverLicenseNumber}</code></td>
          <td>${c.address || 'N/A'}</td>
        </tr>
      `).join('');
    } catch (err) {
      console.error(err);
    }
  },

  async startRental(id) {
    try {
      await Api.startRental(id);
      UI.toast('Vehicle successfully handed over to customer. Rental is now ACTIVE.', 'success');
      this.loadAdminBookings();
      this.loadAdminFleet();
      this.loadDashboardStats();
    } catch (err) {
      UI.toast(err.message, 'error');
    }
  },

  openReturnModal(bookingId) {
    document.getElementById('return-booking-id').value = bookingId;
    document.getElementById('return-date').value = new Date().toISOString().split('T')[0];
    UI.openModal('return-modal');
  },

  async handleReturnSubmit(event) {
    event.preventDefault();
    const bookingId = document.getElementById('return-booking-id').value;
    const returnData = {
      returnDate: document.getElementById('return-date').value,
      currentMileage: parseInt(document.getElementById('return-mileage').value) || null,
      hasDamage: document.getElementById('return-damage').checked,
      inspectionNotes: document.getElementById('return-notes').value,
    };

    try {
      const completed = await Api.completeRental(bookingId, returnData);
      UI.closeModal('return-modal');
      UI.toast('Vehicle returned successfully and marked as AVAILABLE.', 'success');
      this.showInvoiceModal(completed);
      this.loadAdminBookings();
      this.loadAdminFleet();
      this.loadDashboardStats();
    } catch (err) {
      UI.toast(err.message, 'error');
    }
  },

  async cancelBooking(id) {
    if (!confirm('Are you sure you want to cancel this booking?')) return;
    try {
      await Api.cancelBooking(id, 'Customer requested cancellation');
      UI.toast('Booking has been cancelled.', 'info');
      this.loadAdminBookings();
      this.loadAdminFleet();
      this.loadDashboardStats();
    } catch (err) {
      UI.toast(err.message, 'error');
    }
  },

  async viewBookingInvoice(ref) {
    try {
      const booking = await Api.getBookingByRef(ref);
      this.showInvoiceModal(booking);
    } catch (err) {
      UI.toast(err.message, 'error');
    }
  },

  async toggleCarStatus(carId, currentStatus) {
    const nextStatus = currentStatus === 'AVAILABLE' ? 'MAINTENANCE' : 'AVAILABLE';
    try {
      await Api.updateCarStatus(carId, nextStatus);
      UI.toast(`Car status updated to ${nextStatus}`, 'success');
      this.loadAdminFleet();
      this.loadDashboardStats();
    } catch (err) {
      UI.toast(err.message, 'error');
    }
  },

  async deleteCar(id) {
    if (!confirm('Are you sure you want to remove this car from the fleet?')) return;
    try {
      await Api.deleteCar(id);
      UI.toast('Car removed from fleet', 'info');
      this.loadAdminFleet();
      this.loadDashboardStats();
    } catch (err) {
      UI.toast(err.message, 'error');
    }
  },

  openAddCarModal() {
    this.editingCarId = null;
    document.getElementById('car-modal-title').textContent = 'Add New Vehicle to Fleet';
    document.getElementById('car-form').reset();
    UI.openModal('car-form-modal');
  },

  async openEditCarModal(carId) {
    try {
      const car = await Api.getCarById(carId);
      this.editingCarId = carId;
      document.getElementById('car-modal-title').textContent = `Edit ${car.make} ${car.model}`;
      document.getElementById('car-make').value = car.make;
      document.getElementById('car-model').value = car.model;
      document.getElementById('car-year').value = car.year;
      document.getElementById('car-category').value = car.category;
      document.getElementById('car-rate').value = car.dailyRate;
      document.getElementById('car-fuel').value = car.fuelType;
      document.getElementById('car-transmission').value = car.transmission;
      document.getElementById('car-seats').value = car.seats;
      document.getElementById('car-reg').value = car.registrationNumber;
      document.getElementById('car-mileage').value = car.mileage;
      document.getElementById('car-image').value = car.imageUrl;
      document.getElementById('car-features').value = car.features;

      UI.openModal('car-form-modal');
    } catch (err) {
      UI.toast(err.message, 'error');
    }
  },

  async handleCarFormSubmit(event) {
    event.preventDefault();
    const carData = {
      make: document.getElementById('car-make').value,
      model: document.getElementById('car-model').value,
      year: parseInt(document.getElementById('car-year').value),
      category: document.getElementById('car-category').value,
      dailyRate: parseFloat(document.getElementById('car-rate').value),
      fuelType: document.getElementById('car-fuel').value,
      transmission: document.getElementById('car-transmission').value,
      seats: parseInt(document.getElementById('car-seats').value),
      registrationNumber: document.getElementById('car-reg').value,
      mileage: parseInt(document.getElementById('car-mileage').value),
      imageUrl: document.getElementById('car-image').value,
      features: document.getElementById('car-features').value,
    };

    try {
      if (this.editingCarId) {
        await Api.updateCar(this.editingCarId, carData);
        UI.toast('Vehicle updated successfully!', 'success');
      } else {
        await Api.createCar(carData);
        UI.toast('Vehicle added to fleet successfully!', 'success');
      }
      UI.closeModal('car-form-modal');
      this.loadAdminFleet();
      this.loadCars();
      this.loadDashboardStats();
    } catch (err) {
      UI.toast(err.message, 'error');
    }
  },

  async trackBooking() {
    const ref = document.getElementById('track-ref-input')?.value?.trim();
    if (!ref) {
      UI.toast('Please enter a booking reference code', 'error');
      return;
    }
    try {
      const booking = await Api.getBookingByRef(ref);
      this.showInvoiceModal(booking);
    } catch (err) {
      UI.toast(err.message, 'error');
    }
  },

  fillDemoCustomer() {
    document.getElementById('cust-first-name').value = 'Robert';
    document.getElementById('cust-last-name').value = 'Taylor';
    document.getElementById('cust-email').value = 'robert.taylor@example.com';
    document.getElementById('cust-phone').value = '+1 555-0189';
    document.getElementById('cust-license').value = 'DL-8849201';
    document.getElementById('cust-address').value = '550 Grand Ave, Suite 300';
    document.getElementById('booking-promo').value = 'SAVE10';
    this.updateBookingCalculation();
  }
};

document.addEventListener('DOMContentLoaded', () => App.init());
