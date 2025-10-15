// Admin Profile Page JavaScript

// DOM elements - will be initialized after DOM load
let editProfileModal;
let profileForm;
let changePasswordModal;
let passwordForm;

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    // Initialize DOM elements
    editProfileModal = document.getElementById('editProfileModal');
    profileForm = document.getElementById('profileForm');
    changePasswordModal = document.getElementById('changePasswordModal');
    passwordForm = document.getElementById('passwordForm');

    checkAuthentication();
    loadProfileData();
});

// Authentication check
function checkAuthentication() {
    const userType = localStorage.getItem('userType');
    const jwt = localStorage.getItem('jwt');

    if (!userType || userType !== 'admin' || !jwt) {
        window.location.href = '../login.html';
        return;
    }

    // Update user name in header
    const username = localStorage.getItem('username') || 'Administrador';
    const userNameElement = document.getElementById('userName');
    if (userNameElement) {
        userNameElement.textContent = username;
    }
}

// Get authentication headers
function getAuthHeaders() {
    const jwt = localStorage.getItem('jwt');
    return {
        'Content-Type': 'application/json',
        ...(jwt ? { 'Authorization': `Bearer ${jwt}` } : {})
    };
}

// Load profile data from API
async function loadProfileData() {
    try {
        const userId = localStorage.getItem('userId');
        const jwt = localStorage.getItem('jwt');

        console.log('Loading profile data for userId:', userId);
        console.log('JWT token present:', !!jwt);

        if (!userId) {
            console.error('User ID not found in localStorage');
            showProfileError('Usuario no identificado. Por favor, inicia sesión nuevamente.');
            return;
        }

        if (!jwt) {
            console.error('JWT token not found');
            showProfileError('Sesión expirada. Por favor, inicia sesión nuevamente.');
            return;
        }

        // Load user profile
        console.log('Fetching user profile from:', `/api/v1/auth/${userId}`);
        const userResponse = await fetch(`/api/v1/auth/${userId}`, {
            headers: getAuthHeaders()
        });

        console.log('User profile response status:', userResponse.status);

        if (userResponse.ok) {
            const userData = await userResponse.json();
            console.log('User data received:', userData);
            displayUserProfile(userData);
        } else {
            const errorText = await userResponse.text();
            console.error('Failed to load user profile:', userResponse.status, errorText);
            showProfileError(`Error al cargar el perfil: ${userResponse.status}`);
        }

        // Load admin stats
        console.log('Fetching admin stats from:', `/api/v1/admin/stats`);
        const statsResponse = await fetch(`/api/v1/admin/stats`, {
            headers: getAuthHeaders()
        });

        console.log('Stats response status:', statsResponse.status);

        if (statsResponse.ok) {
            const statsData = await statsResponse.json();
            console.log('Stats data received:', statsData);
            displayAccountStatistics(statsData);
        } else {
            const errorText = await statsResponse.text();
            console.error('Failed to load stats data:', statsResponse.status, errorText);
            // Don't show error for stats data, just log it
        }

    } catch (error) {
        console.error('Error loading profile data:', error);
        showProfileError('Error de conexión. Inténtalo de nuevo.');
    }
}

// Display user profile information
function displayUserProfile(userData) {
    document.getElementById('fullName').textContent = userData.fullName || userData.username || 'No especificado';
    document.getElementById('username').textContent = userData.username || 'No especificado';
    document.getElementById('email').textContent = userData.email || 'No especificado';

    // Set form values for editing
    document.getElementById('editFullName').value = userData.fullName || '';
    document.getElementById('editEmail').value = userData.email || '';
    document.getElementById('editPhone').value = userData.phone || '';
    document.getElementById('editAddress').value = userData.address || '';
}

// Display account statistics
function displayAccountStatistics(data) {
    document.getElementById('totalBoats').textContent = data.totalBoats || 0;
    document.getElementById('activeOwners').textContent = data.activeOwners || 0;
    document.getElementById('pendingMaintenances').textContent = data.pendingMaintenances || 0;
    document.getElementById('monthlyPayments').textContent = formatPrice(data.monthlyPayments || 0);
}

// Format price in Colombian pesos
function formatPrice(price) {
    return new Intl.NumberFormat('es-CO', {
        style: 'currency',
        currency: 'COP',
        minimumFractionDigits: 0
    }).format(price);
}

// Show error message
function showProfileError(message) {
    const mainContent = document.querySelector('.main-content');
    if (mainContent) {
        mainContent.innerHTML = `
            <div class="container">
                <div class="page-header">
                    <h2>Error</h2>
                </div>
                <div class="section-card">
                    <div style="text-align: center; padding: 40px; color: #dc2626;">
                        <h3>⚠️ ${message}</h3>
                        <button onclick="location.reload()" style="margin-top: 20px; padding: 10px 20px; background: #3b82f6; color: white; border: none; border-radius: 6px; cursor: pointer;">
                            Reintentar
                        </button>
                    </div>
                </div>
            </div>
        `;
    }
}

// Edit profile
function editProfile() {
    editProfileModal.style.display = 'block';
}

// Close modal
function closeModal() {
    editProfileModal.style.display = 'none';
}

// Save profile changes
async function saveProfile() {
    const formData = new FormData(profileForm);
    const profileData = {
        fullName: formData.get('fullName'),
        email: formData.get('email'),
        phone: formData.get('phone'),
        address: formData.get('address')
    };

    try {
        const userId = localStorage.getItem('userId');
        const response = await fetch(`/api/v1/auth/${userId}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(profileData)
        });

        if (response.ok) {
            const updatedUser = await response.json();
            displayUserProfile(updatedUser);
            closeModal();
            alert('Perfil actualizado exitosamente');
        } else {
            const error = await response.text();
            alert('Error al actualizar el perfil: ' + error);
        }
    } catch (error) {
        console.error('Error saving profile:', error);
        alert('Error al guardar los cambios. Inténtalo de nuevo.');
    }
}

// Change password
function changePassword() {
    changePasswordModal.style.display = 'block';
}

// Close password modal
function closePasswordModal() {
    changePasswordModal.style.display = 'none';
    passwordForm.reset();
}

// Save new password
async function savePassword() {
    const newPassword = document.getElementById('newPassword').value.trim();

    if (!newPassword || newPassword.length < 6) {
        alert('La contraseña debe tener al menos 6 caracteres');
        return;
    }

    try {
        const userId = localStorage.getItem('userId');
        const response = await fetch(`/api/v1/auth/${userId}/password`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'text/plain',
                'Authorization': `Bearer ${localStorage.getItem('jwt')}`
            },
            body: newPassword
        });

        if (response.ok) {
            closePasswordModal();
            alert('Contraseña cambiada exitosamente');
        } else {
            const error = await response.text();
            alert('Error al cambiar la contraseña: ' + error);
        }
    } catch (error) {
        console.error('Error changing password:', error);
        alert('Error al cambiar la contraseña. Inténtalo de nuevo.');
    }
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
    window.location.href = `${page}.html`;
}

// Close modal when clicking outside
window.onclick = function(event) {
    if (event.target === editProfileModal) {
        closeModal();
    }
    if (event.target === changePasswordModal) {
        closePasswordModal();
    }
};

// Export functions for HTML onclick handlers
window.editProfile = editProfile;
window.closeModal = closeModal;
window.saveProfile = saveProfile;
window.changePassword = changePassword;
window.closePasswordModal = closePasswordModal;
window.savePassword = savePassword;
window.logout = logout;
window.navigateTo = navigateTo;