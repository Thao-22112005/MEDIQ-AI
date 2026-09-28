import React from "react";
import { MOCK_PATIENTS } from "../../mocks/mockPatients";

export const DoctorPatients = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-slate-900">Danh Sách Bệnh Nhân Đăng Ký Khám</h1>
      <div className="bg-white border border-slate-200 shadow-sm rounded-2xl p-5">
        <table className="w-full text-left text-xs text-slate-600">
          <thead className="bg-slate-50 text-slate-500 uppercase font-bold border-b border-slate-200">
            <tr>
              <th className="p-3">Mã BN</th>
              <th className="p-3">Họ Tên</th>
              <th className="p-3">Tuổi/Giới</th>
              <th className="p-3">Triệu Chứng</th>
              <th className="p-3">Trạng Thái</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {MOCK_PATIENTS.map((p) => (
              <tr key={p.id} className="hover:bg-slate-50">
                <td className="p-3 font-mono font-bold text-cyan-700">{p.code}</td>
                <td className="p-3 font-bold text-slate-800">{p.name}</td>
                <td className="p-3">{p.age} / {p.gender}</td>
                <td className="p-3">{p.symptoms}</td>
                <td className="p-3">
                  <span className="px-2.5 py-1 rounded-full bg-cyan-100 text-cyan-800 font-semibold border border-cyan-200">
                    {p.status}
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