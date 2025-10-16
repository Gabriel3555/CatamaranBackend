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
    const userNameElement = document.getElementById('userName');
    if (userNameElement) {
        userNameElement.textContent = username;
    }
    const welcomeMessageElement = document.getElementById('welcomeMessage');
    if (welcomeMessageElement) {
        welcomeMessageElement.textContent = `Bienvenido de vuelta, ${username}`;
    }

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
    const userNameElement2 = document.getElementById('userName');
    if (userNameElement2) {
        userNameElement2.textContent = user.fullName;
    }
    const welcomeMessageElement2 = document.getElementById('welcomeMessage');
    if (welcomeMessageElement2) {
        welcomeMessageElement2.textContent = `Bienvenido de vuelta, ${user.fullName}`;
    }

    // Update metrics
    const metrics = data.metrics;
    document.getElementById('myBoatsCount').textContent = metrics.totalBoats;
    document.getElementById('pendingMaintenances').textContent = metrics.pendingMaintenances;
    document.getElementById('completedMaintenances').textContent = metrics.completedMaintenances;
    document.getElementById('documentsCount').textContent = metrics.totalDocuments;
    document.getElementById('pendingPaymentsCount').textContent = metrics.pendingPayments;
}


// Navigation functions
function navigateTo(page) {
    // Navigate to the specified page
    window.location.href = `${page}.html`;
}

// Logout function
function logout() {
    // Clear authentication data
    localStorage.removeItem('userType');
    localStorage.removeItem('username');
    localStorage.removeItem('jwt');

    // Redirect to login
    window.location.href = '../login.html';
}

// Export functions for potential use in other scripts
window.logout = logout;
window.navigateTo = navigateTo;