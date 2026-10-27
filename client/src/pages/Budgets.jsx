import React, { useState, useEffect } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import { Plus, Trash2, AlertTriangle, CheckCircle, PieChart } from 'lucide-react';

export const Budgets = () => {
  const [budgets, setBudgets] = useState([]);
  const [month, setMonth] = useState(new Date().getMonth() + 1);
  const [year, setYear] = useState(new Date().getFullYear());
  const [loading, setLoading] = useState(false);
  const [showAddForm, setShowAddForm] = useState(false);

  const [category, setCategory] = useState('OVERALL');
  const [amount, setAmount] = useState('');
  const [threshold, setThreshold] = useState(80);

  const fetchBudgets = async () => {
    try {
      setLoading(true);
      const res = await api.get('/budgets', { params: { month, year } });
      setBudgets(res.data);
    } catch (err) {
      toast.error('Failed to load budgets');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBudgets();
  }, [month, year]);

  const handleSaveBudget = async (e) => {
    e.preventDefault();
    try {
      await api.post('/budgets', {
        category,
        amount: Number(amount),
        month: Number(month),
        year: Number(year),
        warningThreshold: Number(threshold),
      });
      toast.success('Budget saved successfully');
      setShowAddForm(false);
      setAmount('');
      fetchBudgets();
    } catch (err) {
      toast.error('Failed to save budget');
    }
  };

  const handleDelete = async (id) => {
    try {
      await api.delete(`/budgets/${id}`);
      toast.success('Budget removed');
      fetchBudgets();
    } catch (err) {
      toast.error('Could not delete budget');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Budgets & Limits</h1>
          <p className="text-sm text-slate-500">Configure overall and category monthly spending thresholds.</p>
        </div>
        <button
          onClick={() => setShowAddForm(!showAddForm)}
          className="flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2 rounded-lg font-medium shadow transition-colors"
        >
          <Plus className="w-4 h-4" /> Set Budget Limit
        </button>
      </div>

      {showAddForm && (
        <form onSubmit={handleSaveBudget} className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
          <h2 className="font-bold text-base">Configure Monthly Budget</h2>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Target Category</label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full px-3 py-2 border rounded-lg bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800"
              >
                <option value="OVERALL">OVERALL (Total Monthly)</option>
                <option value="Food">Food</option>
                <option value="Rent">Rent</option>
                <option value="Transportation">Transportation</option>
                <option value="Entertainment">Entertainment</option>
                <option value="Utilities">Utilities</option>
                <option value="Shopping">Shopping</option>
                <option value="Subscriptions">Subscriptions</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Budget Limit ($)</label>
              <input
                type="number"
                step="0.01"
                placeholder="1000.00"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                required
                className="w-full px-3 py-2 border rounded-lg bg-transparent border-slate-200 dark:border-slate-800"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Alert Warning Threshold (%)</label>
              <input
                type="number"
                value={threshold}
                onChange={(e) => setThreshold(e.target.value)}
                className="w-full px-3 py-2 border rounded-lg bg-transparent border-slate-200 dark:border-slate-800"
              />
            </div>
          </div>
          <div className="flex justify-end gap-2">
            <button
              type="button"
              onClick={() => setShowAddForm(false)}
              className="px-4 py-2 text-sm border rounded-lg"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 text-sm text-white bg-emerald-600 rounded-lg hover:bg-emerald-700"
            >
              Save Budget
            </button>
          </div>
        </form>
      )}

      {/* Budgets Progress Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {budgets.map((b) => (
          <div
            key={b.id}
            className={`bg-white dark:bg-slate-900 border rounded-xl p-5 shadow-sm space-y-3 ${
              b.isExceeded
                ? 'border-rose-300 dark:border-rose-900'
                : b.isWarningTriggered
                ? 'border-amber-300 dark:border-amber-900'
                : 'border-slate-200 dark:border-slate-800'
            }`}
          >
            <div className="flex justify-between items-start">
              <div>
                <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">
                  {b.category === 'OVERALL' ? 'Total Monthly Budget' : 'Category Budget'}
                </span>
                <h3 className="text-lg font-bold">{b.category}</h3>
              </div>
              <button
                onClick={() => handleDelete(b.id)}
                className="text-slate-400 hover:text-rose-600 p-1"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            </div>

            <div className="flex justify-between items-baseline text-sm">
              <span className="text-slate-500">
                Spent: <strong className="text-slate-900 dark:text-slate-100">${b.spentAmount.toFixed(2)}</strong>
              </span>
              <span className="text-slate-500">
                Limit: <strong>${b.budgetAmount.toFixed(2)}</strong>
              </span>
            </div>

            {/* Visual Progress Bar */}
            <div className="w-full h-3 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
              <div
                className={`h-full transition-all duration-300 ${
                  b.isExceeded
                    ? 'bg-rose-500'
                    : b.isWarningTriggered
                    ? 'bg-amber-500'
                    : 'bg-emerald-500'
                }`}
                style={{ width: `${Math.min(b.percentageUsed, 100)}%` }}
              ></div>
            </div>

            <div className="flex justify-between items-center text-xs">
              <span className="font-semibold text-slate-600 dark:text-slate-400">
                {b.percentageUsed.toFixed(1)}% utilized
              </span>
              <div className="flex items-center gap-1">
                {b.isExceeded ? (
                  <span className="flex items-center gap-1 text-rose-600 font-semibold">
                    <AlertTriangle className="w-3.5 h-3.5" /> Exceeded by ${(b.spentAmount - b.budgetAmount).toFixed(2)}
                  </span>
                ) : (
                  <span className="flex items-center gap-1 text-emerald-600 font-medium">
                    <CheckCircle className="w-3.5 h-3.5" /> ${b.remainingAmount.toFixed(2)} remaining
                  </span>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
