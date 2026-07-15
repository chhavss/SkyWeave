// SKYWEAVE - Route Planner Page Logic

let airportsList = [];
let selectedOptimization = 'distance';

document.addEventListener('DOMContentLoaded', () => {
    loadAirportsForSuggestions();
    initOptimizationToggles();
    initAutocomplete();
    
    document.getElementById('btn-search-route').addEventListener('click', performRouteSearch);
});

// Load airports to populate suggestions
async function loadAirportsForSuggestions() {
    try {
        const res = await fetch('/api/airports');
        if (res.ok) {
            airportsList = await res.json();
        }
    } catch (err) {
        console.error('Failed to load airports for suggestions:', err);
    }
}

// Setup optimization toggles
function initOptimizationToggles() {
    const buttons = document.querySelectorAll('.opt-btn');
    buttons.forEach(btn => {
        btn.addEventListener('click', () => {
            buttons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            selectedOptimization = btn.getAttribute('data-opt');
        });
    });
}

// Setup autocomplete input fields
function initAutocomplete() {
    setupInputSuggestions('search-source', 'source-suggestions');
    setupInputSuggestions('search-dest', 'dest-suggestions');
}

function setupInputSuggestions(inputId, listId) {
    const input = document.getElementById(inputId);
    const list = document.getElementById(listId);

    input.addEventListener('input', () => {
        const val = input.value.toLowerCase().trim();
        list.innerHTML = '';
        
        if (!val) {
            list.style.display = 'none';
            return;
        }

        const filtered = airportsList.filter(ap => 
            ap.airport_code.toLowerCase().includes(val) ||
            ap.airport_name.toLowerCase().includes(val) ||
            ap.city.toLowerCase().includes(val) ||
            ap.country.toLowerCase().includes(val)
        );

        if (filtered.length === 0) {
            list.style.display = 'none';
            return;
        }

        filtered.slice(0, 5).forEach(ap => {
            const li = document.createElement('li');
            li.innerHTML = `<strong>${ap.airport_code}</strong> - ${ap.city} (${ap.airport_name})`;
            li.addEventListener('click', () => {
                input.value = ap.airport_code;
                list.style.display = 'none';
            });
            list.appendChild(li);
        });

        list.style.display = 'block';
    });

    // Close list when clicking outside
    document.addEventListener('click', (e) => {
        if (e.target !== input && e.target !== list) {
            list.style.display = 'none';
        }
    });
}

// Execute route search
async function performRouteSearch() {
    const source = document.getElementById('search-source').value.toUpperCase().trim();
    const dest = document.getElementById('search-dest').value.toUpperCase().trim();

    if (!source) {
        showToast('Please fill out the Source Airport.', 'warning');
        return;
    }

    if (!dest) {
        showToast('Please specify a Destination Airport.', 'warning');
        return;
    }

    // Hide previous results
    document.getElementById('dijkstra-result').style.display = 'none';
    document.getElementById('traversal-result').style.display = 'none';
    document.getElementById('result-error').style.display = 'none';
    
    // Show spinner
    document.getElementById('loading-spinner').style.display = 'block';

    try {
        const url = `/api/routes?source=${encodeURIComponent(source)}&algorithm=dijkstra&destination=${encodeURIComponent(dest)}&optimization=${encodeURIComponent(selectedOptimization)}`;

        const res = await fetch(url);
        const data = await res.json();

        // Wait a slight fraction of a second for smooth animation feel
        setTimeout(() => {
            document.getElementById('loading-spinner').style.display = 'none';

            if (data.status === 'error') {
                showErrorState(data.message);
                return;
            }

            if (data.found) {
                renderDijkstraResult(data, source, dest);
            } else {
                showErrorState(`No connection path was found from ${source} to ${dest}.`);
            }
        }, 600);

    } catch (err) {
        document.getElementById('loading-spinner').style.display = 'none';
        showErrorState('Failed to connect to the backend server. Make sure the Java engine is running.');
    }
}

// Render error box
function showErrorState(msg) {
    document.getElementById('result-error').style.display = 'block';
    document.getElementById('result-error-msg').innerText = msg;
}

// Render Dijkstra flight path
function renderDijkstraResult(data, source, dest) {
    document.getElementById('dijkstra-result').style.display = 'block';
    
    // Fill summary values
    document.getElementById('sum-stops').innerText = data.stops === 0 ? 'Non-stop' : `${data.stops} stop${data.stops > 1 ? 's' : ''}`;
    document.getElementById('sum-dist').innerText = `${data.total_distance.toLocaleString()} km`;
    
    // Format duration
    const hrs = Math.floor(data.total_duration / 60);
    const mins = data.total_duration % 60;
    document.getElementById('sum-duration').innerText = `${hrs}h ${mins}m`;
    
    // Format cost (INR)
    document.getElementById('sum-cost').innerText = `₹${data.total_cost.toLocaleString()}`;

    // Render timeline path
    const timeline = document.getElementById('route-path-timeline');
    timeline.innerHTML = '';

    // First node (Source)
    appendNodeToTimeline(timeline, source);

    // Alternating edges and nodes
    data.path.forEach(flight => {
        appendEdgeToTimeline(timeline, flight);
        appendNodeToTimeline(timeline, flight.destination_airport);
    });
}

function appendNodeToTimeline(container, code) {
    const nodeDiv = document.createElement('div');
    nodeDiv.className = 'timeline-node';
    
    // Resolve city name if loaded
    const airport = airportsList.find(ap => ap.airport_code === code);
    const label = airport ? `${airport.city} (${airport.country})` : 'Airport';

    nodeDiv.innerHTML = `
        <div class="node-badge">${code}</div>
        <div class="node-info">
            <h4 style="margin:0; font-weight:700;">${label}</h4>
            <p style="margin:0; font-size:0.85rem; color:var(--text-muted);">${airport ? airport.airport_name : ''}</p>
        </div>
    `;
    container.appendChild(nodeDiv);
}

function appendEdgeToTimeline(container, flight) {
    const edgeDiv = document.createElement('div');
    edgeDiv.className = 'timeline-edge';

    const durationHrs = Math.floor(flight.duration / 60);
    const durationMins = flight.duration % 60;

    edgeDiv.innerHTML = `
        <div class="edge-info-card">
            <div class="edge-flight-meta">
                <span class="edge-airline">✈ ${flight.airline}</span>
                <span class="edge-flight-num">Flight ${flight.flight_number}</span>
            </div>
            <div class="edge-stats">
                <span>📏 ${flight.distance} km</span>
                <span>⏱ ${durationHrs}h ${durationMins}m</span>
                <span>💵 ₹${flight.cost.toLocaleString()}</span>
            </div>
        </div>
    `;
    container.appendChild(edgeDiv);
}

// Render BFS/DFS traversals
function renderTraversalResult(data) {
    document.getElementById('traversal-result').style.display = 'block';
    
    const container = document.getElementById('traversal-visit-badges');
    container.innerHTML = '';

    if (!data.visit_order || data.visit_order.length === 0) {
        container.innerHTML = '<p style="color:var(--text-muted);">No traversal path possible.</p>';
        return;
    }

    data.visit_order.forEach((code, index) => {
        const badge = document.createElement('div');
        badge.style.background = 'linear-gradient(135deg, var(--primary), var(--accent))';
        badge.style.color = 'white';
        badge.style.padding = '0.6rem 1.2rem';
        badge.style.borderRadius = '50px';
        badge.style.fontWeight = '700';
        badge.style.boxShadow = 'var(--shadow-soft)';
        badge.style.fontSize = '0.9rem';
        badge.innerHTML = `<span style="font-weight:400; opacity:0.8;">#${index + 1}</span> &nbsp; ${code}`;
        container.appendChild(badge);

        if (index < data.visit_order.length - 1) {
            const arrow = document.createElement('div');
            arrow.style.display = 'flex';
            arrow.style.alignItems = 'center';
            arrow.style.color = 'var(--accent)';
            arrow.style.fontWeight = '800';
            arrow.innerHTML = '➔';
            container.appendChild(arrow);
        }
    });
}
