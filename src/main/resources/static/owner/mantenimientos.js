// Owner Maintenances Page JavaScript

// State management
let maintenances = [];
let filteredMaintenances = [];
let currentPage = 0;
let totalPages = 0;
let totalElements = 0;
let pageSize = 10;
let currentFilters = {
    search: '',
    status: '',
    type: ''
};

// DOM elements
const searchInput = document.getElementById('searchInput');
const statusFilter = document.getElementById('statusFilter');
const typeFilter = document.getElementById('typeFilter');
const maintenanceTableBody = document.getElementById('maintenanceTableBody');

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    checkAuthentication();
    setupEventListeners();
    loadMaintenances(0);
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

// Load maintenances from API with server-side pagination
async function loadMaintenances(page = 0) {
    try {
        const userId = localStorage.getItem('userId');
        if (!userId) {
            console.error('User ID not found');
            return;
        }

        // Build query parameters
        const params = new URLSearchParams({
            page: page.toString(),
            size: pageSize.toString()
        });

        // Add filters if they exist
        if (currentFilters.search && currentFilters.search.trim()) {
            params.append('search', currentFilters.search.trim());
        }
        if (currentFilters.status && currentFilters.status !== 'all') {
            params.append('status', currentFilters.status);
        }
        if (currentFilters.type && currentFilters.type !== 'all') {
            params.append('type', currentFilters.type);
        }

        const response = await fetch(`/api/v1/owner/maintenances/${userId}?${params}`, {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            maintenances = data.content || [];
            totalPages = data.totalPages || 1;
            totalElements = data.totalElements || 0;
            currentPage = page;
            filteredMaintenances = [...maintenances];

            console.log('Loaded maintenances:', maintenances.length, 'maintenances');
            console.log('Total elements:', totalElements);

            displayMaintenances(maintenances);
            updatePaginationControls();
        } else {
            throw new Error(`HTTP ${response.status}`);
        }
    } catch (error) {
        console.error('Error loading maintenances:', error);

        // Provide more specific error messages based on the error type
        let errorMessage = 'Error al cargar los mantenimientos. Por favor, inténtalo de nuevo.';

        if (error.message && error.message.includes('403')) {
            errorMessage = 'Acceso denegado. Verifica que tu sesión sea válida e intenta nuevamente.';
        } else if (error.message && error.message.includes('404')) {
            errorMessage = 'Servicio no encontrado. Contacta al administrador.';
        } else if (error.message && error.message.includes('500')) {
            errorMessage = 'Error interno del servidor. Inténtalo más tarde.';
        } else if (error.message && error.message.includes('network')) {
            errorMessage = 'Error de conexión. Verifica tu conexión a internet.';
        }

        showError(errorMessage);
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

// Show error message
function showError(message) {
    if (maintenanceTableBody) {
        maintenanceTableBody.innerHTML = `
            <tr>
                <td colspan="8" style="text-align: center; padding: 40px; color: #dc2626;">
                    <h3>⚠️ ${message}</h3>
                    <button onclick="location.reload()" style="margin-top: 20px; padding: 10px 20px; background: #3b82f6; color: white; border: none; border-radius: 6px; cursor: pointer;">
                        Reintentar
                    </button>
                </td>
            </tr>
        `;
    }

    // Hide the no maintenances message if it exists
    const noMaintenancesMessage = document.getElementById('noMaintenancesMessage');
    if (noMaintenancesMessage) {
        noMaintenancesMessage.style.display = 'none';
    }
}

// Filter maintenances based on search and filters - now using server-side filtering
function filterMaintenances() {
    currentFilters.search = searchInput.value;
    currentFilters.status = statusFilter.value;
    currentFilters.type = typeFilter.value;

    // Reset to first page when filtering
    currentPage = 0;
    loadMaintenances(0);
}

// Display maintenances in table format
function displayMaintenances() {
    if (filteredMaintenances.length === 0) {
        maintenanceTableBody.innerHTML = '';
        const noMaintenancesMessage = document.createElement('div');
        noMaintenancesMessage.id = 'noMaintenancesMessage';
        noMaintenancesMessage.style.display = 'block';
        noMaintenancesMessage.style.textAlign = 'center';
        noMaintenancesMessage.style.color = '#6b7280';
        noMaintenancesMessage.style.padding = '40px';
        noMaintenancesMessage.textContent = 'No tienes mantenimientos asignados';
        // Insert after table
        const tableContainer = document.querySelector('.table-container');
        tableContainer.appendChild(noMaintenancesMessage);
        return;
    }

    // Hide no maintenances message if it exists
    const noMaintenancesMessage = document.getElementById('noMaintenancesMessage');
    if (noMaintenancesMessage) {
        noMaintenancesMessage.style.display = 'none';
    }

    maintenanceTableBody.innerHTML = filteredMaintenances.map(maintenance => {
        const scheduledDate = maintenance.dateScheduled ?
            new Date(maintenance.dateScheduled).toLocaleString('es-ES') : 'N/A';
        const performedDate = maintenance.datePerformed ?
            new Date(maintenance.datePerformed).toLocaleString('es-ES') : 'Pendiente';
        const status = maintenance.status || 'PROGRAMADO';
        const priority = maintenance.priority || 'MEDIA';

        return `
            <tr>
                <td>${maintenance.boat ? maintenance.boat.name : 'N/A'}</td>
                <td>${formatMaintenanceType(maintenance.type)}</td>
                <td><span class="status-badge status-${status.toLowerCase().replace('_', '_')}">${status.replace('_', ' ')}</span></td>
                <td><span class="priority-badge priority-${priority.toLowerCase()}">${formatPriority(priority)}</span></td>
                <td>${scheduledDate}</td>
                <td>${performedDate}</td>
                <td class="price">${formatPrice(maintenance.cost || 0)}</td>
                <td>${maintenance.description || 'Sin descripción'}</td>
            </tr>
        `;
    }).join('');

    // Update table count
    document.getElementById('tableCount').textContent = `${filteredMaintenances.length} mantenimientos en esta página`;
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
    paginationInfo.textContent = `Mostrando ${startItem}-${endItem} de ${totalElements} mantenimientos`;
}

// Change page function
function changePage(page) {
    if (page < 0 || page >= totalPages) return;

    const userId = localStorage.getItem('userId');
    if (userId) {
        loadMaintenances(page);
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

// Global reference for HTML onclick handlers
window.changePage = changePage;

// Export functions for potential use in other scripts
window.logout = logout;
window.navigateTo = navigateTo;
window.changePage = changePage;