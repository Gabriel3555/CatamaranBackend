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
    const userNameElement = document.getElementById('userName');
    if (userNameElement) {
        userNameElement.textContent = username;
    }

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
                <button onclick="viewBoatDocuments(${boat.id})" class="primary-btn">Ver Documentos</button>
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
function viewBoatDocuments(boatId) {
    // Open modal to show documents
    openDocumentsModal(boatId);
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

// Document management functions
let currentBoatId = null;

function openDocumentsModal(boatId) {
    currentBoatId = boatId;
    document.getElementById('documentsModal').style.display = 'block';
    loadBoatDocuments(boatId);
}

function closeDocumentsModal() {
    document.getElementById('documentsModal').style.display = 'none';
    currentBoatId = null;
    // Clear file input
    document.getElementById('documentFile').value = '';
    document.getElementById('documentName').value = '';
}

async function loadBoatDocuments(boatId) {
    try {
        const response = await fetch(`/api/v1/owner/boats/${boatId}/documents`, {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const documents = await response.json();
            displayDocuments(documents);
        } else {
            showError('Error al cargar los documentos');
        }
    } catch (error) {
        console.error('Error loading documents:', error);
        showError('Error de conexión');
    }
}

function displayDocuments(documents) {
    const documentsList = document.getElementById('documentsList');

    if (documents.length === 0) {
        documentsList.innerHTML = '<p style="text-align: center; color: #6b7280; padding: 20px;">No hay documentos para esta embarcación</p>';
        return;
    }

    documentsList.innerHTML = documents.map(doc => `
        <div class="document-item">
            <div class="document-info">
                <span class="document-name">${doc.name}</span>
                <div class="document-actions">
                    <button onclick="viewDocument('${doc.url}')" class="view-btn">👁️ Ver</button>
                    <button onclick="downloadDocument('${doc.url}', '${doc.name}')" class="download-btn">⬇️ Descargar</button>
                    <button onclick="renameDocument(${doc.id}, '${doc.name}')" class="rename-btn">✏️ Renombrar</button>
                    <button onclick="deleteDocument(${doc.id})" class="delete-btn">🗑️ Eliminar</button>
                </div>
            </div>
        </div>
    `).join('');
}

// File upload handling
document.getElementById('documentFile').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (file && currentBoatId) {
        const documentName = document.getElementById('documentName').value.trim() || file.name;
        uploadDocument(currentBoatId, file, documentName);
    }
});

async function uploadDocument(boatId, file, documentName) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('name', documentName);

    try {
        const response = await fetch(`/api/v1/owner/boats/${boatId}/documents`, {
            method: 'POST',
            headers: {
                'Authorization': getAuthHeaders()['Authorization']
            },
            body: formData
        });

        if (response.ok) {
            loadBoatDocuments(boatId);
            document.getElementById('documentFile').value = '';
            document.getElementById('documentName').value = '';
        } else {
            showError('Error al subir el documento');
        }
    } catch (error) {
        console.error('Error uploading document:', error);
        showError('Error de conexión');
    }
}

function viewDocument(url) {
    window.open(url, '_blank');
}

function downloadDocument(url, name) {
    const link = document.createElement('a');
    link.href = url;
    link.download = name;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

function renameDocument(documentId, currentName) {
    const newName = prompt('Nuevo nombre del documento:', currentName);
    if (newName && newName.trim() && newName !== currentName) {
        updateDocumentName(currentBoatId, documentId, newName.trim());
    }
}

async function updateDocumentName(boatId, documentId, newName) {
    try {
        const response = await fetch(`/api/v1/owner/boats/${boatId}/documents/${documentId}?name=${encodeURIComponent(newName)}`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            loadBoatDocuments(boatId);
        } else {
            showError('Error al renombrar el documento');
        }
    } catch (error) {
        console.error('Error updating document:', error);
        showError('Error de conexión');
    }
}

async function deleteDocument(documentId) {
    if (!confirm('¿Está seguro de que desea eliminar este documento?')) {
        return;
    }

    try {
        const response = await fetch(`/api/v1/owner/boats/${currentBoatId}/documents/${documentId}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            loadBoatDocuments(currentBoatId);
        } else {
            showError('Error al eliminar el documento');
        }
    } catch (error) {
        console.error('Error deleting document:', error);
        showError('Error de conexión');
    }
}

// Close modal when clicking outside
window.onclick = function(event) {
    const modal = document.getElementById('documentsModal');
    if (event.target === modal) {
        closeDocumentsModal();
    }
}

// Export functions for potential use in other scripts
window.logout = logout;
window.viewBoatDocuments = viewBoatDocuments;