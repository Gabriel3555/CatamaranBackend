// Owners Management JavaScript

// State management
let owners = [];
let filteredOwners = [];
let currentPage = 0;
let totalPages = 0;
let totalElements = 0;
let pageSize = 10;
let currentEditingOwner = null;

// DOM elements
const searchInput = document.getElementById('searchInput');
const statusFilter = document.getElementById('statusFilter');
const roleFilter = document.getElementById('roleFilter');
const ownersTableBody = document.getElementById('ownersTableBody');
const ownerModal = document.getElementById('ownerModal');
const boatsModal = document.getElementById('boatsModal');
const ownerForm = document.getElementById('ownerForm');
const modalTitle = document.getElementById('modalTitle');
const saveBtn = document.getElementById('saveBtn');

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    checkAuthentication();
    setupEventListeners();
    loadOwners();
    loadOwnersStats(); // Load general stats

    // Check if we should open the add modal automatically
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('openAddModal') === 'true') {
        // Small delay to ensure page is fully loaded
        setTimeout(() => {
            openAddModal();
        }, 100);
    }

    // Add cache busting parameter to avoid cached JS
    console.log('Page loaded at:', new Date().toISOString());
});

// Authentication check
function checkAuthentication() {
    const userType = localStorage.getItem('userType');
    const jwt = localStorage.getItem('jwt');

    if (!userType || userType !== 'admin' || !jwt) {
        window.location.href = '../login.html';
        return;
    }
}

// Setup event listeners
function setupEventListeners() {
    searchInput.addEventListener('input', filterOwners);
    statusFilter.addEventListener('change', filterOwners);
    roleFilter.addEventListener('change', filterOwners);
    ownerForm.addEventListener('submit', saveOwner);
}

// Get authentication headers
function getAuthHeaders() {
    const jwt = localStorage.getItem('jwt');
    return {
        'Content-Type': 'application/json',
        ...(jwt ? { 'Authorization': `Bearer ${jwt}` } : {})
    };
}

// Format price in Colombian pesos
function formatPrice(price) {
    return new Intl.NumberFormat('es-CO', {
        style: 'currency',
        currency: 'COP',
        minimumFractionDigits: 0
    }).format(price);
}

// Format boat type for display
function formatBoatType(type) {
    const typeMap = {
        'TURISMO': 'Turismo',
        'ALOJAMIENTO': 'Alojamiento',
        'EVENTOS_NEGOCIOS': 'Eventos y Negocios',
        'DISENO_EXCLUSIVO': 'Diseño Exclusivo'
    };
    return typeMap[type] || type;
}

// Load owners general stats from API
async function loadOwnersStats() {
    try {
        const response = await fetch('/api/v1/admin/owners/stats', {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const stats = await response.json();
            updateMetrics(stats);
        } else {
            console.error('Failed to load owners stats - Status:', response.status);
        }
    } catch (error) {
        console.error('Error loading owners stats:', error);
    }
}

// Load owners from API
async function loadOwners(page = 0, search = '', status = 'all', role = 'all') {
    try {
        let url = `/api/v1/admin/owners?page=${page}&size=${pageSize}`;

        // Add filter parameters if provided
        if (search) url += `&search=${encodeURIComponent(search)}`;
        if (status !== 'all') url += `&status=${encodeURIComponent(status)}`;
        if (role !== 'all') url += `&role=${encodeURIComponent(role)}`;

        console.log('Loading owners with URL:', url);
        console.log('Filters - search:', search, 'status:', status, 'role:', role);

        const response = await fetch(url, {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            owners = data.content || [];
            totalPages = data.totalPages || 1;
            totalElements = data.totalElements || 0;
            currentPage = page;
            filteredOwners = [...owners]; // For client-side filtering if needed

            console.log('Loaded owners:', owners.length, 'owners');
            console.log('Total elements:', totalElements);

            updateMetrics();
            renderOwners();
            updatePaginationControls();
        } else {
            console.error('Failed to load owners - Status:', response.status);
            const errorText = await response.text();
            console.error('Error response:', errorText);

            owners = [];
            filteredOwners = [];
            totalPages = 1;
            totalElements = 0;
            updateMetrics();
            renderOwners();
            updatePaginationControls();
        }
    } catch (error) {
        console.error('Error loading owners:', error);
        owners = [];
        filteredOwners = [];
        totalPages = 1;
        totalElements = 0;
        updateMetrics();
        renderOwners();
        updatePaginationControls();
    }
}


// Update metrics cards
function updateMetrics(stats = null) {
    let totalOwners, activeOwners, inactiveOwners, ownersWithBoats;

    if (stats) {
        // Use provided general stats
        totalOwners = stats.totalOwners;
        activeOwners = stats.activeOwners;
        inactiveOwners = stats.inactiveOwners;
        ownersWithBoats = stats.ownersWithBoats;
    } else {
        // Fallback to calculating from current filtered data (for backward compatibility)
        totalOwners = owners.length;
        activeOwners = owners.filter(owner => owner.status === true).length;
        inactiveOwners = owners.filter(owner => owner.status === false).length;
        ownersWithBoats = owners.filter(owner => owner.boatsCount && owner.boatsCount > 0).length;
    }

    document.getElementById('totalOwners').textContent = totalOwners;
    document.getElementById('activeOwners').textContent = activeOwners;
    document.getElementById('inactiveOwners').textContent = inactiveOwners;
    document.getElementById('ownersWithBoats').textContent = ownersWithBoats;
}

// Update pagination controls
function updatePaginationControls() {
    const prevBtn = document.getElementById('prevPageBtn');
    const nextBtn = document.getElementById('nextPageBtn');
    const pageInfo = document.getElementById('currentPageInfo');
    const paginationInfo = document.getElementById('paginationInfo');

    // Check if elements exist before updating them
    if (prevBtn && nextBtn && pageInfo && paginationInfo) {
        // Update buttons
        prevBtn.disabled = currentPage <= 0;
        nextBtn.disabled = currentPage >= totalPages - 1;

        // Update page info
        pageInfo.textContent = `Página ${currentPage + 1} de ${totalPages}`;

        // Update pagination info
        const startItem = currentPage * pageSize + 1;
        const endItem = Math.min((currentPage + 1) * pageSize, totalElements);
        paginationInfo.textContent = `Mostrando ${startItem}-${endItem} de ${totalElements} propietarios`;
    }
}

// Change page function
function changePage(page) {
    if (page < 0 || page >= totalPages) return;
    const searchTerm = searchInput ? searchInput.value : '';
    const statusValue = statusFilter ? statusFilter.value : 'all';
    const roleValue = roleFilter ? roleFilter.value : 'all';
    loadOwners(page, searchTerm, statusValue, roleValue);

    // Also reload general stats to ensure they remain consistent
    loadOwnersStats();
}

// Filter owners based on search and filters
function filterOwners() {
    const searchTerm = searchInput ? searchInput.value : '';
    const statusValue = statusFilter ? statusFilter.value : 'all';
    const roleValue = roleFilter ? roleFilter.value : 'all';

    // Reload data with filters applied server-side
    loadOwners(0, searchTerm, statusValue, roleValue);

    // Also reload general stats to ensure they remain consistent
    loadOwnersStats();
}

// Render owners in the table
function renderOwners() {
    ownersTableBody.innerHTML = '';

    if (filteredOwners.length === 0) {
        const emptyRow = document.createElement('tr');
        emptyRow.innerHTML = `
            <td colspan="6" style="text-align: center; padding: 40px; color: #6b7280;">
                No se encontraron propietarios
            </td>
        `;
        ownersTableBody.appendChild(emptyRow);
    } else {
        filteredOwners.forEach(owner => {
            const row = document.createElement('tr');
            const statusText = owner.status ? 'Activo' : 'Inactivo';
            const statusClass = owner.status ? 'status-activo' : 'status-inactivo';
            const boatsCount = owner.boatsCount || 0;

            row.innerHTML = `
                <td>${owner.fullName}</td>
                <td>${owner.email}</td>
                <td>${owner.phoneNumber || 'N/A'}</td>
                <td><span class="status-badge ${statusClass}">${statusText}</span></td>
                <td><button class="action-btn edit-btn" onclick="showBoats(${owner.id})">${boatsCount} embarcaciones</button></td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn edit-btn" onclick="editOwner(${owner.id})">Editar</button>
                        <button class="action-btn ${owner.status ? 'inactive-btn' : 'active-btn'}" onclick="toggleOwnerStatus(${owner.id})">${owner.status ? 'Desactivar' : 'Activar'}</button>
                        <button class="action-btn delete-btn" onclick="deleteOwner(${owner.id})">Eliminar</button>
                    </div>
                </td>
            `;
            ownersTableBody.appendChild(row);
        });
    }

    document.getElementById('tableCount').textContent = `${filteredOwners.length} propietarios en esta página`;
}

// Open add owner modal
function openAddModal() {
    currentEditingOwner = null;
    modalTitle.textContent = 'Agregar Nuevo Propietario';
    saveBtn.textContent = 'Crear Propietario';

    // Reset form
    ownerForm.reset();

    ownerModal.style.display = 'block';
}

// Edit owner
function editOwner(id) {
    const owner = owners.find(o => o.id === id);
    if (!owner) return;

    currentEditingOwner = owner;
    modalTitle.textContent = 'Editar Propietario';
    saveBtn.textContent = 'Guardar Cambios';

    // Fill form with owner data
    document.getElementById('ownerFullName').value = owner.fullName;
    document.getElementById('ownerEmail').value = owner.email;
    document.getElementById('ownerPhone').value = owner.phoneNumber || '';
    document.getElementById('ownerUsername').value = owner.username;

    ownerModal.style.display = 'block';
}

// Toggle owner status
async function toggleOwnerStatus(id) {
    const owner = owners.find(o => o.id === id);
    if (!owner) return;

    const newStatus = !owner.status;
    const actionText = newStatus ? 'activar' : 'desactivar';

    if (!confirm(`¿Estás seguro de ${actionText} este propietario?`)) return;

    try {
        const updateData = {
            status: newStatus
        };

        const response = await fetch(`/api/v1/auth/${id}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(updateData)
        });

        if (response.ok) {
            loadOwners(); // Reload to ensure table reflects latest server data
            loadOwnersStats(); // Reload general stats
        } else {
            console.error('Failed to update owner status');
        }
    } catch (error) {
        console.error('Error updating owner status:', error);
        // Fallback to local update
        owner.status = newStatus;
        filteredOwners = [...owners];
        updateMetrics();
        renderOwners();
    }
}

// Delete owner
async function deleteOwner(id) {
    if (!confirm('¿Estás seguro de eliminar este propietario? Esta acción no se puede deshacer.')) return;

    try {
        const response = await fetch(`/api/v1/auth/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            loadOwners(); // Reload to ensure table reflects latest server data
            loadOwnersStats(); // Reload general stats
        } else {
            console.error('Failed to delete owner');
            alert('Error al eliminar el propietario');
        }
    } catch (error) {
        console.error('Error deleting owner:', error);
        alert('Error de conexión al eliminar el propietario');
    }
}

// Validar formato de email
function isValidEmail(email) {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(email);
}

// Validar formato de username
function isValidUsername(username) {
    // Solo letras, números, guiones y guiones bajos, entre 3 y 20 caracteres
    const usernameRegex = /^[a-zA-Z0-9_-]{3,20}$/;
    return usernameRegex.test(username);
}

// Handle form submission
async function saveOwner(event) {
    if (event && typeof event.preventDefault === 'function') {
        event.preventDefault();
    }

    const formData = new FormData(ownerForm);
    const email = formData.get('email');
    const username = formData.get('username');
    
    // Validar formato de email en el frontend
    if (!email || !email.trim()) {
        alert('El email es requerido');
        return;
    }
    
    if (!isValidEmail(email.trim())) {
        alert('El formato del email no es válido. Debe tener el formato: ejemplo@dominio.com');
        return;
    }
    
    // Validar formato de username en el frontend
    if (!username || !username.trim()) {
        alert('El username es requerido');
        return;
    }
    
    if (!isValidUsername(username.trim())) {
        alert('El formato del username no es válido. Debe tener entre 3 y 20 caracteres y solo puede contener letras, números, guiones y guiones bajos.');
        return;
    }
    
    const ownerData = {
        fullName: formData.get('fullName'),
        email: email.trim(),
        phoneNumber: formData.get('phoneNumber'),
        username: username.trim(),
        role: 'PROPIETARIO'
    };

    try {
        if (currentEditingOwner) {
            // Update existing owner
            const updateData = {
                ...currentEditingOwner,
                ...ownerData
            };

            const response = await fetch(`/api/v1/auth/${currentEditingOwner.id}`, {
                method: 'PUT',
                headers: getAuthHeaders(),
                body: JSON.stringify(updateData)
            });

            if (response.ok) {
                loadOwners(); // Reload to ensure table reflects latest server data
                loadOwnersStats(); // Reload general stats
            } else {
                const errorData = await response.json().catch(() => ({}));
                console.error('Failed to update owner:', errorData);
                alert(`Error al actualizar propietario: ${errorData.message || 'Error desconocido'}`);
                return;
            }
        } else {
            // Add new owner
            const response = await fetch('/api/v1/auth/create-owner', {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify(ownerData)
            });

            if (response.ok) {
                loadOwners(); // Reload to ensure table reflects latest server data
                loadOwnersStats(); // Reload general stats
                alert('Propietario creado exitosamente');
            } else {
                const errorData = await response.json().catch(() => ({}));
                console.error('Failed to create owner:', errorData);
                
                // Mostrar mensaje de error más descriptivo
                let errorMessage = errorData.message || 'Error desconocido';
                
                // Manejar diferentes tipos de errores
                if (errorData.errorCode === 'EMAIL_ALREADY_EXISTS') {
                    errorMessage = errorData.message || 'El email ya está registrado';
                } else if (errorData.errorCode === 'USERNAME_ALREADY_EXISTS') {
                    errorMessage = errorData.message || 'El username ya está registrado';
                } else if (response.status === 400) {
                    errorMessage = errorData.message || 'Datos inválidos. Verifica el formato del email y username.';
                } else if (response.status === 409) {
                    errorMessage = errorData.message || 'El email o username ya está registrado';
                }
                
                alert(`Error al crear propietario: ${errorMessage}`);
                return;
            }
        }

        closeModal();
    } catch (error) {
        console.error('Error saving owner:', error);
        // Fallback to local state update
        if (currentEditingOwner) {
            const updatedOwner = { ...currentEditingOwner, ...ownerData };
            const index = owners.findIndex(o => o.id === currentEditingOwner.id);
            if (index !== -1) {
                owners[index] = updatedOwner;
            }
        } else {
            const newOwner = {
                ...ownerData,
                id: Math.max(...owners.map(o => o.id), 0) + 1,
                boatsCount: 0,
                createdAt: new Date().toISOString().split('T')[0]
            };
            owners.push(newOwner);
        }
        filteredOwners = [...owners];
        updateMetrics();
        renderOwners();
        closeModal();
    }
}

// Show boats for owner
async function showBoats(ownerId) {
    const owner = owners.find(o => o.id === ownerId);
    if (!owner) return;

    const boatsList = document.getElementById('boatsList');
    boatsList.innerHTML = '<p>Cargando embarcaciones...</p>';

    try {
        // Fetch boats for this owner from the boat API
        const response = await fetch('/api/v1/boat?page=0&size=100', {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            const allBoats = data.content || data;
            const ownerBoats = allBoats.filter(boat => boat.owner && boat.owner.id === ownerId);

            boatsList.innerHTML = '';

            if (ownerBoats.length > 0) {
                const ul = document.createElement('ul');
                ul.style.listStyle = 'none';
                ul.style.padding = '0';

                ownerBoats.forEach(boat => {
                    const li = document.createElement('li');
                    li.style.padding = '8px 0';
                    li.style.borderBottom = '1px solid #e5e7eb';
                    li.innerHTML = `
                        <strong>${boat.name || 'Embarcación'}</strong><br>
                        Tipo: ${formatBoatType(boat.type) || 'N/A'}<br>
                        Precio: ${formatPrice(boat.price || 0)}
                    `;
                    ul.appendChild(li);
                });

                boatsList.appendChild(ul);
            } else {
                boatsList.innerHTML = '<p>No hay embarcaciones registradas para este propietario.</p>';
            }
        } else {
            boatsList.innerHTML = '<p>Error al cargar las embarcaciones.</p>';
        }
    } catch (error) {
        console.error('Error loading boats for owner:', error);
        boatsList.innerHTML = '<p>Error de conexión.</p>';
    }

    boatsModal.style.display = 'block';
}

// Close boats modal
function closeBoatsModal() {
    boatsModal.style.display = 'none';
}

// Close modal
function closeModal() {
    ownerModal.style.display = 'none';
    currentEditingOwner = null;
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
    alert(`Navegando a: ${page}`);
    // You could implement actual navigation like:
    // window.location.href = `${page}.html`;
}

// Close modal when clicking outside
window.onclick = function(event) {
    if (event.target === ownerModal) {
        closeModal();
    }
    if (event.target === boatsModal) {
        closeBoatsModal();
    }
};

// Export functions for HTML onclick handlers
window.saveOwner = saveOwner;
window.openAddModal = openAddModal;
window.closeModal = closeModal;
window.showBoats = showBoats;
window.closeBoatsModal = closeBoatsModal;
window.editOwner = editOwner;
window.toggleOwnerStatus = toggleOwnerStatus;
window.deleteOwner = deleteOwner;
window.changePage = changePage;
window.logout = logout;
window.navigateTo = navigateTo;