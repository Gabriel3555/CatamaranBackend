// Inventory Management JavaScript

// State management
let boats = [];
let filteredBoats = [];
let allBoats = []; // Datos para métricas generales
let currentEditingBoat = null;
let owners = [];
let currentPage = 0;
let totalPages = 0;
let totalElements = 0;
let pageSize = 10;

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
    loadGeneralMetrics(); // Load general metrics data

    // Add cache busting parameter to avoid cached JS
    console.log('Page loaded at:', new Date().toISOString());
});

// Authentication check
function checkAuthentication() {
    const userType = localStorage.getItem('userType');
    const jwt = localStorage.getItem('jwt');
    const username = localStorage.getItem('username');

    console.log('Checking authentication:', { userType, hasJwt: !!jwt, username });

    if (!userType || userType !== 'admin') {
        console.error('Authentication failed: Invalid or missing userType');
        window.location.href = '../login.html';
        return false;
    }

    if (!jwt) {
        console.error('Authentication failed: Missing JWT token');
        window.location.href = '../login.html';
        return false;
    }

    if (!username) {
        console.error('Authentication failed: Missing username');
        window.location.href = '../login.html';
        return false;
    }

    // Validate JWT token format
    try {
        const tokenParts = jwt.split('.');
        if (tokenParts.length !== 3) {
            console.error('Authentication failed: Invalid JWT format');
            localStorage.removeItem('jwt');
            localStorage.removeItem('userType');
            localStorage.removeItem('username');
            window.location.href = '../login.html';
            return false;
        }

        // Decode and check expiration
        const payload = JSON.parse(atob(tokenParts[1]));
        const currentTime = Math.floor(Date.now() / 1000);

        if (payload.exp && payload.exp < currentTime) {
            console.error('Authentication failed: JWT token expired');
            localStorage.removeItem('jwt');
            localStorage.removeItem('userType');
            localStorage.removeItem('username');
            window.location.href = '../login.html';
            return false;
        }

        console.log('Authentication successful for user:', username);
        return true;

    } catch (error) {
        console.error('Authentication failed: Error validating JWT:', error);
        localStorage.removeItem('jwt');
        localStorage.removeItem('userType');
        localStorage.removeItem('username');
        window.location.href = '../login.html';
        return false;
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
async function loadBoats(page = 0, search = '', type = 'all', status = 'all') {
    try {
        let url = `/api/v1/boat?page=${page}&size=${pageSize}`;

        // Add filter parameters if provided
        if (search) url += `&search=${encodeURIComponent(search)}`;
        if (type !== 'all') url += `&type=${encodeURIComponent(type)}`;
        if (status !== 'all') url += `&status=${encodeURIComponent(status)}`;

        console.log('Loading boats with URL:', url);

        const headers = getAuthHeaders();
        console.log('Authentication headers:', headers);

        const response = await fetch(url, {
            headers: headers
        });

        console.log('Response status:', response.status);
        console.log('Response headers:', Object.fromEntries(response.headers.entries()));

        if (response.ok) {
            const data = await response.json();
            boats = data.content || [];
            totalPages = data.totalPages || 1;
            totalElements = data.totalElements || 0;
            currentPage = page;
            filteredBoats = [...boats];

            console.log('Loaded boats:', boats.length, 'boats');
            console.log('Total elements:', totalElements);

            // Update metrics with general data (will load if needed)
            await updateMetrics();
            renderBoats();
            updatePaginationControls();
        } else {
            console.error('Failed to load boats - Status:', response.status, response.statusText);

            // Handle authentication errors
            if (response.status === 401 || response.status === 403) {
                console.error('Authentication failed - redirecting to login');
                localStorage.removeItem('jwt');
                localStorage.removeItem('userType');
                localStorage.removeItem('username');
                window.location.href = '../login.html';
                return;
            }

            // Handle server errors
            if (response.status >= 500) {
                console.error('Server error - showing error message');
                alert('Error del servidor al cargar las embarcaciones. Inténtalo de nuevo.');
            } else if (response.status >= 400) {
                console.error('Client error - Status:', response.status, response.statusText);
                alert(`Error al cargar las embarcaciones: ${response.status} ${response.statusText}`);
            }

            boats = [];
            filteredBoats = [];
            totalPages = 1;
            totalElements = 0;
            // Update metrics with general data (will load if needed)
            await updateMetrics();
            renderBoats();
            updatePaginationControls();
        }
    } catch (error) {
        console.error('Error loading boats:', error);

        // Handle network errors
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            console.error('Network error - server might not be running');
            alert('Error de conexión. Verifica que el servidor esté ejecutándose.');
        } else {
            alert('Error inesperado al cargar las embarcaciones. Revisa la consola para más detalles.');
        }

        boats = [];
        filteredBoats = [];
        totalPages = 1;
        totalElements = 0;
        // Update metrics with general data (will load if needed)
        await updateMetrics();
        renderBoats();
        updatePaginationControls();
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
        owners = [];
    }
}

// Load all boats for general metrics (without filters)
async function loadGeneralMetrics() {
    try {
        console.log('Loading general metrics data...');
        const response = await fetch(`/api/v1/boat?page=0&size=1000`, {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            allBoats = data.content || [];
            console.log('Loaded general metrics data:', allBoats.length, 'boats');
        } else {
            console.error('Failed to load general metrics - Status:', response.status, response.statusText);

            // Handle authentication errors
            if (response.status === 401 || response.status === 403) {
                console.error('Authentication failed for metrics - redirecting to login');
                localStorage.removeItem('jwt');
                localStorage.removeItem('userType');
                localStorage.removeItem('username');
                window.location.href = '../login.html';
                return;
            }

            // Fallback to current boats data if general load fails
            allBoats = [...boats];
            console.log('Using fallback boats data for metrics:', allBoats.length, 'boats');
        }
    } catch (error) {
        console.error('Error loading general metrics:', error);
        // Fallback to current boats data if general load fails
        allBoats = [...boats];
        console.log('Using fallback boats data for metrics after error:', allBoats.length, 'boats');
    }
}


// Update metrics cards (always shows general data, not filtered data)
async function updateMetrics() {
    try {
        // Always use general data for metrics, load if not available
        if (allBoats.length === 0) {
            await loadGeneralMetrics();
        }

        const metricsBoats = allBoats.length > 0 ? allBoats : boats; // Use allBoats if available, fallback to boats
        const totalBoats = metricsBoats.length;
        const availableBoats = metricsBoats.filter(boat => !boat.owner).length; // Available if no owner assigned
        const maintenanceBoats = 0; // For now, no maintenance status tracking
        const ownedBoats = metricsBoats.filter(boat => boat.owner).length;

        // Update DOM elements with new values
        const totalBoatsEl = document.getElementById('totalBoats');
        const availableBoatsEl = document.getElementById('availableBoats');
        const maintenanceBoatsEl = document.getElementById('maintenanceBoats');
        const ownedBoatsEl = document.getElementById('ownedBoats');

        if (totalBoatsEl) totalBoatsEl.textContent = totalBoats;
        if (availableBoatsEl) availableBoatsEl.textContent = availableBoats;
        if (maintenanceBoatsEl) maintenanceBoatsEl.textContent = maintenanceBoats;
        if (ownedBoatsEl) ownedBoatsEl.textContent = ownedBoats;

        console.log('Updated metrics - Total:', totalBoats, 'Available:', availableBoats, 'Owned:', ownedBoats);
    } catch (error) {
        console.error('Error updating metrics:', error);
        // Fallback to local boats data if general metrics fail
        const totalBoats = boats.length;
        const availableBoats = boats.filter(boat => !boat.owner).length;
        const maintenanceBoats = 0;
        const ownedBoats = boats.filter(boat => boat.owner).length;

        document.getElementById('totalBoats').textContent = totalBoats;
        document.getElementById('availableBoats').textContent = availableBoats;
        document.getElementById('maintenanceBoats').textContent = maintenanceBoats;
        document.getElementById('ownedBoats').textContent = ownedBoats;

        console.log('Updated metrics with fallback data - Total:', totalBoats, 'Available:', availableBoats, 'Owned:', ownedBoats);
    }
}

// Filter boats based on search and filters
function filterBoats() {
    const searchTerm = searchInput.value.trim();
    const typeValue = typeFilter.value;
    const statusValue = statusFilter.value;

    // Reload data with filters applied server-side
    loadBoats(0, searchTerm, typeValue, statusValue);

    // Note: Metrics remain unchanged as they always show general data
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
                        <button class="action-btn documents-btn" onclick="manageDocuments(${boat.id})">Documentos</button>
                        <button class="action-btn delete-btn" onclick="deleteBoat(${boat.id})">Eliminar</button>
                    </div>
                </td>
            `;
            inventoryTableBody.appendChild(row);
        });
    }

    document.getElementById('tableCount').textContent = `${filteredBoats.length} embarcaciones en esta página`;
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
    loadBoats(page);
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
                <p><strong>Precio Total:</strong> ${formatPrice(boat.price || 0)}</p>
                <div class="form-group">
                    <label for="ownerSelect">Seleccionar Propietario</label>
                    <select id="ownerSelect" required>
                        <option value="">Seleccionar propietario...</option>
                        ${owners.map(owner => `<option value="${owner.id}">${owner.fullName} (${owner.email})</option>`).join('')}
                    </select>
                </div>
                <div class="form-group">
                    <label class="checkbox-label">
                        <input type="checkbox" id="manualCheckbox">
                        <span class="checkmark"></span>
                        Asignación Manual (sin crear pagos automáticos)
                    </label>
                </div>
                <div class="form-group">
                    <label for="installmentPlan">Plan de Pago</label>
                    <select id="installmentPlan">
                        <option value="">Seleccionar plan de pago...</option>
                        <option value="1">Contado</option>
                        <option value="6">6 Cuotas</option>
                        <option value="12">12 Cuotas</option>
                        <option value="24">24 Cuotas</option>
                        <option value="48">48 Cuotas</option>
                        <option value="72">72 Cuotas</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Monto por Cuota Calculado</label>
                    <input type="text" id="calculatedAmount" readonly style="background-color: #f5f5f5;">
                </div>
            </div>
            <div class="modal-footer">
                <button onclick="closeOwnerModal()" class="btn-secondary">Cancelar</button>
                <button onclick="goToOwnersAndAdd()" class="btn-secondary">Añadir Propietario</button>
                <button onclick="confirmAssignOwner(${boatId})" class="btn-primary">Asignar Propietario</button>
            </div>
        </div>
    `;

    document.body.appendChild(ownerModal);
    ownerModal.style.display = 'block';

    // Add event listeners for calculation
    const installmentPlanSelect = document.getElementById('installmentPlan');
    const calculatedAmountInput = document.getElementById('calculatedAmount');
    const manualCheckbox = document.getElementById('manualCheckbox');

    function togglePlanFields() {
        const isManual = manualCheckbox.checked;
        installmentPlanSelect.disabled = isManual;
        calculatedAmountInput.disabled = isManual;
        
        if (isManual) {
            calculatedAmountInput.value = 'Asignación manual - no se crearán pagos automáticos';
            installmentPlanSelect.value = '';
        } else {
            calculateAmount();
        }
    }

    function calculateAmount() {
        if (manualCheckbox.checked) {
            return; // Don't calculate if manual is selected
        }

        const numberOfInstallments = parseInt(installmentPlanSelect.value) || 0;
        const boatPrice = boat.price || 0;

        if (numberOfInstallments > 0 && boatPrice > 0) {
            if (numberOfInstallments === 1) {
                calculatedAmountInput.value = formatPrice(boatPrice) + ' (Contado)';
            } else {
                const amountPerInstallment = boatPrice / numberOfInstallments;
                calculatedAmountInput.value = formatPrice(amountPerInstallment) + ` (${numberOfInstallments} cuotas)`;
            }
        } else {
            calculatedAmountInput.value = '';
        }
    }

    manualCheckbox.addEventListener('change', togglePlanFields);
    installmentPlanSelect.addEventListener('change', calculateAmount);

    // Initial setup
    togglePlanFields();
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
            const searchTerm = searchInput.value;
            const typeValue = typeFilter.value;
            const statusValue = statusFilter.value;
            loadBoats(currentPage, searchTerm, typeValue, statusValue);
            // Reload general metrics data and update metrics sequentially
            await loadGeneralMetrics(); // Also reload general metrics data
            updateMetrics(); // Update the statistics display
        } else {
            console.error('Failed to delete boat');
            // Fallback to local deletion
            boats = boats.filter(boat => boat.id !== id);
            filteredBoats = filteredBoats.filter(boat => boat.id !== id);
            updateMetrics(); // Update statistics after local deletion
            renderBoats();
            updatePaginationControls();
        }
    } catch (error) {
        console.error('Error deleting boat:', error);
        // Fallback to local deletion
        boats = boats.filter(boat => boat.id !== id);
        filteredBoats = filteredBoats.filter(boat => boat.id !== id);
        updateMetrics();
        renderBoats();
        updatePaginationControls();
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
                price: parseFloat(boatData.price) || 0,
                balance: 0.0
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

        // Reload current page to ensure table is updated with latest data
        const searchTerm = searchInput.value;
        const typeValue = typeFilter.value;
        const statusValue = statusFilter.value;
        loadBoats(currentPage, searchTerm, typeValue, statusValue);

        // Reload general metrics data and update metrics sequentially
        await loadGeneralMetrics(); // Also reload general metrics data
        updateMetrics(); // Update the statistics display
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
        updateMetrics(); // Update statistics after local modification
        renderBoats();
        closeModal();
    }
}

// Confirm owner assignment
async function confirmAssignOwner(boatId) {
    const ownerSelect = document.getElementById('ownerSelect');
    const manualCheckbox = document.getElementById('manualCheckbox');
    const installmentPlanSelect = document.getElementById('installmentPlan');

    const ownerId = ownerSelect.value;
    const isManualAssignment = manualCheckbox && manualCheckbox.checked;

    if (!ownerId) {
        alert('Por favor selecciona un propietario');
        return;
    }

    // If manual assignment is selected, no need to validate payment fields
    if (!isManualAssignment) {
        const numberOfInstallments = parseInt(installmentPlanSelect.value);

        if (!numberOfInstallments || numberOfInstallments <= 0) {
            alert('Por favor selecciona un plan de pago válido');
            return;
        }

        const validInstallments = [1, 6, 12, 24, 48, 72];
        if (!validInstallments.includes(numberOfInstallments)) {
            alert('Por favor selecciona un plan de pago válido (Contado, 6, 12, 24, 48 o 72 cuotas)');
            return;
        }
    }

    try {
        let response;
        let url;

        if (isManualAssignment) {
            // Manual assignment - no payment creation
            url = `/api/v1/boat/${boatId}/owner/${ownerId}/manual`;
            response = await fetch(url, {
                method: 'PUT',
                headers: getAuthHeaders()
            });
        } else {
            // Automatic assignment with payment creation
            const numberOfInstallments = parseInt(installmentPlanSelect.value);
            url = `/api/v1/boat/${boatId}/owner/${ownerId}?numberOfInstallments=${numberOfInstallments}`;
            response = await fetch(url, {
                method: 'PUT',
                headers: getAuthHeaders()
            });
        }

        if (response.ok) {
            try {
                const updatedBoat = await response.json();
                const searchTerm = searchInput.value;
                const typeValue = typeFilter.value;
                const statusValue = statusFilter.value;
                loadBoats(currentPage, searchTerm, typeValue, statusValue);
                // Reload general metrics data and update metrics sequentially
                await loadGeneralMetrics(); // Also reload general metrics data
                updateMetrics(); // Update the statistics display
                closeOwnerModal();

                if (isManualAssignment) {
                    alert('Propietario asignado exitosamente (sin crear pagos automáticos)');
                } else {
                    alert('Propietario asignado exitosamente');
                }
            } catch (jsonError) {
                console.error('Error parsing JSON response:', jsonError);
                alert('Error al procesar la respuesta del servidor');
            }
        } else {
            // Try to get error message from response
            try {
                const errorData = await response.json();
                console.error('Server error:', errorData);
                if (response.status === 400) {
                    alert('Esta embarcación ya tiene un propietario asignado o los datos son inválidos');
                } else {
                    alert(`Error del servidor: ${errorData.message || 'Error desconocido'}`);
                }
            } catch (errorJsonError) {
                // If response is not JSON, get text
                const errorText = await response.text();
                console.error('Server error (text):', errorText);
                if (response.status === 400) {
                    alert('Esta embarcación ya tiene un propietario asignado o los datos son inválidos');
                } else {
                    alert(`Error del servidor: ${response.status} ${response.statusText}`);
                }
            }
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

// Go to owners page and open add modal
function goToOwnersAndAdd() {
    // Close current modal
    closeOwnerModal();

    // Navigate to owners page
    window.location.href = 'propietarios.html?openAddModal=true';
}

// Manage documents for a boat
function manageDocuments(boatId) {
    const boat = boats.find(b => b.id === boatId);
    if (!boat) return;

    // Create documents modal
    const documentsModal = document.createElement('div');
    documentsModal.id = 'documentsModal';
    documentsModal.className = 'modal';
    documentsModal.innerHTML = `
        <div class="modal-content large-modal">
            <div class="modal-header">
                <h3>Documentos de ${boat.name}</h3>
                <button onclick="closeDocumentsModal()" class="close-btn">✕</button>
            </div>
            <div class="modal-body">
                <div class="documents-section">
                    <div class="upload-section">
                        <h4>Subir Nuevo Documento</h4>
                        <div class="form-group">
                            <label for="documentName">Nombre del Documento</label>
                            <input type="text" id="documentName" placeholder="Ej: Licencia de navegación">
                        </div>
                        <div class="form-group">
                            <label for="documentFile">Archivo</label>
                            <input type="file" id="documentFile">
                        </div>
                        <button onclick="uploadDocument(${boatId})" class="btn-primary">Subir Documento</button>
                    </div>
                    <div class="documents-list">
                        <h4>Documentos Existentes</h4>
                        <div id="documentsList" class="documents-container">
                            <!-- Documents will be loaded here -->
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button onclick="closeDocumentsModal()" class="btn-secondary">Cerrar</button>
            </div>
        </div>
    `;

    document.body.appendChild(documentsModal);
    documentsModal.style.display = 'block';

    // Load documents
    loadDocuments(boatId);
}

// Close documents modal
function closeDocumentsModal() {
    const documentsModal = document.getElementById('documentsModal');
    if (documentsModal) {
        documentsModal.remove();
    }
}

// Load documents for a boat
async function loadDocuments(boatId) {
    const documentsList = document.getElementById('documentsList');
    if (!documentsList) return;

    try {
        const response = await fetch(`/api/v1/boat/${boatId}/documents`, {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const documents = await response.json();
            renderDocuments(documents, boatId);
        } else {
            console.error('Failed to load documents');
            documentsList.innerHTML = '<p class="no-documents">Error al cargar documentos</p>';
        }
    } catch (error) {
        console.error('Error loading documents:', error);
        documentsList.innerHTML = '<p class="no-documents">Error de conexión</p>';
    }
}

// Render documents list
function renderDocuments(documents, boatId) {
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
                    <button onclick="downloadDocument('${filename}')" class="btn-small">Descargar</button>
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
        const response = await fetch(`/api/v1/boat/${boatId}/documents`, {
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
            loadDocuments(boatId);
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
        const response = await fetch(`/api/v1/boat/${boatId}/documents/${documentId}?name=${encodeURIComponent(newName)}`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            loadDocuments(boatId);
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

// Download document
async function downloadDocument(filename) {
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
        const response = await fetch(`/api/v1/boat/${boatId}/documents/${documentId}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            loadDocuments(boatId);
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
    if (event.target.id === 'documentsModal') {
        closeDocumentsModal();
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
window.goToOwnersAndAdd = goToOwnersAndAdd;
window.manageDocuments = manageDocuments;
window.closeDocumentsModal = closeDocumentsModal;
window.uploadDocument = uploadDocument;
window.downloadDocument = downloadDocument;
window.editDocumentName = editDocumentName;
window.deleteDocument = deleteDocument;
window.changePage = changePage;
window.logout = logout;
window.navigateTo = navigateTo;