import React, { useState } from "react";
import { Mic, Bot, Save, UserCheck } from "lucide-react";
import { MOCK_PATIENTS } from "../../mocks/mockPatients";

export const DoctorWorkspace = () => {
  const [selectedPatient, setSelectedPatient] = useState(MOCK_PATIENTS[0]);
  const [medicalNote, setMedicalNote] = useState("Bệnh nhân tỉnh táo, tiếp xúc tốt. Đau ngực vùng trước tim nhẹ.");
  const [prescription, setPrescription] = useState("Amlodipine 5mg - 1 viên/ngày");

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 h-[calc(100vh-8rem)]">
      {/* Waiting Queue Sidebar */}
      <div className="lg:col-span-4 bg-white border border-slate-200 rounded-2xl p-4 flex flex-col shadow-sm">
        <h2 className="text-sm font-bold text-slate-800 mb-3 flex items-center justify-between">
          <span>Hàng Chờ Khám (Queue)</span>
          <span className="text-xs px-2.5 py-0.5 rounded-full bg-cyan-100 text-cyan-800 font-bold border border-cyan-200">4 Bệnh nhân</span>
        </h2>
        <div className="space-y-2 flex-1 overflow-y-auto">
          {MOCK_PATIENTS.map((p) => (
            <div
              key={p.id}
              onClick={() => setSelectedPatient(p)}
              className={`p-3 rounded-xl border cursor-pointer transition ${
                selectedPatient?.id === p.id
                  ? "bg-cyan-50 border-cyan-500 text-slate-900 shadow-xs"
                  : "bg-slate-50 border-slate-200 text-slate-600 hover:bg-slate-100"
              }`}
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-slate-800">{p.name}</span>
                <span className="text-[10px] font-mono font-bold text-cyan-700">{p.code}</span>
              </div>
              <p className="text-[11px] text-slate-500 mt-1">{p.symptoms}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Main Examination Window */}
      <div className="lg:col-span-8 bg-white border border-slate-200 rounded-2xl p-5 flex flex-col space-y-4 shadow-sm">
        <div className="flex items-center justify-between pb-3 border-b border-slate-100">
          <div>
            <span className="text-xs text-slate-500 font-medium">Đang khám bệnh nhân:</span>
            <h3 className="text-base font-bold text-slate-900">{selectedPatient?.name} ({selectedPatient?.gender}, {selectedPatient?.age}t)</h3>
          </div>
          <button className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs rounded-xl flex items-center gap-1 shadow-xs transition">
            <UserCheck className="w-4 h-4" /> Hoàn thành
          </button>
        </div>

        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <label className="text-xs font-bold text-cyan-800 uppercase flex items-center gap-2">
              <Bot className="w-4 h-4 text-cyan-600" /> AI Medical Note (Ghi Chép Thông Minh)
            </label>
            <button className="text-xs px-2.5 py-1 rounded-lg bg-slate-100 text-slate-700 border border-slate-200 flex items-center gap-1 hover:bg-slate-200 font-medium transition">
              <Mic className="w-3.5 h-3.5 text-red-500 animate-pulse" /> Giọng nói $\rightarrow$ Văn bản
            </button>
          </div>
          <textarea
            rows={4}
            value={medicalNote}
            onChange={(e) => setMedicalNote(e.target.value)}
            className="w-full bg-slate-50 border border-slate-200 rounded-xl p-3 text-xs text-slate-800 focus:outline-none focus:border-cyan-500 focus:bg-white"
          />
        </div>

        <div className="space-y-2 flex-1">
          <label className="text-xs font-bold text-slate-500 uppercase">Kê Đơn Thuốc (Prescription)</label>
          <input
            type="text"
            value={prescription}
            onChange={(e) => setPrescription(e.target.value)}
            className="w-full bg-slate-50 border border-slate-200 rounded-xl p-3 text-xs text-slate-800 focus:outline-none focus:border-cyan-500 focus:bg-white font-mono"
          />
        </div>

        <button className="w-full py-3 bg-cyan-600 hover:bg-cyan-500 text-white font-bold text-xs rounded-xl flex items-center justify-center gap-2 shadow-md shadow-cyan-600/20 transition">
          <Save className="w-4 h-4" /> Lưu Hồ Sơ Bệnh Án Vào Medical Record Service
        </button>
      </div>
    </div>
  );
};