import React, { useState } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import { Download, Upload, FileText } from 'lucide-react';

export const Reports = () => {
  const [month, setMonth] = useState(new Date().getMonth() + 1);
  const [year, setYear] = useState(new Date().getFullYear());
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);

  const handleDownloadCsv = async () => {
    try {
      const response = await api.get('/reports/export/csv', { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `transactions_${new Date().toISOString().slice(0, 10)}.csv`);
      document.body.appendChild(link);
      link.click();
      toast.success('CSV downloaded');
    } catch (err) {
      toast.error('Failed to export CSV');
    }
  };

  const handleDownloadPdf = async () => {
    try {
      const response = await api.get('/reports/export/pdf', {
        params: { month, year },
        responseType: 'blob'
      });
      const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `statement_${year}_${month}.pdf`);
      document.body.appendChild(link);
      link.click();
      toast.success('Statement downloaded');
    } catch (err) {
      toast.error('Failed to export PDF');
    }
  };

  const handleImportCsv = async (e) => {
    e.preventDefault();
    if (!file) {
      toast.error('Please choose a CSV file');
      return;
    }
    const formData = new FormData();
    formData.append('file', file);

    try {
      setUploading(true);
      const res = await api.post('/reports/import/csv', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      toast.success(`Import complete! Processed: ${res.data.successful} records.`);
      setFile(null);
    } catch (err) {
      toast.error('CSV import failed');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Reports & Import / Export</h1>
        <p className="text-sm text-slate-500">Generate monthly PDF statements or synchronize records via CSV.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Export Card */}
        <div className="bg-white dark:bg-slate-900 border rounded-xl p-5 shadow-sm space-y-4">
          <div className="flex items-center gap-2">
            <Download className="w-5 h-5 text-emerald-600" />
            <h3 className="font-bold text-base">Export Financial Data</h3>
          </div>
          <p className="text-xs text-slate-500">
            Download your raw financial history in CSV format or generate a printable monthly statement.
          </p>

          <div className="pt-2 flex flex-col gap-3">
            <button
              onClick={handleDownloadCsv}
              className="flex items-center justify-center gap-2 py-2 px-4 border rounded-lg text-sm font-semibold hover:bg-slate-50 dark:hover:bg-slate-800"
            >
              <Download className="w-4 h-4" /> Export All Transactions (CSV)
            </button>

            <div className="flex gap-2">
              <select
                value={month}
                onChange={(e) => setMonth(Number(e.target.value))}
                className="w-1/2 px-3 py-2 text-sm border rounded-lg bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800"
              >
                {Array.from({ length: 12 }, (_, i) => (
                  <option key={i + 1} value={i + 1}>Month {i + 1}</option>
                ))}
              </select>
              <button
                onClick={handleDownloadPdf}
                className="w-1/2 flex items-center justify-center gap-2 py-2 px-4 bg-emerald-600 text-white rounded-lg text-sm font-semibold hover:bg-emerald-700"
              >
                <FileText className="w-4 h-4" /> Download PDF
              </button>
            </div>
          </div>
        </div>

        {/* Import Card */}
        <div className="bg-white dark:bg-slate-900 border rounded-xl p-5 shadow-sm space-y-4">
          <div className="flex items-center gap-2">
            <Upload className="w-5 h-5 text-blue-600" />
            <h3 className="font-bold text-base">Import Transactions via CSV</h3>
          </div>
          <p className="text-xs text-slate-500">
            Upload CSV with columns: <strong>Date, Type, Amount, Category, Description</strong>.
          </p>

          <form onSubmit={handleImportCsv} className="pt-2 space-y-3">
            <input
              type="file"
              accept=".csv"
              onChange={(e) => setFile(e.target.files[0])}
              className="w-full text-xs text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-xs file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
            />
            <button
              type="submit"
              disabled={uploading}
              className="w-full flex items-center justify-center gap-2 py-2 px-4 bg-blue-600 text-white rounded-lg text-sm font-semibold hover:bg-blue-700 disabled:opacity-50"
            >
              <Upload className="w-4 h-4" /> {uploading ? 'Processing...' : 'Upload & Import'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
