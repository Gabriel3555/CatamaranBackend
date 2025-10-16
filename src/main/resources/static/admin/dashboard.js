// Admin Dashboard JavaScript

// Check authentication on page load
document.addEventListener('DOMContentLoaded', function() {
    checkAuthentication();
    loadDashboardData();
});

// Authentication check
function checkAuthentication() {
    const userType = localStorage.getItem('userType');
    const jwt = localStorage.getItem('jwt');

    if (!userType || userType !== 'admin' || !jwt) {
        // Redirect to login if not authenticated as admin
        window.location.href = '../login.html';
        return;
    }
}

// Load dashboard data
function loadDashboardData() {
    console.log('Loading dashboard data...');

    // Fetch real data from API
    fetchStats();
    fetchBoatTypesData();
    fetchMaintenanceStatusData();
}

function getAuthHeaders() {
    const jwt = localStorage.getItem('jwt');
    return {
        'Content-Type': 'application/json',
        ...(jwt ? { 'Authorization': `Bearer ${jwt}` } : {})
    };
}

function fetchStats() {
    fetch('/api/v1/admin/stats', {
        headers: getAuthHeaders()
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to fetch stats');
        }
        return response.json();
    })
    .then(data => {
        document.getElementById('totalBoats').textContent = data.totalBoats || 0;
        document.getElementById('activeOwners').textContent = data.activeOwners || 0;
        document.getElementById('pendingMaintenances').textContent = data.pendingMaintenances || 0;
        document.getElementById('monthlyPayments').textContent = `$${data.monthlyPayments?.toLocaleString() || 0}`;
    })
    .catch(error => {
        console.error('Error fetching stats:', error);
        // Keep default values if API fails
    });
}

function fetchBoatTypesData() {
    fetch('/api/v1/admin/charts/boats-by-type', {
        headers: getAuthHeaders()
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to fetch boat types');
        }
        return response.json();
    })
    .then(data => {
        updateBoatTypesChart(data);
    })
    .catch(error => {
        console.error('Error fetching boat types:', error);
    });
}

function fetchMaintenanceStatusData() {
    fetch('/api/v1/admin/charts/maintenances-by-status', {
        headers: getAuthHeaders()
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to fetch maintenance status');
        }
        return response.json();
    })
    .then(data => {
        updateMaintenanceChart(data);
    })
    .catch(error => {
        console.error('Error fetching maintenance status:', error);
    });
}


function updateBoatTypesChart(data) {
    const chartContainer = document.querySelector('.bar-chart');
    if (!chartContainer) return;

    chartContainer.innerHTML = '';

    Object.entries(data).forEach(([type, count]) => {
        const bar = document.createElement('div');
        bar.className = 'bar';
        bar.style.height = `${Math.max(20, (count / Math.max(...Object.values(data))) * 80)}%`;
        bar.textContent = `${type.replace('_', ' ')} (${count})`;
        chartContainer.appendChild(bar);
    });
}

function updateMaintenanceChart(data) {
    const chartContainer = document.querySelector('.pie-chart');
    if (!chartContainer) return;

    chartContainer.innerHTML = '';

    const colors = ['#ef4444', '#f59e0b', '#10b981'];
    let index = 0;

    Object.entries(data).forEach(([status, count]) => {
        const segment = document.createElement('div');
        segment.className = 'pie-segment';
        segment.style.backgroundColor = colors[index % colors.length];
        segment.textContent = `${status.replace('_', ' ')} (${count})`;
        chartContainer.appendChild(segment);
        index++;
    });
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

// Navigation function
function navigateTo(page) {
    // Map page names to actual HTML files
    const pageMap = {
        'inventario': 'inventario.html',
        'propietarios': 'propietarios.html',
        'mantenimiento': 'mantenimiento.html',
        'pagos': 'pagos.html',
        'reportes': 'reportes.html'
    };

    const targetPage = pageMap[page];
    if (targetPage) {
        window.location.href = targetPage;
    } else {
        alert(`Página no encontrada: ${page}`);
    }
}

// API call examples (for future implementation)
function fetchDashboardStats() {
    const jwt = localStorage.getItem('jwt');

    return fetch('/api/v1/admin/stats', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${jwt}`,
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to fetch dashboard stats');
        }
        return response.json();
    })
    .then(data => {
        // Update UI with real data
        updateDashboardUI(data);
    })
    .catch(error => {
        console.error('Error fetching dashboard stats:', error);
        // Handle error (show message, redirect to login if unauthorized, etc.)
    });
}

function updateDashboardUI(data) {
    // Update metrics
    document.getElementById('totalBoats').textContent = data.totalBoats || 0;
    document.getElementById('activeOwners').textContent = data.activeOwners || 0;
    document.getElementById('pendingMaintenances').textContent = data.pendingMaintenances || 0;
    document.getElementById('monthlyPayments').textContent = `$${data.monthlyPayments?.toLocaleString() || 0}`;
}

// Export functions for potential use in other scripts
window.logout = logout;
window.navigateTo = navigateTo;