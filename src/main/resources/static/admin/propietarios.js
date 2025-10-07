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

// Load owners from API
async function loadOwners() {
    try {
        const response = await fetch('/api/v1/auth?page=0&size=100', {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            owners = data.content || data; // Handle paginated response
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
            boats: [],
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
            boats: [],
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
    const ownersWithBoats = owners.filter(owner => owner.boats && owner.boats.length > 0).length;

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
            const boatsCount = owner.boats ? owner.boats.length : 0;

            row.innerHTML = `
                <td>${owner.fullName}</td>
                <td>${owner.email}</td>
                <td>${owner.phoneNumber || 'N/A'}</td>
                <td><span class="status-badge ${statusClass}">${statusText}</span></td>
                <td>${boatsCount} embarcaciones</td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn edit-btn" onclick="editOwner(${owner.id})">Editar</button>
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
    document.getElementById('ownerPassword').value = ''; // Don't show existing password
    document.getElementById('ownerStatus').value = owner.status.toString();

    ownerModal.style.display = 'block';
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
        password: formData.get('password'),
        status: formData.get('status') === 'true',
        role: 'PROPIETARIO'
    };

    try {
        if (currentEditingOwner) {
            // Update existing owner
            const updateData = {
                ...currentEditingOwner,
                ...ownerData,
                // Don't update password if empty
                password: ownerData.password || currentEditingOwner.password
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
                console.error('Failed to update owner');
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
                console.error('Failed to create owner');
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
                boats: [],
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
};

// Export functions for HTML onclick handlers
window.saveOwner = saveOwner;
window.openAddModal = openAddModal;
window.closeModal = closeModal;
window.editOwner = editOwner;
window.deleteOwner = deleteOwner;
window.logout = logout;
window.navigateTo = navigateTo;