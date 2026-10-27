import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import api from '../../services/api';
import toast from 'react-hot-toast';
import { X } from 'lucide-react';

const transactionSchema = z.object({
  type: z.enum(['INCOME', 'EXPENSE']),
  amount: z.coerce.number().positive('Amount must be positive'),
  category: z.string().min(1, 'Category is required'),
  description: z.string().optional(),
  transactionDate: z.string().min(1, 'Date is required'),
  merchant: z.string().optional(),
  paymentMethod: z.string().optional(),
});

const INCOME_CATEGORIES = [
  'Salary', 'Freelance', 'Business', 'Investment income', 'Interest', 'Gifts', 'Refunds', 'Other income'
];

const EXPENSE_CATEGORIES = [
  'Food', 'Rent', 'Transportation', 'Shopping', 'Healthcare', 'Education', 'Utilities',
  'Entertainment', 'Travel', 'Subscriptions', 'Insurance', 'Loan payments', 'Taxes', 'Other expenses'
];

export const AddTransactionModal = ({ isOpen, onClose, onSuccess, initialData = null }) => {
  const [loading, setLoading] = useState(false);
  const isEditing = !!initialData;

  const { register, handleSubmit, watch, formState: { errors } } = useForm({
    resolver: zodResolver(transactionSchema),
    defaultValues: initialData || {
      type: 'EXPENSE',
      amount: '',
      category: 'Food',
      description: '',
      transactionDate: new Date().toISOString().split('T')[0],
      merchant: '',
      paymentMethod: 'Credit Card',
    }
  });

  const selectedType = watch('type');
  const availableCategories = selectedType === 'INCOME' ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;

  if (!isOpen) return null;

  const onSubmit = async (data) => {
    try {
      setLoading(true);
      if (isEditing) {
        await api.put(`/transactions/${initialData.id}`, data);
        toast.success('Transaction updated');
      } else {
        await api.post('/transactions', data);
        toast.success('Transaction added');
      }
      onSuccess();
      onClose();
    } catch (err) {
      toast.error('Failed to save transaction');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4">
      <div className="bg-white dark:bg-slate-900 rounded-xl shadow-2xl max-w-md w-full p-6 relative">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
        >
          <X className="w-5 h-5" />
        </button>

        <h2 className="text-xl font-bold mb-4">
          {isEditing ? 'Edit Transaction' : 'Add New Transaction'}
        </h2>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Type</label>
            <div className="grid grid-cols-2 gap-2">
              <label className={`cursor-pointer border rounded-lg p-2.5 text-center font-medium text-sm transition-all ${
                selectedType === 'EXPENSE'
                  ? 'border-rose-500 bg-rose-50 text-rose-700 dark:bg-rose-950/40 dark:text-rose-300'
                  : 'border-slate-200 dark:border-slate-800'
              }`}>
                <input type="radio" value="EXPENSE" {...register('type')} className="sr-only" />
                Expense
              </label>
              <label className={`cursor-pointer border rounded-lg p-2.5 text-center font-medium text-sm transition-all ${
                selectedType === 'INCOME'
                  ? 'border-emerald-500 bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300'
                  : 'border-slate-200 dark:border-slate-800'
              }`}>
                <input type="radio" value="INCOME" {...register('type')} className="sr-only" />
                Income
              </label>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Amount</label>
            <input
              type="number"
              step="0.01"
              placeholder="0.00"
              {...register('amount')}
              className="w-full px-3 py-2 border rounded-lg bg-transparent border-slate-200 dark:border-slate-800 focus:outline-none focus:ring-2 focus:ring-emerald-500"
            />
            {errors.amount && <p className="text-rose-500 text-xs mt-1">{errors.amount.message}</p>}
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Category</label>
              <select
                {...register('category')}
                className="w-full px-3 py-2 border rounded-lg bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800 focus:outline-none focus:ring-2 focus:ring-emerald-500"
              >
                {availableCategories.map(cat => (
                  <option key={cat} value={cat}>{cat}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Date</label>
              <input
                type="date"
                {...register('transactionDate')}
                className="w-full px-3 py-2 border rounded-lg bg-transparent border-slate-200 dark:border-slate-800 focus:outline-none focus:ring-2 focus:ring-emerald-500"
              >
              </input>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Description</label>
            <input
              type="text"
              placeholder="e.g. Groceries at Whole Foods"
              {...register('description')}
              className="w-full px-3 py-2 border rounded-lg bg-transparent border-slate-200 dark:border-slate-800 focus:outline-none focus:ring-2 focus:ring-emerald-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Merchant / Payee</label>
              <input
                type="text"
                placeholder="Optional"
                {...register('merchant')}
                className="w-full px-3 py-2 border rounded-lg bg-transparent border-slate-200 dark:border-slate-800"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">Payment Method</label>
              <select
                {...register('paymentMethod')}
                className="w-full px-3 py-2 border rounded-lg bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800"
              >
                <option value="Credit Card">Credit Card</option>
                <option value="Debit Card">Debit Card</option>
                <option value="Cash">Cash</option>
                <option value="Bank Transfer">Bank Transfer</option>
                <option value="UPI">UPI</option>
              </select>
            </div>
          </div>

          <div className="pt-2 flex justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium border border-slate-200 dark:border-slate-800 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 text-sm font-medium text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg shadow disabled:opacity-50"
            >
              {loading ? 'Saving...' : isEditing ? 'Update Transaction' : 'Add Transaction'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
