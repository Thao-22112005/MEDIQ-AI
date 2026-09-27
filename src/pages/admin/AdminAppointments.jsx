import React from "react";
import { MOCK_APPOINTMENTS } from "../../mocks/mockAppointments";

export const AdminAppointments = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-white">Quản Lý Lịch Hẹn (Appointment Service)</h1>
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
        <table className="w-full text-left text-xs text-slate-300">
          <thead className="bg-slate-950 text-slate-400 uppercase">
            <tr>
              <th className="p-3">Mã Lịch</th>
              <th className="p-3">Bệnh Nhân</th>
              <th className="p-3">Bác Sĩ</th>
              <th className="p-3">Khung Giờ</th>
              <th className="p-3">Trạng Thái</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800">
            {MOCK_APPOINTMENTS.map((apt) => (
              <tr key={apt.id}>
                <td className="p-3 font-mono text-cyan-400">{apt.id}</td>
                <td className="p-3 font-bold text-white">{apt.patientName}</td>
                <td className="p-3">{apt.doctorName}</td>
                <td className="p-3">{apt.time}</td>
                <td className="p-3">{apt.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};