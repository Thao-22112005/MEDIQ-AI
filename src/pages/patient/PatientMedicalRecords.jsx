import React from "react";
import { MOCK_MEDICAL_RECORDS } from "../../mocks/mockMedicalRecords";

export const PatientMedicalRecords = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-slate-900">Hồ Sơ Bệnh Án Điện Tử (EMR)</h1>
      <div className="space-y-4">
        {MOCK_MEDICAL_RECORDS.map((rec) => (
          <div key={rec.id} className="bg-white border border-slate-200 shadow-sm rounded-2xl p-5 space-y-3">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <div>
                <span className="text-xs font-mono font-bold text-cyan-700">{rec.id}</span>
                <h3 className="text-sm font-bold text-slate-800">{rec.diagnosis}</h3>
              </div>
              <span className="text-xs text-slate-400 font-mono font-semibold">{rec.date}</span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs">
              <div className="bg-slate-50 p-3 rounded-xl border border-slate-200">
                <span className="text-slate-500 block font-semibold mb-1">Triệu chứng:</span>
                <span className="text-slate-700 font-medium">{rec.symptoms}</span>
              </div>
              <div className="bg-slate-50 p-3 rounded-xl border border-slate-200">
                <span className="text-slate-500 block font-semibold mb-1">Đơn thuốc chỉ định:</span>
                <span className="text-cyan-700 font-mono font-bold">{rec.prescription}</span>
              </div>
            </div>

            <div className="text-xs text-slate-500 pt-2">
              Bác sĩ phụ trách: <b className="text-slate-800">{rec.doctorName}</b>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};