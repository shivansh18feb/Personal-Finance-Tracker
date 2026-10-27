import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { AddTransactionModal } from '../components/transactions/AddTransactionModal';
import {
  TrendingUp,
  TrendingDown,
  Wallet,
  PiggyBank,
  Plus,
  ArrowUpRight,
  ArrowDownRight,
  AlertCircle
} from 'lucide-react';
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Legend
} from 'recharts';

const COLORS = ['#10B981', '#3B82F6', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899', '#6366F1'];

export const Dashboard = () => {
  const [summary, setSummary] = useState(null);
  const [categoryData, setCategoryData] = useState([]);
  const [monthlyTrends, setMonthlyTrends] = useState([]);
  const [insights, setInsights] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const [sumRes, catRes, trendRes, insRes] = await Promise.all([
        api.get('/dashboard/summary'),
        api.get('/analytics/expenses-by-category'),
        api.get('/analytics/monthly-trends'),
        api.get('/analytics/financial-insights'),
      ]);

      setSummary(sumRes.data);
      setCategoryData(catRes.data);
      setMonthlyTrends(trendRes.data);
      setInsights(insRes.data);
    } catch (err) {
      console.error('Failed to load dashboard metrics', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  if (loading && !summary) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-10 h-10 border-4 border-emerald-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  const currency = summary?.currency || 'USD';

  return (
    <div className="space-y-6">
      {/* Top Banner with Quick Action */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Financial Overview</h1>
          <p className="text-sm text-slate-500">Track and manage your monthly income, budget, and expenses.</p>
        </div>
        <button
          onClick={() => setIsModalOpen(true)}
          className="flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2 rounded-lg font-medium shadow transition-colors"
        >
          <Plus className="w-4 h-4" /> Add Transaction
        </button>
      </div>

      {/* KPI Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 shadow-sm">
          <div className="flex justify-between items-start">
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Total Income</p>
            <span className="p-2 bg-emerald-50 dark:bg-emerald-950/50 text-emerald-600 rounded-lg">
              <TrendingUp className="w-4 h-4" />
            </span>
          </div>
          <h3 className="text-2xl font-bold mt-2 text-emerald-600">
            {currency} {summary?.totalIncome?.toFixed(2) || '0.00'}
          </h3>
          <p className="text-xs text-slate-400 mt-1">This current month</p>
        </div>

        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 shadow-sm">
          <div className="flex justify-between items-start">
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Total Expenses</p>
            <span className="p-2 bg-rose-50 dark:bg-rose-950/50 text-rose-600 rounded-lg">
              <TrendingDown className="w-4 h-4" />
            </span>
          </div>
          <h3 className="text-2xl font-bold mt-2 text-rose-600">
            {currency} {summary?.totalExpenses?.toFixed(2) || '0.00'}
          </h3>
          <p className="text-xs text-slate-400 mt-1">This current month</p>
        </div>

        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 shadow-sm">
          <div className="flex justify-between items-start">
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Net Balance</p>
            <span className="p-2 bg-blue-50 dark:bg-blue-950/50 text-blue-600 rounded-lg">
              <Wallet className="w-4 h-4" />
            </span>
          </div>
          <h3 className={`text-2xl font-bold mt-2 ${summary?.netBalance >= 0 ? 'text-slate-900 dark:text-slate-100' : 'text-rose-600'}`}>
            {currency} {summary?.netBalance?.toFixed(2) || '0.00'}
          </h3>
          <p className="text-xs text-slate-400 mt-1">Savings Rate: {summary?.savingsRate?.toFixed(1) || '0'}%</p>
        </div>

        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 shadow-sm">
          <div className="flex justify-between items-start">
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Remaining Budget</p>
            <span className="p-2 bg-amber-50 dark:bg-amber-950/50 text-amber-600 rounded-lg">
              <PiggyBank className="w-4 h-4" />
            </span>
          </div>
          <h3 className="text-2xl font-bold mt-2 text-slate-900 dark:text-slate-100">
            {currency} {summary?.remainingBudget?.toFixed(2) || '0.00'}
          </h3>
          <p className="text-xs text-slate-400 mt-1">
            {summary?.budgetUsedPercentage ? `${summary.budgetUsedPercentage.toFixed(1)}% utilized` : 'No overall limit set'}
          </p>
        </div>
      </div>

      {/* Smart Insights Alert */}
      {insights.length > 0 && (
        <div className="space-y-2">
          {insights.map((ins, idx) => (
            <div
              key={idx}
              className={`p-4 rounded-xl border flex items-start gap-3 ${
                ins.type === 'ALERT'
                  ? 'bg-rose-50 border-rose-200 text-rose-900 dark:bg-rose-950/30 dark:border-rose-900'
                  : ins.type === 'WARNING'
                  ? 'bg-amber-50 border-amber-200 text-amber-900 dark:bg-amber-950/30 dark:border-amber-900'
                  : 'bg-emerald-50 border-emerald-200 text-emerald-900 dark:bg-emerald-950/30 dark:border-emerald-900'
              }`}
            >
              <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold text-sm">{ins.title}</p>
                <p className="text-xs mt-0.5 opacity-90">{ins.explanation}</p>
                {ins.action && <p className="text-xs font-medium mt-1 underline cursor-pointer">{ins.action}</p>}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Interactive Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Category Expenses Donut */}
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 shadow-sm">
          <h3 className="text-base font-bold mb-4">Expenses by Category</h3>
          <div className="h-64">
            {categoryData.length === 0 ? (
              <div className="flex items-center justify-center h-full text-sm text-slate-400">
                No expense data for this month
              </div>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={categoryData}
                    dataKey="amount"
                    nameKey="category"
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={85}
                    paddingAngle={4}
                  >
                    {categoryData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value) => `${currency} ${Number(value).toFixed(2)}`} />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>

        {/* 12-Month Trends Bar Chart */}
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 shadow-sm">
          <h3 className="text-base font-bold mb-4">Income vs Expense Trend</h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={monthlyTrends}>
                <CartesianGrid strokeDasharray="3 3" opacity={0.2} />
                <XAxis dataKey="monthName" />
                <YAxis />
                <Tooltip formatter={(value) => `${currency} ${Number(value).toFixed(2)}`} />
                <Legend />
                <Bar dataKey="income" fill="#10B981" radius={[4, 4, 0, 0]} />
                <Bar dataKey="expense" fill="#EF4444" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Recent Transactions List */}
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 shadow-sm">
        <h3 className="text-base font-bold mb-4">Recent Transactions</h3>
        {summary?.recentTransactions?.length === 0 ? (
          <p className="text-sm text-slate-400 py-6 text-center">No recent transactions recorded.</p>
        ) : (
          <div className="divide-y divide-slate-100 dark:divide-slate-800">
            {summary?.recentTransactions?.map((tx) => (
              <div key={tx.id} className="py-3 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className={`p-2 rounded-lg ${
                    tx.type === 'INCOME' ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'
                  }`}>
                    {tx.type === 'INCOME' ? <ArrowDownRight className="w-4 h-4" /> : <ArrowUpRight className="w-4 h-4" />}
                  </div>
                  <div>
                    <p className="text-sm font-medium">{tx.description || tx.category}</p>
                    <p className="text-xs text-slate-400">{tx.category} • {tx.transactionDate}</p>
                  </div>
                </div>
                <div className={`text-sm font-semibold ${
                  tx.type === 'INCOME' ? 'text-emerald-600' : 'text-slate-900 dark:text-slate-100'
                }`}>
                  {tx.type === 'INCOME' ? '+' : '-'}{currency} {tx.amount.toFixed(2)}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <AddTransactionModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={fetchDashboardData}
      />
    </div>
  );
};
