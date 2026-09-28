import React, { useState } from "react";
import { MOCK_APPOINTMENTS } from "../../mocks/mockAppointments";
import { Calendar, Clock, Search, Filter, RotateCcw } from "lucide-react";

export const MyAppointments = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const [filterDate, setFilterDate] = useState("");
  const [filterStatus, setFilterStatus] = useState("ALL");

  // Logic Lọc Lịch Khám
  const filteredAppointments = MOCK_APPOINTMENTS.filter((apt) => {
    const matchesSearch =
      apt.doctorName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      apt.specialty.toLowerCase().includes(searchTerm.toLowerCase()) ||
      apt.id.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus = filterStatus === "ALL" || apt.status === filterStatus;
    const matchesDate = !filterDate || apt.date.includes(filterDate);

    return matchesSearch && matchesStatus && matchesDate;
  });

  const handleReset = () => {
    setSearchTerm("");
    setFilterDate("");
    setFilterStatus("ALL");
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <h1 className="text-xl font-bold text-slate-900">Lịch Khám Của Tôi</h1>
      </div>

      {/* Thanh Bộ Lọc & Tìm Kiếm Lịch Khám */}
      <div className="bg-white border border-slate-200 shadow-sm rounded-2xl p-4 grid grid-cols-1 sm:grid-cols-12 gap-3">
        {/* Tìm kiếm ô chữ */}
        <div className="sm:col-span-5 relative">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Tìm theo mã lịch (APT-101), bác sĩ, chuyên khoa..."
            className="w-full pl-9 pr-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-800 focus:outline-none focus:border-cyan-500"
          />
        </div>

        {/* Lọc theo Trạng thái */}
        <div className="sm:col-span-3">
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-700 font-semibold focus:outline-none focus:border-cyan-500"
          >
            <option value="ALL">Tất cả trạng thái</option>
            <option value="Xác nhận">Xác nhận</option>
            <option value="Đang chờ">Đang chờ</option>
            <option value="Đã xong">Đã xong</option>
          </select>
        </div>

        {/* Lọc theo Ngày */}
        <div className="sm:col-span-3">
          <input
            type="date"
            value={filterDate}
            onChange={(e) => setFilterDate(e.target.value)}
            className="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-700 font-semibold focus:outline-none focus:border-cyan-500"
          />
        </div>

        {/* Nút Đặt lại lọc */}
        <div className="sm:col-span-1 flex items-center justify-center">
          <button
            onClick={handleReset}
            title="Xóa bộ lọc"
            className="p-2 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl transition"
          >
            <RotateCcw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Danh Sách Lịch Khám */}
      <div className="bg-white border border-slate-200 shadow-sm rounded-2xl p-5">
        {filteredAppointments.length > 0 ? (
          <div className="space-y-4">
            {filteredAppointments.map((apt) => (
              <div key={apt.id} className="p-4 bg-slate-50 border border-slate-200 rounded-xl flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:bg-cyan-50/50 transition">
                <div className="flex items-start gap-3">
                  <div className="p-3 bg-cyan-100 rounded-xl text-cyan-700 border border-cyan-200 shrink-0">
                    <Calendar className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-xs font-mono font-bold text-cyan-700">{apt.id}</p>
                    <p className="text-sm font-bold text-slate-800">{apt.doctorName} - {apt.specialty}</p>
                    <p className="text-xs text-slate-500 mt-1 flex items-center gap-2 font-medium">
                      <Clock className="w-3.5 h-3.5" />
                      {apt.time} ({apt.date}) • {apt.type}
                    </p>
                  </div>
                </div>
                <span className={`px-3 py-1 rounded-full text-xs font-semibold border self-start sm:self-center ${
                  apt.status === "Xác nhận"
                    ? "bg-emerald-100 text-emerald-800 border-emerald-200"
                    : apt.status === "Đang chờ"
                    ? "bg-amber-100 text-amber-800 border-amber-200"
                    : "bg-slate-200 text-slate-700 border-slate-300"
                }`}>
                  {apt.status}
                </span>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-10 text-slate-400 text-xs font-medium">
            Không tìm thấy lịch khám phù hợp với tìm kiếm của bạn.
          </div>
        )}
      </div>
    </div>
  );
};