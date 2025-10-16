// Owner Maintenances Page JavaScript

// State management
let maintenances = [];
let filteredMaintenances = [];
let paginator = null;

// DOM elements
const searchInput = document.getElementById('searchInput');
const statusFilter = document.getElementById('statusFilter');
const typeFilter = document.getElementById('typeFilter');
const maintenanceTableBody = document.getElementById('maintenanceTableBody');

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    checkAuthentication();
    setupEventListeners();
    loadMaintenances();
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
    const userNameElement = document.getElementById('userName');
    if (userNameElement) {
        userNameElement.textContent = username;
    }
}

// Setup event listeners
function setupEventListeners() {
    searchInput.addEventListener('input', filterMaintenances);
    statusFilter.addEventListener('change', filterMaintenances);
    typeFilter.addEventListener('change', filterMaintenances);
}

// Get authentication headers
function getAuthHeaders() {
    const jwt = localStorage.getItem('jwt');
    return {
        'Content-Type': 'application/json',
        ...(jwt ? { 'Authorization': `Bearer ${jwt}` } : {})
    };
}

// Load maintenances from API
async function loadMaintenances() {
    try {
        const userId = localStorage.getItem('userId');
        if (!userId) {
            console.error('User ID not found');
            return;
        }

        const response = await fetch(`/api/v1/owner/dashboard/${userId}`, {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            maintenances = data.allMaintenances || [];
            filteredMaintenances = [...maintenances];
            paginator = new Paginator(filteredMaintenances, 10);
            window.paginator_maintenances = paginator;
            window.renderWithPagination_maintenances = renderMaintenances;
            renderMaintenances();
        } else {
            console.error('Failed to load maintenances');
            maintenances = [];
            filteredMaintenances = [];
            renderMaintenances();
        }
    } catch (error) {
        console.error('Error loading maintenances:', error);
        maintenances = [];
        filteredMaintenances = [];
        renderMaintenances();
    }
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
        'CORRECTIVO': 'Correctivo',
        'PREDICTIVO': 'Predictivo'
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

// Filter maintenances based on search and filters
function filterMaintenances() {
    const searchTerm = searchInput.value.toLowerCase();
    const statusValue = statusFilter.value;
    const typeValue = typeFilter.value;

    filteredMaintenances = maintenances.filter(maintenance => {
        const matchesSearch = maintenance.description.toLowerCase().includes(searchTerm) ||
                             maintenance.boatName.toLowerCase().includes(searchTerm);

        const matchesStatus = statusValue === 'all' || maintenance.status === statusValue;
        const matchesType = typeValue === 'all' || maintenance.type === typeValue;

        return matchesSearch && matchesStatus && matchesType;
    });

    if (paginator) {
        paginator.updateItems(filteredMaintenances);
    }
    renderMaintenances();
}

// Render maintenances in the table
function renderMaintenances() {
    maintenanceTableBody.innerHTML = '';

    const itemsToDisplay = paginator ? paginator.getCurrentPageItems() : filteredMaintenances;

    if (itemsToDisplay.length === 0) {
        const emptyRow = document.createElement('tr');
        emptyRow.innerHTML = `
            <td colspan="8" style="text-align: center; padding: 40px; color: #6b7280;">
                No se encontraron mantenimientos
            </td>
        `;
        maintenanceTableBody.appendChild(emptyRow);
    } else {
        itemsToDisplay.forEach(maintenance => {
            const row = document.createElement('tr');
            const scheduledDate = maintenance.scheduledDate ?
                new Date(maintenance.scheduledDate).toLocaleString('es-ES') : 'N/A';
            const performedDate = maintenance.performedDate ?
                new Date(maintenance.performedDate).toLocaleString('es-ES') : 'Pendiente';
            const status = maintenance.status || 'PROGRAMADO';
            const priority = maintenance.priority || 'MEDIA';

            row.innerHTML = `
                <td>${maintenance.boatName || 'N/A'}</td>
                <td>${formatMaintenanceType(maintenance.type)}</td>
                <td><span class="status-badge status-${status.toLowerCase().replace('_', '_')}">${status.replace('_', ' ')}</span></td>
                <td><span class="priority-badge priority-${priority.toLowerCase()}">${formatPriority(priority)}</span></td>
                <td>${scheduledDate}</td>
                <td>${performedDate}</td>
                <td class="price">${formatPrice(maintenance.cost || 0)}</td>
                <td>${maintenance.description || 'Sin descripción'}</td>
            `;
            maintenanceTableBody.appendChild(row);
        });
    }

    document.getElementById('tableCount').textContent = `${filteredMaintenances.length} de ${maintenances.length} mantenimientos`;
    
    // Render pagination
    const paginationContainer = document.getElementById('paginationContainer');
    if (paginationContainer && paginator) {
        paginationContainer.innerHTML = paginator.generatePaginationHTML('maintenances');
    }
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
    window.location.href = `${page}.html`;
}

// Export functions for HTML onclick handlers
window.logout = logout;
window.navigateTo = navigateTo;