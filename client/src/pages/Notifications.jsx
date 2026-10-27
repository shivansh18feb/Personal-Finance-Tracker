import React, { useState, useEffect } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import { Bell, CheckCheck } from 'lucide-react';

export const Notifications = () => {
  const [notifications, setNotifications] = useState([]);

  const fetchNotifications = async () => {
    try {
      const res = await api.get('/notifications');
      setNotifications(res.data);
    } catch (err) {
      console.error('Failed to load notifications');
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  const handleMarkAllRead = async () => {
    try {
      await api.post('/notifications/mark-all-read');
      toast.success('Marked all as read');
      fetchNotifications();
    } catch (err) {
      toast.error('Failed');
    }
  };

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">System & Budget Alerts</h1>
          <p className="text-sm text-slate-500">In-app notifications for exceeding budgets and recurring bills.</p>
        </div>
        <button
          onClick={handleMarkAllRead}
          className="flex items-center gap-2 text-xs font-semibold px-3 py-1.5 border rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800"
        >
          <CheckCheck className="w-4 h-4" /> Mark All Read
        </button>
      </div>

      <div className="bg-white dark:bg-slate-900 border rounded-xl divide-y border-slate-200 dark:border-slate-800 divide-slate-100 dark:divide-slate-800">
        {notifications.length === 0 ? (
          <div className="p-12 text-center text-slate-400 text-sm">
            <Bell className="w-8 h-8 mx-auto mb-2 opacity-40" />
            No new notifications. You are all caught up!
          </div>
        ) : (
          notifications.map((n) => (
            <div key={n.id} className={`p-4 flex items-start gap-3 ${!n.read ? 'bg-emerald-50/20' : ''}`}>
              <div className="p-2 rounded-lg bg-emerald-100 text-emerald-700">
                <Bell className="w-4 h-4" />
              </div>
              <div className="flex-1">
                <h4 className="font-semibold text-sm">{n.title}</h4>
                <p className="text-xs text-slate-500 mt-0.5">{n.message}</p>
                <span className="text-[10px] text-slate-400 mt-2 block">{n.createdAt}</span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
