// Owner Embarcaciones JavaScript

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
    loadBoatsData();
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

// Load boats data
function loadBoatsData() {
    // Get user ID from localStorage
    const userId = localStorage.getItem('userId');

    if (!userId) {
        console.error('User ID not found');
        return;
    }

    // Update welcome message
    const username = localStorage.getItem('username') || 'Propietario';
    document.getElementById('userName').textContent = username;

    // Load boats from API
    loadOwnerBoats(userId);
}

// Load owner boats from API
async function loadOwnerBoats(userId) {
    try {
        const response = await fetch(`/api/v1/owner/dashboard/${userId}`, {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            displayBoats(data.boats);
        } else {
            console.error('Failed to load boats');
            showError('Error al cargar las embarcaciones. Por favor, inténtalo de nuevo.');
        }
    } catch (error) {
        console.error('Error loading boats:', error);
        showError('Error de conexión. Verifica tu conexión a internet.');
    }
}

// Display boats in detailed cards
function displayBoats(boats) {
    const boatsGrid = document.getElementById('boatsGrid');
    const noBoatsMessage = document.getElementById('noBoatsMessage');

    if (boats.length === 0) {
        boatsGrid.innerHTML = '';
        noBoatsMessage.style.display = 'block';
        return;
    }

    noBoatsMessage.style.display = 'none';

    boatsGrid.innerHTML = boats.map(boat => `
        <div class="boat-card detailed">
            <div class="boat-header">
                <div class="boat-title">${boat.name}</div>
                <span class="boat-badge available">Asignada</span>
            </div>
            <div class="boat-info-grid">
                <div class="info-item">
                    <span class="info-label">Modelo:</span>
                    <span class="info-value">${boat.model || 'N/A'}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Tipo:</span>
                    <span class="info-value">${formatBoatType(boat.type)}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Ubicación:</span>
                    <span class="info-value">${boat.location}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Precio:</span>
                    <span class="info-value">${formatPrice(boat.price || 0)}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Deuda Mantenimiento:</span>
                    <span class="info-value debt ${boat.maintenanceDebt > 0 ? 'debt-warning' : ''}">${formatPrice(boat.maintenanceDebt || 0)}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Deuda Barco:</span>
                    <span class="info-value debt ${boat.boatDebt > 0 ? 'debt-warning' : ''}">${formatPrice(boat.boatDebt || 0)}</span>
                </div>
            </div>
            <div class="boat-actions">
                <button onclick="viewBoatDetails(${boat.id})" class="primary-btn">Ver Detalles</button>
            </div>
        </div>
    `).join('');
}

// Show error message
function showError(message) {
    const boatsGrid = document.getElementById('boatsGrid');
    boatsGrid.innerHTML = `
        <div style="text-align: center; padding: 40px; color: #dc2626;">
            <h3>⚠️ ${message}</h3>
            <button onclick="location.reload()" style="margin-top: 20px; padding: 10px 20px; background: #3b82f6; color: white; border: none; border-radius: 6px; cursor: pointer;">
                Reintentar
            </button>
        </div>
    `;
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

// Navigation functions
function viewBoatDetails(boatId) {
    alert(`Ver detalles de embarcación ${boatId}`);
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

// Export functions for potential use in other scripts
window.logout = logout;
window.viewBoatDetails = viewBoatDetails;