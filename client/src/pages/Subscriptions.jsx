import React, { useState, useEffect } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import { CreditCard, Plus, Trash2 } from 'lucide-react';

export const Subscriptions = () => {
  const [subs, setSubs] = useState([]);
  const [showAdd, setShowAdd] = useState(false);
  const [name, setName] = useState('');
  const [amount, setAmount] = useState('');
  const [frequency, setFrequency] = useState('MONTHLY');
  const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);

  const fetchSubscriptions = async () => {
    try {
      const res = await api.get('/recurring/subscriptions');
      setSubs(res.data);
    } catch (err) {
      toast.error('Failed to load subscriptions');
    }
  };

  useEffect(() => {
    fetchSubscriptions();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await api.post('/recurring', {
        type: 'EXPENSE',
        category: 'Subscriptions',
        description: name,
        amount: Number(amount),
        frequency,
        startDate,
        isSubscription: true,
      });
      toast.success('Subscription saved');
      setShowAdd(false);
      setName('');
      setAmount('');
      fetchSubscriptions();
    } catch (err) {
      toast.error('Could not save subscription');
    }
  };

  const handleDelete = async (id) => {
    try {
      await api.delete(`/recurring/${id}`);
      toast.success('Subscription deleted');
      fetchSubscriptions();
    } catch (err) {
      toast.error('Could not delete');
    }
  };

  const totalMonthlyCost = subs.reduce((acc, sub) => acc + (sub.amount || 0), 0);

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Subscriptions Manager</h1>
          <p className="text-sm text-slate-500">
            Keep track of active memberships and recurring services. Total commitment: <strong>${totalMonthlyCost.toFixed(2)}/mo</strong>
          </p>
        </div>
        <button
          onClick={() => setShowAdd(!showAdd)}
          className="flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2 rounded-lg font-medium shadow"
        >
          <Plus className="w-4 h-4" /> Add Subscription
        </button>
      </div>

      {showAdd && (
        <form onSubmit={handleCreate} className="bg-white dark:bg-slate-900 border rounded-xl p-5 shadow-sm space-y-4">
          <h2 className="font-bold text-base">New Subscription</h2>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Service Name</label>
              <input type="text" placeholder="e.g. Netflix, Spotify" value={name} onChange={(e) => setName(e.target.value)} required className="w-full px-3 py-2 border rounded-lg bg-transparent border-slate-200 dark:border-slate-800" />
            </div>
            <div>
              <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Cost ($)</label>
              <input type="number" step="0.01" placeholder="14.99" value={amount} onChange={(e) => setAmount(e.target.value)} required className="w-full px-3 py-2 border rounded-lg bg-transparent border-slate-200 dark:border-slate-800" />
            </div>
            <div>
              <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Renewal Cycle</label>
              <select value={frequency} onChange={(e) => setFrequency(e.target.value)} className="w-full px-3 py-2 border rounded-lg bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800">
                <option value="MONTHLY">Monthly</option>
                <option value="YEARLY">Yearly</option>
              </select>
            </div>
          </div>
          <div className="flex justify-end gap-2">
            <button type="button" onClick={() => setShowAdd(false)} className="px-4 py-2 text-sm border rounded-lg">Cancel</button>
            <button type="submit" className="px-4 py-2 text-sm bg-emerald-600 text-white rounded-lg">Add Subscription</button>
          </div>
        </form>
      )}

      {/* Subscriptions Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {subs.map((sub) => (
          <div key={sub.id} className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 shadow-sm space-y-3">
            <div className="flex justify-between items-start">
              <div className="flex items-center gap-3">
                <div className="p-2.5 bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 rounded-lg">
                  <CreditCard className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="font-bold text-base">{sub.description}</h3>
                  <p className="text-xs text-slate-400">Renews on {sub.nextExecutionDate}</p>
                </div>
              </div>
              <button onClick={() => handleDelete(sub.id)} className="text-slate-400 hover:text-rose-600">
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
            <div className="flex justify-between items-baseline pt-2 border-t border-slate-100 dark:border-slate-800">
              <span className="text-xs text-slate-500 uppercase font-semibold">{sub.frequency}</span>
              <span className="text-lg font-bold">${sub.amount?.toFixed(2)}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
