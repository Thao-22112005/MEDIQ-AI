import React from "react";
import { MOCK_PATIENTS } from "../../mocks/mockPatients";

export const AdminPatients = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-white">Quản Lý Bệnh Nhân (Patient Service)</h1>
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
        <table className="w-full text-left text-xs text-slate-300">
          <thead className="bg-slate-950 text-slate-400 uppercase">
            <tr>
              <th className="p-3">Mã Định Danh</th>
              <th className="p-3">Tên Bệnh Nhân</th>
              <th className="p-3">SĐT</th>
              <th className="p-3">Email</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800">
            {MOCK_PATIENTS.map((p) => (
              <tr key={p.id}>
                <td className="p-3 font-mono text-cyan-400">{p.code}</td>
                <td className="p-3 font-bold text-white">{p.name}</td>
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