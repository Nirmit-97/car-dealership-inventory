import { useState } from 'react';
import { createVehicle } from '../api/vehicles';

const CATEGORIES = ['Sedan', 'SUV', 'Coupe', 'Truck', 'Hatchback', 'Electric', 'Convertible', 'Van'];

export default function AddVehicleModal({ onClose, onAdded }) {
  const [form, setForm] = useState({
    make: '', model: '', category: 'Sedan', price: '', quantity: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handle = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await createVehicle({
        ...form,
        price: parseFloat(form.price),
        quantity: parseInt(form.quantity),
      });
      onAdded();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add vehicle.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <div className="modal-title">🚗 Add New Vehicle</div>

        {error && <div className="alert alert-error">⚠ {error}</div>}

        <form onSubmit={submit}>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Make</label>
              <input id="add-make" className="form-input" name="make" placeholder="Toyota" value={form.make} onChange={handle} required />
            </div>
            <div className="form-group">
              <label className="form-label">Model</label>
              <input id="add-model" className="form-input" name="model" placeholder="Camry" value={form.model} onChange={handle} required />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Category</label>
            <select id="add-category" className="form-select" name="category" value={form.category} onChange={handle}>
              {CATEGORIES.map(c => <option key={c}>{c}</option>)}
            </select>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Price ($)</label>
              <input id="add-price" className="form-input" name="price" type="number" min={0} step={100} placeholder="25000" value={form.price} onChange={handle} required />
            </div>
            <div className="form-group">
              <label className="form-label">Quantity</label>
              <input id="add-quantity" className="form-input" name="quantity" type="number" min={1} placeholder="10" value={form.quantity} onChange={handle} required />
            </div>
          </div>

          <div className="btn-group" style={{ justifyContent: 'flex-end', marginTop: 4 }}>
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button id="add-vehicle-submit" type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Adding…' : '+ Add Vehicle'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
