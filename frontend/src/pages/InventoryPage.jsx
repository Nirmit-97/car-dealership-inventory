import { useEffect, useState } from 'react';
import { getVehicles, searchVehicles } from '../api/vehicles';
import { useAuth } from '../context/AuthContext';
import Sidebar from '../components/Sidebar';
import VehicleCard from '../components/VehicleCard';
import AddVehicleModal from '../components/AddVehicleModal';

export default function InventoryPage() {
  const { isAdmin } = useAuth();
  const [vehicles, setVehicles] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingVehicle, setEditingVehicle] = useState(null);

  // Responsive design Menu
  const [sidebarOpen, setSidebarOpen] = useState(false);

  // Search parameters states for backend-driven Specification search
  const [searchMake, setSearchMake] = useState('');
  const [searchModel, setSearchModel] = useState('');
  const [searchCategory, setSearchCategory] = useState('');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [showFilters, setShowFilters] = useState(false);

  // Fetch full inventory once for stats, then let backend search handle list views
  const fetchInventory = async () => {
    try {
      const { data } = await getVehicles();
      setVehicles(data);
    } catch (e) {
      console.error('Failed to load inventory stats', e);
    }
  };

  const handleSearch = async () => {
    setLoading(true);
    try {
      // Build request query object for backend search
      const params = {};
      if (searchMake.trim()) params.make = searchMake.trim();
      if (searchModel.trim()) params.model = searchModel.trim();
      if (searchCategory) params.category = searchCategory;
      if (minPrice) params.minPrice = parseFloat(minPrice);
      if (maxPrice) params.maxPrice = parseFloat(maxPrice);

      const hasActiveFilters = Object.keys(params).length > 0;

      let resultData;
      if (hasActiveFilters) {
        const res = await searchVehicles(params);
        resultData = res.data;
      } else {
        const res = await getVehicles();
        resultData = res.data;
      }
      
      setFiltered(resultData);
    } catch (e) {
      console.error('Search failed', e);
    } finally {
      setLoading(false);
    }
  };

  // Initially fetch everything
  const init = async () => {
    setLoading(true);
    await fetchInventory();
    try {
      const { data } = await getVehicles();
      setFiltered(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    init();
  }, []);

  const handleUpdate = async () => {
    await fetchInventory();
    await handleSearch();
  };

  const clearSearch = () => {
    setSearchMake('');
    setSearchModel('');
    setSearchCategory('');
    setMinPrice('');
    setMaxPrice('');
    init();
  };

  // Collect unique categories for dropdown
  const categories = [...new Set(vehicles.map(v => v.category))].sort();

  // Metrics
  const totalValue = vehicles.reduce((s, v) => s + v.price * v.quantity, 0);
  const inStock    = vehicles.filter(v => v.quantity > 0).length;
  const outStock   = vehicles.filter(v => v.quantity === 0).length;

  return (
    <div className={`app-layout ${sidebarOpen ? 'sidebar-open' : ''}`}>
      {/* Mobile Burger Header */}
      <div className="mobile-header">
        <button className="mobile-toggle" onClick={() => setSidebarOpen(true)}>
          ☰
        </button>
        <div style={{ fontWeight: 800, fontSize: '18px' }}>🚗 AutoVault</div>
        <div style={{ width: 24 }}></div> {/* spacer */}
      </div>

      {/* Sidebar background overlay for mobile */}
      {sidebarOpen && (
        <div className="sidebar-overlay" onClick={() => setSidebarOpen(false)} />
      )}

      <Sidebar onClose={() => setSidebarOpen(false)} />

      <main className="main-content">
        {/* Header */}
        <div className="topbar">
          <div>
            <div className="page-title">Inventory <span>Dashboard</span></div>
            <div className="text-muted" style={{ fontSize: 13, marginTop: 4 }}>
              {vehicles.length} vehicles registered in vault
            </div>
          </div>
          {isAdmin && (
            <button id="add-vehicle-btn" className="btn btn-primary" onClick={() => setShowAddModal(true)}>
              + Add Vehicle
            </button>
          )}
        </div>

        {/* Stats Grid */}
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
            <div className="stat-sub">needs re-order</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Inventory Value</div>
            <div className="stat-value" style={{ fontSize: 20 }}>${totalValue.toLocaleString()}</div>
            <div className="stat-sub">total value</div>
          </div>
        </div>

        {/* Dynamic Search & Specification Query Box */}
        <div className="search-bar" style={{ display: 'flex', flexDirection: 'column', gap: 12, background: 'var(--bg-card)', padding: '16px', borderRadius: 'var(--radius-md)', border: '1px solid var(--border)', marginBottom: 24 }}>
          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', width: '100%' }}>
            
            {/* Standard quick search (make / model query) */}
            <input
              id="search-make"
              style={{ flex: 1, minWidth: 150 }}
              className="search-input"
              placeholder="🔍 Filter by Make (e.g. Toyota)"
              value={searchMake}
              onChange={e => setSearchMake(e.target.value)}
            />

            <input
              id="search-model"
              style={{ flex: 1, minWidth: 150 }}
              className="search-input"
              placeholder="🔍 Filter by Model (e.g. Camry)"
              value={searchModel}
              onChange={e => setSearchModel(e.target.value)}
            />

            <button className="btn btn-primary" onClick={handleSearch}>
              Search
            </button>

            <button className="btn btn-secondary" onClick={() => setShowFilters(!showFilters)}>
              ⚙️ {showFilters ? 'Hide price/cat filters' : 'Advanced Filters'}
            </button>

            {(searchMake || searchModel || searchCategory || minPrice || maxPrice) && (
              <button className="btn btn-danger btn-sm" onClick={clearSearch}>
                ✕ Clear
              </button>
            )}
          </div>

          {/* Collapsible advanced filters verifying Criteria Specification */}
          {showFilters && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))', gap: 12, marginTop: 12, paddingTop: 12, borderTop: '1px solid var(--border)' }}>
              
              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label">Category</label>
                <select
                  id="search-category"
                  className="search-select"
                  value={searchCategory}
                  onChange={e => setSearchCategory(e.target.value)}
                >
                  <option value="">All Categories</option>
                  {categories.map(c => <option key={c} value={c}>{c}</option>)}
                </select>
              </div>

              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label">Min Price ($)</label>
                <input
                  id="search-price-min"
                  type="number"
                  className="form-input"
                  placeholder="0"
                  value={minPrice}
                  onChange={e => setMinPrice(e.target.value)}
                />
              </div>

              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label">Max Price ($)</label>
                <input
                  id="search-price-max"
                  type="number"
                  className="form-input"
                  placeholder="999999"
                  value={maxPrice}
                  onChange={e => setMaxPrice(e.target.value)}
                />
              </div>

            </div>
          )}
        </div>

        {/* Vehicles Display Grid */}
        {loading ? (
          <div className="loading-wrap">
            <div className="spinner" />
            <span className="text-muted">Fetching matching cars…</span>
          </div>
        ) : filtered.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">🔍</div>
            <p>No vehicles match your Criteria spec conditions.</p>
          </div>
        ) : (
          <div className="vehicle-grid">
            {filtered.map(v => (
              <VehicleCard 
                key={v.id} 
                vehicle={v} 
                onUpdate={handleUpdate} 
                onEdit={(veh) => setEditingVehicle(veh)}
              />
            ))}
          </div>
        )}
      </main>

      {/* Reusable dialog for either adding or editing a vehicle record */}
      {(showAddModal || editingVehicle) && (
        <AddVehicleModal
          editingVehicle={editingVehicle}
          onClose={() => {
            setShowAddModal(false);
            setEditingVehicle(null);
          }}
          onAdded={() => {
            setShowAddModal(false);
            setEditingVehicle(null);
            handleUpdate();
          }}
        />
      )}
    </div>
  );
}
