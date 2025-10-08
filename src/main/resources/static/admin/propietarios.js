// Owners Management JavaScript

// State management
let owners = [];
let filteredOwners = [];
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

// Load owners from API
async function loadOwners() {
    try {
        const response = await fetch('/api/v1/auth/with-boats', {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            owners = await response.json();
            filteredOwners = [...owners];
            updateMetrics();
            renderOwners();
        } else {
            console.error('Failed to load owners');
            showFallbackData();
        }
    } catch (error) {
        console.error('Error loading owners:', error);
        showFallbackData();
    }
}

// Show fallback data when API fails
function showFallbackData() {
    owners = [
        {
            id: 1,
            fullName: "Carlos Rodríguez",
            email: "carlos@example.com",
            phoneNumber: "+57 300 123 4567",
            username: "carlosr",
            status: true,
            role: "PROPIETARIO",
            boatsCount: 0,
            createdAt: "2024-01-15"
        },
        {
            id: 2,
            fullName: "María González",
            email: "maria@example.com",
            phoneNumber: "+57 301 987 6543",
            username: "mariag",
            status: false,
            role: "PROPIETARIO",
            boatsCount: 0,
            createdAt: "2024-02-20"
        }
    ];
    filteredOwners = [...owners];
    updateMetrics();
    renderOwners();
}

// Update metrics cards
function updateMetrics() {
    const totalOwners = owners.length;
    const activeOwners = owners.filter(owner => owner.status === true).length;
    const inactiveOwners = owners.filter(owner => owner.status === false).length;
    const ownersWithBoats = owners.filter(owner => owner.boatsCount && owner.boatsCount > 0).length;

    document.getElementById('totalOwners').textContent = totalOwners;
    document.getElementById('activeOwners').textContent = activeOwners;
    document.getElementById('inactiveOwners').textContent = inactiveOwners;
    document.getElementById('ownersWithBoats').textContent = ownersWithBoats;
}

// Filter owners based on search and filters
function filterOwners() {
    const searchTerm = searchInput.value.toLowerCase();
    const statusValue = statusFilter.value;
    const roleValue = roleFilter.value;

    filteredOwners = owners.filter(owner => {
        const matchesSearch = owner.fullName.toLowerCase().includes(searchTerm) ||
                             owner.email.toLowerCase().includes(searchTerm) ||
                             owner.username.toLowerCase().includes(searchTerm);

        const matchesStatus = statusValue === 'all' || owner.status.toString() === statusValue;
        const matchesRole = roleValue === 'all' || owner.role === roleValue;

        return matchesSearch && matchesStatus && matchesRole;
    });

    renderOwners();
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

    document.getElementById('tableCount').textContent = `${filteredOwners.length} de ${owners.length} propietarios`;
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
            const updatedOwner = await response.json();
            const index = owners.findIndex(o => o.id === id);
            if (index !== -1) {
                owners[index] = updatedOwner;
            }
            filteredOwners = [...owners];
            updateMetrics();
            renderOwners();
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
function deleteOwner(id) {
    if (!confirm('¿Estás seguro de eliminar este propietario? Esta acción no se puede deshacer.')) return;

    // For now, just remove from local array
    // In a real app, this would call the API
    owners = owners.filter(owner => owner.id !== id);
    filteredOwners = filteredOwners.filter(owner => owner.id !== id);
    updateMetrics();
    renderOwners();
}

// Handle form submission
async function saveOwner(event) {
    if (event && typeof event.preventDefault === 'function') {
        event.preventDefault();
    }

    const formData = new FormData(ownerForm);
    const ownerData = {
        fullName: formData.get('fullName'),
        email: formData.get('email'),
        phoneNumber: formData.get('phoneNumber'),
        username: formData.get('username'),
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
                const updatedOwner = await response.json();
                const index = owners.findIndex(o => o.id === currentEditingOwner.id);
                if (index !== -1) {
                    owners[index] = updatedOwner;
                }
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
                const newOwner = await response.json();
                owners.push(newOwner);
            } else {
                const errorData = await response.json().catch(() => ({}));
                console.error('Failed to create owner:', errorData);
                alert(`Error al crear propietario: ${errorData.message || 'Error desconocido'}`);
                return;
            }
        }

        filteredOwners = [...owners];
        updateMetrics();
        renderOwners();
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
    localStorage.removeItem('refreshToken');
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
window.logout = logout;
window.navigateTo = navigateTo;