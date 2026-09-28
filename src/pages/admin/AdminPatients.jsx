import React from "react";
import { MOCK_PATIENTS } from "../../mocks/mockPatients";

export const AdminPatients = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-slate-900">Quản Lý Bệnh Nhân (Patient Service)</h1>
      <div className="bg-white border border-slate-200 shadow-sm rounded-2xl p-5">
        <table className="w-full text-left text-xs text-slate-600">
          <thead className="bg-slate-50 text-slate-500 uppercase font-bold border-b border-slate-200">
            <tr>
              <th className="p-3">Mã Định Danh</th>
              <th className="p-3">Tên Bệnh Nhân</th>
              <th className="p-3">SĐT</th>
              <th className="p-3">Email</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {MOCK_PATIENTS.map((p) => (
              <tr key={p.id} className="hover:bg-slate-50">
                <td className="p-3 font-mono font-bold text-cyan-700">{p.code}</td>
                <td className="p-3 font-bold text-slate-800">{p.name}</td>
                <td className="p-3">{p.phone}</td>
                <td className="p-3">{p.email}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};