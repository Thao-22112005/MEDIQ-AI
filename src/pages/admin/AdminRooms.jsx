import React from "react";
import { MOCK_ROOMS } from "../../mocks/mockRooms";

export const AdminRooms = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-slate-900">Quản Lý Phòng Khám & Lịch</h1>
      <div className="bg-white border border-slate-200 shadow-sm rounded-2xl p-5">
        <table className="w-full text-left text-xs text-slate-600">
          <thead className="bg-slate-50 text-slate-500 uppercase font-bold border-b border-slate-200">
            <tr>
              <th className="p-3">Tên Phòng</th>
              <th className="p-3">Chuyên Khoa</th>
              <th className="p-3">Bác Sĩ Phụ Trách</th>
              <th className="p-3">Trạng Thái</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {MOCK_ROOMS.map((r) => (
              <tr key={r.id} className="hover:bg-slate-50">
                <td className="p-3 font-bold text-slate-900">{r.name}</td>
                <td className="p-3">{r.department}</td>
                <td className="p-3">{r.doctor}</td>
                <td className="p-3">
                  <span className="px-2.5 py-1 rounded-full bg-emerald-100 text-emerald-800 font-semibold border border-emerald-200">
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