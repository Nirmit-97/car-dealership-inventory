import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { purchaseVehicle, deleteVehicle, restockVehicle } from '../api/vehicles';

export default function VehicleCard({ vehicle, onUpdate }) {
  const { isAdmin } = useAuth();
  const [loading, setLoading] = useState('');
  const [showRestock, setShowRestock] = useState(false);
  const [restockAmt, setRestockAmt] = useState(5);

  const fmtPrice = (p) => `$${p?.toLocaleString()}`;
  const qtyClass = vehicle.quantity === 0 ? 'qty-out' : vehicle.quantity < 3 ? 'qty-low' : 'qty-num';

  const doAction = async (action, fn) => {
    setLoading(action);
    try { await fn(); onUpdate(); }
    catch (e) { alert(e.response?.data?.message || 'Operation failed'); }
    finally { setLoading(''); }
  };

  const handlePurchase = () => doAction('purchase', () => purchaseVehicle(vehicle.id));
  const handleDelete   = () => {
    if (!confirm(`Delete ${vehicle.make} ${vehicle.model}?`)) return;
    doAction('delete', () => deleteVehicle(vehicle.id));
  };
  const handleRestock  = () =>
    doAction('restock', async () => {
      await restockVehicle(vehicle.id, restockAmt);
      setShowRestock(false);
    });

  return (
    <div className="vehicle-card">
      <div className="vehicle-make">{vehicle.make}</div>
      <div className="vehicle-model">{vehicle.model}</div>
      <div>
        <span className="vehicle-category">{vehicle.category}</span>
      </div>

      <div className="vehicle-meta">
        <div>
          <div className="vehicle-price">
            {fmtPrice(vehicle.price)}
            <span> /unit</span>
          </div>
        </div>
        <div className="vehicle-qty">
          Stock: <span className={qtyClass}>
            {vehicle.quantity === 0 ? 'Out of stock' : vehicle.quantity}
          </span>
        </div>
      </div>

      {/* Restock input */}
      {showRestock && (
        <div style={{ display: 'flex', gap: 8, marginBottom: 12, alignItems: 'center' }}>
          <input
            type="number" min={1} max={999}
            value={restockAmt}
            onChange={e => setRestockAmt(Number(e.target.value))}
            className="form-input"
            style={{ width: 80 }}
          />
          <button className="btn btn-warning btn-sm" onClick={handleRestock} disabled={loading === 'restock'}>
            {loading === 'restock' ? '…' : 'Confirm'}
          </button>
          <button className="btn btn-secondary btn-sm" onClick={() => setShowRestock(false)}>Cancel</button>
        </div>
      )}

      <div className="btn-group">
        {/* Any user can purchase */}
        <button
          id={`purchase-${vehicle.id}`}
          className="btn btn-success btn-sm"
          onClick={handlePurchase}
          disabled={vehicle.quantity === 0 || !!loading}
        >
          {loading === 'purchase' ? '…' : '🛒 Buy'}
        </button>

        {/* Admin only */}
        {isAdmin && (
          <>
            <button
              id={`restock-${vehicle.id}`}
              className="btn btn-warning btn-sm"
              onClick={() => setShowRestock(!showRestock)}
              disabled={!!loading}
            >
              📦 Restock
            </button>
            <button
              id={`delete-${vehicle.id}`}
              className="btn btn-danger btn-sm"
              onClick={handleDelete}
              disabled={!!loading}
            >
              {loading === 'delete' ? '…' : '🗑'}
            </button>
          </>
        )}
      </div>
    </div>
  );
}
