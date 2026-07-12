import { useEffect, useState } from 'react';
import { getVehicles } from '../api/vehicles';
import { useAuth } from '../context/AuthContext';
import Sidebar from '../components/Sidebar';
import VehicleCard from '../components/VehicleCard';
import AddVehicleModal from '../components/AddVehicleModal';

export default function InventoryPage() {
  const { isAdmin } = useAuth();
  const [vehicles, setVehicles] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [search, setSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');

  const fetchVehicles = async () => {
    setLoading(true);
    try {
      const { data } = await getVehicles();
      setVehicles(data);
      setFiltered(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchVehicles(); }, []);

  // Client-side filtering
  useEffect(() => {
    let result = vehicles;
    if (search) {
      const q = search.toLowerCase();
      result = result.filter(v =>
        v.make.toLowerCase().includes(q) || v.model.toLowerCase().includes(q)
      );
    }
    if (categoryFilter) {
      result = result.filter(v => v.category === categoryFilter);
    }
    setFiltered(result);
  }, [search, categoryFilter, vehicles]);

  const categories = [...new Set(vehicles.map(v => v.category))].sort();

  const totalValue = vehicles.reduce((s, v) => s + v.price * v.quantity, 0);
  const inStock    = vehicles.filter(v => v.quantity > 0).length;
  const outStock   = vehicles.filter(v => v.quantity === 0).length;

  return (
    <div className="app-layout">
      <Sidebar />
      <main className="main-content">
        {/* Header */}
        <div className="topbar">
          <div>
            <div className="page-title">Inventory <span>Dashboard</span></div>
            <div className="text-muted" style={{ fontSize: 13, marginTop: 4 }}>
              {vehicles.length} vehicles in system
            </div>
          </div>
          {isAdmin && (
            <button id="add-vehicle-btn" className="btn btn-primary" onClick={() => setShowModal(true)}>
              + Add Vehicle
            </button>
          )}
        </div>

        {/* Stats */}
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-label">Total Vehicles</div>
            <div className="stat-value">{vehicles.length}</div>
            <div className="stat-sub">unique models</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">In Stock</div>
            <div className="stat-value" style={{ color: 'var(--success)' }}>{inStock}</div>
            <div className="stat-sub">available</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Out of Stock</div>
            <div className="stat-value" style={{ color: 'var(--danger)' }}>{outStock}</div>
            <div className="stat-sub">need restocking</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Inventory Value</div>
            <div className="stat-value" style={{ fontSize: 20 }}>${totalValue.toLocaleString()}</div>
            <div className="stat-sub">total</div>
          </div>
        </div>

        {/* Search Bar */}
        <div className="search-bar">
          <input
            id="search-input"
            className="search-input"
            placeholder="🔍  Search by make or model…"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
          <select
            id="category-filter"
            className="search-select"
            value={categoryFilter}
            onChange={e => setCategoryFilter(e.target.value)}
          >
            <option value="">All Categories</option>
            {categories.map(c => <option key={c}>{c}</option>)}
          </select>
          {(search || categoryFilter) && (
            <button className="btn btn-secondary" onClick={() => { setSearch(''); setCategoryFilter(''); }}>
              ✕ Clear
            </button>
          )}
        </div>

        {/* Grid */}
        {loading ? (
          <div className="loading-wrap">
            <div className="spinner" />
            <span className="text-muted">Loading inventory…</span>
          </div>
        ) : filtered.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">🔍</div>
            <p>No vehicles match your search.</p>
          </div>
        ) : (
          <div className="vehicle-grid">
            {filtered.map(v => (
              <VehicleCard key={v.id} vehicle={v} onUpdate={fetchVehicles} />
            ))}
          </div>
        )}
      </main>

      {showModal && (
        <AddVehicleModal
          onClose={() => setShowModal(false)}
          onAdded={() => { setShowModal(false); fetchVehicles(); }}
        />
      )}
    </div>
  );
}
