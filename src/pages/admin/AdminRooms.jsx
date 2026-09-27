import React from "react";
import { MOCK_ROOMS } from "../../mocks/mockRooms";

export const AdminRooms = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-white">Quản Lý Phòng Khám & Lịch</h1>
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
        <table className="w-full text-left text-xs text-slate-300">
          <thead className="bg-slate-950 text-slate-400 uppercase">
            <tr>
              <th className="p-3">Tên Phòng</th>
              <th className="p-3">Chuyên Khoa</th>
              <th className="p-3">Bác Sĩ Phụ Trách</th>
              <th className="p-3">Trạng Thái</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800">
            {MOCK_ROOMS.map((r) => (
              <tr key={r.id}>
                <td className="p-3 font-bold text-white">{r.name}</td>
                <td className="p-3">{r.department}</td>
                <td className="p-3">{r.doctor}</td>
                <td className="p-3">
                  <span className="px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                    {r.status}
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