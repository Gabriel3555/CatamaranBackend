// State management
let currentTab = 'admin';
let showPassword = false;

// DOM elements
const adminForm = document.getElementById('adminForm');
const ownerForm = document.getElementById('ownerForm');
const adminTab = document.querySelector('[data-tab="admin"]');
const ownerTab = document.querySelector('[data-tab="owner"]');
const cardTitle = document.getElementById('cardTitle');
const cardSubtitle = document.getElementById('cardSubtitle');
const errorMessage = document.getElementById('errorMessage');

// Tab switching functionality
function switchTab(tab) {
    currentTab = tab;

    // Update tab buttons
    adminTab.classList.toggle('active', tab === 'admin');
    ownerTab.classList.toggle('active', tab === 'owner');

    // Update card header
    if (tab === 'admin') {
        cardTitle.innerHTML = '<span class="icon">🛡️</span><span>Acceso Administrativo</span>';
        cardSubtitle.textContent = 'Ingresa con tu cuenta de administrador';
    } else {
        cardTitle.innerHTML = '<span class="icon">👤</span><span>Acceso Propietario</span>';
        cardSubtitle.textContent = 'Accede a la información de tu embarcación';
    }

    // Show/hide forms
    adminForm.classList.toggle('hidden', tab !== 'admin');
    ownerForm.classList.toggle('hidden', tab !== 'owner');

    // Hide error message when switching tabs
    hideError();
}

// Password toggle functionality
function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    const eyeIcon = document.getElementById(inputId === 'admin-password' ? 'admin-eye-icon' : 'owner-eye-icon');

    showPassword = !showPassword;
    input.type = showPassword ? 'text' : 'password';
    eyeIcon.textContent = showPassword ? '🙈' : '👁️';
}

// Error handling
function showError(message) {
    errorMessage.textContent = message;
    errorMessage.style.display = 'block';
}

function hideError() {
    errorMessage.style.display = 'none';
}

// Loading state management
function setLoading(buttonId, spinnerId, loading) {
    const button = document.getElementById(buttonId);
    const spinner = document.getElementById(spinnerId);
    const buttonText = button.querySelector('.button-text');

    if (loading) {
        button.disabled = true;
        spinner.style.display = 'block';
        buttonText.textContent = 'Iniciando sesión...';
    } else {
        button.disabled = false;
        spinner.style.display = 'none';
        buttonText.textContent = 'Iniciar Sesión';
    }
}

// Login functionality
async function handleLogin(event, userType) {
    event.preventDefault();
    hideError();

    const formData = new FormData(event.target);
    const username = formData.get('username');
    const password = formData.get('password');

    // Set loading state
    const buttonId = userType === 'admin' ? 'adminSubmitBtn' : 'ownerSubmitBtn';
    const spinnerId = userType === 'admin' ? 'adminSpinner' : 'ownerSpinner';
    setLoading(buttonId, spinnerId, true);

    try {
        // Call the backend API
        const response = await fetch('/api/v1/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        const data = await response.json();

        if (response.ok && data.status) {
            // Store authentication data
                localStorage.setItem('userType', userType);
                localStorage.setItem('username', username);
                localStorage.setItem('userId', data.id);
                localStorage.setItem('jwt', data.jwt);
                localStorage.setItem('refreshToken', data.refreshToken);

            // Redirect based on user type
            if (userType === 'admin') {
                window.location.href = 'admin/dashboard.html';
            } else {
                window.location.href = 'owner/dashboard.html';
            }
        } else {
            showError(data.message || 'Credenciales incorrectas. Verifica tu usuario y contraseña.');
        }
    } catch (error) {
        console.error('Login error:', error);
        showError('Error de conexión. Inténtalo de nuevo.');
    } finally {
        setLoading(buttonId, spinnerId, false);
    }
}

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    // Set initial tab
    switchTab('admin');

    // Add form submit handlers (already in HTML, but ensuring they're bound)
    adminForm.addEventListener('submit', (e) => handleLogin(e, 'admin'));
    ownerForm.addEventListener('submit', (e) => handleLogin(e, 'owner'));
});