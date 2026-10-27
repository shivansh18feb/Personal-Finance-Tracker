import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { format, startOfMonth, endOfMonth, eachDayOfInterval, isSameDay } from 'date-fns';
import { ChevronLeft, ChevronRight } from 'lucide-react';

export const CalendarView = () => {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [transactions, setTransactions] = useState([]);
  const [selectedDate, setSelectedDate] = useState(new Date());

  const monthStart = startOfMonth(currentDate);
  const monthEnd = endOfMonth(currentDate);
  const daysInMonth = eachDayOfInterval({ start: monthStart, end: monthEnd });

  useEffect(() => {
    const fetchMonthTransactions = async () => {
      try {
        const res = await api.get('/transactions', {
          params: {
            startDate: format(monthStart, 'yyyy-MM-dd'),
            endDate: format(monthEnd, 'yyyy-MM-dd'),
            size: 100,
          }
        });
        setTransactions(res.data.content);
      } catch (err) {
        console.error('Failed to load calendar data', err);
      }
    };
    fetchMonthTransactions();
  }, [currentDate]);

  const selectedDateTransactions = transactions.filter(t =>
    isSameDay(new Date(t.transactionDate), selectedDate)
  );

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Financial Calendar</h1>
          <p className="text-sm text-slate-500">Day-by-day visualization of transaction activity.</p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => setCurrentDate(new Date(currentDate.setMonth(currentDate.getMonth() - 1)))}
            className="p-2 border rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800"
          >
            <ChevronLeft className="w-4 h-4" />
          </button>
          <span className="font-bold text-sm min-w-28 text-center">
            {format(currentDate, 'MMMM yyyy')}
          </span>
          <button
            onClick={() => setCurrentDate(new Date(currentDate.setMonth(currentDate.getMonth() + 1)))}
            className="p-2 border rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800"
          >
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Calendar Grid */}
        <div className="lg:col-span-2 bg-white dark:bg-slate-900 border rounded-xl p-4 shadow-sm">
          <div className="grid grid-cols-7 gap-2 text-center text-xs font-semibold text-slate-400 pb-2">
            <div>Sun</div><div>Mon</div><div>Tue</div><div>Wed</div><div>Thu</div><div>Fri</div><div>Sat</div>
          </div>
          <div className="grid grid-cols-7 gap-2">
            {daysInMonth.map((day) => {
              const dayStr = format(day, 'yyyy-MM-dd');
              const dayTx = transactions.filter(t => t.transactionDate === dayStr);
              const isSelected = isSameDay(day, selectedDate);

              return (
                <div
                  key={day.toISOString()}
                  onClick={() => setSelectedDate(day)}
                  className={`min-h-20 p-2 rounded-lg border text-left cursor-pointer transition-colors ${
                    isSelected
                      ? 'border-emerald-500 bg-emerald-50/30 dark:bg-emerald-950/20'
                      : 'border-slate-100 dark:border-slate-800 hover:border-slate-300'
                  }`}
                >
                  <span className="text-xs font-bold block">{format(day, 'd')}</span>
                  {dayTx.length > 0 && (
                    <div className="mt-1 space-y-0.5">
                      <div className="text-[10px] font-medium text-emerald-600 truncate">
                        +{dayTx.filter(t => t.type === 'INCOME').reduce((a, b) => a + b.amount, 0).toFixed(0)}
                      </div>
                      <div className="text-[10px] font-medium text-rose-600 truncate">
                        -{dayTx.filter(t => t.type === 'EXPENSE').reduce((a, b) => a + b.amount, 0).toFixed(0)}
                      </div>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>

        {/* Selected Day Details */}
        <div className="bg-white dark:bg-slate-900 border rounded-xl p-5 shadow-sm space-y-4">
          <h3 className="font-bold text-base">
            Activity for {format(selectedDate, 'MMM d, yyyy')}
          </h3>
          {selectedDateTransactions.length === 0 ? (
            <p className="text-xs text-slate-400 py-6 text-center">No transactions on this date.</p>
          ) : (
            <div className="divide-y divide-slate-100 dark:divide-slate-800">
              {selectedDateTransactions.map(tx => (
                <div key={tx.id} className="py-2.5 flex justify-between items-center text-sm">
                  <div>
                    <p className="font-medium text-xs">{tx.description || tx.category}</p>
                    <span className="text-[10px] text-slate-400">{tx.category}</span>
                  </div>
                  <span className={`font-semibold text-xs ${tx.type === 'INCOME' ? 'text-emerald-600' : 'text-rose-600'}`}>
                    {tx.type === 'INCOME' ? '+' : '-'}${tx.amount.toFixed(2)}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
