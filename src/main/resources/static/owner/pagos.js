// Owner Payments Page JavaScript

// State management
let payments = [];
let filteredPayments = [];
let ownerBoats = [];
let paginator = null;

// DOM elements
const searchInput = document.getElementById('searchInput');
const statusFilter = document.getElementById('statusFilter');
const reasonFilter = document.getElementById('reasonFilter');
const boatFilter = document.getElementById('boatFilter');
const paymentsTableBody = document.getElementById('paymentsTableBody');

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    checkAuthentication();
    setupEventListeners();
    loadPayments();
});

// Authentication check
function checkAuthentication() {
    const userType = localStorage.getItem('userType');
    const jwt = localStorage.getItem('jwt');

    if (!userType || userType !== 'owner' || !jwt) {
        window.location.href = '../login.html';
        return;
    }

    // Update user name
    const username = localStorage.getItem('username') || 'Propietario';
    document.getElementById('userName').textContent = username;
}

// Setup event listeners
function setupEventListeners() {
    searchInput.addEventListener('input', filterPayments);
    statusFilter.addEventListener('change', filterPayments);
    reasonFilter.addEventListener('change', filterPayments);
    boatFilter.addEventListener('change', filterPayments);
}

// Get authentication headers
function getAuthHeaders() {
    const jwt = localStorage.getItem('jwt');
    return {
        'Content-Type': 'application/json',
        ...(jwt ? { 'Authorization': `Bearer ${jwt}` } : {})
    };
}

// Load payments from API
async function loadPayments() {
    try {
        const userId = localStorage.getItem('userId');
        if (!userId) {
            console.error('User ID not found');
            return;
        }

        // Load payments
        const paymentsResponse = await fetch(`/api/v1/owner/payments/${userId}`, {
            headers: getAuthHeaders()
        });

        if (paymentsResponse.ok) {
            payments = await paymentsResponse.json();
            filteredPayments = [...payments];
            paginator = new Paginator(filteredPayments, 10);
            window.paginator_payments = paginator;
            window.renderWithPagination_payments = renderPayments;
        } else {
            console.error('Failed to load payments');
            payments = [];
            filteredPayments = [];
        }

        // Load boats for filter
        const dashboardResponse = await fetch(`/api/v1/owner/dashboard/${userId}`, {
            headers: getAuthHeaders()
        });

        if (dashboardResponse.ok) {
            const dashboardData = await dashboardResponse.json();
            ownerBoats = dashboardData.boats || [];
            populateBoatFilter();
        }

        renderPayments();
        updateMetrics();
    } catch (error) {
        console.error('Error loading payments:', error);
        payments = [];
        filteredPayments = [];
        renderPayments();
    }
}

// Populate boat filter dropdown
function populateBoatFilter() {
    const boatFilter = document.getElementById('boatFilter');
    boatFilter.innerHTML = '<option value="all">Todas las embarcaciones</option>';

    ownerBoats.forEach(boat => {
        const option = document.createElement('option');
        option.value = boat.name;
        option.textContent = boat.name;
        boatFilter.appendChild(option);
    });
}

// Format price in Colombian pesos
function formatPrice(price) {
    return new Intl.NumberFormat('es-CO', {
        style: 'currency',
        currency: 'COP',
        minimumFractionDigits: 0
    }).format(price);
}

// Format payment reason for display
function formatPaymentReason(reason) {
    const reasonMap = {
        'PAGO': 'Pago de embarcación',
        'MANTENIMIENTO': 'Pago de mantenimiento',
        'ADMIN': 'Pago administrativo'
    };
    return reasonMap[reason] || reason;
}

// Update metrics
function updateMetrics() {
    const totalPayments = payments.length;
    const pendingPayments = payments.filter(p => p.status === 'POR_PAGAR').length;
    const paidPayments = payments.filter(p => p.status === 'PAGADO').length;
    const totalAmount = payments.reduce((sum, payment) => sum + (payment.amount || 0), 0);

    document.getElementById('totalPayments').textContent = totalPayments;
    document.getElementById('pendingPayments').textContent = pendingPayments;
    document.getElementById('paidPayments').textContent = paidPayments;
    document.getElementById('totalAmount').textContent = formatPrice(totalAmount);
}

// Filter payments based on search and filters
function filterPayments() {
    const searchTerm = searchInput.value.toLowerCase();
    const statusValue = statusFilter.value;
    const reasonValue = reasonFilter.value;
    const boatValue = boatFilter.value;

    filteredPayments = payments.filter(payment => {
        const matchesSearch = payment.boatName.toLowerCase().includes(searchTerm) ||
                             formatPaymentReason(payment.reason).toLowerCase().includes(searchTerm);

        const matchesStatus = statusValue === 'all' || payment.status === statusValue;
        const matchesReason = reasonValue === 'all' || payment.reason === reasonValue;
        const matchesBoat = boatValue === 'all' || payment.boatName === boatValue;

        return matchesSearch && matchesStatus && matchesReason && matchesBoat;
    });

    if (paginator) {
        paginator.updateItems(filteredPayments);
    }
    renderPayments();
}

// Render payments in the table
function renderPayments() {
    paymentsTableBody.innerHTML = '';

    const itemsToDisplay = paginator ? paginator.getCurrentPageItems() : filteredPayments;

    if (itemsToDisplay.length === 0) {
        const emptyRow = document.createElement('tr');
        emptyRow.innerHTML = `
            <td colspan="6" style="text-align: center; padding: 40px; color: #6b7280;">
                No se encontraron pagos
            </td>
        `;
        paymentsTableBody.appendChild(emptyRow);
    } else {
        itemsToDisplay.forEach(payment => {
            const row = document.createElement('tr');
            const paymentDate = payment.date ?
                new Date(payment.date).toLocaleString('es-ES') : 'N/A';
            const status = payment.status || 'POR_PAGAR';
            const reason = payment.reason || 'PAGO';

            let statusBadge = '';
            if (status === 'PAGADO') {
                statusBadge = '<span class="status-badge status-completed">Pagado</span>';
            } else if (status === 'POR_PAGAR') {
                statusBadge = '<span class="status-badge status-pending">Por pagar</span>';
            }

            let invoiceLink = 'N/A';
            if (payment.invoiceUrl) {
                invoiceLink = `<a href="${payment.invoiceUrl}" target="_blank" class="invoice-link">Ver factura</a>`;
            }

            row.innerHTML = `
                <td>${payment.boatName || 'N/A'}</td>
                <td>${formatPaymentReason(reason)}</td>
                <td class="price">${formatPrice(payment.amount || 0)}</td>
                <td>${paymentDate}</td>
                <td>${statusBadge}</td>
                <td>${invoiceLink}</td>
            `;
            paymentsTableBody.appendChild(row);
        });
    }

    document.getElementById('tableCount').textContent = `${filteredPayments.length} de ${payments.length} pagos`;
    
    // Render pagination
    const paginationContainer = document.getElementById('paginationContainer');
    if (paginationContainer && paginator) {
        paginationContainer.innerHTML = paginator.generatePaginationHTML('payments');
    }
}

// Logout function
function logout() {
    localStorage.removeItem('userType');
    localStorage.removeItem('username');
    localStorage.removeItem('jwt');
    localStorage.removeItem('refreshToken');
    window.location.href = '../login.html';
}

// Navigation function
function navigateTo(page) {
    window.location.href = `${page}.html`;
}

// Export functions for HTML onclick handlers
window.logout = logout;
window.navigateTo = navigateTo;