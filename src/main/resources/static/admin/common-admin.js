// Common Admin JavaScript Functions

// Get authentication headers
function getAuthHeaders() {
    const jwt = localStorage.getItem('jwt');
    return {
        'Content-Type': 'application/json',
        ...(jwt ? { 'Authorization': `Bearer ${jwt}` } : {})
    };
}

// Fetch current user and update header
function fetchCurrentUser() {
    // Get fullName from localStorage
    const fullName = localStorage.getItem('fullName');
    const userInfoSpan = document.querySelector('.user-info span');
    if (userInfoSpan && fullName) {
        userInfoSpan.textContent = fullName;
    }
}

// Authentication check
function checkAuthentication() {
    const userType = localStorage.getItem('userType');
    const jwt = localStorage.getItem('jwt');

    if (!userType || userType !== 'admin' || !jwt) {
        // Redirect to login if not authenticated as admin
        window.location.href = '../login.html';
        return;
    }
}

// Logout function
function logout() {
    // Clear authentication data
    localStorage.removeItem('userType');
    localStorage.removeItem('username');
    localStorage.removeItem('jwt');

    // Redirect to login
    window.location.href = '../login.html';
}

// Export functions for global use
window.getAuthHeaders = getAuthHeaders;
window.fetchCurrentUser = fetchCurrentUser;
window.checkAuthentication = checkAuthentication;
window.logout = logout;