import React from "react";
import { MOCK_PATIENTS } from "../../mocks/mockPatients";

export const DoctorPatients = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-white">Danh Sách Bệnh Nhân Đăng Ký Khám</h1>
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
        <table className="w-full text-left text-xs text-slate-300">
          <thead className="bg-slate-950 text-slate-400 uppercase">
            <tr>
              <th className="p-3">Mã BN</th>
              <th className="p-3">Họ Tên</th>
              <th className="p-3">Tuổi/Giới</th>
              <th className="p-3">Triệu Chứng</th>
              <th className="p-3">Trạng Thái</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800">
            {MOCK_PATIENTS.map((p) => (
              <tr key={p.id}>
                <td className="p-3 font-mono text-cyan-400">{p.code}</td>
                <td className="p-3 font-bold text-white">{p.name}</td>
                <td className="p-3">{p.age} / {p.gender}</td>
                <td className="p-3">{p.symptoms}</td>
                <td className="p-3">
                  <span className="px-2 py-0.5 rounded bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
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