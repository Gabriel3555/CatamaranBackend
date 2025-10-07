// Payments Management JavaScript

// State management
let payments = [];
let filteredPayments = [];
let users = [];
let boats = [];
let currentPage = 0;
let totalPages = 0;
let totalElements = 0;
let pageSize = 10;

// DOM elements
const searchInput = document.getElementById('searchInput');
const reasonFilter = document.getElementById('reasonFilter');
const monthFilter = document.getElementById('monthFilter');
const statusFilter = document.getElementById('statusFilter');
const paymentsTableBody = document.getElementById('paymentsTableBody');
const paymentModal = document.getElementById('paymentModal');
const paymentForm = document.getElementById('paymentForm');
const modalTitle = document.getElementById('modalTitle');
const saveBtn = document.getElementById('saveBtn');

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    checkAuthentication();
    setupEventListeners();
    loadUsers();
    loadBoats();
    loadPayments();
});

// Authentication check
function checkAuthentication() {
    const userType = localStorage.getItem('userType');
    const jwt = localStorage.getItem('jwt');

    if (!userType || userType !== 'admin' || !jwt) {
        window.location.href = '../login.html';
        return;
    }
}

// Setup event listeners
function setupEventListeners() {
    searchInput.addEventListener('input', filterPayments);
    reasonFilter.addEventListener('change', filterPayments);
    monthFilter.addEventListener('change', filterPayments);
    statusFilter.addEventListener('change', filterPayments);
    paymentForm.addEventListener('submit', savePayment);
}

// Get authentication headers
function getAuthHeaders() {
    const jwt = localStorage.getItem('jwt');
    return {
        'Content-Type': 'application/json',
        ...(jwt ? { 'Authorization': `Bearer ${jwt}` } : {})
    };
}

// Load users for the dropdown
async function loadUsers() {
    try {
        const response = await fetch('/api/v1/auth', {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            users = data.content || data;
            populateUserSelect();
        } else {
            console.error('Failed to load users');
            users = [];
            populateUserSelect();
        }
    } catch (error) {
        console.error('Error loading users:', error);
        users = [];
        populateUserSelect();
    }
}

// Load boats for the dropdown
async function loadBoats() {
    try {
        const response = await fetch('/api/v1/boat?page=0&size=100', {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            boats = data.content || data;
            populateBoatSelect();
        } else {
            console.error('Failed to load boats');
            boats = [];
            populateBoatSelect();
        }
    } catch (error) {
        console.error('Error loading boats:', error);
        boats = [];
        populateBoatSelect();
    }
}

// Populate user select dropdown
function populateUserSelect() {
    const userSelect = document.getElementById('userSelect');
    userSelect.innerHTML = '<option value="">Seleccionar propietario...</option>';

    users.forEach(user => {
        const option = document.createElement('option');
        option.value = user.id;
        option.textContent = `${user.fullName} (${user.email})`;
        userSelect.appendChild(option);
    });
}

// Populate boat select dropdown
function populateBoatSelect() {
    const boatSelect = document.getElementById('boatSelect');
    boatSelect.innerHTML = '<option value="">Seleccionar embarcación...</option>';

    boats.forEach(boat => {
        const option = document.createElement('option');
        option.value = boat.id;
        option.textContent = `${boat.name} (${boat.model})`;
        boatSelect.appendChild(option);
    });
}

// Load payments from API
async function loadPayments(page = 0, search = '', reason = 'all', month = 'all', status = 'POR_PAGAR') {
    try {
        let url = `/api/v1/payments?page=${page}&size=${pageSize}`;

        // Add filter parameters if provided
        if (search) url += `&search=${encodeURIComponent(search)}`;
        if (reason !== 'all') url += `&reason=${encodeURIComponent(reason)}`;
        if (month !== 'all') url += `&month=${encodeURIComponent(month)}`;
        if (status !== 'all') url += `&status=${encodeURIComponent(status)}`;

        const response = await fetch(url, {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            payments = data.content || [];
            totalPages = data.totalPages || 1;
            totalElements = data.totalElements || 0;
            currentPage = page;
            filteredPayments = [...payments]; // For client-side filtering if needed
            updateMetrics();
            renderPayments();
            updatePaginationControls();
        } else {
            console.error('Failed to load payments');
            payments = [];
            filteredPayments = [];
            totalPages = 1;
            totalElements = 0;
            updateMetrics();
            renderPayments();
            updatePaginationControls();
        }
    } catch (error) {
        console.error('Error loading payments:', error);
        payments = [];
        filteredPayments = [];
        totalPages = 1;
        totalElements = 0;
        updateMetrics();
        renderPayments();
        updatePaginationControls();
    }
}


// Update metrics cards
function updateMetrics() {
    // For metrics, we need all payments, not just current page
    // This is a limitation - ideally backend should provide aggregated data
    // For now, we'll use the current page data for calculations
    const totalPayments = totalElements;
    const totalAmount = payments.reduce((sum, payment) => sum + (payment.mount || 0), 0);

    // Calculate current month payments (same logic as backend)
    const now = new Date();
    const currentYear = now.getFullYear();
    const currentMonth = now.getMonth(); // 0-based

    const monthlyPayments = payments
        .filter(payment => {
            const paymentDate = new Date(payment.date);
            return paymentDate.getFullYear() === currentYear &&
                   paymentDate.getMonth() === currentMonth;
        })
        .reduce((sum, payment) => sum + (payment.mount || 0), 0);

    const activePayers = new Set(payments.map(payment => payment.boat && payment.boat.owner ? payment.boat.owner.id : null).filter(id => id)).size;

    document.getElementById('totalPayments').textContent = totalPayments;
    document.getElementById('totalAmount').textContent = formatPrice(totalAmount);
    document.getElementById('monthlyPayments').textContent = formatPrice(monthlyPayments);
    document.getElementById('activePayers').textContent = activePayers;
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
        'PAGO': 'Pago',
        'MANTENIMIENTO': 'Mantenimiento'
    };
    return reasonMap[reason] || reason;
}

// Get reason class for styling
function getReasonClass(reason) {
    return `reason-${reason.toLowerCase()}`;
}

// Download receipt function
function downloadReceipt(paymentId) {
    const link = document.createElement('a');
    link.href = `/api/v1/payments/${paymentId}/download-receipt`;
    link.download = ''; // Let the server set the filename
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

// Update pagination controls
function updatePaginationControls() {
    const prevBtn = document.getElementById('prevPageBtn');
    const nextBtn = document.getElementById('nextPageBtn');
    const pageInfo = document.getElementById('currentPageInfo');
    const paginationInfo = document.getElementById('paginationInfo');

    // Update buttons
    prevBtn.disabled = currentPage <= 0;
    nextBtn.disabled = currentPage >= totalPages - 1;

    // Update page info
    pageInfo.textContent = `Página ${currentPage + 1} de ${totalPages}`;

    // Update pagination info
    const startItem = currentPage * pageSize + 1;
    const endItem = Math.min((currentPage + 1) * pageSize, totalElements);
    paginationInfo.textContent = `Mostrando ${startItem}-${endItem} de ${totalElements} pagos`;
}

// Change page function
function changePage(page) {
    if (page < 0 || page >= totalPages) return;
    loadPayments(page);
}

// Filter payments based on search and filters
function filterPayments() {
    const searchTerm = searchInput.value;
    const reasonValue = reasonFilter.value;
    const monthValue = monthFilter.value;
    const statusValue = statusFilter.value;

    // Reload data with filters applied server-side
    loadPayments(0, searchTerm, reasonValue, monthValue, statusValue);
}

// Render payments in the table
function renderPayments() {
    paymentsTableBody.innerHTML = '';

    if (filteredPayments.length === 0) {
        const emptyRow = document.createElement('tr');
        emptyRow.innerHTML = `
            <td colspan="7" style="text-align: center; padding: 40px; color: #6b7280;">
                No se encontraron pagos
            </td>
        `;
        paymentsTableBody.appendChild(emptyRow);
    } else {
        filteredPayments.forEach(payment => {
            const row = document.createElement('tr');
            const paymentDate = new Date(payment.date).toLocaleString('es-ES');
            const boat = payment.boat;

            row.innerHTML = `
                <td>${payment.id}</td>
                <td>${boat ? boat.name : 'Sin embarcación'}</td>
                <td><span class="reason-badge ${getReasonClass(payment.reason)}">${formatPaymentReason(payment.reason)}</span></td>
                <td class="price">${formatPrice(payment.mount)}</td>
                <td>${paymentDate}</td>
                <td>${payment.invoice_url ? `<button class="action-btn download-btn" onclick="downloadReceipt(${payment.id})">Descargar</button>` : 'Sin factura'}</td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn view-btn" onclick="viewPayment(${payment.id})">Ver</button>
                        ${!payment.invoice_url ? `<button class="action-btn attach-btn" onclick="openReceiptModal(${payment.id})">Adjuntar Recibo</button>` : ''}
                        <button class="action-btn delete-btn" onclick="deletePayment(${payment.id})">Eliminar</button>
                    </div>
                </td>
            `;
            paymentsTableBody.appendChild(row);
        });
    }

    document.getElementById('tableCount').textContent = `${filteredPayments.length} pagos en esta página`;
}

// Open add payment modal
function openAddModal() {
    modalTitle.textContent = 'Agregar Nuevo Pago';
    saveBtn.textContent = 'Crear Pago';

    // Reset form
    paymentForm.reset();

    // Set default date to now
    const now = new Date();
    const localDateTime = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
    document.getElementById('paymentDate').value = localDateTime.toISOString().slice(0, 16);

    paymentModal.style.display = 'block';
}

// View payment details
function viewPayment(id) {
    const payment = payments.find(p => p.id === id);
    if (!payment) return;

    const boat = payment.boat;
    const boatInfo = boat ? `Embarcación: ${boat.name}` : 'Sin embarcación asignada';

    // For now, just show an alert with payment details
    alert(`Pago #${payment.id}\n\n${boatInfo}\nMonto: ${formatPrice(payment.mount)}\nRazón: ${formatPaymentReason(payment.reason)}\nFecha: ${new Date(payment.date).toLocaleString('es-ES')}\nFactura: ${payment.invoice_url || 'Sin factura'}`);
}

// Delete payment
function deletePayment(id) {
    if (!confirm('¿Estás seguro de eliminar este pago? Esta acción no se puede deshacer.')) return;

    // For now, just remove from local array
    // In a real app, this would call the API
    payments = payments.filter(payment => payment.id !== id);
    filteredPayments = filteredPayments.filter(payment => payment.id !== id);
    updateMetrics();
    renderPayments();
}

// Handle form submission
async function savePayment(event) {
    if (event && typeof event.preventDefault === 'function') {
        event.preventDefault();
    }

    const formData = new FormData(paymentForm);
    const userId = formData.get('userId');
    const boatId = formData.get('boatId');

    const paymentData = {
        mount: parseFloat(formData.get('amount')),
        date: new Date(formData.get('date')).toISOString(),
        reason: formData.get('reason'),
        invoice_url: formData.get('invoice') || null,
        boat: { id: parseInt(boatId) }
    };

    try {
        // Create payment using the existing API endpoint
        const response = await fetch('/api/v1/payments', {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(paymentData)
        });

        if (response.ok) {
            const newPayment = await response.json();
            payments.push(newPayment);
            filteredPayments = [...payments];
            updateMetrics();
            renderPayments();
            closeModal();
        } else {
            console.error('Failed to create payment');
            closeModal();
        }
    } catch (error) {
        console.error('Error saving payment:', error);
        closeModal();
    }
}

// Open receipt modal
function openReceiptModal(paymentId) {
    const payment = payments.find(p => p.id === paymentId);
    if (!payment) return;

    const boat = payment.boat;
    const boatName = boat ? boat.name : 'Sin embarcación';

    // Show payment info
    document.getElementById('receiptInfo').innerHTML = `
        <div style="background: #f3f4f6; padding: 15px; border-radius: 8px; margin-bottom: 20px;">
            <h4 style="margin: 0 0 10px 0; color: #1f2937;">Información del Pago</h4>
            <p style="margin: 5px 0;"><strong>ID:</strong> ${payment.id}</p>
            <p style="margin: 5px 0;"><strong>Embarcación:</strong> ${boatName}</p>
            <p style="margin: 5px 0;"><strong>Monto:</strong> ${formatPrice(payment.mount)}</p>
            <p style="margin: 5px 0;"><strong>Razón:</strong> ${formatPaymentReason(payment.reason)}</p>
            <p style="margin: 5px 0;"><strong>Fecha:</strong> ${new Date(payment.date).toLocaleString('es-ES')}</p>
        </div>
    `;

    // Reset form
    document.getElementById('receiptForm').reset();

    // Store payment ID for later use
    document.getElementById('receiptModal').dataset.paymentId = paymentId;

    // Show modal
    document.getElementById('receiptModal').style.display = 'block';
}

// Close receipt modal
function closeReceiptModal() {
    document.getElementById('receiptModal').style.display = 'none';
}

// Attach receipt to payment
async function attachReceipt() {
    const modal = document.getElementById('receiptModal');
    const paymentId = modal.dataset.paymentId;
    const fileInput = document.getElementById('receiptFile');
    const attachBtn = document.getElementById('attachBtn');

    if (!fileInput.files[0]) {
        alert('Por favor selecciona un archivo para el recibo.');
        return;
    }

    const file = fileInput.files[0];

    // Validate file size (5MB max)
    if (file.size > 5 * 1024 * 1024) {
        alert('El archivo es demasiado grande. Tamaño máximo: 5MB');
        return;
    }

    // Validate file type
    const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg', 'application/pdf'];
    if (!allowedTypes.includes(file.type)) {
        alert('Tipo de archivo no permitido. Solo se permiten imágenes (JPG, PNG) y PDF.');
        return;
    }

    // Disable button and show loading
    attachBtn.disabled = true;
    attachBtn.textContent = 'Subiendo...';

    try {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch(`/api/v1/payments/${paymentId}/receipt`, {
            method: 'POST',
            headers: {
                'Authorization': getAuthHeaders()['Authorization'] // Only include auth header for multipart
            },
            body: formData
        });

        if (response.ok) {
            // Reload the table to reflect changes
            const searchTerm = searchInput.value;
            const reasonValue = reasonFilter.value;
            const monthValue = monthFilter.value;
            const statusValue = statusFilter.value;
            loadPayments(currentPage, searchTerm, reasonValue, monthValue, statusValue);

            alert('Recibo adjuntado exitosamente.');
            closeReceiptModal();
        } else {
            const error = await response.text();
            alert('Error al adjuntar el recibo: ' + error);
        }
    } catch (error) {
        console.error('Error attaching receipt:', error);
        alert('Error de conexión. Por favor, inténtalo de nuevo.');
    } finally {
        // Re-enable button
        attachBtn.disabled = false;
        attachBtn.textContent = 'Adjuntar Recibo';
    }
}

// Close modal
function closeModal() {
    paymentModal.style.display = 'none';
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
    alert(`Navegando a: ${page}`);
    // You could implement actual navigation like:
    // window.location.href = `${page}.html`;
}

// Close modal when clicking outside
window.onclick = function(event) {
    if (event.target === paymentModal) {
        closeModal();
    }
    if (event.target === document.getElementById('receiptModal')) {
        closeReceiptModal();
    }
};

// Export functions for HTML onclick handlers
window.savePayment = savePayment;
window.openAddModal = openAddModal;
window.closeModal = closeModal;
window.viewPayment = viewPayment;
window.deletePayment = deletePayment;
window.openReceiptModal = openReceiptModal;
window.closeReceiptModal = closeReceiptModal;
window.attachReceipt = attachReceipt;
window.downloadReceipt = downloadReceipt;
window.changePage = changePage;
window.logout = logout;
window.navigateTo = navigateTo;