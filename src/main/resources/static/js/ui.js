const UI = {
  toast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      container.className = 'toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<strong>${type === 'success' ? '✓ ' : type === 'error' ? '⚠ ' : 'ℹ '}</strong> ${message}`;

    container.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(50px)';
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  },

  formatCurrency(amount) {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount || 0);
  },

  formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  },

  openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.add('open');
  },

  closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.remove('open');
  },

  renderCarCard(car) {
    const statusClass = {
      AVAILABLE: 'badge-available',
      RENTED: 'badge-rented',
      MAINTENANCE: 'badge-maintenance',
    }[car.status] || 'badge-available';

    const defaultImg = 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80';
    const imgUrl = car.imageUrl || defaultImg;

    return `
      <div class="car-card">
        <div class="car-image-wrapper">
          <img src="${imgUrl}" alt="${car.make} ${car.model}" class="car-image" onerror="this.src='${defaultImg}'"/>
          <span class="badge-category">${car.category}</span>
          <span class="badge ${statusClass}">${car.status}</span>
        </div>
        <div class="car-content">
          <h3 class="car-title">${car.make} ${car.model}</h3>
          <p class="car-subtitle">${car.year} • ${car.transmission} • ${car.fuelType}</p>
          
          <div class="car-features-grid">
            <div class="feature-item">
              <svg fill="currentColor" viewBox="0 0 20 20"><path d="M9 6a3 3 0 11-6 0 3 3 0 016 0zM17 6a3 3 0 11-6 0 3 3 0 016 0zM12.93 17c.046-.327.07-.66.07-1a6.97 6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0 015 5v1H1v-1a5 5 0 015-5z"/></svg>
              <span>${car.seats} Seats</span>
            </div>
            <div class="feature-item">
              <svg fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd"/></svg>
              <span>${car.mileage.toLocaleString()} mi</span>
            </div>
          </div>
          
          <p style="font-size: 0.8rem; color: var(--text-muted); margin-bottom: 1rem;">
            ${car.features || 'Standard luxury & safety features'}
          </p>

          <div class="car-footer">
            <div class="car-price">
              ${UI.formatCurrency(car.dailyRate)} <span>/ day</span>
            </div>
            <button class="btn btn-primary btn-sm" onclick="App.openBookingModal(${car.id})" ${car.status !== 'AVAILABLE' ? 'disabled style="opacity:0.5; cursor:not-allowed;"' : ''}>
              ${car.status === 'AVAILABLE' ? 'Reserve Now' : car.status}
            </button>
          </div>
        </div>
      </div>
    `;
  },

  renderInvoice(booking) {
    return `
      <div style="background:#0f172a; border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 1.5rem;">
        <div style="display:flex; justify-content:space-between; align-items:flex-start; border-bottom: 1px solid var(--border-color); padding-bottom: 1rem; margin-bottom: 1.25rem;">
          <div>
            <h2 style="font-size: 1.4rem; color: var(--text-primary); font-weight:800;">Apex Auto Rentals</h2>
            <p style="color: var(--text-secondary); font-size: 0.85rem;">Official Rental Invoice & Agreement</p>
          </div>
          <div style="text-align:right;">
            <div style="font-family:monospace; background:rgba(59,130,246,0.15); color:#60a5fa; padding:0.3rem 0.6rem; border-radius:4px; font-weight:700; font-size:0.95rem;">
              ${booking.bookingReference}
            </div>
            <span style="font-size: 0.75rem; color: var(--text-muted);">Issued: ${UI.formatDate(booking.createdAt)}</span>
          </div>
        </div>

        <div style="display:grid; grid-template-columns:1fr 1fr; gap:1.5rem; margin-bottom: 1.25rem; font-size:0.9rem;">
          <div>
            <h4 style="color: var(--accent-secondary); margin-bottom:0.4rem; font-size:0.8rem; text-transform:uppercase;">Customer Details</h4>
            <p><strong>${booking.customerName}</strong></p>
            <p style="color: var(--text-secondary); font-size:0.85rem;">${booking.customerEmail}</p>
            <p style="color: var(--text-secondary); font-size:0.85rem;">Phone: ${booking.customerPhone}</p>
            <p style="color: var(--text-secondary); font-size:0.85rem;">License: ${booking.customerDriverLicense}</p>
          </div>
          <div>
            <h4 style="color: var(--accent-secondary); margin-bottom:0.4rem; font-size:0.8rem; text-transform:uppercase;">Vehicle Reserved</h4>
            <p><strong>${booking.carMake} ${booking.carModel} (${booking.carYear})</strong></p>
            <p style="color: var(--text-secondary); font-size:0.85rem;">Category: ${booking.carCategory}</p>
            <p style="color: var(--text-secondary); font-size:0.85rem;">Reg: ${booking.carRegistrationNumber}</p>
            <p style="color: var(--text-secondary); font-size:0.85rem;">Location: ${booking.pickupLocation}</p>
          </div>
        </div>

        <div style="background:rgba(255,255,255,0.03); border-radius:6px; padding:1rem; margin-bottom:1.25rem;">
          <div style="display:flex; justify-content:space-between; margin-bottom:0.4rem; font-size:0.85rem;">
            <span>Rental Period:</span>
            <strong>${UI.formatDate(booking.startDate)} &rarr; ${UI.formatDate(booking.endDate)} (${booking.totalDays} Days)</strong>
          </div>
          <div style="display:flex; justify-content:space-between; margin-bottom:0.4rem; font-size:0.85rem;">
            <span>Base Rate (${booking.totalDays} days &times; ${UI.formatCurrency(booking.dailyRate)}):</span>
            <span>${UI.formatCurrency(booking.dailyRate * booking.totalDays)}</span>
          </div>
          ${booking.insuranceFee > 0 ? `
          <div style="display:flex; justify-content:space-between; margin-bottom:0.4rem; font-size:0.85rem;">
            <span>Full Coverage Insurance:</span>
            <span>${UI.formatCurrency(booking.insuranceFee)}</span>
          </div>` : ''}
          ${booking.discountAmount > 0 ? `
          <div style="display:flex; justify-content:space-between; margin-bottom:0.4rem; font-size:0.85rem; color:#34d399;">
            <span>Promotional Discount:</span>
            <span>-${UI.formatCurrency(booking.discountAmount)}</span>
          </div>` : ''}
          ${booking.lateFee > 0 ? `
          <div style="display:flex; justify-content:space-between; margin-bottom:0.4rem; font-size:0.85rem; color:#f87171;">
            <span>Late Return Fee Penalty:</span>
            <span>+${UI.formatCurrency(booking.lateFee)}</span>
          </div>` : ''}
          <div style="display:flex; justify-content:space-between; font-size:1.15rem; font-weight:800; border-top:1px solid var(--border-color); padding-top:0.6rem; margin-top:0.6rem; color:var(--text-primary);">
            <span>Total Paid:</span>
            <span style="color:#60a5fa;">${UI.formatCurrency(booking.totalAmount)}</span>
          </div>
        </div>

        <div style="display:flex; justify-content:space-between; align-items:center; font-size:0.8rem; color:var(--text-muted);">
          <span>Payment: ${booking.paymentMethod || 'CREDIT_CARD'} (${booking.paymentStatus || 'PAID'})</span>
          <span>Status: <strong style="color:#34d399;">${booking.status}</strong></span>
        </div>
      </div>
    `;
  }
};
