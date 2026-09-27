import React from "react";
import { useAuth } from "../../auth/AuthContext";
import { Bot, Calendar, FileText, Activity, Clock, ShieldAlert, CheckCircle2 } from "lucide-react";
import { Link } from "react-router-dom";
import { MOCK_APPOINTMENTS } from "../../mocks/mockAppointments";

export const PatientDashboard = () => {
  const { user } = useAuth();

  return (
    <div className="space-y-6">
      <div className="bg-gradient-to-r from-slate-900 via-slate-900 to-cyan-950/40 p-6 rounded-2xl border border-slate-800 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white">Xin chào, {user?.name}! 👋</h1>
          <p className="text-slate-400 text-sm mt-1">
            Mã định danh: <span className="font-mono text-cyan-400">{user?.medicalCode || "PAT-88291"}</span> | Trạng thái AI Triage: <span className="text-emerald-400 font-medium">Bình thường</span>
          </p>
        </div>
        <Link
          to="/patient/ai-chat"
          className="px-4 py-2.5 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-sm flex items-center gap-2 shadow-lg shadow-cyan-500/20 transition"
        >
          <Bot className="w-4 h-4" />
          Tư Vấn AI Triage Ngay
        </Link>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        <div className="bg-slate-900 p-5 rounded-2xl border border-slate-800 flex items-center gap-4">
          <div className="p-3 bg-cyan-500/10 rounded-xl text-cyan-400 border border-cyan-500/20">
            <Calendar className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs text-slate-400">Lịch hẹn sắp tới</p>
            <p className="text-lg font-bold text-white">09:30 AM - Hôm nay</p>
          </div>
        </div>

        <div className="bg-slate-900 p-5 rounded-2xl border border-slate-800 flex items-center gap-4">
          <div className="p-3 bg-emerald-500/10 rounded-xl text-emerald-400 border border-emerald-500/20">
            <FileText className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs text-slate-400">Hồ sơ bệnh án</p>
            <p className="text-lg font-bold text-white">2 Lần khám gần nhất</p>
          </div>
        </div>

        <div className="bg-slate-900 p-5 rounded-2xl border border-slate-800 flex items-center gap-4">
          <div className="p-3 bg-purple-500/10 rounded-xl text-purple-400 border border-purple-500/20">
            <Activity className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs text-slate-400">Chỉ số AI Risk</p>
            <p className="text-lg font-bold text-emerald-400">Nguy cơ Thấp (LOW)</p>
          </div>
        </div>
      </div>

      <div className="bg-slate-900 rounded-2xl border border-slate-800 p-6">
        <h3 className="text-base font-bold text-white mb-4 flex items-center gap-2">
          <Clock className="w-5 h-5 text-cyan-400" />
          Lịch Khám Đã Đặt
        </h3>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-300">
            <thead className="bg-slate-950 text-xs uppercase text-slate-400">
              <tr>
                <th className="p-3 rounded-l-lg">Mã Lịch</th>
                <th className="p-3">Bác Sĩ</th>
                <th className="p-3">Chuyên Khoa</th>
                <th className="p-3">Thời Gian</th>
                <th className="p-3 rounded-r-lg">Trạng Thái</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {MOCK_APPOINTMENTS.map((apt) => (
                <tr key={apt.id} className="hover:bg-slate-800/40">
                  <td className="p-3 font-mono text-cyan-400">{apt.id}</td>
                  <td className="p-3 font-medium text-white">{apt.doctorName}</td>
                  <td className="p-3">{apt.specialty}</td>
                  <td className="p-3">{apt.time} ({apt.date})</td>
                  <td className="p-3">
                    <span className="px-2.5 py-1 rounded-full text-xs bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                      {apt.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};