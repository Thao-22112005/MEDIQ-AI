import React from "react";
import { MOCK_MEDICAL_RECORDS } from "../../mocks/mockMedicalRecords";
import { FileText, CheckCircle2 } from "lucide-react";

export const PatientMedicalRecords = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-white">Hồ Sơ Bệnh Án Điển Tử (EMR)</h1>
      <div className="space-y-4">
        {MOCK_MEDICAL_RECORDS.map((rec) => (
          <div key={rec.id} className="bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-3">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <div>
                <span className="text-xs font-mono text-cyan-400">{rec.id}</span>
                <h3 className="text-sm font-bold text-white">{rec.diagnosis}</h3>
              </div>
              <span className="text-xs text-slate-400 font-mono">{rec.date}</span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs">
              <div className="bg-slate-950 p-3 rounded-xl border border-slate-800">
                <span className="text-slate-500 block font-semibold mb-1">Triệu chứng:</span>
                <span className="text-slate-300">{rec.symptoms}</span>
              </div>
              <div className="bg-slate-950 p-3 rounded-xl border border-slate-800">
                <span className="text-slate-500 block font-semibold mb-1">Đơn thuốc chỉ định:</span>
                <span className="text-emerald-400 font-mono">{rec.prescription}</span>
              </div>
            </div>

            <div className="text-xs text-slate-400 pt-2">
              <span className="text-slate-500">Bác sĩ phụ trách:</span> <b className="text-slate-200">{rec.doctorName}</b>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};