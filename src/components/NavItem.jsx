import React from "react";
import { NavLink } from "react-router-dom";

export const NavItem = ({ to, icon: Icon, label, badge }) => {
  return (
    <NavLink
      to={to}
      className={({ isActive }) =>
        `flex items-center justify-between px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all ${
          isActive
            ? "bg-cyan-500/15 text-cyan-400 border border-cyan-500/30 shadow-lg shadow-cyan-500/5 font-semibold"
            : "text-slate-400 hover:text-slate-200 hover:bg-slate-800/60"
        }`
      }
    >
      <div className="flex items-center gap-3">
        <Icon className="w-4 h-4" />
        <span>{label}</span>
      </div>
      {badge && (
        <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-cyan-500/20 text-cyan-300 font-semibold border border-cyan-500/30">
          {badge}
        </span>
      )}
    </NavLink>
  );
};