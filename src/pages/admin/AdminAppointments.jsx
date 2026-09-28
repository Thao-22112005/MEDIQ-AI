import React from "react";
import { MOCK_APPOINTMENTS } from "../../mocks/mockAppointments";

export const AdminAppointments = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-slate-900">Quản Lý Lịch Hẹn (Appointment Service)</h1>
      <div className="bg-white border border-slate-200 shadow-sm rounded-2xl p-5">
        <table className="w-full text-left text-xs text-slate-600">
          <thead className="bg-slate-50 text-slate-500 uppercase font-bold border-b border-slate-200">
            <tr>
              <th className="p-3">Mã Lịch</th>
              <th className="p-3">Bệnh Nhân</th>
              <th className="p-3">Bác Sĩ</th>
              <th className="p-3">Khung Giờ</th>
              <th className="p-3">Trạng Thái</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {MOCK_APPOINTMENTS.map((apt) => (
              <tr key={apt.id} className="hover:bg-slate-50">
                <td className="p-3 font-mono font-bold text-cyan-700">{apt.id}</td>
                <td className="p-3 font-bold text-slate-800">{apt.patientName}</td>
                <td className="p-3">{apt.doctorName}</td>
                <td className="p-3">{apt.time}</td>
                <td className="p-3">
                  <span className="px-2.5 py-1 rounded-full bg-emerald-100 text-emerald-800 font-semibold border border-emerald-200">
                    {apt.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};