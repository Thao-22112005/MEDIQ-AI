import React, { useState } from "react";
import { useAuth } from "../../auth/AuthContext";
import { Bot, Calendar, FileText, Activity, Clock, Search, Filter } from "lucide-react";
import { Link } from "react-router-dom";
import { MOCK_APPOINTMENTS } from "../../mocks/mockAppointments";

export const PatientDashboard = () => {
  const { user } = useAuth();
  
  // State lọc theo lịch & tìm kiếm
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedStatus, setSelectedStatus] = useState("ALL");

  // Filter danh sách lịch khám
  const filteredAppointments = MOCK_APPOINTMENTS.filter((apt) => {
    const matchesSearch =
      apt.doctorName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      apt.specialty.toLowerCase().includes(searchTerm.toLowerCase()) ||
      apt.id.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus = selectedStatus === "ALL" || apt.status === selectedStatus;

    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-6">
      {/* Banner Chào Mừng */}
      <div className="bg-gradient-to-r from-cyan-500 via-cyan-600 to-blue-600 p-6 rounded-2xl shadow-md text-white flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white">Xin chào, {user?.name}! 👋</h1>
          <p className="text-cyan-100 text-sm mt-1">
            Mã định danh: <span className="font-mono font-bold text-white">{user?.medicalCode || "PAT-88291"}</span> | Trạng thái AI Triage: <span className="text-emerald-200 font-bold">Bình thường</span>
          </p>
        </div>
        <Link
          to="/patient/ai-chat"
          className="px-4 py-2.5 rounded-xl bg-white hover:bg-cyan-50 text-cyan-700 font-bold text-sm flex items-center gap-2 shadow-sm transition"
        >
          <Bot className="w-4 h-4 text-cyan-600" />
          Tư Vấn AI Triage Ngay
        </Link>
      </div>

      {/* 3 Thẻ Chỉ Số */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-cyan-50 rounded-xl text-cyan-600 border border-cyan-100">
            <Calendar className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs text-slate-500 font-medium">Lịch hẹn sắp tới</p>
            <p className="text-lg font-bold text-slate-800">09:30 AM - Hôm nay</p>
          </div>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-emerald-50 rounded-xl text-emerald-600 border border-emerald-100">
            <FileText className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs text-slate-500 font-medium">Hồ sơ bệnh án</p>
            <p className="text-lg font-bold text-slate-800">2 Lần khám gần nhất</p>
          </div>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-purple-50 rounded-xl text-purple-600 border border-purple-100">
            <Activity className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs text-slate-500 font-medium">Chỉ số AI Risk</p>
            <p className="text-lg font-bold text-emerald-600">Nguy cơ Thấp (LOW)</p>
          </div>
        </div>
      </div>

      {/* Bảng Danh Sách Lịch Khám Đã Đặt + Chức năng Tra cứu Tìm kiếm */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <h3 className="text-base font-bold text-slate-800 flex items-center gap-2">
            <Clock className="w-5 h-5 text-cyan-600" />
            Lịch Khám Đã Đặt
          </h3>

          {/* Thanh lọc tìm kiếm nhanh */}
          <div className="flex flex-wrap items-center gap-2">
            <div className="relative flex-1 sm:w-64">
              <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Tìm mã lịch, tên bác sĩ..."
                className="w-full pl-9 pr-3 py-1.5 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-800 focus:outline-none focus:border-cyan-500 focus:bg-white"
              />
            </div>
            <select
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
              className="px-3 py-1.5 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-700 font-semibold focus:outline-none focus:border-cyan-500"
            >
              <option value="ALL">Tất cả trạng thái</option>
              <option value="Xác nhận">Xác nhận</option>
              <option value="Đang chờ">Đang chờ</option>
              <option value="Đã xong">Đã xong</option>
            </select>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-600">
            <thead className="bg-slate-50 text-xs uppercase text-slate-500 font-bold border-b border-slate-200">
              <tr>
                <th className="p-3.5 rounded-l-lg">Mã Lịch</th>
                <th className="p-3.5">Bác Sĩ</th>
                <th className="p-3.5">Chuyên Khoa</th>
                <th className="p-3.5">Thời Gian</th>
                <th className="p-3.5 rounded-r-lg">Trạng Thái</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filteredAppointments.length > 0 ? (
                filteredAppointments.map((apt) => (
                  <tr key={apt.id} className="hover:bg-slate-50 transition">
                    <td className="p-3.5 font-mono font-bold text-cyan-600">{apt.id}</td>
                    <td className="p-3.5 font-bold text-slate-800">{apt.doctorName}</td>
                    <td className="p-3.5">{apt.specialty}</td>
                    <td className="p-3.5 font-medium">{apt.time} ({apt.date})</td>
                    <td className="p-3.5">
                      <span className={`px-2.5 py-1 rounded-full text-xs font-semibold border ${
                        apt.status === "Xác nhận"
                          ? "bg-emerald-50 text-emerald-700 border-emerald-200"
                          : apt.status === "Đang chờ"
                          ? "bg-amber-50 text-amber-700 border-amber-200"
                          : "bg-slate-100 text-slate-600 border-slate-200"
                      }`}>
                        {apt.status}
                      </span>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={5} className="text-center py-6 text-xs text-slate-400 font-medium">
                    Không tìm thấy lịch khám phù hợp.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};