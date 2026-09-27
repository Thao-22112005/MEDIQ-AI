import React from "react";
import { MOCK_APPOINTMENTS } from "../../mocks/mockAppointments";
import { Calendar, Clock } from "lucide-react";

export const MyAppointments = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-white">Lịch Khám Của Tôi</h1>
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
        <div className="space-y-4">
          {MOCK_APPOINTMENTS.map((apt) => (
            <div key={apt.id} className="p-4 bg-slate-950 border border-slate-800 rounded-xl flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div className="flex items-start gap-3">
                <div className="p-3 bg-cyan-500/10 rounded-xl text-cyan-400 border border-cyan-500/20 shrink-0">
                  <Calendar className="w-5 h-5" />
                </div>
                <div>
                  <p className="text-xs font-mono text-cyan-400">{apt.id}</p>
                  <p className="text-sm font-bold text-white">{apt.doctorName} - {apt.specialty}</p>
                  <p className="text-xs text-slate-400 mt-1 flex items-center gap-2">
                    <Clock className="w-3.5 h-3.5" />
                    {apt.time} ({apt.date}) • {apt.type}
                  </p>
                </div>
              </div>
              <span className="px-3 py-1 rounded-full text-xs bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-medium self-start sm:self-center">
                {apt.status}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};