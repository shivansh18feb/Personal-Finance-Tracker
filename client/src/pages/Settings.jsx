import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import toast from 'react-hot-toast';
import { Shield, Trash2, KeyRound } from 'lucide-react';

export const Settings = () => {
  const { user, updateProfile, logout } = useAuth();
  const [fullName, setFullName] = useState(user?.fullName || '');
  const [preferredCurrency, setPreferredCurrency] = useState(user?.preferredCurrency || 'USD');
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    try {
      await updateProfile({ fullName, preferredCurrency });
    } catch (err) {
      // toast shown in context
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    try {
      await api.post('/auth/change-password', { oldPassword, newPassword });
      toast.success('Password changed successfully');
      setOldPassword('');
      setNewPassword('');
    } catch (err) {
      toast.error('Failed to update password');
    }
  };

  const handleDeleteAccount = async () => {
    if (!window.confirm('WARNING: Are you absolutely sure? This will delete all your financial records permanently.')) return;
    try {
      await api.delete('/auth/account');
      toast.success('Account deleted.');
      logout();
    } catch (err) {
      toast.error('Could not delete account');
    }
  };

  return (
    <div className="space-y-6 max-w-3xl">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Account & Preferences</h1>
        <p className="text-sm text-slate-500">Manage profile data, currency preferences, and security credentials.</p>
      </div>

      {/* Profile Form */}
      <div className="bg-white dark:bg-slate-900 border rounded-xl p-5 shadow-sm space-y-4">
        <h2 className="font-bold text-base">User Details</h2>
        <form onSubmit={handleUpdateProfile} className="space-y-3">
          <div>
            <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Full Name</label>
            <input
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              className="w-full px-3 py-2 border rounded-lg bg-transparent border-slate-200 dark:border-slate-800"
            />
          </div>
          <div>
            <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Display Currency</label>
            <select
              value={preferredCurrency}
              onChange={(e) => setPreferredCurrency(e.target.value)}
              className="w-full px-3 py-2 border rounded-lg bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800"
            >
              <option value="USD">USD ($)</option>
              <option value="EUR">EUR (€)</option>
              <option value="GBP">GBP (£)</option>
              <option value="INR">INR (₹)</option>
              <option value="CAD">CAD ($)</option>
              <option value="AUD">AUD ($)</option>
            </select>
          </div>
          <button type="submit" className="px-4 py-2 text-sm bg-emerald-600 text-white rounded-lg">
            Save Preferences
          </button>
        </form>
      </div>

      {/* Security Form */}
      <div className="bg-white dark:bg-slate-900 border rounded-xl p-5 shadow-sm space-y-4">
        <div className="flex items-center gap-2">
          <KeyRound className="w-5 h-5 text-emerald-600" />
          <h2 className="font-bold text-base">Security & Password</h2>
        </div>
        <form onSubmit={handleChangePassword} className="space-y-3">
          <div>
            <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Current Password</label>
            <input
              type="password"
              value={oldPassword}
              onChange={(e) => setOldPassword(e.target.value)}
              required
              className="w-full px-3 py-2 border rounded-lg bg-transparent border-slate-200 dark:border-slate-800"
            />
          </div>
          <div>
            <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">New Password</label>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
              className="w-full px-3 py-2 border rounded-lg bg-transparent border-slate-200 dark:border-slate-800"
            />
          </div>
          <button type="submit" className="px-4 py-2 text-sm bg-slate-800 text-white dark:bg-slate-100 dark:text-slate-900 rounded-lg">
            Update Password
          </button>
        </form>
      </div>

      {/* Danger Zone */}
      <div className="bg-rose-50/40 dark:bg-rose-950/20 border border-rose-200 dark:border-rose-900 rounded-xl p-5 space-y-3">
        <h2 className="font-bold text-base text-rose-700 dark:text-rose-400">Danger Zone</h2>
        <p className="text-xs text-rose-600 dark:text-rose-300">
          Permanently delete your account along with all associated transactions, goals, and history.
        </p>
        <button
          onClick={handleDeleteAccount}
          className="flex items-center gap-2 px-4 py-2 bg-rose-600 text-white text-sm font-semibold rounded-lg hover:bg-rose-700"
        >
          <Trash2 className="w-4 h-4" /> Delete Account
        </button>
      </div>
    </div>
  );
};
