import React, { useState } from "react";
import { useAuth } from "../auth/AuthContext";
import { useNavigate, useLocation } from "react-router-dom";
import { Activity, Lock, Mail, Eye, EyeOff, AlertCircle, ArrowRight } from "lucide-react";

export const LoginPage = () => {
  const [email, setEmail] = useState("patient@mediq.ai");
  const [password, setPassword] = useState("123456");
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");
    setIsSubmitting(true);

    try {
      const loggedUser = await login(email, password);
      const redirectPath =
        loggedUser.role === "PATIENT"
          ? "/patient/dashboard"
          : loggedUser.role === "DOCTOR"
          ? "/doctor/workspace"
          : "/admin/dashboard";

      navigate(redirectPath, { replace: true });
    } catch (err) {
      setError(err.message || "Đã xảy ra lỗi đăng nhập.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const quickFill = (accEmail) => {
    setEmail(accEmail);
    setPassword("123456");
    setError("");
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col justify-center items-center p-4 relative overflow-hidden">
      {/* Background Orbs */}
      <div className="absolute top-1/4 left-1/3 w-96 h-96 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none"></div>
      <div className="absolute bottom-1/4 right-1/3 w-96 h-96 bg-blue-600/10 rounded-full blur-3xl pointer-events-none"></div>

      <div className="w-full max-w-md relative z-10">
        {/* Brand Header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center p-3 bg-cyan-500/10 rounded-2xl border border-cyan-500/20 mb-4 text-cyan-400">
            <Activity className="w-10 h-10 animate-pulse" />
          </div>
          <h1 className="text-3xl font-extrabold tracking-tight text-white">MEDIQ AI</h1>
          <p className="text-sm text-cyan-400 font-medium tracking-wide mt-1 uppercase">Smart Clinic Platform</p>
        </div>

        {/* Login Card */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 sm:p-8 shadow-2xl backdrop-blur-xl">
          <h2 className="text-xl font-bold text-slate-100 mb-6">Đăng nhập hệ thống</h2>

          {error && (
            <div className="mb-6 p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-400 text-sm flex items-start gap-3">
              <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleLogin} className="space-y-5">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
                Email
              </label>
              <div className="relative">
                <Mail className="w-5 h-5 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" />
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@mediq.ai"
                  className="w-full pl-11 pr-4 py-3 rounded-xl bg-slate-950 border border-slate-800 text-slate-100 placeholder-slate-600 focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 transition text-sm"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
                Mật khẩu
              </label>
              <div className="relative">
                <Lock className="w-5 h-5 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" />
                <input
                  type={showPassword ? "text" : "password"}
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-11 pr-11 py-3 rounded-xl bg-slate-950 border border-slate-800 text-slate-100 placeholder-slate-600 focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 transition text-sm"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            <div className="flex items-center justify-between text-xs text-slate-400">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  className="rounded border-slate-800 bg-slate-950 text-cyan-500 focus:ring-cyan-500/20"
                />
                <span>Ghi nhớ đăng nhập</span>
              </label>
              <a href="#forgot" onClick={(e) => e.preventDefault()} className="text-cyan-400 hover:underline">
                Quên mật khẩu?
              </a>
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full py-3.5 px-4 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold flex items-center justify-center gap-2 transition shadow-lg shadow-cyan-500/20 disabled:opacity-50"
            >
              {isSubmitting ? (
                <div className="w-5 h-5 border-2 border-slate-950 border-t-transparent rounded-full animate-spin"></div>
              ) : (
                <>
                  <span>Đăng nhập</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>

          {/* Quick Demo Login selector */}
          <div className="mt-8 pt-6 border-t border-slate-800/80">
            <p className="text-xs text-slate-500 font-medium mb-3 text-center">Tài khoản DEMO trải nghiệm nhanh:</p>
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => quickFill("patient@mediq.ai")}
                className="py-2 px-1 text-xs rounded-lg bg-slate-800 hover:bg-slate-700 text-cyan-300 border border-slate-700 text-center font-medium transition"
              >
                PATIENT
              </button>
              <button
                type="button"
                onClick={() => quickFill("doctor@mediq.ai")}
                className="py-2 px-1 text-xs rounded-lg bg-slate-800 hover:bg-slate-700 text-emerald-300 border border-slate-700 text-center font-medium transition"
              >
                DOCTOR
              </button>
              <button
                type="button"
                onClick={() => quickFill("admin@mediq.ai")}
                className="py-2 px-1 text-xs rounded-lg bg-slate-800 hover:bg-slate-700 text-purple-300 border border-slate-700 text-center font-medium transition"
              >
                ADMIN
              </button>
            </div>
          </div>
        </div>

        <p className="text-center text-xs text-slate-600 mt-6">
          MEDIQ Smart Clinic Platform &copy; 2026. Microservices & AI Architecture.
        </p>
      </div>
    </div>
  );
};