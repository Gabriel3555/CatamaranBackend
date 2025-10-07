// Owner Dashboard JavaScript

// Get authentication headers
function getAuthHeaders() {
    const jwt = localStorage.getItem('jwt');
    return {
        'Content-Type': 'application/json',
        ...(jwt ? { 'Authorization': `Bearer ${jwt}` } : {})
    };
}

// Check authentication on page load
document.addEventListener('DOMContentLoaded', function() {
    checkAuthentication();
    loadDashboardData();
});

// Authentication check
function checkAuthentication() {
    const userType = localStorage.getItem('userType');
    const jwt = localStorage.getItem('jwt');

    if (!userType || userType !== 'owner' || !jwt) {
        // Redirect to login if not authenticated as owner
        window.location.href = '../login.html';
        return;
    }
}

// Load dashboard data
function loadDashboardData() {
    // Get user ID from localStorage (assuming it's stored during login)
    const userId = localStorage.getItem('userId');

    if (!userId) {
        console.error('User ID not found');
        return;
    }

    // Update welcome message
    const username = localStorage.getItem('username') || 'Propietario';
    document.getElementById('userName').textContent = username;
    document.getElementById('welcomeMessage').textContent = `Bienvenido de vuelta, ${username}`;

    // Load real data from API
    loadOwnerDashboard(userId);
}

// Load owner dashboard from API
async function loadOwnerDashboard(userId) {
    try {
        const response = await fetch(`/api/v1/owner/dashboard/${userId}`, {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            updateOwnerDashboard(data);
        } else {
            console.error('Failed to load owner dashboard');
            // Show error message
            showDashboardError('Error al cargar el dashboard. Por favor, inténtalo de nuevo.');
        }
    } catch (error) {
        console.error('Error loading owner dashboard:', error);
        showDashboardError('Error de conexión. Verifica tu conexión a internet.');
    }
}

// Show error message on dashboard
function showDashboardError(message) {
    const mainContent = document.querySelector('.main-content');
    if (mainContent) {
        mainContent.innerHTML = `
            <div class="container">
                <div class="page-header">
                    <h2>Error</h2>
                </div>
                <div class="section-card">
                    <div style="text-align: center; padding: 40px; color: #dc2626;">
                        <h3>⚠️ ${message}</h3>
                        <button onclick="location.reload()" style="margin-top: 20px; padding: 10px 20px; background: #3b82f6; color: white; border: none; border-radius: 6px; cursor: pointer;">
                            Reintentar
                        </button>
                    </div>
                </div>
            </div>
        `;
    }
}

// Update dashboard with real API data
function updateOwnerDashboard(data) {
    // Update user info
    const user = data.user;
    document.getElementById('userName').textContent = user.fullName;
    document.getElementById('welcomeMessage').textContent = `Bienvenido de vuelta, ${user.fullName}`;

    // Update metrics
    const metrics = data.metrics;
    document.getElementById('myBoatsCount').textContent = metrics.totalBoats;
    document.getElementById('pendingMaintenances').textContent = metrics.pendingMaintenances;
    document.getElementById('completedMaintenances').textContent = metrics.completedMaintenances;
    document.getElementById('documentsCount').textContent = metrics.totalDocuments;

    // Update boats
    loadBoatsFromData(data.boats);

    // Update upcoming maintenances
    loadUpcomingEventsFromData(data.upcomingMaintenances);

    // Update recent maintenances
    loadRecentMaintenancesFromData(data.recentMaintenances);
}

// Load boats from API data
function loadBoatsFromData(boats) {
    const boatsGrid = document.getElementById('boatsGrid');
    const boatsToShow = boats.slice(0, 2); // Show only first 2 boats

    if (boatsToShow.length === 0) {
        boatsGrid.innerHTML = '<p style="text-align: center; color: #6b7280; padding: 20px;">No tienes embarcaciones asignadas</p>';
        return;
    }

    boatsGrid.innerHTML = boatsToShow.map(boat => `
        <div class="boat-card">
            <div class="boat-header">
                <div class="boat-title">${boat.name}</div>
                <span class="boat-badge available">Asignada</span>
            </div>
            <div class="boat-info">
                <span>${formatBoatType(boat.type)} • ${boat.location}</span>
            </div>
            <div class="boat-details">
                <span>Precio: ${formatPrice(boat.price || 0)}</span>
                <button onclick="viewBoatDetails(${boat.id})" class="primary-btn" style="padding: 0.25rem 0.75rem; font-size: 0.75rem;">Ver Detalles</button>
            </div>
        </div>
    `).join('');
}

// Load upcoming events from API data
function loadUpcomingEventsFromData(upcomingMaintenances) {
    const eventsList = document.getElementById('eventsList');

    if (upcomingMaintenances.length === 0) {
        eventsList.innerHTML = '<p style="text-align: center; color: #6b7280; padding: 20px;">No hay mantenimientos próximos</p>';
        return;
    }

    eventsList.innerHTML = upcomingMaintenances.map(maintenance => {
        const scheduledDate = new Date(maintenance.scheduledDate);
        const daysUntil = getDaysUntil(scheduledDate.toISOString().split('T')[0]);

        return `
            <div class="event-card">
                <div class="event-header">
                    <div class="event-title">${maintenance.boatName}</div>
                    <span class="event-badge ${getEventBadgeClass(daysUntil)}">${getEventBadgeText(daysUntil)}</span>
                </div>
                <div class="event-description">${maintenance.description}</div>
                <div class="event-details">
                    <span>📅 ${formatDate(scheduledDate.toISOString().split('T')[0])}</span>
                    <span>Prioridad: ${maintenance.priority}</span>
                </div>
            </div>
        `;
    }).join('');
}

// Load recent maintenances from API data
function loadRecentMaintenancesFromData(recentMaintenances) {
    const maintenancesList = document.getElementById('maintenancesList');

    if (recentMaintenances.length === 0) {
        maintenancesList.innerHTML = '<p style="text-align: center; color: #6b7280; padding: 20px;">No hay mantenimientos recientes</p>';
        return;
    }

    maintenancesList.innerHTML = recentMaintenances.slice(0, 3).map(maintenance => `
        <div class="maintenance-card">
            <div class="maintenance-header">
                <div class="maintenance-title">${maintenance.boatName}</div>
                <span class="maintenance-status ${getMaintenanceStatusClass(maintenance.status)}">
                    ${maintenance.status}
                </span>
            </div>
            <div class="maintenance-description">${maintenance.description}</div>
            <div class="maintenance-details">
                <span>Tipo: ${maintenance.type}</span>
                <span>Prioridad: ${maintenance.priority}</span>
                <span>Fecha: ${formatDate(maintenance.scheduledDate ? maintenance.scheduledDate.split('T')[0] : 'N/A')}</span>
                ${maintenance.cost ? `<span>Costo: ${formatPrice(maintenance.cost)}</span>` : ''}
            </div>
        </div>
    `).join('');
}

// Helper functions
function formatBoatType(type) {
    const typeMap = {
        'TURISMO': 'Turismo',
        'ALOJAMIENTO': 'Alojamiento',
        'EVENTOS_NEGOCIOS': 'Eventos y Negocios',
        'DISENO_EXCLUSIVO': 'Diseño Exclusivo'
    };
    return typeMap[type] || type;
}

function formatPrice(price) {
    return new Intl.NumberFormat('es-CO', {
        style: 'currency',
        currency: 'COP',
        minimumFractionDigits: 0
    }).format(price);
}

function getEventBadgeClass(daysUntil) {
    if (daysUntil <= 7) return "soon";
    if (daysUntil <= 30) return "upcoming";
    return "normal";
}

function getEventBadgeText(daysUntil) {
    if (daysUntil === 0) return "Hoy";
    if (daysUntil === 1) return "Mañana";
    if (daysUntil < 0) return "Vencido";
    return `En ${daysUntil} días`;
}

function getMaintenanceStatusClass(status) {
    const classes = {
        "Pendiente": "pending",
        "En Proceso": "progress",
        "Completado": "completed"
    };
    return classes[status] || "pending";
}

function formatDate(dateString) {
    return new Date(dateString).toLocaleDateString('es-ES');
}

function getDaysUntil(dateString) {
    const today = new Date();
    const targetDate = new Date(dateString);
    const diffTime = targetDate - today;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
}

// Navigation functions
function navigateTo(page) {
    // In a real application, this would navigate to different pages
    alert(`Navegando a: ${page}`);

    // You could implement actual navigation like:
    // window.location.href = `${page}.html`;
}

function viewBoatDetails(boatId) {
    alert(`Ver detalles de embarcación ${boatId}`);
}

function reportProblem() {
    alert('Función para reportar problemas - próximamente');
}

// Logout function
function logout() {
    // Clear authentication data
    localStorage.removeItem('userType');
    localStorage.removeItem('username');
    localStorage.removeItem('jwt');
    localStorage.removeItem('refreshToken');

    // Redirect to login
    window.location.href = '../login.html';
}

// API call examples (for future implementation)
function fetchOwnerDashboard() {
    const jwt = localStorage.getItem('jwt');

    return fetch('/api/v1/owner/dashboard', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${jwt}`,
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to fetch owner dashboard');
        }
        return response.json();
    })
    .then(data => {
        // Update UI with real data
        updateOwnerDashboard(data);
    })
    .catch(error => {
        console.error('Error fetching owner dashboard:', error);
        // Handle error (show message, redirect to login if unauthorized, etc.)
    });
}

function updateOwnerDashboard(data) {
    // Update metrics and content with real API data
    console.log('Updating dashboard with API data:', data);
}

// Export functions for potential use in other scripts
window.logout = logout;
window.navigateTo = navigateTo;
window.viewBoatDetails = viewBoatDetails;
window.reportProblem = reportProblem;