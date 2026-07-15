// SKYWEAVE - Flights Page Logic

let allFlights = [];

document.addEventListener('DOMContentLoaded', () => {
    fetchFlights();
    
    // Add event listeners for filters
    document.getElementById('flight-source-filter').addEventListener('change', applyFilters);
    document.getElementById('flight-dest-filter').addEventListener('change', applyFilters);
    document.getElementById('flight-airline-search').addEventListener('input', applyFilters);
    document.getElementById('flight-sort').addEventListener('change', applyFilters);
});

// Fetch flight schedules
async function fetchFlights() {
    const spinner = document.getElementById('flights-spinner');
    const tableContainer = document.getElementById('flights-table-container');

    try {
        const res = await fetch('/api/flights');
        if (!res.ok) throw new Error('Network response not ok');

        allFlights = await res.json();
        spinner.style.display = 'none';
        tableContainer.style.display = 'block';

        populateFilters();
        applyFilters();
    } catch (err) {
        console.error('Failed to load flights:', err);
        spinner.style.display = 'none';
        showToast('Error loading flights list.', 'error');
    }
}

// Populate filters drop-downs
function populateFilters() {
    const sourceSelect = document.getElementById('flight-source-filter');
    const destSelect = document.getElementById('flight-dest-filter');

    const origins = [...new Set(allFlights.map(f => f.source_airport))].sort();
    const destinations = [...new Set(allFlights.map(f => f.destination_airport))].sort();

    origins.forEach(code => {
        const opt = document.createElement('option');
        opt.value = code;
        opt.innerText = code;
        sourceSelect.appendChild(opt);
    });

    destinations.forEach(code => {
        const opt = document.createElement('option');
        opt.value = code;
        opt.innerText = code;
        destSelect.appendChild(opt);
    });
}

// Apply sorting & filtering
function applyFilters() {
    const originVal = document.getElementById('flight-source-filter').value;
    const destVal = document.getElementById('flight-dest-filter').value;
    const airlineVal = document.getElementById('flight-airline-search').value.toLowerCase().trim();
    const sortVal = document.getElementById('flight-sort').value;

    let filtered = allFlights.filter(f => {
        const matchesOrigin = !originVal || f.source_airport === originVal;
        const matchesDest = !destVal || f.destination_airport === destVal;
        const matchesAirline = !airlineVal || f.airline.toLowerCase().includes(airlineVal);
        return matchesOrigin && matchesDest && matchesAirline;
    });

    // Sort
    filtered.sort((a, b) => {
        if (sortVal === 'flight_number') {
            return a.flight_number.localeCompare(b.flight_number);
        } else if (sortVal === 'cost_low') {
            return a.cost - b.cost;
        } else if (sortVal === 'cost_high') {
            return b.cost - a.cost;
        } else if (sortVal === 'duration') {
            return a.duration - b.duration;
        } else if (sortVal === 'distance') {
            return a.distance - b.distance;
        }
        return 0;
    });

    renderFlights(filtered);
}

// Render rows
function renderFlights(list) {
    const tbody = document.getElementById('flights-tbody');
    const emptyState = document.getElementById('flights-empty-state');
    const tableContainer = document.getElementById('flights-table-container');
    tbody.innerHTML = '';

    if (list.length === 0) {
        tableContainer.style.display = 'none';
        emptyState.style.display = 'block';
        return;
    }

    tableContainer.style.display = 'block';
    emptyState.style.display = 'none';

    list.forEach(f => {
        const tr = document.createElement('tr');
        
        // Format duration
        const hrs = Math.floor(f.duration / 60);
        const mins = f.duration % 60;
        const durationStr = `${hrs}h ${mins}m`;

        // Format status badge class
        let badgeClass = 'available';
        if (f.status.toLowerCase() === 'delayed') badgeClass = 'delayed';
        if (f.status.toLowerCase() === 'cancelled') badgeClass = 'cancelled';

        tr.innerHTML = `
            <td style="font-weight: 700; color: var(--text-color);">${f.flight_number}</td>
            <td>${f.airline}</td>
            <td style="font-weight: 600;">${f.source_airport}</td>
            <td style="font-weight: 600;">${f.destination_airport}</td>
            <td>${f.distance.toLocaleString()} km</td>
            <td>${durationStr}</td>
            <td style="font-weight: 700; color: var(--primary);">₹${f.cost.toLocaleString()}</td>
            <td><span class="status-badge ${badgeClass}">${f.status}</span></td>
        `;
        tbody.appendChild(tr);
    });
}
