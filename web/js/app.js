// SKYWEAVE - Shared Application Logic

// Run theme detection immediately to avoid Light Mode flash during loading
(function() {
    let savedTheme = localStorage.getItem('skyweave_theme');
    if (!savedTheme) {
        // Follow OS preference if no saved preference
        savedTheme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }
    document.documentElement.setAttribute('data-theme', savedTheme);
})();

document.addEventListener('DOMContentLoaded', () => {
    initNavbar();
    initThemeToggle();
});

// ─── Premium Sky/Space Theme Toggle ───────────────────────────────────────────
function initThemeToggle() {
    const navbar = document.querySelector('.navbar');
    if (!navbar) return;

    const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
    const isDark = currentTheme === 'dark';

    // ── Build toggle HTML ──
    const wrapper = document.createElement('div');
    wrapper.className = 'sky-toggle-wrapper';
    wrapper.setAttribute('role', 'switch');
    wrapper.setAttribute('aria-checked', isDark ? 'true' : 'false');
    wrapper.setAttribute('aria-label', 'Toggle dark/light mode');
    wrapper.setAttribute('tabindex', '0');
    if (isDark) wrapper.classList.add('dark');

    wrapper.innerHTML = `
        <div class="sky-toggle-track">
            <!-- Light mode: clouds -->
            <div class="toggle-cloud toggle-cloud-1"></div>
            <div class="toggle-cloud toggle-cloud-2"></div>
            <div class="toggle-cloud toggle-cloud-3"></div>
            <!-- Dark mode: stars -->
            <div class="toggle-stars">
                <span class="t-star t-star-1"></span>
                <span class="t-star t-star-2"></span>
                <span class="t-star t-star-3"></span>
                <span class="t-star t-star-4"></span>
                <span class="t-star t-star-5"></span>
                <span class="t-star t-star-6"></span>
                <span class="t-star t-star-7"></span>
                <span class="t-star t-star-8"></span>
            </div>
            <!-- Knob: sun or moon -->
            <div class="sky-toggle-knob">
                <!-- Sun face -->
                <div class="knob-sun">
                    <div class="sun-ray sun-ray-1"></div>
                    <div class="sun-ray sun-ray-2"></div>
                    <div class="sun-ray sun-ray-3"></div>
                    <div class="sun-ray sun-ray-4"></div>
                    <div class="sun-ray sun-ray-5"></div>
                    <div class="sun-ray sun-ray-6"></div>
                    <div class="sun-ray sun-ray-7"></div>
                    <div class="sun-ray sun-ray-8"></div>
                    <div class="sun-core"></div>
                </div>
                <!-- Moon face -->
                <div class="knob-moon">
                    <div class="moon-core"></div>
                    <div class="moon-crater moon-crater-1"></div>
                    <div class="moon-crater moon-crater-2"></div>
                    <div class="moon-crater moon-crater-3"></div>
                </div>
            </div>
        </div>
    `;

    // ── Click / keyboard handler ──
    function doToggle() {
        const nowDark = wrapper.classList.contains('dark');
        const newTheme = nowDark ? 'light' : 'dark';

        // Animate page body
        document.body.classList.add('theme-transitioning');

        wrapper.classList.toggle('dark');
        wrapper.setAttribute('aria-checked', nowDark ? 'false' : 'true');

        // Small delay before committing theme for a sunset tint effect
        setTimeout(() => {
            document.documentElement.setAttribute('data-theme', newTheme);
            localStorage.setItem('skyweave_theme', newTheme);
        }, 120);

        setTimeout(() => {
            document.body.classList.remove('theme-transitioning');
        }, 600);
    }

    wrapper.addEventListener('click', doToggle);
    wrapper.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); doToggle(); }
    });

    // ── Insert into navbar ──
    const navToggle = navbar.querySelector('.nav-toggle');
    if (navToggle) {
        navbar.insertBefore(wrapper, navToggle);
    } else {
        const navLinks = navbar.querySelector('.nav-links');
        if (navLinks) navbar.insertBefore(wrapper, navLinks);
        else navbar.appendChild(wrapper);
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
