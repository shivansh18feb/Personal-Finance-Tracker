import React, { useState, useEffect } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import { Repeat, Plus, Trash2, CheckCircle2, PauseCircle } from 'lucide-react';

export const Recurring = () => {
  const [items, setItems] = useState([]);
  const [showAdd, setShowAdd] = useState(false);
  const [type, setType] = useState('EXPENSE');
  const [amount, setAmount] = useState('');
  const [category, setCategory] = useState('Utilities');
  const [description, setDescription] = useState('');
  const [frequency, setFrequency] = useState('MONTHLY');
  const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);

  const fetchItems = async () => {
    try {
      const res = await api.get('/recurring');
      setItems(res.data);
    } catch (err) {
      toast.error('Failed to load recurring rules');
    }
  };

  useEffect(() => {
    fetchItems();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await api.post('/recurring', {
        type,
        amount: Number(amount),
        category,
        description,
        frequency,
        startDate,
        isSubscription: false,
      });
      toast.success('Recurring rule created');
      setShowAdd(false);
      setAmount('');
      setDescription('');
      fetchItems();
    } catch (err) {
      toast.error('Could not create recurring rule');
    }
  };

  const handleToggleStatus = async (id, currentStatus) => {
    const nextStatus = currentStatus === 'ACTIVE' ? 'PAUSED' : 'ACTIVE';
    try {
      await api.patch(`/recurring/${id}/status`, { status: nextStatus });
      toast.success(`Rule ${nextStatus.toLowerCase()}`);
      fetchItems();
    } catch (err) {
      toast.error('Failed to change status');
    }
  };

  const handleDelete = async (id) => {
    try {
      await api.delete(`/recurring/${id}`);
      toast.success('Rule deleted');
      fetchItems();
    } catch (err) {
      toast.error('Could not delete');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Recurring Transactions</h1>
          <p className="text-sm text-slate-500">Automate repeat salary deposits, rent payments, and utility bills.</p>
        </div>
        <button
          onClick={() => setShowAdd(!showAdd)}
          className="flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2 rounded-lg font-medium shadow"
        >
          <Plus className="w-4 h-4" /> Add Rule
        </button>
      </div>

      {showAdd && (
        <form onSubmit={handleCreate} className="bg-white dark:bg-slate-900 border rounded-xl p-5 shadow-sm space-y-4">
          <h2 className="font-bold text-base">New Recurring Schedule</h2>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Type</label>
              <select value={type} onChange={(e) => setType(e.target.value)} className="w-full px-3 py-2 border rounded-lg bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800">
                <option value="EXPENSE">Expense</option>
                <option value="INCOME">Income</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Amount ($)</label>
              <input type="number" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} required className="w-full px-3 py-2 border rounded-lg bg-transparent border-slate-200 dark:border-slate-800" />
            </div>
            <div>
              <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Frequency</label>
              <select value={frequency} onChange={(e) => setFrequency(e.target.value)} className="w-full px-3 py-2 border rounded-lg bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800">
                <option value="MONTHLY">Monthly</option>
                <option value="WEEKLY">Weekly</option>
                <option value="DAILY">Daily</option>
                <option value="YEARLY">Yearly</option>
              </select>
            </div>
          </div>
          <div>
            <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Description</label>
            <input type="text" placeholder="e.g. Monthly Rent Transfer" value={description} onChange={(e) => setDescription(e.target.value)} required className="w-full px-3 py-2 border rounded-lg bg-transparent border-slate-200 dark:border-slate-800" />
          </div>
          <div className="flex justify-end gap-2">
            <button type="button" onClick={() => setShowAdd(false)} className="px-4 py-2 text-sm border rounded-lg">Cancel</button>
            <button type="submit" className="px-4 py-2 text-sm bg-emerald-600 text-white rounded-lg">Create Schedule</button>
          </div>
        </form>
      )}

      {/* Rules List */}
      <div className="bg-white dark:bg-slate-900 border rounded-xl divide-y border-slate-200 dark:border-slate-800 divide-slate-100 dark:divide-slate-800">
        {items.length === 0 ? (
          <p className="p-8 text-center text-slate-400 text-sm">No recurring rules configured.</p>
        ) : (
          items.map((rule) => (
            <div key={rule.id} className="p-4 flex items-center justify-between">
              <div>
                <p className="font-semibold text-sm">{rule.description}</p>
                <p className="text-xs text-slate-400">
                  {rule.frequency} • Next Run: <strong>{rule.nextExecutionDate}</strong> • {rule.category}
                </p>
              </div>
              <div className="flex items-center gap-4">
                <span className="font-bold text-sm">${rule.amount?.toFixed(2)}</span>
                <button
                  onClick={() => handleToggleStatus(rule.id, rule.status)}
                  className={`text-xs px-2.5 py-1 rounded font-semibold ${
                    rule.status === 'ACTIVE'
                      ? 'bg-emerald-50 text-emerald-600'
                      : 'bg-amber-50 text-amber-600'
                  }`}
                >
                  {rule.status}
                </button>
                <button onClick={() => handleDelete(rule.id)} className="text-slate-400 hover:text-rose-600">
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
