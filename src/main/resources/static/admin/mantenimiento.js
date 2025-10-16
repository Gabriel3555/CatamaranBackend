// Maintenance Management JavaScript

// State management
let maintenances = [];
let filteredMaintenances = [];
let boats = [];
let currentEditingMaintenance = null;
let currentPaymentId = null;
let currentPage = 0;
let totalPages = 0;
let totalElements = 0;
let pageSize = 10;

// DOM elements
const searchInput = document.getElementById('searchInput');
const statusFilter = document.getElementById('statusFilter');
const typeFilter = document.getElementById('typeFilter');
const maintenanceTableBody = document.getElementById('maintenanceTableBody');
const maintenanceModal = document.getElementById('maintenanceModal');
const maintenanceForm = document.getElementById('maintenanceForm');
const modalTitle = document.getElementById('modalTitle');
const saveBtn = document.getElementById('saveBtn');
const receiptModal = document.getElementById('receiptModal');
const receiptForm = document.getElementById('receiptForm');
const uploadBtn = document.getElementById('uploadBtn');

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    checkAuthentication();
    setupEventListeners();
    loadBoats();
    loadMaintenances();
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
    searchInput.addEventListener('input', filterMaintenances);
    statusFilter.addEventListener('change', filterMaintenances);
    typeFilter.addEventListener('change', filterMaintenances);
    maintenanceForm.addEventListener('submit', saveMaintenance);
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

// Load maintenances from API
async function loadMaintenances(page = 0, search = '', status = 'all', type = 'all') {
    try {
        let url = `/api/v1/maintenances?page=${page}&size=${pageSize}`;

        // Add filter parameters if provided
        if (search) url += `&search=${encodeURIComponent(search)}`;
        if (status !== 'all') url += `&status=${encodeURIComponent(status)}`;
        if (type !== 'all') url += `&type=${encodeURIComponent(type)}`;

        console.log('Loading maintenances with URL:', url);
        console.log('Filters - search:', search, 'status:', status, 'type:', type);

        const response = await fetch(url, {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            maintenances = data.content || [];
            totalPages = data.totalPages || 1;
            totalElements = data.totalElements || 0;
            currentPage = page;
            filteredMaintenances = [...maintenances]; // For client-side filtering if needed

            console.log('Loaded maintenances:', maintenances.length, 'maintenances');
            console.log('Total elements:', totalElements);

            // Update filteredMaintenances for client-side operations if needed
            filteredMaintenances = [...maintenances];

            updateMetrics();
            renderMaintenances();
            updatePaginationControls();
        } else {
            console.error('Failed to load maintenances - Status:', response.status);
            const errorText = await response.text();
            console.error('Error response:', errorText);

            maintenances = [];
            filteredMaintenances = [];
            totalPages = 1;
            totalElements = 0;
            updateMetrics();
            renderMaintenances();
            updatePaginationControls();
        }
    } catch (error) {
        console.error('Error loading maintenances:', error);
        maintenances = [];
        filteredMaintenances = [];
        totalPages = 1;
        totalElements = 0;
        updateMetrics();
        renderMaintenances();
        updatePaginationControls();
    }
}


// Update metrics cards
function updateMetrics() {
    const totalMaintenances = maintenances.length;
    const pendingMaintenances = maintenances.filter(m => m.status === 'PROGRAMADO').length;
    const completedMaintenances = maintenances.filter(m => m.status === 'COMPLETADO').length;
    const totalCost = maintenances.reduce((sum, m) => sum + (m.cost || 0), 0);

    document.getElementById('totalMaintenances').textContent = totalMaintenances;
    document.getElementById('pendingMaintenances').textContent = pendingMaintenances;
    document.getElementById('completedMaintenances').textContent = completedMaintenances;
    document.getElementById('totalCost').textContent = formatPrice(totalCost);
}

// Format price in Colombian pesos
function formatPrice(price) {
    return new Intl.NumberFormat('es-CO', {
        style: 'currency',
        currency: 'COP',
        minimumFractionDigits: 0
    }).format(price);
}

// Format maintenance type for display
function formatMaintenanceType(type) {
    const typeMap = {
        'PREVENTIVO': 'Preventivo',
        'CORRECTIVO': 'Correctivo'
    };
    return typeMap[type] || type;
}

// Format priority for display
function formatPriority(priority) {
    const priorityMap = {
        'BAJA': 'Baja',
        'MEDIA': 'Media',
        'ALTA': 'Alta',
        'CRITICA': 'Crítica'
    };
    return priorityMap[priority] || priority;
}

// Format payment status for display
function formatPaymentStatus(payment) {
    if (!payment) return 'Sin pago';
    const statusMap = {
        'PAGADO': 'Pagado',
        'POR_PAGAR': 'Por Pagar'
    };
    return statusMap[payment.status] || payment.status;
}

// Get payment status class for styling
function getPaymentStatusClass(payment) {
    if (!payment) return 'payment-none';
    return `payment-${payment.status.toLowerCase().replace('_', '_')}`;
}

// Get priority class for styling
function getPriorityClass(priority) {
    return `priority-${priority.toLowerCase()}`;
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
    if (prevBtn) prevBtn.disabled = currentPage <= 0;
    if (nextBtn) nextBtn.disabled = currentPage >= totalPages - 1;

    // Update page info
    if (pageInfo) pageInfo.textContent = `Página ${currentPage + 1} de ${totalPages}`;

    // Update pagination info
    if (paginationInfo) {
        const startItem = currentPage * pageSize + 1;
        const endItem = Math.min((currentPage + 1) * pageSize, totalElements);
        paginationInfo.textContent = `Mostrando ${startItem}-${endItem} de ${totalElements} mantenimientos`;
    }
}

// Change page function
function changePage(page) {
    if (page < 0 || page >= totalPages) return;
    const searchTerm = searchInput.value;
    const statusValue = statusFilter.value;
    const typeValue = typeFilter.value;
    loadMaintenances(page, searchTerm, statusValue, typeValue);
}

// Filter maintenances based on search and filters
function filterMaintenances() {
    const searchTerm = searchInput.value;
    const statusValue = statusFilter.value;
    const typeValue = typeFilter.value;

    // Reload data with filters applied server-side
    loadMaintenances(0, searchTerm, statusValue, typeValue);
}

// Render maintenances in the table
function renderMaintenances() {
    console.log('Rendering maintenances:', maintenances);
    maintenanceTableBody.innerHTML = '';

    if (maintenances.length === 0) {
        const emptyRow = document.createElement('tr');
        emptyRow.innerHTML = `
            <td colspan="10" style="text-align: center; padding: 40px; color: #6b7280;">
                No se encontraron mantenimientos
            </td>
        `;
        maintenanceTableBody.appendChild(emptyRow);
    } else {
        maintenances.forEach(maintenance => {
            const row = document.createElement('tr');
            const scheduledDate = maintenance.dateScheduled ?
                new Date(maintenance.dateScheduled).toLocaleString('es-ES') : 'N/A';
            const performedDate = maintenance.datePerformed ?
                new Date(maintenance.datePerformed).toLocaleString('es-ES') : 'Pendiente';
            const status = maintenance.status || 'PROGRAMADO';
            const priority = maintenance.priority || 'MEDIA';

            row.innerHTML = `
                <td>${maintenance.boat ? maintenance.boat.name + ' (' + maintenance.boat.model + ')' : 'N/A'}</td>
                <td>${formatMaintenanceType(maintenance.type)}</td>
                <td><span class="status-badge status-${status.toLowerCase().replace('_', '_')}">${status.replace('_', ' ')}</span></td>
                <td><span class="priority-badge ${getPriorityClass(priority)}">${formatPriority(priority)}</span></td>
                <td>${scheduledDate}</td>
                <td>${performedDate}</td>
                <td class="price">${formatPrice(maintenance.cost || 0)}</td>
                <td>${maintenance.description || 'Sin descripción'}</td>
                <td><span class="payment-badge ${getPaymentStatusClass(maintenance.payment)}">${formatPaymentStatus(maintenance.payment)}</span></td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn edit-btn" onclick="editMaintenance(${maintenance.id})">Editar</button>
                        <button class="action-btn delete-btn" onclick="deleteMaintenance(${maintenance.id})">Eliminar</button>
                        ${maintenance.payment && !maintenance.payment.invoice_url ? `<button class="action-btn receipt-btn" onclick="openReceiptModal(${maintenance.payment.id})">Añadir Recibo</button>` : ''}
                        ${maintenance.payment && maintenance.payment.invoice_url ? `<button class="action-btn download-btn" onclick="downloadReceipt(${maintenance.payment.id})">Descargar Recibo</button>` : ''}
                    </div>
                </td>
            `;
            maintenanceTableBody.appendChild(row);
        });
    }

    // Update table count with pagination info
    const startItem = currentPage * pageSize + 1;
    const endItem = Math.min((currentPage + 1) * pageSize, totalElements);
    document.getElementById('tableCount').textContent = `${maintenances.length} mantenimientos en esta página`;
}

// Open add maintenance modal
function openAddModal() {
    currentEditingMaintenance = null;
    modalTitle.textContent = 'Agregar Nuevo Mantenimiento';
    saveBtn.textContent = 'Crear Mantenimiento';

    // Reset form
    maintenanceForm.reset();

    maintenanceModal.style.display = 'block';
}

// Edit maintenance
function editMaintenance(id) {
    const maintenance = maintenances.find(m => m.id === id);
    if (!maintenance) return;

    currentEditingMaintenance = maintenance;
    modalTitle.textContent = 'Editar Mantenimiento';
    saveBtn.textContent = 'Guardar Cambios';

    // Fill form with maintenance data
    document.getElementById('boatSelect').value = maintenance.boat && maintenance.boat.id ? maintenance.boat.id : '';
    document.getElementById('maintenanceType').value = maintenance.type;
    document.getElementById('maintenanceStatus').value = maintenance.status;
    document.getElementById('priority').value = maintenance.priority;
    document.getElementById('scheduledDate').value = maintenance.dateScheduled ?
        new Date(maintenance.dateScheduled).toISOString().slice(0, 16) : '';
    document.getElementById('performedDate').value = maintenance.datePerformed ?
        new Date(maintenance.datePerformed).toISOString().slice(0, 16) : '';
    document.getElementById('cost').value = maintenance.cost || '';
    document.getElementById('description').value = maintenance.description || '';

    maintenanceModal.style.display = 'block';
}

// Delete maintenance
async function deleteMaintenance(id) {
    if (!confirm('¿Estás seguro de eliminar este mantenimiento? Esta acción no se puede deshacer.')) return;

    try {
        const response = await fetch(`/api/v1/maintenances/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            // Remove from local arrays only after successful API deletion
            maintenances = maintenances.filter(maintenance => maintenance.id !== id);
            filteredMaintenances = filteredMaintenances.filter(maintenance => maintenance.id !== id);
            updateMetrics();
            renderMaintenances();
        } else {
            console.error('Failed to delete maintenance - Status:', response.status);
            const errorText = await response.text();
            console.error('Error response:', errorText);
            alert('Error al eliminar el mantenimiento. Inténtalo de nuevo.');
        }
    } catch (error) {
        console.error('Error deleting maintenance:', error);
        alert('Error al eliminar el mantenimiento. Inténtalo de nuevo.');
    }
}

// Handle form submission
async function saveMaintenance(event) {
    if (event && typeof event.preventDefault === 'function') {
        event.preventDefault();
    }

    const formData = new FormData(maintenanceForm);
    const boatId = formData.get('boatId');

    if (!boatId || boatId.trim() === '') {
        alert('Por favor selecciona una embarcación');
        return;
    }

    const maintenanceData = {
        type: formData.get('type'),
        status: formData.get('status'),
        priority: formData.get('priority'),
        dateScheduled: formData.get('dateScheduled') ? formData.get('dateScheduled') + ':00' : null,
        datePerformed: formData.get('datePerformed') ? formData.get('datePerformed') + ':00' : null,
        description: formData.get('description'),
        cost: parseFloat(formData.get('cost')) || null
    };

    try {
        if (currentEditingMaintenance) {
            // Update existing maintenance
            const response = await fetch(`/api/v1/maintenances/${currentEditingMaintenance.id}`, {
                method: 'PUT',
                headers: getAuthHeaders(),
                body: JSON.stringify(maintenanceData)
            });

            if (response.ok) {
                const updatedMaintenance = await response.json();
                const index = maintenances.findIndex(m => m.id === currentEditingMaintenance.id);
                if (index !== -1) {
                    maintenances[index] = updatedMaintenance;
                }
            } else {
                console.error('Failed to update maintenance');
                closeModal();
                return;
            }
        } else {
            // Add new maintenance
            const response = await fetch(`/api/v1/maintenances/boat/${boatId}`, {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify(maintenanceData)
            });

            if (response.ok) {
                const newMaintenance = await response.json();
                maintenances.push(newMaintenance);
            } else {
                console.error('Failed to create maintenance');
                closeModal();
                return;
            }
        }

        // Reload the table to reflect changes
        const searchTerm = searchInput.value;
        const statusValue = statusFilter.value;
        const typeValue = typeFilter.value;
        loadMaintenances(currentPage, searchTerm, statusValue, typeValue);
        closeModal();
    } catch (error) {
        console.error('Error saving maintenance:', error);
        closeModal();
    }
}

// Close modal
function closeModal() {
    maintenanceModal.style.display = 'none';
    currentEditingMaintenance = null;
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

// Open receipt modal
function openReceiptModal(paymentId) {
    currentPaymentId = paymentId;
    receiptForm.reset();
    receiptModal.style.display = 'block';
}

// Close receipt modal
function closeReceiptModal() {
    receiptModal.style.display = 'none';
    currentPaymentId = null;
}

// Upload receipt
async function uploadReceipt() {
    if (!currentPaymentId) {
        alert('Este mantenimiento no tiene un pago asociado.');
        return;
    }

    const formData = new FormData(receiptForm);
    const receiptFile = formData.get('receipt');

    if (!receiptFile || receiptFile.size === 0) {
        alert('Por favor selecciona un archivo de recibo');
        return;
    }

    // Validate file size (5MB max)
    if (receiptFile.size > 5 * 1024 * 1024) {
        alert('El archivo es demasiado grande. Tamaño máximo: 5MB');
        return;
    }

    // Validate file type
    const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg', 'application/pdf'];
    if (!allowedTypes.includes(receiptFile.type)) {
        alert('Tipo de archivo no permitido. Solo se permiten imágenes (JPG, PNG) y PDF');
        return;
    }

    uploadBtn.disabled = true;
    uploadBtn.textContent = 'Subiendo...';

    try {
        const uploadData = new FormData();
        uploadData.append('file', receiptFile);

        const response = await fetch(`/api/v1/payments/${currentPaymentId}/receipt`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('jwt')}`
            },
            body: uploadData
        });

        if (response.ok) {
            alert('Recibo subido exitosamente');

            // Reload the maintenances to reflect changes
            const searchTerm = searchInput.value;
            const statusValue = statusFilter.value;
            const typeValue = typeFilter.value;
            loadMaintenances(currentPage, searchTerm, statusValue, typeValue);
            closeReceiptModal();
        } else {
            const error = response.statusText || 'Error desconocido';
            alert('Error al subir el recibo: ' + error);
        }
    } catch (error) {
        console.error('Error uploading receipt:', error);
        alert('Error al subir el recibo. Inténtalo de nuevo.');
    } finally {
        uploadBtn.disabled = false;
        uploadBtn.textContent = 'Subir Recibo';
    }
}

// Close modal when clicking outside
window.onclick = function(event) {
    if (event.target === maintenanceModal) {
        closeModal();
    }
    if (event.target === receiptModal) {
        closeReceiptModal();
    }
};

// Export functions for HTML onclick handlers
window.saveMaintenance = saveMaintenance;
window.openAddModal = openAddModal;
window.closeModal = closeModal;
window.editMaintenance = editMaintenance;
window.deleteMaintenance = deleteMaintenance;
window.openReceiptModal = openReceiptModal;
window.closeReceiptModal = closeReceiptModal;
window.uploadReceipt = uploadReceipt;
window.downloadReceipt = downloadReceipt;
window.changePage = changePage;
window.logout = logout;
window.navigateTo = navigateTo;