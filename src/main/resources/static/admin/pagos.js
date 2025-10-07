// Payments Management JavaScript

// State management
let payments = [];
let filteredPayments = [];
let users = [];
let boats = [];

// DOM elements
const searchInput = document.getElementById('searchInput');
const reasonFilter = document.getElementById('reasonFilter');
const monthFilter = document.getElementById('monthFilter');
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
        }
    } catch (error) {
        console.error('Error loading users:', error);
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
        }
    } catch (error) {
        console.error('Error loading boats:', error);
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
async function loadPayments() {
    try {
        const response = await fetch('/api/v1/payments', {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            payments = await response.json();
            filteredPayments = [...payments];
            updateMetrics();
            renderPayments();
        } else {
            console.error('Failed to load payments');
            showFallbackData();
        }
    } catch (error) {
        console.error('Error loading payments:', error);
        showFallbackData();
    }
}

// Show fallback data when API fails
function showFallbackData() {
    payments = [
        {
            id: 1,
            user: { id: 1, fullName: 'Carlos Rodríguez', email: 'carlos.rodriguez@email.com' },
            mount: 500000.0,
            date: '2024-12-15T10:00:00',
            reason: 'COUTA',
            invoice_url: 'INV-001-2024'
        },
        {
            id: 2,
            user: { id: 2, fullName: 'María González', email: 'maria.gonzalez@email.com' },
            mount: 750000.0,
            date: '2024-12-10T14:00:00',
            reason: 'MANTENIMIENTO',
            invoice_url: 'INV-002-2024'
        }
    ];
    filteredPayments = [...payments];
    updateMetrics();
    renderPayments();
}

// Update metrics cards
function updateMetrics() {
    const totalPayments = payments.length;
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

    const activePayers = new Set(payments.map(payment => payment.user.id)).size;

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
        'COUTA': 'Cuota',
        'MANTENIMIENTO': 'Mantenimiento'
    };
    return reasonMap[reason] || reason;
}

// Get reason class for styling
function getReasonClass(reason) {
    return `reason-${reason.toLowerCase()}`;
}

// Filter payments based on search and filters
function filterPayments() {
    const searchTerm = searchInput.value.toLowerCase();
    const reasonValue = reasonFilter.value;
    const monthValue = monthFilter.value;

    filteredPayments = payments.filter(payment => {
        const matchesSearch = payment.user.fullName.toLowerCase().includes(searchTerm) ||
                             payment.user.email.toLowerCase().includes(searchTerm) ||
                             (payment.invoice_url && payment.invoice_url.toLowerCase().includes(searchTerm));

        const matchesReason = reasonValue === 'all' || payment.reason === reasonValue;

        let matchesMonth = true;
        if (monthValue !== 'all') {
            const paymentDate = new Date(payment.date);
            const now = new Date();

            switch (monthValue) {
                case 'current':
                    matchesMonth = paymentDate.getMonth() === now.getMonth() &&
                                  paymentDate.getFullYear() === now.getFullYear();
                    break;
                case 'last3':
                    const threeMonthsAgo = new Date(now.getFullYear(), now.getMonth() - 3, 1);
                    matchesMonth = paymentDate >= threeMonthsAgo;
                    break;
                case 'last6':
                    const sixMonthsAgo = new Date(now.getFullYear(), now.getMonth() - 6, 1);
                    matchesMonth = paymentDate >= sixMonthsAgo;
                    break;
            }
        }

        return matchesSearch && matchesReason && matchesMonth;
    });

    renderPayments();
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

            row.innerHTML = `
                <td>${payment.id}</td>
                <td>${payment.user.fullName}<br><small style="color: #6b7280;">${payment.user.email}</small></td>
                <td><span class="reason-badge ${getReasonClass(payment.reason)}">${formatPaymentReason(payment.reason)}</span></td>
                <td class="price">${formatPrice(payment.mount)}</td>
                <td>${paymentDate}</td>
                <td>${payment.invoice_url || 'Sin factura'}</td>
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

    document.getElementById('tableCount').textContent = `${filteredPayments.length} de ${payments.length} pagos`;
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

    // For now, just show an alert with payment details
    alert(`Pago #${payment.id}\n\nPropietario: ${payment.user.fullName}\nEmail: ${payment.user.email}\nMonto: ${formatPrice(payment.mount)}\nRazón: ${formatPaymentReason(payment.reason)}\nFecha: ${new Date(payment.date).toLocaleString('es-ES')}\nFactura: ${payment.invoice_url || 'Sin factura'}`);
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
        invoice_url: formData.get('invoice') || null
    };

    try {
        // Create payment using the existing API endpoint
        const response = await fetch(`/api/v1/payments/${boatId}/${userId}`, {
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
            // Fallback to local state update
            const selectedUser = users.find(u => u.id == userId);
            const newPayment = {
                ...paymentData,
                id: Math.max(...payments.map(p => p.id), 0) + 1,
                user: selectedUser
            };
            payments.push(newPayment);
            filteredPayments = [...payments];
            updateMetrics();
            renderPayments();
            closeModal();
        }
    } catch (error) {
        console.error('Error saving payment:', error);
        // Fallback to local state update
        const selectedUser = users.find(u => u.id == userId);
        const newPayment = {
            ...paymentData,
            id: Math.max(...payments.map(p => p.id), 0) + 1,
            user: selectedUser
        };
        payments.push(newPayment);
        filteredPayments = [...payments];
        updateMetrics();
        renderPayments();
        closeModal();
    }
}

// Open receipt modal
function openReceiptModal(paymentId) {
    const payment = payments.find(p => p.id === paymentId);
    if (!payment) return;

    // Show payment info
    document.getElementById('receiptInfo').innerHTML = `
        <div style="background: #f3f4f6; padding: 15px; border-radius: 8px; margin-bottom: 20px;">
            <h4 style="margin: 0 0 10px 0; color: #1f2937;">Información del Pago</h4>
            <p style="margin: 5px 0;"><strong>ID:</strong> ${payment.id}</p>
            <p style="margin: 5px 0;"><strong>Propietario:</strong> ${payment.user.fullName}</p>
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
        formData.append('receipt', file);

        const response = await fetch(`/api/v1/payments/${paymentId}/attach-receipt`, {
            method: 'PUT',
            headers: {
                'Authorization': getAuthHeaders()['Authorization'] // Only include auth header for multipart
            },
            body: formData
        });

        if (response.ok) {
            const updatedPayment = await response.json();

            // Update payment in local array
            const index = payments.findIndex(p => p.id === paymentId);
            if (index !== -1) {
                payments[index] = updatedPayment;
                filteredPayments = [...payments];
                updateMetrics();
                renderPayments();
            }

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
window.logout = logout;
window.navigateTo = navigateTo;