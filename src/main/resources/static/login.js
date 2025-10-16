// State management
let currentTab = 'admin';

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
            try {
                // Determine user type based on role from backend
                const actualUserType = data.role === 'ADMIN' ? 'admin' : 'owner';

                // Validate that the user type matches the selected login type
                if (userType !== actualUserType) {
                    if (userType === 'admin') {
                        showError('Este usuario no tiene permisos de administrador. Por favor, usa el acceso de propietario.');
                    } else {
                        showError('Este usuario es administrador. Por favor, usa el acceso administrativo.');
                    }
                    return;
                }

                // Store authentication data
                localStorage.setItem('userType', actualUserType);
                localStorage.setItem('username', username);
                localStorage.setItem('userId', data.id);
                localStorage.setItem('jwt', data.jwt);

                // Extract and store fullName and role from JWT token
                if (data.jwt) {
                    try {
                        const payload = data.jwt.split('.')[1];
                        // Properly decode UTF-8 base64 payload
                        const decodedPayload = JSON.parse(decodeURIComponent(escape(atob(payload))));
                        localStorage.setItem('fullName', decodedPayload.fullName || username);
                        localStorage.setItem('role', decodedPayload.role || data.role);
                    } catch (jwtError) {
                        console.warn('Error decoding JWT payload:', jwtError);
                        // Use data from response if JWT decoding fails
                        localStorage.setItem('fullName', username);
                        localStorage.setItem('role', data.role);
                    }
                } else {
                    // Fallback if no JWT token
                    localStorage.setItem('fullName', username);
                    localStorage.setItem('role', data.role);
                }

                // Redirect based on actual user type
                if (actualUserType === 'admin') {
                    window.location.href = 'admin/dashboard.html';
                } else {
                    window.location.href = 'owner/dashboard.html';
                }
            } catch (error) {
                console.error('Error processing login data:', error);
                showError('Error procesando la información de login. Inténtalo de nuevo.');
            }
        } else {
            // Handle error responses from the global exception handler
            if (data && data.message) {
                showError(data.message);
            } else {
                showError('Credenciales incorrectas. Verifica tu usuario y contraseña.');
            }
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