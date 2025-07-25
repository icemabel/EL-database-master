// Client-side authentication helper
// Include this script in pages that require authentication

function getAuthToken() {
    return localStorage.getItem('jwt-token') || sessionStorage.getItem('jwt-token');
}

function getCurrentUsername() {
    return localStorage.getItem('username');
}

function isLoggedIn() {
    return !!(getAuthToken() && getCurrentUsername());
}

function logout() {
    localStorage.removeItem('jwt-token');
    sessionStorage.removeItem('jwt-token');
    localStorage.removeItem('username');
}

function redirectToLogin(message = 'Please log in to access this page.') {
    if (confirm(message + ' Would you like to log in now?')) {
        window.location.href = '/login';
    } else {
        window.location.href = '/';
    }
}

async function checkAuthenticationStatus() {
    const token = getAuthToken();
    const username = getCurrentUsername();

    if (!token || !username) {
        return { authenticated: false, error: 'No token or username found' };
    }

    try {
        const response = await fetch('/api/profile', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const profile = await response.json();
            return {
                authenticated: true,
                user: profile
            };
        } else if (response.status === 401) {
            // Token expired or invalid
            logout();
            return {
                authenticated: false,
                error: 'Token expired or invalid'
            };
        } else {
            return {
                authenticated: false,
                error: `Server error: ${response.status}`
            };
        }
    } catch (error) {
        return {
            authenticated: false,
            error: `Network error: ${error.message}`
        };
    }
}

// Function to make authenticated API calls
async function authenticatedFetch(url, options = {}) {
    const token = getAuthToken();

    if (!token) {
        throw new Error('No authentication token found');
    }

    // Add authorization header
    const headers = {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
        ...options.headers
    };

    const response = await fetch(url, {
        ...options,
        headers
    });

    // Check if token is expired
    if (response.status === 401) {
        logout();
        redirectToLogin('Your session has expired.');
        throw new Error('Authentication expired');
    }

    return response;
}

// Function to require authentication on page load
async function requireAuthentication(redirectOnFail = true) {
    const authStatus = await checkAuthenticationStatus();

    if (!authStatus.authenticated) {
        console.warn('Authentication required:', authStatus.error);

        if (redirectOnFail) {
            redirectToLogin();
        }

        return false;
    }

    console.log('User authenticated:', authStatus.user.username);
    return authStatus;
}

// Show user info in the UI
function displayUserInfo(containerId = 'user-info') {
    const container = document.getElementById(containerId);
    if (!container) return;

    const username = getCurrentUsername();
    if (username) {
        container.innerHTML = `
            <span>Welcome, ${username}!</span>
            <button onclick="logout(); window.location.reload();" style="margin-left: 10px;">Logout</button>
        `;
    } else {
        container.innerHTML = `
            <a href="/login">Login</a>
        `;
    }
}