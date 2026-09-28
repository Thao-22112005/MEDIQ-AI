import React from "react";

export const AdminDashboard = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-slate-900">Bảng Điều Khiển Quản Trị Hệ Thống</h1>

      <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
        <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm">
          <p className="text-xs text-slate-500 font-medium">Tổng Người Dùng</p>
          <p className="text-xl font-bold text-slate-900 mt-1">1,248</p>
        </div>
        <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm">
          <p className="text-xs text-slate-500 font-medium">Bác Sĩ Active</p>
          <p className="text-xl font-bold text-cyan-700 mt-1">32</p>
        </div>
        <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm">
          <p className="text-xs text-slate-500 font-medium">Lịch Khám Hôm Nay</p>
          <p className="text-xl font-bold text-emerald-700 mt-1">86</p>
        </div>
        <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm">
          <p className="text-xs text-slate-500 font-medium">Microservices Health</p>
          <p className="text-xl font-bold text-purple-700 mt-1">100% OK</p>
        </div>
      </div>

      <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
        <h3 className="text-sm font-bold text-slate-900 mb-3">Trạng Thái Kết Nối Các Services</h3>
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 text-xs">
          <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 flex items-center justify-between font-medium text-slate-700">
            <span>Auth Service</span>
            <span className="text-emerald-700 font-bold">● Online</span>
          </div>
          <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 flex items-center justify-between font-medium text-slate-700">
            <span>Patient Service</span>
            <span className="text-emerald-700 font-bold">● Online</span>
          </div>
          <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 flex items-center justify-between font-medium text-slate-700">
            <span>Doctor Service</span>
            <span className="text-emerald-700 font-bold">● Online</span>
          </div>
          <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 flex items-center justify-between font-medium text-slate-700">
            <span>Appointment Service</span>
            <span className="text-emerald-700 font-bold">● Online</span>
          </div>
          <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 flex items-center justify-between font-medium text-slate-700">
            <span>Medical Record Service</span>
            <span className="text-emerald-700 font-bold">● Online</span>
          </div>
          <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 flex items-center justify-between font-medium text-slate-700">
            <span>AI Triage Service</span>
            <span className="text-emerald-700 font-bold">● Online</span>
          </div>
        </div>
      </div>
    </div>
  );
};