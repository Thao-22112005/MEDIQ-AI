import React from "react";
import { useAuth } from "../auth/AuthContext";
import { LogOut, Bell, Activity, User } from "lucide-react";
import { NavLink } from "react-router-dom";
import {
  LayoutDashboard, Bot, CalendarPlus, Compass, Calendar, FileText
} from "lucide-react";

export const Header = () => {
  const { user, logout } = useAuth();

  const getRoleBadge = (role) => {
    switch (role) {
      case "ADMIN":
        return { text: "ADMINISTRATOR", bg: "bg-purple-100 text-purple-700 border-purple-200" };
      case "DOCTOR":
        return { text: "BÁC SĨ CHUYÊN KHOA", bg: "bg-emerald-100 text-emerald-700 border-emerald-200" };
      default:
        return { text: "BỆNH NHÂN", bg: "bg-cyan-100 text-cyan-700 border-cyan-200" };
    }
  };

  const badge = getRoleBadge(user?.role);

  // Danh sách Menu ngang cho Bệnh nhân
  const patientNavItems = [
    { to: "/patient/dashboard", icon: LayoutDashboard, label: "Tổng quan" },
    { to: "/patient/ai-chat", icon: Bot, label: "AI Chat & Triage", badge: "AI" },
    { to: "/patient/booking", icon: CalendarPlus, label: "Đặt Lịch Khám" },
    { to: "/patient/map", icon: Compass, label: "Sơ Đồ Phòng Khám" },
    { to: "/patient/appointments", icon: Calendar, label: "Lịch Khám Của Tôi" },
    { to: "/patient/records", icon: FileText, label: "Hồ Sơ Bệnh Án" },
    { to: "/patient/profile", icon: User, label: "Thông Tin Cá Nhân" },
  ];

  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-30 shadow-xs">
      {/* Top Main Header */}
      <div className="h-16 px-6 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 text-cyan-600 font-bold text-lg tracking-wider">
            <Activity className="w-6 h-6 animate-pulse text-cyan-600" />
            <span>MEDIQ AI</span>
          </div>
          <span className="text-xs px-2.5 py-1 rounded-full bg-cyan-50 text-cyan-700 font-mono border border-cyan-200">
            v2.4 Smart Clinic
          </span>
        </div>

        <div className="flex items-center gap-4">
          <button className="relative p-2 text-slate-600 hover:text-cyan-600 rounded-lg hover:bg-slate-100 transition">
            <Bell className="w-5 h-5" />
            <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-cyan-500 rounded-full animate-ping"></span>
            <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-cyan-500 rounded-full"></span>
          </button>

          <div className="h-6 w-[1px] bg-slate-200"></div>

          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-full ring-2 ring-cyan-500/30 bg-cyan-100 border border-cyan-300 flex items-center justify-center text-cyan-700 font-bold text-sm shrink-0">
              {user?.name ? user.name.charAt(0).toUpperCase() : <User className="w-5 h-5" />}
            </div>
            <div className="hidden sm:block text-left">
              <div className="text-sm font-semibold text-slate-800">{user?.name}</div>
              <div className={`text-[10px] font-bold px-2 py-0.5 rounded border inline-block mt-0.5 ${badge.bg}`}>
                {badge.text}
              </div>
            </div>
          </div>

          <button
            onClick={logout}
            title="Đăng xuất"
            className="flex items-center gap-2 px-3 py-1.5 rounded-lg text-slate-600 hover:text-red-600 hover:bg-red-50 border border-transparent hover:border-red-200 text-sm font-medium transition ml-2"
          >
            <LogOut className="w-4 h-4" />
            <span className="hidden md:inline">Đăng xuất</span>
          </button>
        </div>
      </div>

      {/* Menu Ngang dành riêng cho PATIENT */}
      {user?.role === "PATIENT" && (
        <div className="border-t border-slate-100 bg-slate-50/80 px-6 overflow-x-auto">
          <nav className="flex items-center gap-2 py-2 min-w-max">
            {patientNavItems.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) =>
                    `flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-semibold transition-all ${
                      isActive
                        ? "bg-cyan-600 text-white shadow-sm font-bold"
                        : "text-slate-600 hover:text-cyan-600 hover:bg-cyan-50"
                    }`
                  }
                >
                  <Icon className="w-4 h-4" />
                  <span>{item.label}</span>
                  {item.badge && (
                    <span className="text-[10px] px-1.5 py-0.2 rounded-full bg-cyan-100 text-cyan-800 font-mono font-bold border border-cyan-200">
                      {item.badge}
                    </span>
                  )}
                </NavLink>
              );
            })}
          </nav>
        </div>
      )}
    </header>
  );
};