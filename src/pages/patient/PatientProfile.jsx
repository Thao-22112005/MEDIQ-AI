import React from "react";
import { useAuth } from "../../auth/AuthContext";
import { User, Mail, Phone, MapPin, Calendar, ShieldCheck } from "lucide-react";

export const PatientProfile = () => {
  const { user } = useAuth();

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
        <div className="flex items-center gap-4 pb-6 border-b border-slate-800">
          <img src={user?.avatar} alt={user?.name} className="w-16 h-16 rounded-full object-cover ring-2 ring-cyan-500/30" />
          <div>
            <h1 className="text-xl font-bold text-white">{user?.name}</h1>
            <p className="text-xs text-cyan-400 font-mono mt-0.5">Mã Bệnh Nhân: {user?.medicalCode || "PAT-88291"}</p>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mt-6 text-sm">
          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800">
            <span className="text-xs text-slate-500 block">Email:</span>
            <span className="text-slate-200 font-medium">{user?.email}</span>
          </div>
          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800">
            <span className="text-xs text-slate-500 block">Số điện thoại:</span>
            <span className="text-slate-200 font-medium">{user?.phone || "0912 345 678"}</span>
          </div>
          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800">
            <span className="text-xs text-slate-500 block">Ngày sinh:</span>
            <span className="text-slate-200 font-medium">{user?.dob || "1992-05-15"}</span>
          </div>
          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800">
            <span className="text-xs text-slate-500 block">Địa chỉ:</span>
            <span className="text-slate-200 font-medium">{user?.address || "Hà Nội"}</span>
          </div>
        </div>
      </div>
    </div>
  );
};