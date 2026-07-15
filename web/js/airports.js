// SKYWEAVE - Airports Page Logic

let allAirports = [];

document.addEventListener('DOMContentLoaded', () => {
    fetchAirports();
    document.getElementById('airport-search-input').addEventListener('input', applyFilters);
    document.getElementById('airport-country-filter').addEventListener('change', applyFilters);
});

// Fetch airports list
async function fetchAirports() {
    const spinner = document.getElementById('airports-spinner');
    const grid = document.getElementById('airports-grid');

    try {
        const res = await fetch('/api/airports');
        if (!res.ok) throw new Error('Network response not ok');
        
        allAirports = await res.json();
        spinner.style.display = 'none';
        
        populateCountryFilter();
        renderAirports(allAirports);
    } catch (err) {
        console.error('Failed to load airports:', err);
        spinner.style.display = 'none';
        showToast('Error loading airports list.', 'error');
    }
}

// Populate unique countries
function populateCountryFilter() {
    const filter = document.getElementById('airport-country-filter');
    const countries = [...new Set(allAirports.map(ap => ap.country))].sort();
    
    countries.forEach(country => {
        const opt = document.createElement('option');
        opt.value = country;
        opt.innerText = country;
        filter.appendChild(opt);
    });
}

// Render cards
function renderAirports(list) {
    const grid = document.getElementById('airports-grid');
    const emptyState = document.getElementById('airports-empty-state');
    grid.innerHTML = '';

    if (list.length === 0) {
        emptyState.style.display = 'block';
        return;
    }
    emptyState.style.display = 'none';

    list.forEach(ap => {
        const card = document.createElement('div');
        card.className = 'card';
        card.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 1.5rem;">
                <div class="card-icon" style="margin: 0; width: 50px; height: 50px; font-size: 1.4rem;">📍</div>
                <span style="font-weight: 800; font-size: 1.4rem; color: var(--primary); background: var(--bg-color); padding: 0.3rem 0.8rem; border-radius: var(--border-radius-md); border: 1px solid var(--border-color);">${ap.airport_code}</span>
            </div>
            <h3 class="card-title">${ap.city}</h3>
            <p style="font-weight: 600; font-size: 0.9rem; color: var(--text-color); margin-bottom: 0.3rem;">${ap.country}</p>
            <p class="card-text" style="font-size: 0.85rem;">${ap.airport_name}</p>
        `;
        grid.appendChild(card);
    });
}

// Filter logic
function applyFilters() {
    const searchVal = document.getElementById('airport-search-input').value.toLowerCase().trim();
    const countryVal = document.getElementById('airport-country-filter').value;

    const filtered = allAirports.filter(ap => {
        const matchesSearch = 
            ap.airport_code.toLowerCase().includes(searchVal) ||
            ap.airport_name.toLowerCase().includes(searchVal) ||
            ap.city.toLowerCase().includes(searchVal) ||
            ap.country.toLowerCase().includes(searchVal);
            
        const matchesCountry = !countryVal || ap.country === countryVal;

        return matchesSearch && matchesCountry;
    });

    renderAirports(filtered);
}
