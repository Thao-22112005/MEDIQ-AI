import React from "react";
import { MOCK_ACCOUNTS } from "../../mocks/mockAccounts";

export const AdminUsers = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-slate-900">Quản Lý Người Dùng & Phân Quyền (Users)</h1>
      <div className="bg-white border border-slate-200 shadow-sm rounded-2xl p-5">
        <table className="w-full text-left text-xs text-slate-600">
          <thead className="bg-slate-50 text-slate-500 uppercase font-bold border-b border-slate-200">
            <tr>
              <th className="p-3">ID</th>
              <th className="p-3">Họ Tên</th>
              <th className="p-3">Email</th>
              <th className="p-3">Quyền (Role)</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {MOCK_ACCOUNTS.map((acc) => (
              <tr key={acc.id} className="hover:bg-slate-50">
                <td className="p-3 font-mono font-bold text-cyan-700">#{acc.id}</td>
                <td className="p-3 font-bold text-slate-800">{acc.name}</td>
                <td className="p-3">{acc.email}</td>
                <td className="p-3">
                  <span className="px-2.5 py-1 rounded-full bg-purple-100 text-purple-800 font-bold border border-purple-200">
                    {acc.role}
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