import React from "react";
import { MOCK_ACCOUNTS } from "../../mocks/mockAccounts";

export const AdminUsers = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-white">Quản Lý Người Dùng & Phân Quyền (Users)</h1>
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
        <table className="w-full text-left text-xs text-slate-300">
          <thead className="bg-slate-950 text-slate-400 uppercase">
            <tr>
              <th className="p-3">ID</th>
              <th className="p-3">Họ Tên</th>
              <th className="p-3">Email</th>
              <th className="p-3">Quyền (Role)</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800">
            {MOCK_ACCOUNTS.map((acc) => (
              <tr key={acc.id}>
                <td className="p-3 font-mono text-cyan-400">#{acc.id}</td>
                <td className="p-3 font-bold text-white">{acc.name}</td>
                <td className="p-3">{acc.email}</td>
                <td className="p-3">
                  <span className="px-2 py-0.5 rounded bg-purple-500/20 text-purple-300 font-bold">
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