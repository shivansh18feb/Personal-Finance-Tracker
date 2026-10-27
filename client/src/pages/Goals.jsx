import React, { useState, useEffect } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import { Target, Plus, Trash2, ArrowUpRight } from 'lucide-react';

export const Goals = () => {
  const [goals, setGoals] = useState([]);
  const [showAdd, setShowAdd] = useState(false);
  const [name, setName] = useState('');
  const [targetAmount, setTargetAmount] = useState('');
  const [targetDate, setTargetDate] = useState('');
  const [description, setDescription] = useState('');

  const fetchGoals = async () => {
    try {
      const res = await api.get('/goals');
      setGoals(res.data);
    } catch (err) {
      toast.error('Failed to load goals');
    }
  };

  useEffect(() => {
    fetchGoals();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await api.post('/goals', {
        name,
        targetAmount: Number(targetAmount),
        targetDate,
        description,
      });
      toast.success('Savings goal established!');
      setShowAdd(false);
      setName('');
      setTargetAmount('');
      fetchGoals();
    } catch (err) {
      toast.error('Could not create goal');
    }
  };

  const handleContribute = async (id) => {
    const amountStr = prompt('Enter contribution amount ($):');
    if (!amountStr || isNaN(amountStr) || Number(amountStr) <= 0) return;

    try {
      await api.post(`/goals/${id}/contribute`, { amount: Number(amountStr) });
      toast.success('Contribution recorded!');
      fetchGoals();
    } catch (err) {
      toast.error('Failed to contribute');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this goal?')) return;
    try {
      await api.delete(`/goals/${id}`);
      toast.success('Goal deleted');
      fetchGoals();
    } catch (err) {
      toast.error('Could not delete');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Savings Goals</h1>
          <p className="text-sm text-slate-500">Plan and save for major purchases, emergency funds, and future milestones.</p>
        </div>
        <button
          onClick={() => setShowAdd(!showAdd)}
          className="flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2 rounded-lg font-medium shadow"
        >
          <Plus className="w-4 h-4" /> New Goal
        </button>
      </div>

      {showAdd && (
        <form onSubmit={handleCreate} className="bg-white dark:bg-slate-900 border rounded-xl p-5 shadow-sm space-y-4">
          <h2 className="font-bold text-base">Create Financial Goal</h2>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Goal Name</label>
              <input
                type="text"
                placeholder="e.g. Emergency Fund"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                className="w-full px-3 py-2 border rounded-lg bg-transparent border-slate-200 dark:border-slate-800"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Target Amount ($)</label>
              <input
                type="number"
                placeholder="5000"
                value={targetAmount}
                onChange={(e) => setTargetAmount(e.target.value)}
                required
                className="w-full px-3 py-2 border rounded-lg bg-transparent border-slate-200 dark:border-slate-800"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Target Date</label>
              <input
                type="date"
                value={targetDate}
                onChange={(e) => setTargetDate(e.target.value)}
                required
                className="w-full px-3 py-2 border rounded-lg bg-transparent border-slate-200 dark:border-slate-800"
              />
            </div>
          </div>
          <div className="flex justify-end gap-2">
            <button type="button" onClick={() => setShowAdd(false)} className="px-4 py-2 text-sm border rounded-lg">
              Cancel
            </button>
            <button type="submit" className="px-4 py-2 text-sm bg-emerald-600 text-white rounded-lg">
              Create Goal
            </button>
          </div>
        </form>
      )}

      {/* Goals Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {goals.map((goal) => {
          const pct = Math.min((goal.currentAmount / goal.targetAmount) * 100, 100);
          return (
            <div key={goal.id} className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 shadow-sm space-y-3">
              <div className="flex justify-between items-start">
                <div>
                  <h3 className="font-bold text-lg">{goal.name}</h3>
                  <p className="text-xs text-slate-400">Target: {goal.targetDate || 'No deadline'}</p>
                </div>
                <button onClick={() => handleDelete(goal.id)} className="text-slate-400 hover:text-rose-600">
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>

              <div className="flex justify-between items-baseline text-sm">
                <span className="text-emerald-600 font-bold">${goal.currentAmount?.toFixed(2) || '0.00'}</span>
                <span className="text-slate-400 text-xs">of ${goal.targetAmount?.toFixed(2)}</span>
              </div>

              <div className="w-full h-2.5 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                <div
                  className="h-full bg-emerald-500 transition-all duration-300"
                  style={{ width: `${pct}%` }}
                ></div>
              </div>

              <div className="flex justify-between items-center pt-2">
                <span className="text-xs font-semibold text-slate-500">{pct.toFixed(1)}% Completed</span>
                <button
                  onClick={() => handleContribute(goal.id)}
                  className="flex items-center gap-1 text-xs font-semibold bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 px-3 py-1.5 rounded-lg hover:bg-emerald-100"
                >
                  <ArrowUpRight className="w-3.5 h-3.5" /> Deposit
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
