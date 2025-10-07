// Inventory Management JavaScript

// State management
let boats = [];
let filteredBoats = [];
let currentEditingBoat = null;
let owners = [];

// DOM elements
const searchInput = document.getElementById('searchInput');
const typeFilter = document.getElementById('typeFilter');
const statusFilter = document.getElementById('statusFilter');
const inventoryTableBody = document.getElementById('inventoryTableBody');
const boatModal = document.getElementById('boatModal');
const boatForm = document.getElementById('boatForm');
const modalTitle = document.getElementById('modalTitle');
const saveBtn = document.getElementById('saveBtn');

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    checkAuthentication();
    setupEventListeners();
    loadBoats();
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
    searchInput.addEventListener('input', filterBoats);
    typeFilter.addEventListener('change', filterBoats);
    statusFilter.addEventListener('change', filterBoats);
    boatForm.addEventListener('submit', saveBoat);
}

// Get authentication headers
function getAuthHeaders() {
    const jwt = localStorage.getItem('jwt');
    return {
        'Content-Type': 'application/json',
        ...(jwt ? { 'Authorization': `Bearer ${jwt}` } : {})
    };
}

// Load boats from API
async function loadBoats() {
    try {
        const response = await fetch('/api/v1/boat?page=0&size=100', {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            boats = data.content || data; // Handle paginated response
            filteredBoats = [...boats];
            updateMetrics();
            renderBoats();
        } else {
            console.error('Failed to load boats');
            showFallbackData();
        }
    } catch (error) {
        console.error('Error loading boats:', error);
        showFallbackData();
    }
}

// Load owners from API
async function loadOwners() {
    try {
        const response = await fetch('/api/v1/auth?page=0&size=100', {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            owners = (data.content || data).filter(owner => owner.role === 'PROPIETARIO' && owner.status === true);
        } else {
            console.error('Failed to load owners');
            // Fallback owners
            owners = [
                { id: 1, fullName: 'Carlos Rodríguez', email: 'carlos.rodriguez@email.com' },
                { id: 2, fullName: 'María González', email: 'maria.gonzalez@email.com' },
                { id: 3, fullName: 'Juan Martínez', email: 'juan.martinez@email.com' }
            ];
        }
    } catch (error) {
        console.error('Error loading owners:', error);
        // Fallback owners
        owners = [
            { id: 1, fullName: 'Carlos Rodríguez', email: 'carlos.rodriguez@email.com' },
            { id: 2, fullName: 'María González', email: 'maria.gonzalez@email.com' },
            { id: 3, fullName: 'Juan Martínez', email: 'juan.martinez@email.com' }
        ];
    }
}

// Show fallback data when API fails
function showFallbackData() {
    boats = [
        {
            id: 1,
            name: "Catamarán Manta Explorer",
            type: "TURISMO",
            model: "Explorer 2024",
            location: "Cartagena",
            owner: null,
            price: 850000000
        },
        {
            id: 2,
            name: "Velero Alianza Premium",
            type: "ALOJAMIENTO",
            model: "Premium 2024",
            location: "San Andrés",
            owner: { fullName: "Carlos Rodríguez" },
            price: 1200000000
        }
    ];
    filteredBoats = [...boats];
    updateMetrics();
    renderBoats();
}

// Update metrics cards
function updateMetrics() {
    const totalBoats = boats.length;
    const availableBoats = boats.filter(boat => !boat.owner).length; // Available if no owner assigned
    const maintenanceBoats = 0; // For now, no maintenance status tracking
    const ownedBoats = boats.filter(boat => boat.owner).length;

    document.getElementById('totalBoats').textContent = totalBoats;
    document.getElementById('availableBoats').textContent = availableBoats;
    document.getElementById('maintenanceBoats').textContent = maintenanceBoats;
    document.getElementById('ownedBoats').textContent = ownedBoats;
}

// Filter boats based on search and filters
function filterBoats() {
    const searchTerm = searchInput.value.toLowerCase();
    const typeValue = typeFilter.value;
    const statusValue = statusFilter.value;

    filteredBoats = boats.filter(boat => {
        const matchesSearch = boat.name.toLowerCase().includes(searchTerm) ||
                             boat.model.toLowerCase().includes(searchTerm) ||
                             boat.location.toLowerCase().includes(searchTerm);

        const matchesType = typeValue === 'all' || boat.type === typeValue;
        const matchesStatus = statusValue === 'all' ||
                             (statusValue === 'Disponible' && !boat.owner) ||
                             (statusValue === 'Ocupado' && boat.owner);

        return matchesSearch && matchesType && matchesStatus;
    });

    renderBoats();
}

// Render boats in the table
function renderBoats() {
    inventoryTableBody.innerHTML = '';

    if (filteredBoats.length === 0) {
        const emptyRow = document.createElement('tr');
        emptyRow.innerHTML = `
            <td colspan="8" style="text-align: center; padding: 40px; color: #6b7280;">
                No se encontraron embarcaciones
            </td>
        `;
        inventoryTableBody.appendChild(emptyRow);
    } else {
        filteredBoats.forEach(boat => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${boat.name}</td>
                <td>${formatBoatType(boat.type)}</td>
                <td>${boat.model}</td>
                <td>${boat.location}</td>
                <td><span class="status-badge status-${boat.owner ? 'ocupado' : 'disponible'}">${boat.owner ? 'Ocupado' : 'Disponible'}</span></td>
                <td class="price">${formatPrice(boat.price || 0)}</td>
                <td>${boat.owner ? boat.owner.fullName : '<span class="owner-none">Sin asignar</span>'}</td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn edit-btn" onclick="editBoat(${boat.id})">Editar</button>
                        ${!boat.owner ? `<button class="action-btn assign-btn" onclick="assignOwner(${boat.id})">Asignar Propietario</button>` : ''}
                        <button class="action-btn delete-btn" onclick="deleteBoat(${boat.id})">Eliminar</button>
                    </div>
                </td>
            `;
            inventoryTableBody.appendChild(row);
        });
    }

    document.getElementById('tableCount').textContent = `${filteredBoats.length} de ${boats.length} embarcaciones`;
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

// Open add boat modal
function openAddModal() {
    currentEditingBoat = null;
    modalTitle.textContent = 'Agregar Nueva Embarcación';
    saveBtn.textContent = 'Agregar Embarcación';

    // Reset form
    boatForm.reset();

    boatModal.style.display = 'block';
}

// Edit boat
function editBoat(id) {
    const boat = boats.find(b => b.id === id);
    if (!boat) return;

    currentEditingBoat = boat;
    modalTitle.textContent = 'Editar Embarcación';
    saveBtn.textContent = 'Guardar Cambios';

    // Fill form with boat data
    document.getElementById('boatName').value = boat.name;
    document.getElementById('boatType').value = boat.type;
    document.getElementById('boatModel').value = boat.model;
    document.getElementById('boatLocation').value = boat.location;
    document.getElementById('boatPrice').value = boat.price;

    boatModal.style.display = 'block';
}

// Assign owner to boat
async function assignOwner(boatId) {
    const boat = boats.find(b => b.id === boatId);
    if (!boat || boat.owner) return;

    // Create owner selection modal
    const ownerModal = document.createElement('div');
    ownerModal.id = 'ownerModal';
    ownerModal.className = 'modal';
    ownerModal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h3>Asignar Propietario</h3>
                <button onclick="closeOwnerModal()" class="close-btn">✕</button>
            </div>
            <div class="modal-body">
                <p><strong>Embarcación:</strong> ${boat.name}</p>
                <div class="form-group">
                    <label for="ownerSelect">Seleccionar Propietario</label>
                    <select id="ownerSelect" required>
                        <option value="">Seleccionar propietario...</option>
                        ${owners.map(owner => `<option value="${owner.id}">${owner.fullName} (${owner.email})</option>`).join('')}
                    </select>
                </div>
            </div>
            <div class="modal-footer">
                <button onclick="closeOwnerModal()" class="btn-secondary">Cancelar</button>
                <button onclick="confirmAssignOwner(${boatId})" class="btn-primary">Asignar Propietario</button>
            </div>
        </div>
    `;

    document.body.appendChild(ownerModal);
    ownerModal.style.display = 'block';
}

// Delete boat
async function deleteBoat(id) {
    if (!confirm('¿Estás seguro de eliminar esta embarcación?')) return;

    try {
        const response = await fetch(`/api/v1/boat/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            boats = boats.filter(boat => boat.id !== id);
            filteredBoats = filteredBoats.filter(boat => boat.id !== id);
            updateMetrics();
            renderBoats();
        } else {
            console.error('Failed to delete boat');
            // Fallback to local deletion
            boats = boats.filter(boat => boat.id !== id);
            filteredBoats = filteredBoats.filter(boat => boat.id !== id);
            updateMetrics();
            renderBoats();
        }
    } catch (error) {
        console.error('Error deleting boat:', error);
        // Fallback to local deletion
        boats = boats.filter(boat => boat.id !== id);
        filteredBoats = filteredBoats.filter(boat => boat.id !== id);
        updateMetrics();
        renderBoats();
    }
}

// Handle form submission
async function saveBoat(event) {
    // Handle both onclick calls (no event) and form submit events (with event)
    if (event && typeof event.preventDefault === 'function') {
        event.preventDefault();
    }

    const formData = new FormData(boatForm);
    const boatData = {
        name: formData.get('name'),
        type: formData.get('type'),
        model: formData.get('model'),
        location: formData.get('location'),
        price: parseFloat(formData.get('price'))
    };

    try {
        if (currentEditingBoat) {
            // Update existing boat - send full BoatEntity
            const updateData = {
                id: currentEditingBoat.id,
                name: boatData.name,
                type: boatData.type,
                model: boatData.model,
                location: boatData.location,
                price: parseFloat(boatData.price) || 0,
                balance: currentEditingBoat.balance || 0,
                documents: currentEditingBoat.documents || [],
                owner: currentEditingBoat.owner || null
            };

            const response = await fetch(`/api/v1/boat/${currentEditingBoat.id}`, {
                method: 'PUT',
                headers: getAuthHeaders(),
                body: JSON.stringify(updateData)
            });

            if (response.ok) {
                const updatedBoat = await response.json();
                const index = boats.findIndex(b => b.id === currentEditingBoat.id);
                if (index !== -1) {
                    boats[index] = updatedBoat;
                }
            } else {
                console.error('Failed to update boat');
                return;
            }
        } else {
            // Add new boat - send CreateBoatRequest fields including price
            const createData = {
                type: boatData.type,
                name: boatData.name,
                model: boatData.model,
                location: boatData.location,
                price: parseFloat(boatData.price) || 0
            };

            const response = await fetch('/api/v1/boat', {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify(createData)
            });

            if (response.ok) {
                const newBoat = await response.json();
                boats.push(newBoat);
            } else {
                console.error('Failed to create boat');
                return;
            }
        }

        filteredBoats = [...boats];
        updateMetrics();
        renderBoats();
        closeModal();
    } catch (error) {
        console.error('Error saving boat:', error);
        // Fallback to local state update
        if (currentEditingBoat) {
            const updatedBoat = { ...currentEditingBoat, ...boatData };
            const index = boats.findIndex(b => b.id === currentEditingBoat.id);
            if (index !== -1) {
                boats[index] = updatedBoat;
            }
        } else {
            const newBoat = {
                ...boatData,
                id: Math.max(...boats.map(b => b.id), 0) + 1,
                owner: null
            };
            boats.push(newBoat);
        }
        filteredBoats = [...boats];
        updateMetrics();
        renderBoats();
        closeModal();
    }
}

// Confirm owner assignment
async function confirmAssignOwner(boatId) {
    const ownerSelect = document.getElementById('ownerSelect');
    const ownerId = ownerSelect.value;

    if (!ownerId) {
        alert('Por favor selecciona un propietario');
        return;
    }

    try {
        const response = await fetch(`/api/v1/boat/${boatId}/assign-owner/${ownerId}`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const updatedBoat = await response.json();
            const index = boats.findIndex(b => b.id === boatId);
            if (index !== -1) {
                boats[index] = updatedBoat;
            }
            filteredBoats = [...boats];
            updateMetrics();
            renderBoats();
            closeOwnerModal();
            alert('Propietario asignado exitosamente');
        } else if (response.status === 400) {
            alert('Esta embarcación ya tiene un propietario asignado');
        } else {
            console.error('Failed to assign owner');
            alert('Error al asignar propietario');
        }
    } catch (error) {
        console.error('Error assigning owner:', error);
        alert('Error de conexión. Inténtalo de nuevo.');
    }
}

// Close owner modal
function closeOwnerModal() {
    const ownerModal = document.getElementById('ownerModal');
    if (ownerModal) {
        ownerModal.remove();
    }
}

// Close modal
function closeModal() {
    boatModal.style.display = 'none';
    currentEditingBoat = null;
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
    if (event.target === boatModal) {
        closeModal();
    }
};

// Export functions for HTML onclick handlers
window.saveBoat = saveBoat;
window.openAddModal = openAddModal;
window.closeModal = closeModal;
window.editBoat = editBoat;
window.deleteBoat = deleteBoat;
window.assignOwner = assignOwner;
window.confirmAssignOwner = confirmAssignOwner;
window.closeOwnerModal = closeOwnerModal;
window.logout = logout;
window.navigateTo = navigateTo;