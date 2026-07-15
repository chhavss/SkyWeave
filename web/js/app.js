// SKYWEAVE - Shared Application Logic

// Run theme detection immediately to avoid Light Mode flash during loading
(function() {
    const savedTheme = localStorage.getItem('skyweave_theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
})();

document.addEventListener('DOMContentLoaded', () => {
    initNavbar();
    initThemeToggle();
});

// Theme toggle button injection and logic
function initThemeToggle() {
    const navbar = document.querySelector('.navbar');
    if (!navbar) return;

    // Create theme toggle button
    const btn = document.createElement('button');
    btn.id = 'theme-toggle';
    btn.className = 'btn-theme-toggle';
    btn.setAttribute('aria-label', 'Toggle Theme');
    
    // Set initial icon based on theme state
    const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
    btn.innerText = currentTheme === 'dark' ? '☀️' : '🌙';

    btn.addEventListener('click', () => {
        const theme = document.documentElement.getAttribute('data-theme') || 'light';
        const newTheme = theme === 'dark' ? 'light' : 'dark';
        
        document.documentElement.setAttribute('data-theme', newTheme);
        localStorage.setItem('skyweave_theme', newTheme);
        btn.innerText = newTheme === 'dark' ? '☀️' : '🌙';
        
        showToast(`Switched to ${newTheme} mode`, 'success');
    });

    // Insert button in the navbar right before the mobile toggle button (if present)
    const navToggle = navbar.querySelector('.nav-toggle');
    if (navToggle) {
        navbar.insertBefore(btn, navToggle);
    } else {
        // Fallback: insert right before the nav-links menu
        const navLinks = navbar.querySelector('.nav-links');
        if (navLinks) {
            navbar.insertBefore(btn, navLinks);
        } else {
            navbar.appendChild(btn);
        }
    }
}

// Toast notification helper
const showToast = (message, type = 'info') => {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    // Choose icon based on type
    let icon = '✈';
    if (type === 'success') icon = '✓';
    if (type === 'error') icon = '✗';
    if (type === 'warning') icon = '⚠';

    toast.innerHTML = `
        <span class="toast-icon">${icon}</span>
        <span class="toast-message">${message}</span>
    `;

    container.appendChild(toast);

    // Fade out and remove toast after 4s
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(20px)';
        toast.style.transition = 'all 0.4s ease';
        setTimeout(() => {
            toast.remove();
            if (container.children.length === 0) {
                container.remove();
            }
        }, 400);
    }, 4000);
};

// Navbar logic
function initNavbar() {
    const navbar = document.querySelector('.navbar');
    
    // Sticky navbar on scroll
    window.addEventListener('scroll', () => {
        if (window.scrollY > 50) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
    });

    // Mobile nav toggle
    const toggleBtn = document.querySelector('.nav-toggle');
    const navLinks = document.querySelector('.nav-links');
    
    if (toggleBtn && navLinks) {
        toggleBtn.addEventListener('click', () => {
            navLinks.classList.toggle('mobile-show');
        });
    }

    // Set active link highlight based on current path
    const links = document.querySelectorAll('.nav-links a');
    const path = window.location.pathname;

    links.forEach(link => {
        const href = link.getAttribute('href');
        if (href && (path.endsWith(href) || (path.endsWith('/') && href.includes('index')))) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });

    // Dynamically adjust nav links if admin is logged in
    updateAdminNavState();
}

function updateAdminNavState() {
    const token = localStorage.getItem('skyweave_admin_token');
    const navLinks = document.querySelector('.nav-links');
    
    if (token && navLinks) {
        // Add Dashboard link if it's not there
        const hasDash = document.querySelector('.nav-dash-link');
        if (!hasDash) {
            const li = document.createElement('li');
            li.className = 'nav-dash-link';
            li.innerHTML = '<a href="dashboard.html" class="btn-admin-nav">Dashboard</a>';
            navLinks.appendChild(li);
        }
    }
}
