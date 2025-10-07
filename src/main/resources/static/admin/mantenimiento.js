// Maintenance Management JavaScript

// State management
let maintenances = [];
let filteredMaintenances = [];
let boats = [];
let currentEditingMaintenance = null;

// DOM elements
const searchInput = document.getElementById('searchInput');
const statusFilter = document.getElementById('statusFilter');
const typeFilter = document.getElementById('typeFilter');
const maintenanceTableBody = document.getElementById('maintenanceTableBody');
const maintenanceModal = document.getElementById('maintenanceModal');
const maintenanceForm = document.getElementById('maintenanceForm');
const modalTitle = document.getElementById('modalTitle');
const saveBtn = document.getElementById('saveBtn');

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
        }
    } catch (error) {
        console.error('Error loading boats:', error);
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
async function loadMaintenances() {
    try {
        const response = await fetch('/api/v1/admin/maintenances', {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            maintenances = await response.json();
            filteredMaintenances = [...maintenances];
            updateMetrics();
            renderMaintenances();
        } else {
            console.error('Failed to load maintenances');
            showFallbackData();
        }
    } catch (error) {
        console.error('Error loading maintenances:', error);
        showFallbackData();
    }
}

// Show fallback data when API fails
function showFallbackData() {
    maintenances = [
        {
            id: 1,
            boat: { id: 1, name: 'Catamaran A', model: 'Modelo X' },
            type: 'PREVENTIVO',
            status: 'PROGRAMADO',
            priority: 'MEDIA',
            dateScheduled: '2024-12-15T10:00:00',
            description: 'Mantenimiento preventivo mensual',
            cost: 150000
        },
        {
            id: 2,
            boat: { id: 2, name: 'Catamaran B', model: 'Modelo Y' },
            type: 'CORRECTIVO',
            status: 'EN_PROCESO',
            priority: 'ALTA',
            dateScheduled: '2024-12-10T14:00:00',
            datePerformed: '2024-12-10T16:00:00',
            description: 'Reparación de motor',
            cost: 500000
        }
    ];
    filteredMaintenances = [...maintenances];
    updateMetrics();
    renderMaintenances();
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

// Get priority class for styling
function getPriorityClass(priority) {
    return `priority-${priority.toLowerCase()}`;
}

// Filter maintenances based on search and filters
function filterMaintenances() {
    const searchTerm = searchInput.value.toLowerCase();
    const statusValue = statusFilter.value;
    const typeValue = typeFilter.value;

    filteredMaintenances = maintenances.filter(maintenance => {
        const matchesSearch = maintenance.description.toLowerCase().includes(searchTerm) ||
                             maintenance.boat.name.toLowerCase().includes(searchTerm) ||
                             maintenance.boat.model.toLowerCase().includes(searchTerm);

        const matchesStatus = statusValue === 'all' || maintenance.status === statusValue;
        const matchesType = typeValue === 'all' || maintenance.type === typeValue;

        return matchesSearch && matchesStatus && matchesType;
    });

    renderMaintenances();
}

// Render maintenances in the table
function renderMaintenances() {
    maintenanceTableBody.innerHTML = '';

    if (filteredMaintenances.length === 0) {
        const emptyRow = document.createElement('tr');
        emptyRow.innerHTML = `
            <td colspan="9" style="text-align: center; padding: 40px; color: #6b7280;">
                No se encontraron mantenimientos
            </td>
        `;
        maintenanceTableBody.appendChild(emptyRow);
    } else {
        filteredMaintenances.forEach(maintenance => {
            const row = document.createElement('tr');
            const scheduledDate = maintenance.dateScheduled ?
                new Date(maintenance.dateScheduled).toLocaleString('es-ES') : 'N/A';
            const performedDate = maintenance.datePerformed ?
                new Date(maintenance.datePerformed).toLocaleString('es-ES') : 'Pendiente';

            row.innerHTML = `
                <td>${maintenance.boat.name} (${maintenance.boat.model})</td>
                <td>${formatMaintenanceType(maintenance.type)}</td>
                <td><span class="status-badge status-${maintenance.status.toLowerCase().replace('_', '_')}">${maintenance.status.replace('_', ' ')}</span></td>
                <td><span class="priority-badge ${getPriorityClass(maintenance.priority)}">${formatPriority(maintenance.priority)}</span></td>
                <td>${scheduledDate}</td>
                <td>${performedDate}</td>
                <td class="price">${formatPrice(maintenance.cost || 0)}</td>
                <td>${maintenance.description || 'Sin descripción'}</td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn edit-btn" onclick="editMaintenance(${maintenance.id})">Editar</button>
                        <button class="action-btn delete-btn" onclick="deleteMaintenance(${maintenance.id})">Eliminar</button>
                    </div>
                </td>
            `;
            maintenanceTableBody.appendChild(row);
        });
    }

    document.getElementById('tableCount').textContent = `${filteredMaintenances.length} de ${maintenances.length} mantenimientos`;
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
    document.getElementById('boatSelect').value = maintenance.boat.id;
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
function deleteMaintenance(id) {
    if (!confirm('¿Estás seguro de eliminar este mantenimiento? Esta acción no se puede deshacer.')) return;

    // For now, just remove from local array
    // In a real app, this would call the API
    maintenances = maintenances.filter(maintenance => maintenance.id !== id);
    filteredMaintenances = filteredMaintenances.filter(maintenance => maintenance.id !== id);
    updateMetrics();
    renderMaintenances();
}

// Handle form submission
async function saveMaintenance(event) {
    if (event && typeof event.preventDefault === 'function') {
        event.preventDefault();
    }

    const formData = new FormData(maintenanceForm);
    const boatId = formData.get('boatId');
    const maintenanceData = {
        type: formData.get('type'),
        status: formData.get('status'),
        priority: formData.get('priority'),
        dateScheduled: formData.get('dateScheduled') ? new Date(formData.get('dateScheduled')).toISOString() : null,
        datePerformed: formData.get('datePerformed') ? new Date(formData.get('datePerformed')).toISOString() : null,
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
                return;
            }
        } else {
            // Add new maintenance
            const response = await fetch(`/api/v1/maintenances/${boatId}`, {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify(maintenanceData)
            });

            if (response.ok) {
                const newMaintenance = await response.json();
                maintenances.push(newMaintenance);
            } else {
                console.error('Failed to create maintenance');
                return;
            }
        }

        filteredMaintenances = [...maintenances];
        updateMetrics();
        renderMaintenances();
        closeModal();
    } catch (error) {
        console.error('Error saving maintenance:', error);
        // Fallback to local state update
        if (currentEditingMaintenance) {
            const updatedMaintenance = { ...currentEditingMaintenance, ...maintenanceData };
            const index = maintenances.findIndex(m => m.id === currentEditingMaintenance.id);
            if (index !== -1) {
                maintenances[index] = updatedMaintenance;
            }
        } else {
            const selectedBoat = boats.find(b => b.id == boatId);
            const newMaintenance = {
                ...maintenanceData,
                id: Math.max(...maintenances.map(m => m.id), 0) + 1,
                boat: selectedBoat
            };
            maintenances.push(newMaintenance);
        }
        filteredMaintenances = [...maintenances];
        updateMetrics();
        renderMaintenances();
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

// Close modal when clicking outside
window.onclick = function(event) {
    if (event.target === maintenanceModal) {
        closeModal();
    }
};

// Export functions for HTML onclick handlers
window.saveMaintenance = saveMaintenance;
window.openAddModal = openAddModal;
window.closeModal = closeModal;
window.editMaintenance = editMaintenance;
window.deleteMaintenance = deleteMaintenance;
window.logout = logout;
window.navigateTo = navigateTo;