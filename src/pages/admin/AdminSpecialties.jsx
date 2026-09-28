import React from "react";
import { MOCK_SPECIALTIES } from "../../mocks/mockAccounts";

export const AdminSpecialties = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-slate-900">Quản Lý Chuyên Khoa</h1>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {MOCK_SPECIALTIES.map((s) => (
          <div key={s.id} className="p-4 bg-white border border-slate-200 shadow-sm rounded-2xl">
            <h3 className="text-base font-bold text-slate-900">{s.name}</h3>
            <p className="text-xs text-slate-500 mt-1 font-medium">Số lượng bác sĩ: {s.doctorsCount} | Vị trí: {s.room}</p>
          </div>
        ))}
      </div>
    </div>
  );
};