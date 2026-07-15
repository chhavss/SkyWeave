// SKYWEAVE - Administrative Dashboard Logic

let currentTab = 'airports'; // airports, flights
let airportsList = [];
let flightsList = [];

document.addEventListener('DOMContentLoaded', () => {
    // 1. Session check
    checkSession();
    
    // 2. Fetch all data
    refreshDashboardData();
});

function checkSession() {
    const token = localStorage.getItem('skyweave_admin_token');
    if (!token) {
        window.location.href = 'admin-login.html';
    }
}

function handleSignOut() {
    localStorage.removeItem('skyweave_admin_token');
    showToast('Signed out successfully.', 'info');
    setTimeout(() => {
        window.location.href = 'index.html';
    }, 500);
}

// Switch menu tabs
function switchTab(tab) {
    currentTab = tab;
    
    // Update active side links
    document.getElementById('menu-airports').classList.toggle('active', tab === 'airports');
    document.getElementById('menu-flights').classList.toggle('active', tab === 'flights');

    // Update containers
    document.getElementById('tab-airports-container').style.display = tab === 'airports' ? 'block' : 'none';
    document.getElementById('tab-flights-container').style.display = tab === 'flights' ? 'block' : 'none';

    // Update header button text
    const btn = document.getElementById('btn-add-item');
    if (tab === 'airports') {
        btn.innerText = '+ Add Airport';
        btn.setAttribute('onclick', 'openAddModal()');
    } else {
        btn.innerText = '+ Add Flight';
        btn.setAttribute('onclick', 'openAddModal()');
    }
}

// Fetch lists from backend
async function refreshDashboardData() {
    document.getElementById('dash-spinner').style.display = 'block';
    
    try {
        const [resAirports, resFlights] = await Promise.all([
            fetch('/api/airports'),
            fetch('/api/flights')
        ]);

        airportsList = await resAirports.json();
        flightsList = await resFlights.json();

        document.getElementById('dash-spinner').style.display = 'none';

        // Calculate statistics
        document.getElementById('stat-airports').innerText = airportsList.length;
        document.getElementById('stat-flights').innerText = flightsList.length;
        
        const activeCount = flightsList.filter(f => f.status.toLowerCase() === 'available').length;
        document.getElementById('stat-active').innerText = activeCount;

        // Render tables
        renderAirportsTable();
        renderFlightsTable();

        // Populate Airport select inputs for Flights modal
        populateAirportDropdowns();
    } catch (err) {
        console.error('Failed to load dashboard data:', err);
        document.getElementById('dash-spinner').style.display = 'none';
        showToast('Error syncing dashboard data.', 'error');
    }
}

// Populate Airport select lists
function populateAirportDropdowns() {
    const selects = [
        document.getElementById('add-f-source'),
        document.getElementById('add-f-dest'),
        document.getElementById('edit-f-source'),
        document.getElementById('edit-f-dest')
    ];

    selects.forEach(select => {
        if (select) {
            select.innerHTML = '<option value="">Select Airport</option>';
            airportsList.forEach(ap => {
                const opt = document.createElement('option');
                opt.value = ap.airport_code;
                opt.innerText = `${ap.airport_code} - ${ap.city}`;
                select.appendChild(opt);
            });
        }
    });
}

// Render Airports list
function renderAirportsTable() {
    const tbody = document.getElementById('dash-airports-tbody');
    tbody.innerHTML = '';

    airportsList.forEach(ap => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td style="font-weight: 700; color: var(--text-color);">${ap.airport_code}</td>
            <td>${ap.airport_name}</td>
            <td>${ap.city}</td>
            <td>${ap.country}</td>
            <td class="table-actions">
                <button class="btn btn-secondary btn-small" onclick="openEditAirportModal(${ap.id}, '${ap.airport_code}', '${escapeHtml(ap.airport_name)}', '${escapeHtml(ap.city)}', '${escapeHtml(ap.country)}')">Edit</button>
                <button class="btn btn-danger btn-small" onclick="deleteAirport(${ap.id})">Delete</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Render Flights list
function renderFlightsTable() {
    const tbody = document.getElementById('dash-flights-tbody');
    tbody.innerHTML = '';

    flightsList.forEach(f => {
        const tr = document.createElement('tr');

        const hrs = Math.floor(f.duration / 60);
        const mins = f.duration % 60;
        const durationStr = `${hrs}h ${mins}m`;

        let badgeClass = 'available';
        if (f.status.toLowerCase() === 'delayed') badgeClass = 'delayed';
        if (f.status.toLowerCase() === 'cancelled') badgeClass = 'cancelled';

        tr.innerHTML = `
            <td style="font-weight: 700; color: var(--text-color);">${f.flight_number}</td>
            <td>${f.airline}</td>
            <td style="font-weight: 600;">${f.source_airport}</td>
            <td style="font-weight: 600;">${f.destination_airport}</td>
            <td>${f.distance} km</td>
            <td>${durationStr}</td>
            <td style="font-weight: 700; color: var(--primary);">₹${f.cost.toLocaleString()}</td>
            <td><span class="status-badge ${badgeClass}">${f.status}</span></td>
            <td class="table-actions">
                <button class="btn btn-secondary btn-small" onclick="openEditFlightModal(${f.id}, '${f.flight_number}', '${escapeHtml(f.airline)}', '${f.source_airport}', '${f.destination_airport}', ${f.distance}, ${f.duration}, ${f.cost}, '${f.status}')">Edit</button>
                <button class="btn btn-danger btn-small" onclick="deleteFlight(${f.id})">Delete</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Modal helper controls
function openModal(id) {
    document.getElementById(id).style.display = 'flex';
}

function closeModal(id) {
    document.getElementById(id).style.display = 'none';
}

function openAddModal() {
    if (currentTab === 'airports') {
        document.getElementById('add-ap-code').value = '';
        document.getElementById('add-ap-name').value = '';
        document.getElementById('add-ap-city').value = '';
        document.getElementById('add-ap-country').value = '';
        openModal('modal-add-airport');
    } else {
        document.getElementById('add-f-num').value = '';
        document.getElementById('add-f-airline').value = '';
        document.getElementById('add-f-source').value = '';
        document.getElementById('add-f-dest').value = '';
        document.getElementById('add-f-distance').value = '';
        document.getElementById('add-f-duration').value = '';
        document.getElementById('add-f-cost').value = '';
        document.getElementById('add-f-status').value = 'Available';
        openModal('modal-add-flight');
    }
}

// CRUD Operations: AIRPORTS
async function submitAddAirport() {
    const code = document.getElementById('add-ap-code').value.toUpperCase().trim();
    const name = document.getElementById('add-ap-name').value.trim();
    const city = document.getElementById('add-ap-city').value.trim();
    const country = document.getElementById('add-ap-country').value.trim();

    try {
        const res = await fetch('/api/airports', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ airport_code: code, airport_name: name, city, country })
        });
        const data = await res.json();
        
        if (data.status === 'success') {
            closeModal('modal-add-airport');
            showToast(data.message, 'success');
            refreshDashboardData();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Error connecting to backend.', 'error');
    }
}

function openEditAirportModal(id, code, name, city, country) {
    document.getElementById('edit-ap-id').value = id;
    document.getElementById('edit-ap-code').value = code;
    document.getElementById('edit-ap-name').value = name;
    document.getElementById('edit-ap-city').value = city;
    document.getElementById('edit-ap-country').value = country;
    openModal('modal-edit-airport');
}

async function submitEditAirport() {
    const id = document.getElementById('edit-ap-id').value;
    const code = document.getElementById('edit-ap-code').value.toUpperCase().trim();
    const name = document.getElementById('edit-ap-name').value.trim();
    const city = document.getElementById('edit-ap-city').value.trim();
    const country = document.getElementById('edit-ap-country').value.trim();

    try {
        const res = await fetch('/api/airports', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, airport_code: code, airport_name: name, city, country })
        });
        const data = await res.json();
        
        if (data.status === 'success') {
            closeModal('modal-edit-airport');
            showToast(data.message, 'success');
            refreshDashboardData();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Error connecting to backend.', 'error');
    }
}

async function deleteAirport(id) {
    if (!confirm('Are you sure you want to delete this airport? This will delete all connected flight routes!')) return;

    try {
        const res = await fetch(`/api/airports?id=${id}`, { method: 'DELETE' });
        const data = await res.json();
        
        if (data.status === 'success') {
            showToast(data.message, 'success');
            refreshDashboardData();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Error connecting to backend.', 'error');
    }
}

// CRUD Operations: FLIGHTS
async function submitAddFlight() {
    const fn = document.getElementById('add-f-num').value.toUpperCase().trim();
    const airline = document.getElementById('add-f-airline').value.trim();
    const source = document.getElementById('add-f-source').value;
    const dest = document.getElementById('add-f-dest').value;
    const dist = document.getElementById('add-f-distance').value;
    const dur = document.getElementById('add-f-duration').value;
    const cost = document.getElementById('add-f-cost').value;
    const status = document.getElementById('add-f-status').value;

    if (source === dest) {
        showToast('Origin and Destination airports cannot be the same.', 'warning');
        return;
    }

    try {
        const res = await fetch('/api/flights', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                flight_number: fn, airline, source_airport: source, 
                destination_airport: dest, distance: dist, duration: dur, cost, status 
            })
        });
        const data = await res.json();
        
        if (data.status === 'success') {
            closeModal('modal-add-flight');
            showToast(data.message, 'success');
            refreshDashboardData();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Error connecting to backend.', 'error');
    }
}

function openEditFlightModal(id, num, airline, source, dest, dist, dur, cost, status) {
    document.getElementById('edit-f-id').value = id;
    document.getElementById('edit-f-num').value = num;
    document.getElementById('edit-f-airline').value = airline;
    document.getElementById('edit-f-source').value = source;
    document.getElementById('edit-f-dest').value = dest;
    document.getElementById('edit-f-distance').value = dist;
    document.getElementById('edit-f-duration').value = dur;
    document.getElementById('edit-f-cost').value = cost;
    document.getElementById('edit-f-status').value = status;
    openModal('modal-edit-flight');
}

async function submitEditFlight() {
    const id = document.getElementById('edit-f-id').value;
    const fn = document.getElementById('edit-f-num').value.toUpperCase().trim();
    const airline = document.getElementById('edit-f-airline').value.trim();
    const source = document.getElementById('edit-f-source').value;
    const dest = document.getElementById('edit-f-dest').value;
    const dist = document.getElementById('edit-f-distance').value;
    const dur = document.getElementById('edit-f-duration').value;
    const cost = document.getElementById('edit-f-cost').value;
    const status = document.getElementById('edit-f-status').value;

    if (source === dest) {
        showToast('Origin and Destination airports cannot be the same.', 'warning');
        return;
    }

    try {
        const res = await fetch('/api/flights', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                id, flight_number: fn, airline, source_airport: source, 
                destination_airport: dest, distance: dist, duration: dur, cost, status 
            })
        });
        const data = await res.json();
        
        if (data.status === 'success') {
            closeModal('modal-edit-flight');
            showToast(data.message, 'success');
            refreshDashboardData();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Error connecting to backend.', 'error');
    }
}

async function deleteFlight(id) {
    if (!confirm('Are you sure you want to delete this flight route segment?')) return;

    try {
        const res = await fetch(`/api/flights?id=${id}`, { method: 'DELETE' });
        const data = await res.json();
        
        if (data.status === 'success') {
            showToast(data.message, 'success');
            refreshDashboardData();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Error connecting to backend.', 'error');
    }
}

// Escape helper
function escapeHtml(text) {
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
