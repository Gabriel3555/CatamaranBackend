// Payments Management JavaScript

// State management
let payments = [];
let filteredPayments = [];
let boats = [];
let currentPage = 0;
let totalPages = 0;
let totalElements = 0;
let pageSize = 10;
let generalStatistics = {
    totalPayments: 0,
    totalAmount: 0,
    monthlyAmount: 0,
    activePayers: 0
};

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
    loadBoats();
    
    // Cargar pagos con filtro "Por pagar" por defecto
    const statusFilter = document.getElementById('statusFilter');
    const defaultStatus = statusFilter ? statusFilter.value : 'POR_PAGAR';
    loadPayments(0, '', 'all', 'all', defaultStatus);
    
    loadGeneralStatistics();

    // Add cache busting parameter to avoid cached JS
    console.log('Page loaded at:', new Date().toISOString());
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

// Load general statistics from API
async function loadGeneralStatistics() {
    try {
        console.log('Loading general payment statistics...');
        const response = await fetch('/api/v1/payments/statistics', {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            generalStatistics = await response.json();
            console.log('Loaded general statistics:', generalStatistics);
            updateMetrics();
        } else {
            console.error('Failed to load general statistics - Status:', response.status);
            const errorText = await response.text();
            console.error('Error response:', errorText);
        }
    } catch (error) {
        console.error('Error loading general statistics:', error);
    }
}


// Load payments from API
async function loadPayments(page = 0, search = '', reason = 'all', month = 'all', status = 'all') {
    try {
        let url = `/api/v1/payments?page=${page}&size=${pageSize}`;

        // Add filter parameters if provided
        if (search) url += `&search=${encodeURIComponent(search)}`;
        if (reason !== 'all') url += `&reason=${encodeURIComponent(reason)}`;
        if (month !== 'all') url += `&month=${encodeURIComponent(month)}`;
        if (status !== 'all') url += `&status=${encodeURIComponent(status)}`;

        console.log('Loading payments with URL:', url);
        console.log('Filters - search:', search, 'reason:', reason, 'month:', month, 'status:', status);

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

            console.log('Loaded payments:', payments.length, 'payments');
            console.log('Total elements:', totalElements);

            updateMetrics();
            renderPayments();
            updatePaginationControls();
        } else {
            console.error('Failed to load payments - Status:', response.status);
            const errorText = await response.text();
            console.error('Error response:', errorText);

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
    // Use general statistics that are always constant regardless of filters or pagination
    document.getElementById('totalPayments').textContent = generalStatistics.totalPayments;
    document.getElementById('totalAmount').textContent = formatPrice(generalStatistics.totalAmount);
    document.getElementById('monthlyPayments').textContent = formatPrice(generalStatistics.monthlyAmount);
    document.getElementById('activePayers').textContent = generalStatistics.activePayers;

    console.log('Updated metrics with general statistics:', generalStatistics);
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
        'ADMIN': 'Administrativo',
        'CUOTA': 'Cuota',
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
async function downloadReceipt(paymentId) {
    try {
        const jwt = localStorage.getItem('jwt');
        const response = await fetch(`/api/v1/payments/${paymentId}/download-receipt`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${jwt}`
            }
        });

        if (!response.ok) {
            throw new Error('Error al descargar el recibo');
        }

        // Obtener el blob del archivo
        const blob = await response.blob();

        // Obtener el nombre del archivo del header Content-Disposition
        const contentDisposition = response.headers.get('Content-Disposition');
        let filename = 'recibo.pdf';
        if (contentDisposition) {
            const filenameMatch = contentDisposition.match(/filename="?(.+)"?/i);
            if (filenameMatch && filenameMatch[1]) {
                filename = filenameMatch[1];
            }
        }

        // Crear un link temporal y descargarlo
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();

        // Limpiar
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);

        console.log('Recibo descargado exitosamente');
    } catch (error) {
        console.error('Error downloading receipt:', error);
        alert('Error al descargar el recibo. Por favor, inténtalo de nuevo.');
    }
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

    // Get current filter values
    const searchTerm = searchInput.value;
    const reasonValue = reasonFilter.value;
    const monthValue = monthFilter.value;
    const statusValue = statusFilter.value;

    // Load payments with current filters
    loadPayments(page, searchTerm, reasonValue, monthValue, statusValue);
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

    // Set default values
    document.getElementById('paymentReason').value = 'CUOTA';
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
    alert(`Pago #${payment.id}\n\n${boatInfo}\nMonto: ${formatPrice(payment.mount)}\nRazón: ${formatPaymentReason(payment.reason)}\nFecha: ${new Date(payment.date).toLocaleString('es-ES')}\nFactura: ${payment.invoice_url || 'N/A'}`);
}

// Delete payment
async function deletePayment(id) {
    if (!confirm('¿Estás seguro de eliminar este pago? Esta acción no se puede deshacer.')) return;

    try {
        const response = await fetch(`/api/v1/payments/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            // Remove from local arrays only after successful deletion from database
            payments = payments.filter(payment => payment.id !== id);
            filteredPayments = filteredPayments.filter(payment => payment.id !== id);

            // Reload general statistics since total payments count changed
            await loadGeneralStatistics();

            // Update metrics and re-render the table
            updateMetrics();
            renderPayments();

            alert('Pago eliminado exitosamente.');
        } else {
            const errorText = await response.text();
            console.error('Error deleting payment:', errorText);
            alert('Error al eliminar el pago. Por favor, inténtalo de nuevo.');
        }
    } catch (error) {
        console.error('Error deleting payment:', error);
        alert('Error de conexión. Por favor, inténtalo de nuevo.');
    }
}

// Handle form submission
async function savePayment(event) {
    if (event && typeof event.preventDefault === 'function') {
        event.preventDefault();
    }

    const formData = new FormData(paymentForm);
    const boatId = formData.get('boatId');

    const paymentData = {
        mount: parseFloat(formData.get('amount')),
        date: new Date(formData.get('date')).toISOString(),
        reason: formData.get('reason'),
        status: 'POR_PAGAR',
        boatId: parseInt(boatId)
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

// Debug function to test filters
window.testFilters = function() {
    console.log('Testing filters...');
    console.log('Current filter values:');
    console.log('  searchInput:', searchInput.value);
    console.log('  reasonFilter:', reasonFilter.value);
    console.log('  monthFilter:', monthFilter.value);
    console.log('  statusFilter:', statusFilter.value);

    // Test loading with current filters
    filterPayments();
};

// Clear cache and reload
window.clearCacheAndReload = function() {
    console.log('Clearing cache and reloading...');
    localStorage.clear();
    sessionStorage.clear();

    // Force cache refresh by adding timestamp to URL
    const currentUrl = new URL(window.location.href);
    currentUrl.searchParams.set('t', Date.now());
    window.location.href = currentUrl.toString();
};

// Force refresh JavaScript files
window.refreshJSFiles = function() {
    console.log('Refreshing JavaScript files...');

    // Remove existing scripts
    const scripts = document.querySelectorAll('script[src*="pagos.js"], script[src*="common-admin.js"]');
    scripts.forEach(script => {
        script.remove();
    });

    // Reload scripts with cache busting
    const timestamp = Date.now();
    const pagosScript = document.createElement('script');
    pagosScript.src = `pagos.js?t=${timestamp}`;
    document.body.appendChild(pagosScript);

    const commonScript = document.createElement('script');
    commonScript.src = `common-admin.js?t=${timestamp}`;
    document.body.appendChild(commonScript);

    console.log('JavaScript files refreshed');
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
window.testFilters = testFilters;
window.clearCacheAndReload = clearCacheAndReload;
window.refreshJSFiles = refreshJSFiles;