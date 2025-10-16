// Owner Embarcaciones JavaScript

// State management
let boats = [];
let filteredBoats = [];
let currentPage = 0;
let totalPages = 0;
let totalElements = 0;
let pageSize = 10;

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

    // Load boats from API (start from page 0)
    loadOwnerBoats(userId, 0);
}

// Load owner boats from API with pagination
async function loadOwnerBoats(userId, page = 0) {
    try {
        console.log('Loading boats for user:', userId, 'page:', page);

        const response = await fetch(`/api/v1/owner/boats/${userId}?page=${page}&size=${pageSize}`, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        console.log('Response status:', response.status);

        if (response.ok) {
            const data = await response.json();
            console.log('Response data:', data);

            boats = data.content || [];
            totalPages = data.totalPages || 1;
            totalElements = data.totalElements || 0;
            currentPage = page;
            filteredBoats = [...boats];

            console.log('Loaded boats:', boats.length, 'boats');
            console.log('Total elements:', totalElements);

            displayBoats(boats);
            updatePaginationControls();
        } else if (response.status === 404) {
            // Fallback to dashboard endpoint if new endpoint doesn't exist
            console.log('New endpoint not found, trying fallback...');
            const fallbackResponse = await fetch(`/api/v1/owner/dashboard/${userId}`, {
                method: 'GET',
                headers: getAuthHeaders()
            });

            if (fallbackResponse.ok) {
                const data = await fallbackResponse.json();
                boats = data.boats || [];
                totalPages = 1;
                totalElements = boats.length;
                currentPage = 0;
                filteredBoats = [...boats];

                console.log('Loaded boats from fallback:', boats.length, 'boats');

                displayBoats(boats);
                updatePaginationControls();
            } else {
                throw new Error(`HTTP ${fallbackResponse.status}`);
            }
        } else {
            throw new Error(`HTTP ${response.status}`);
        }

    } catch (error) {
        console.error('Error loading boats:', error);

        // Provide more specific error messages based on the error type
        let errorMessage = 'Error al cargar las embarcaciones. Por favor, inténtalo de nuevo.';

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

// Display boats in table format
function displayBoats() {
    const boatsTableBody = document.getElementById('boatsTableBody');
    const noBoatsMessage = document.getElementById('noBoatsMessage');

    if (filteredBoats.length === 0) {
        boatsTableBody.innerHTML = '';
        noBoatsMessage.style.display = 'block';
        return;
    }

    noBoatsMessage.style.display = 'none';

    boatsTableBody.innerHTML = filteredBoats.map(boat => `
        <tr>
            <td>${boat.name}</td>
            <td>${formatBoatType(boat.type)}</td>
            <td>${boat.model || 'N/A'}</td>
            <td>${boat.location}</td>
            <td class="price">${formatPrice(boat.price || 0)}</td>
            <td class="debt ${boat.maintenanceDebt > 0 ? 'debt-warning' : ''}">${formatPrice(boat.maintenanceDebt || 0)}</td>
            <td class="debt ${boat.boatDebt > 0 ? 'debt-warning' : ''}">${formatPrice(boat.boatDebt || 0)}</td>
            <td>
                <div class="action-buttons">
                    <button onclick="viewBoatDocuments(${boat.id})" class="action-btn documents-btn">Ver Documentos</button>
                </div>
            </td>
        </tr>
    `).join('');

    // Update table count
    document.getElementById('tableCount').textContent = `${filteredBoats.length} embarcaciones en esta página`;
}

// Show error message
function showError(message) {
    const boatsTableBody = document.getElementById('boatsTableBody');
    const noBoatsMessage = document.getElementById('noBoatsMessage');

    if (boatsTableBody) {
        boatsTableBody.innerHTML = `
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

    // Hide the no boats message if it exists
    if (noBoatsMessage) {
        noBoatsMessage.style.display = 'none';
    }
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
    localStorage.removeItem('userId');
    localStorage.removeItem('jwt');

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
    paginationInfo.textContent = `Mostrando ${startItem}-${endItem} de ${totalElements} embarcaciones`;
}

// Change page function
function changePage(page) {
    if (page < 0 || page >= totalPages) return;

    const userId = localStorage.getItem('userId');
    if (userId) {
        loadOwnerBoats(userId, page);
    }
}

// Global reference for HTML onclick handlers
window.changePage = changePage;

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
window.changePage = changePage;