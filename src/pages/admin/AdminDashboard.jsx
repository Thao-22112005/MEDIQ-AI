import React from "react";
import { Users, Stethoscope, Calendar, Activity, Database, Server } from "lucide-react";

export const AdminDashboard = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-white">Bảng Điều Khiển Quản Trị Hệ Thống</h1>

      <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
        <div className="bg-slate-900 p-4 rounded-2xl border border-slate-800">
          <p className="text-xs text-slate-400">Tổng Người Dùng</p>
          <p className="text-xl font-bold text-white mt-1">1,248</p>
        </div>
        <div className="bg-slate-900 p-4 rounded-2xl border border-slate-800">
          <p className="text-xs text-slate-400">Bác Sĩ Active</p>
          <p className="text-xl font-bold text-cyan-400 mt-1">32</p>
        </div>
        <div className="bg-slate-900 p-4 rounded-2xl border border-slate-800">
          <p className="text-xs text-slate-400">Lịch Khám Hôm Nay</p>
          <p className="text-xl font-bold text-emerald-400 mt-1">86</p>
        </div>
        <div className="bg-slate-900 p-4 rounded-2xl border border-slate-800">
          <p className="text-xs text-slate-400">Microservices Health</p>
          <p className="text-xl font-bold text-purple-400 mt-1">100% OK</p>
        </div>
      </div>

      <div className="bg-slate-900 p-5 rounded-2xl border border-slate-800">
        <h3 className="text-sm font-bold text-white mb-3">Trạng Thái Kết Nối Các Services</h3>
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 text-xs">
          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 flex items-center justify-between">
            <span>Auth Service</span>
            <span className="text-emerald-400">● Online</span>
          </div>
          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 flex items-center justify-between">
            <span>Patient Service</span>
            <span className="text-emerald-400">● Online</span>
          </div>
          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 flex items-center justify-between">
            <span>Doctor Service</span>
            <span className="text-emerald-400">● Online</span>
          </div>
          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 flex items-center justify-between">
            <span>Appointment Service</span>
            <span className="text-emerald-400">● Online</span>
          </div>
          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 flex items-center justify-between">
            <span>Medical Record Service</span>
            <span className="text-emerald-400">● Online</span>
          </div>
          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 flex items-center justify-between">
            <span>AI Triage Service</span>
            <span className="text-emerald-400">● Online</span>
          </div>
        </div>
      </div>
    </div>
  );
};