// Reports Page JavaScript

// Check authentication on page load
document.addEventListener('DOMContentLoaded', function() {
    checkAuthentication();
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

// Get authentication headers
function getAuthHeaders() {
    const jwt = localStorage.getItem('jwt');
    return {
        'Authorization': `Bearer ${jwt}`
    };
}

// Download report function
async function downloadReport(reportType, format) {
    // Show loading overlay
    showLoading();

    try {
        // Construct API endpoint
        const endpoint = `/api/v1/reports/${reportType}/${format}`;
        
        // Make API request
        const response = await fetch(endpoint, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            throw new Error(`Error al generar el reporte: ${response.status}`);
        }

        // Get the blob from response
        const blob = await response.blob();
        
        // Get filename from Content-Disposition header or create default
        let filename = `reporte_${reportType}_${new Date().getTime()}.${format}`;
        const contentDisposition = response.headers.get('Content-Disposition');
        if (contentDisposition) {
            const filenameMatch = contentDisposition.match(/filename=([^;]+)/);
            if (filenameMatch && filenameMatch[1]) {
                filename = filenameMatch[1].trim();
            }
        }

        // Create download link
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.style.display = 'none';
        a.href = url;
        a.download = filename;
        
        // Trigger download
        document.body.appendChild(a);
        a.click();
        
        // Cleanup
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);

        // Hide loading overlay
        hideLoading();

        // Show success message
        showNotification('Reporte descargado exitosamente', 'success');

    } catch (error) {
        console.error('Error downloading report:', error);
        hideLoading();
        showNotification('Error al descargar el reporte. Por favor, intente nuevamente.', 'error');
    }
}

// Show loading overlay
function showLoading() {
    const overlay = document.getElementById('loadingOverlay');
    if (overlay) {
        overlay.style.display = 'flex';
    }
}

// Hide loading overlay
function hideLoading() {
    const overlay = document.getElementById('loadingOverlay');
    if (overlay) {
        overlay.style.display = 'none';
    }
}

// Show notification
function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    // Style the notification
    notification.style.position = 'fixed';
    notification.style.top = '20px';
    notification.style.right = '20px';
    notification.style.padding = '1rem 1.5rem';
    notification.style.borderRadius = '8px';
    notification.style.color = 'white';
    notification.style.fontWeight = '600';
    notification.style.zIndex = '10000';
    notification.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
    notification.style.animation = 'slideIn 0.3s ease';
    
    // Set background color based on type
    if (type === 'success') {
        notification.style.background = 'linear-gradient(135deg, #10b981 0%, #059669 100%)';
    } else if (type === 'error') {
        notification.style.background = 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)';
    } else {
        notification.style.background = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
    }
    
    // Add to document
    document.body.appendChild(notification);
    
    // Remove after 3 seconds
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 3000);
}

// Add CSS animations for notifications
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(400px);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(400px);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);

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

// Export functions for use in HTML
window.downloadReport = downloadReport;
window.logout = logout;