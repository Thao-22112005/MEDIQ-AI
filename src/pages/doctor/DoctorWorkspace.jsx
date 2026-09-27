import React, { useState } from "react";
import { Mic, Bot, Stethoscope, Save, UserCheck, Plus } from "lucide-react";
import { MOCK_PATIENTS } from "../../mocks/mockPatients";

export const DoctorWorkspace = () => {
  const [selectedPatient, setSelectedPatient] = useState(MOCK_PATIENTS[0]);
  const [medicalNote, setMedicalNote] = useState("Bệnh nhân tỉnh táo, tiếp xúc tốt. Đau ngực vùng trước tim nhẹ.");
  const [prescription, setPrescription] = useState("Amlodipine 5mg - 1 viên/ngày");

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 h-[calc(100vh-8rem)]">
      {/* Waiting Queue Sidebar */}
      <div className="lg:col-span-4 bg-slate-900 border border-slate-800 rounded-2xl p-4 flex flex-col">
        <h2 className="text-sm font-bold text-white mb-3 flex items-center justify-between">
          <span>Hàng Chờ Khám (Queue)</span>
          <span className="text-xs px-2 py-0.5 rounded bg-cyan-500/20 text-cyan-400 font-mono">4 Bệnh nhân</span>
        </h2>
        <div className="space-y-2 flex-1 overflow-y-auto">
          {MOCK_PATIENTS.map((p) => (
            <div
              key={p.id}
              onClick={() => setSelectedPatient(p)}
              className={`p-3 rounded-xl border cursor-pointer transition ${
                selectedPatient?.id === p.id
                  ? "bg-cyan-500/10 border-cyan-500 text-white"
                  : "bg-slate-950 border-slate-800 text-slate-400 hover:bg-slate-800"
              }`}
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-white">{p.name}</span>
                <span className="text-[10px] font-mono text-cyan-400">{p.code}</span>
              </div>
              <p className="text-[11px] text-slate-400 mt-1">{p.symptoms}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Main Examination & AI Medical Note Window */}
      <div className="lg:col-span-8 bg-slate-900 border border-slate-800 rounded-2xl p-5 flex flex-col space-y-4">
        <div className="flex items-center justify-between pb-3 border-b border-slate-800">
          <div>
            <span className="text-xs text-slate-400">Đang khám bệnh nhân:</span>
            <h3 className="text-base font-bold text-white">{selectedPatient?.name} ({selectedPatient?.gender}, {selectedPatient?.age}t)</h3>
          </div>
          <button className="px-3 py-1.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold text-xs rounded-xl flex items-center gap-1">
            <UserCheck className="w-4 h-4" /> Hoàn thành
          </button>
        </div>

        {/* AI Medical Note Generator */}
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <label className="text-xs font-bold text-cyan-400 uppercase flex items-center gap-2">
              <Bot className="w-4 h-4" /> AI Medical Note (Ghi Chép Thông Minh)
            </label>
            <button className="text-xs px-2.5 py-1 rounded bg-slate-800 text-cyan-300 border border-slate-700 flex items-center gap-1 hover:bg-slate-700">
              <Mic className="w-3.5 h-3.5 text-red-400 animate-pulse" /> Giọng nói $\rightarrow$ Văn bản
            </button>
          </div>
          <textarea
            rows={4}
            value={medicalNote}
            onChange={(e) => setMedicalNote(e.target.value)}
            className="w-full bg-slate-950 border border-slate-800 rounded-xl p-3 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
          />
        </div>

        {/* Prescription Builder */}
        <div className="space-y-2 flex-1">
          <label className="text-xs font-bold text-slate-400 uppercase">Kê Đơn Thuốc (Prescription)</label>
          <input
            type="text"
            value={prescription}
            onChange={(e) => setPrescription(e.target.value)}
            className="w-full bg-slate-950 border border-slate-800 rounded-xl p-3 text-xs text-slate-200 focus:outline-none focus:border-cyan-500 font-mono"
          />
        </div>

        <button className="w-full py-3 bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-xs rounded-xl flex items-center justify-center gap-2">
          <Save className="w-4 h-4" /> Lưu Hồ Sơ Bệnh Án Vào Medical Record Service
        </button>
      </div>
    </div>
  );
};