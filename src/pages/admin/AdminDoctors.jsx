import React from "react";
import { MOCK_DOCTORS } from "../../mocks/mockDoctors";

export const AdminDoctors = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-slate-900">Quản Lý Bác Sĩ (Doctor Service)</h1>
      <div className="bg-white border border-slate-200 shadow-sm rounded-2xl p-5">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {MOCK_DOCTORS.map((d) => (
            <div key={d.id} className="p-4 bg-slate-50 border border-slate-200 rounded-xl flex items-center gap-3">
              <img src={d.avatar} alt={d.name} className="w-12 h-12 rounded-full object-cover border border-slate-200" />
              <div>
                <p className="text-sm font-bold text-slate-900">{d.name}</p>
                <p className="text-xs text-cyan-800 font-semibold">{d.specialty} • Kinh nghiệm: {d.experience}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};