import React from "react";
import { useAuth } from "../auth/AuthContext";
import { NavItem } from "./NavItem";
import {
  LayoutDashboard, Stethoscope, Users, Building2, DoorOpen, CalendarCheck, Activity, Calendar, User
} from "lucide-react";

export const Sidebar = () => {
  const { user } = useAuth();
  const role = user?.role;

  // Nếu là PATIENT thì không hiển thị Sidebar dọc nữa
  if (role === "PATIENT") {
    return null;
  }

  return (
    <aside className="w-64 bg-white border-r border-slate-200 flex flex-col shrink-0 min-h-[calc(100vh-4rem)] shadow-sm">
      <div className="p-4 space-y-6 flex-1">
        {role === "DOCTOR" && (
          <div>
            <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider px-3 mb-2">
              Menu Bác Sĩ
            </div>
            <nav className="space-y-1">
              <NavItem to="/doctor/workspace" icon={Stethoscope} label="Doctor Workspace" badge="Live" />
              <NavItem to="/doctor/patients" icon={Users} label="Danh Sách Bệnh Nhân" />
              <NavItem to="/doctor/schedules" icon={CalendarCheck} label="Lịch Khám & Phòng" />
            </nav>
          </div>
        )}

        {role === "ADMIN" && (
          <div>
            <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider px-3 mb-2">
              Quản Trị Hệ Thống
            </div>
            <nav className="space-y-1">
              <NavItem to="/admin/dashboard" icon={LayoutDashboard} label="Admin Dashboard" />
              <NavItem to="/admin/users" icon={Users} label="Quản Lý User" />
              <NavItem to="/admin/patients" icon={User} label="Quản Lý Bệnh Nhân" />
              <NavItem to="/admin/doctors" icon={Stethoscope} label="Quản Lý Bác Sĩ" />
              <NavItem to="/admin/specialties" icon={Building2} label="Quản Lý Chuyên Khoa" />
              <NavItem to="/admin/rooms" icon={DoorOpen} label="Quản Lý Phòng & Lịch" />
              <NavItem to="/admin/appointments" icon={Calendar} label="Quản Lý Lịch Hẹn" />
            </nav>
          </div>
        )}
      </div>

      <div className="p-4 border-t border-slate-100">
        <div className="p-3 rounded-xl bg-cyan-50 border border-cyan-100 text-xs text-slate-600 space-y-1">
          <div className="flex items-center gap-2 text-cyan-600 font-semibold">
            <Activity className="w-3.5 h-3.5" />
            <span>AI Engine: Online</span>
          </div>
          <p className="text-[11px] text-slate-500">Tích hợp Microservices & Smart Triage v2</p>
        </div>
      </div>
    </aside>
  );
};