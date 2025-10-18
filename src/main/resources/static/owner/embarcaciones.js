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
    // Clear form
    document.getElementById('documentName').value = '';
    document.getElementById('documentFile').value = '';
}

// Load documents for a boat
async function loadBoatDocuments(boatId) {
    const documentsList = document.getElementById('documentsList');
    if (!documentsList) return;

    try {
        const response = await fetch(`/api/v1/owner/boats/${boatId}/documents`, {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const documents = await response.json();
            displayDocuments(documents, boatId);
        } else {
            console.error('Failed to load documents');
            documentsList.innerHTML = '<p class="no-documents">Error al cargar documentos</p>';
        }
    } catch (error) {
        console.error('Error loading documents:', error);
        documentsList.innerHTML = '<p class="no-documents">Error de conexión</p>';
    }
}

// Display documents list
function displayDocuments(documents, boatId) {
    const documentsList = document.getElementById('documentsList');
    if (!documentsList) return;

    if (documents.length === 0) {
        documentsList.innerHTML = '<p class="no-documents">No hay documentos para esta embarcación</p>';
        return;
    }

    documentsList.innerHTML = documents.map(doc => {
        // Extract just the filename from the URL
        const filename = doc.url.split('/').pop();

        return `
            <div class="document-item">
                <div class="document-info">
                    <h5>${doc.name}</h5>
                </div>
                <div class="document-actions">
                    <button onclick="viewDocument('${doc.url}')" class="btn-small">Ver</button>
                    <button onclick="downloadDocument('${filename}', '${doc.name}')" class="btn-small">Descargar</button>
                    <button onclick="editDocumentName(${boatId}, ${doc.id}, '${doc.name}')" class="btn-small">Editar</button>
                    <button onclick="deleteDocument(${boatId}, ${doc.id})" class="btn-small delete">Eliminar</button>
                </div>
            </div>
        `;
    }).join('');
}

// Upload document
async function uploadDocument(boatId) {
    const documentName = document.getElementById('documentName').value.trim();
    const documentFile = document.getElementById('documentFile').files[0];

    if (!documentName) {
        alert('Por favor ingresa un nombre para el documento');
        return;
    }

    if (!documentFile) {
        alert('Por favor selecciona un archivo');
        return;
    }

    // Check file size (10MB limit)
    const maxSize = 10 * 1024 * 1024; // 10MB in bytes
    if (documentFile.size > maxSize) {
        alert('El archivo es demasiado grande. El tamaño máximo permitido es 10MB.');
        return;
    }

    const formData = new FormData();
    formData.append('file', documentFile);
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
            // Clear form
            document.getElementById('documentName').value = '';
            document.getElementById('documentFile').value = '';
            // Reload documents
            loadBoatDocuments(boatId);
            alert('Documento subido exitosamente');
        } else {
            console.error('Failed to upload document');
            alert('Error al subir el documento');
        }
    } catch (error) {
        console.error('Error uploading document:', error);
        alert('Error de conexión');
    }
}

// Edit document name
function editDocumentName(boatId, documentId, currentName) {
    const newName = prompt('Nuevo nombre del documento:', currentName);
    if (newName && newName.trim() !== currentName) {
        updateDocumentName(boatId, documentId, newName.trim());
    }
}

// Update document name
async function updateDocumentName(boatId, documentId, newName) {
    try {
        const response = await fetch(`/api/v1/owner/boats/${boatId}/documents/${documentId}?name=${encodeURIComponent(newName)}`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            loadBoatDocuments(boatId);
            alert('Nombre del documento actualizado');
        } else {
            console.error('Failed to update document name');
            alert('Error al actualizar el nombre');
        }
    } catch (error) {
        console.error('Error updating document name:', error);
        alert('Error de conexión');
    }
}

// View document
function viewDocument(url) {
    window.open(url, '_blank');
}

// Download document
async function downloadDocument(filename, documentName) {
    try {
        const response = await fetch(`/api/v1/boat/documents/${filename}`, {
            headers: {
                'Authorization': getAuthHeaders()['Authorization']
            }
        });

        if (response.ok) {
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = filename;
            link.style.display = 'none';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
        } else {
            console.error('Failed to download document');
            alert('Error al descargar el documento');
        }
    } catch (error) {
        console.error('Error downloading document:', error);
        alert('Error de conexión');
    }
}

// Delete document
async function deleteDocument(boatId, documentId) {
    if (!confirm('¿Estás seguro de eliminar este documento?')) return;

    try {
        const response = await fetch(`/api/v1/owner/boats/${boatId}/documents/${documentId}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            loadBoatDocuments(boatId);
            alert('Documento eliminado exitosamente');
        } else {
            console.error('Failed to delete document');
            alert('Error al eliminar el documento');
        }
    } catch (error) {
        console.error('Error deleting document:', error);
        alert('Error de conexión');
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

// Close modal when clicking outside
window.onclick = function(event) {
    const modal = document.getElementById('documentsModal');
    if (event.target === modal) {
        closeDocumentsModal();
    }
}

// Export functions for HTML onclick handlers
window.logout = logout;
window.viewBoatDocuments = viewBoatDocuments;
window.openDocumentsModal = openDocumentsModal;
window.closeDocumentsModal = closeDocumentsModal;
window.uploadDocument = uploadDocument;
window.downloadDocument = downloadDocument;
window.editDocumentName = editDocumentName;
window.deleteDocument = deleteDocument;
window.viewDocument = viewDocument;
window.changePage = changePage;