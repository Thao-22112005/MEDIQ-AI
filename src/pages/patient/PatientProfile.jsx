import React, { useState, useEffect } from "react";
import { useAuth } from "../../auth/AuthContext";
import {
  User, Mail, Phone, MapPin, Calendar, Edit3, Save, X, CheckCircle2,
  Lock, RefreshCw, Send, ShieldCheck, Smartphone
} from "lucide-react";

export const PatientProfile = () => {
  const { user } = useAuth();

  // Trạng thái bật/tắt chế độ chỉnh sửa thông tin cá nhân
  const [isEditing, setIsEditing] = useState(false);

  const [formData, setFormData] = useState({
    name: "",
    email: "",
    phone: "",
    dob: "",
    address: ""
  });

  const [initialData, setInitialData] = useState({
    name: "",
    email: "",
    phone: "",
    dob: "",
    address: ""
  });

  const [showSuccessAlert, setShowSuccessAlert] = useState(false);
  const [alertMessage, setAlertMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Modal 1: OTP khi Cập nhật Email / Số điện thoại
  const [showProfileOtpModal, setShowProfileOtpModal] = useState(false);
  const [profileOtp, setProfileOtp] = useState(["", "", "", "", "", ""]);
  const [profileOtpError, setProfileOtpError] = useState("");

  // Modal 2: Đổi Mật Khẩu qua OTP SĐT
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [pwdStep, setPwdStep] = useState(1);
  const [pwdOtp, setPwdOtp] = useState(["", "", "", "", "", ""]);
  const [passwordData, setPasswordData] = useState({
    newPassword: "",
    confirmPassword: ""
  });
  const [modalError, setModalError] = useState("");

  // Đếm ngược OTP
  const [countdown, setCountdown] = useState(60);

  useEffect(() => {
    if (user) {
      const data = {
        name: user.name || "Nguyễn Văn An",
        email: user.email || "patient@mediq.ai",
        phone: user.phone || "0912 345 678",
        dob: user.dob || "1992-05-15",
        address: user.address || "Cầu Giấy, Hà Nội"
      };
      setFormData(data);
      setInitialData(data);
    }
  }, [user]);

  useEffect(() => {
    let timer;
    if ((showPasswordModal || showProfileOtpModal) && countdown > 0) {
      timer = setInterval(() => setCountdown((prev) => prev - 1), 1000);
    }
    return () => clearInterval(timer);
  }, [showPasswordModal, showProfileOtpModal, countdown]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleStartEdit = (e) => {
    e.preventDefault();
    setIsEditing(true);
  };

  const handleCancelEdit = (e) => {
    e.preventDefault();
    setFormData(initialData);
    setIsEditing(false);
  };

  // Submit cập nhật thông tin
  const handleProfileSubmit = (e) => {
    e.preventDefault();

    // Kiểm tra xem có thay đổi Email hoặc Số điện thoại (hoặc cả hai) hay không
    const isSensitiveInfoChanged =
      formData.email !== initialData.email || formData.phone !== initialData.phone;

    if (isSensitiveInfoChanged) {
      setShowProfileOtpModal(true);
      setProfileOtp(["", "", "", "", "", ""]);
      setProfileOtpError("");
      setCountdown(60);
    } else {
      setIsSubmitting(true);
      setTimeout(() => {
        setIsSubmitting(false);
        setIsEditing(false);
        setInitialData(formData);
        setAlertMessage("Cập nhật thông tin thành công!");
        setShowSuccessAlert(true);
        setTimeout(() => setShowSuccessAlert(false), 4000);
      }, 600);
    }
  };

  // Xác nhận OTP cho Email / Số điện thoại
  const handleVerifyProfileOtp = (e) => {
    e.preventDefault();
    setProfileOtpError("");

    if (profileOtp.join("").length < 6) {
      setProfileOtpError("Vui lòng nhập đủ 6 chữ số mã OTP!");
      return;
    }

    setIsSubmitting(true);
    setTimeout(() => {
      setIsSubmitting(false);
      setShowProfileOtpModal(false);
      setIsEditing(false);
      setInitialData(formData);
      setAlertMessage("Cập nhật thông tin thành công!");
      setShowSuccessAlert(true);
      setTimeout(() => setShowSuccessAlert(false), 4000);
    }, 1000);
  };

  // Xử lý đổi mật khẩu
  const handleRequestPasswordOtp = () => {
    setModalError("");
    setIsSubmitting(true);
    setTimeout(() => {
      setIsSubmitting(false);
      setPwdStep(2);
      setCountdown(60);
    }, 800);
  };

  const handleChangePasswordSubmit = (e) => {
    e.preventDefault();
    setModalError("");

    if (pwdOtp.join("").length < 6) {
      setModalError("Vui lòng nhập đủ 6 chữ số mã OTP!");
      return;
    }

    if (passwordData.newPassword.length < 6) {
      setModalError("Mật khẩu mới phải có ít nhất 6 ký tự!");
      return;
    }

    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setModalError("Mật khẩu xác nhận không khớp!");
      return;
    }

    setIsSubmitting(true);
    setTimeout(() => {
      setIsSubmitting(false);
      setShowPasswordModal(false);
      setPwdStep(1);
      setPwdOtp(["", "", "", "", "", ""]);
      setPasswordData({ newPassword: "", confirmPassword: "" });
      setAlertMessage("Cập nhật thông tin thành công!");
      setShowSuccessAlert(true);
      setTimeout(() => setShowSuccessAlert(false), 4000);
    }, 1000);
  };

  const handleOtpInputChange = (value, index, otpArray, setOtpArray) => {
    if (isNaN(value)) return;
    const newOtp = [...otpArray];
    newOtp[index] = value;
    setOtpArray(newOtp);
    if (value !== "" && index < 5) {
      const nextInput = document.getElementById(`otp-input-${index + 1}`);
      if (nextInput) nextInput.focus();
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      {/* Banner Thông Báo Thành Công */}
      {showSuccessAlert && (
        <div className="p-4 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm flex items-center justify-between shadow-sm">
          <div className="flex items-center gap-3">
            <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0" />
            <span className="font-semibold">{alertMessage}</span>
          </div>
          <button onClick={() => setShowSuccessAlert(false)} className="text-emerald-600 hover:bg-emerald-100 p-1 rounded-lg">
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* 1. CARD THÔNG TIN CÁ NHÂN CƠ BẢN */}
      <div className="bg-white border border-slate-200 shadow-sm rounded-2xl p-6">
        <form onSubmit={handleProfileSubmit}>
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-slate-100">
            <div className="flex items-center gap-4">
              <img
                src={user?.avatar || "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=150"}
                alt={formData.name}
                className="w-16 h-16 rounded-full object-cover ring-2 ring-cyan-500/30 border border-slate-200 shrink-0"
              />
              <div>
                {isEditing ? (
                  <div>
                    <label className="block text-[11px] font-bold text-slate-400 uppercase mb-1">Họ và tên</label>
                    <input
                      type="text"
                      name="name"
                      required
                      value={formData.name}
                      onChange={handleChange}
                      className="px-3 py-1.5 rounded-lg bg-white border border-slate-300 text-slate-900 font-bold text-lg focus:outline-none focus:border-cyan-500"
                    />
                  </div>
                ) : (
                  <>
                    <h1 className="text-xl font-bold text-slate-900">{formData.name}</h1>
                    <p className="text-xs text-cyan-700 font-mono font-bold mt-0.5">
                      Mã Bệnh Nhân: {user?.medicalCode || "PAT-88291"}
                    </p>
                  </>
                )}
              </div>
            </div>

            {/* Nút Chỉnh Sửa / Cập Nhật */}
            <div className="flex items-center gap-2">
              {!isEditing ? (
                <button
                  type="button"
                  onClick={handleStartEdit}
                  className="px-4 py-2.5 rounded-xl bg-cyan-600 hover:bg-cyan-500 text-white font-bold text-xs flex items-center gap-2 shadow-sm transition"
                >
                  <Edit3 className="w-4 h-4" />
                  <span>Chỉnh sửa</span>
                </button>
              ) : (
                <>
                  <button
                    type="button"
                    onClick={handleCancelEdit}
                    className="px-3.5 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold text-xs flex items-center gap-1.5 transition"
                  >
                    <X className="w-4 h-4" />
                    <span>Hủy</span>
                  </button>
                  <button
                    type="submit"
                    disabled={isSubmitting}
                    className="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs flex items-center gap-2 shadow-sm transition disabled:opacity-50"
                  >
                    {isSubmitting ? (
                      <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    ) : (
                      <>
                        <Save className="w-4 h-4" />
                        <span>Cập nhật</span>
                      </>
                    )}
                  </button>
                </>
              )}
            </div>
          </div>

          {/* Lưới 4 ô thông tin cơ bản */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mt-6 text-sm">
            {/* Email */}
            <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200">
              <span className="text-xs text-slate-500 block font-medium flex items-center gap-1.5 mb-1">
                <Mail className="w-3.5 h-3.5 text-slate-400" /> Email tài khoản:
              </span>
              {isEditing ? (
                <input
                  type="email"
                  name="email"
                  required
                  value={formData.email}
                  onChange={handleChange}
                  className="w-full px-3 py-1.5 rounded-lg bg-white border border-slate-300 text-slate-900 font-semibold text-sm focus:outline-none focus:border-cyan-500"
                />
              ) : (
                <span className="text-slate-800 font-semibold">{formData.email}</span>
              )}
            </div>

            {/* Số điện thoại */}
            <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200">
              <span className="text-xs text-slate-500 block font-medium flex items-center gap-1.5 mb-1">
                <Phone className="w-3.5 h-3.5 text-slate-400" /> Số điện thoại:
              </span>
              {isEditing ? (
                <input
                  type="tel"
                  name="phone"
                  required
                  value={formData.phone}
                  onChange={handleChange}
                  className="w-full px-3 py-1.5 rounded-lg bg-white border border-slate-300 text-slate-900 font-semibold text-sm focus:outline-none focus:border-cyan-500"
                />
              ) : (
                <span className="text-slate-800 font-semibold">{formData.phone}</span>
              )}
            </div>

            {/* Ngày sinh */}
            <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200">
              <span className="text-xs text-slate-500 block font-medium flex items-center gap-1.5 mb-1">
                <Calendar className="w-3.5 h-3.5 text-slate-400" /> Ngày sinh:
              </span>
              {isEditing ? (
                <input
                  type="date"
                  name="dob"
                  required
                  value={formData.dob}
                  onChange={handleChange}
                  className="w-full px-3 py-1.5 rounded-lg bg-white border border-slate-300 text-slate-900 font-semibold text-sm focus:outline-none focus:border-cyan-500"
                />
              ) : (
                <span className="text-slate-800 font-semibold">{formData.dob}</span>
              )}
            </div>

            {/* Địa chỉ */}
            <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200">
              <span className="text-xs text-slate-500 block font-medium flex items-center gap-1.5 mb-1">
                <MapPin className="w-3.5 h-3.5 text-slate-400" /> Địa chỉ cư trú:
              </span>
              {isEditing ? (
                <input
                  type="text"
                  name="address"
                  required
                  value={formData.address}
                  onChange={handleChange}
                  className="w-full px-3 py-1.5 rounded-lg bg-white border border-slate-300 text-slate-900 font-semibold text-sm focus:outline-none focus:border-cyan-500"
                />
              ) : (
                <span className="text-slate-800 font-semibold">{formData.address}</span>
              )}
            </div>
          </div>
        </form>
      </div>

      {/* 2. KHU VỰC BẢO MẬT & ĐỔI MẬT KHẨU */}
      <div className="bg-white border border-slate-200 shadow-sm rounded-2xl p-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex items-start gap-3.5">
            <div className="p-3 bg-cyan-50 rounded-xl text-cyan-600 border border-cyan-100 shrink-0">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900">Mật khẩu & Bảo mật</h3>
              <p className="text-xs text-slate-500 mt-1 leading-relaxed">
                Đổi mật khẩu định kỳ giúp tăng cường bảo mật cho tài khoản hồ sơ bệnh án của bạn.
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={() => {
              setShowPasswordModal(true);
              setPwdStep(1);
            }}
            className="px-4 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs flex items-center justify-center gap-2 transition shrink-0"
          >
            <Lock className="w-4 h-4 text-cyan-600" />
            <span>Đổi mật khẩu</span>
          </button>
        </div>
      </div>

      {/* MODAL OTP KHI THAY ĐỔI EMAIL / SỐ ĐIỆN THOẠI */}
      {showProfileOtpModal && (
        <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-2xl w-full max-w-md p-6 space-y-5">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
                <Smartphone className="w-5 h-5 text-cyan-600" /> Xác Thực OTP Số Điện Thoại
              </h3>
              <button onClick={() => setShowProfileOtpModal(false)} className="text-slate-400 hover:text-slate-600 p-1 rounded-lg">
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Thông báo chuẩn theo yêu cầu */}
            <p className="text-xs text-slate-600 leading-relaxed">
              Để thực hiện thay đổi, MEDIQ AI sẽ gửi mã SMS OTP gồm 6 chữ số đến số điện thoại: <b className="text-slate-900 font-bold">{formData.phone}</b>
            </p>

            {profileOtpError && (
              <div className="p-3 rounded-xl bg-red-50 border border-red-200 text-red-600 text-xs font-medium">
                {profileOtpError}
              </div>
            )}

            <form onSubmit={handleVerifyProfileOtp} className="space-y-4">
              <div className="flex justify-between gap-2">
                {profileOtp.map((digit, index) => (
                  <input
                    key={index}
                    id={`otp-input-${index}`}
                    type="text"
                    maxLength="1"
                    value={digit}
                    onChange={(e) => handleOtpInputChange(e.target.value, index, profileOtp, setProfileOtp)}
                    className="w-10 h-11 text-center text-base font-bold rounded-xl bg-slate-50 border border-slate-200 focus:outline-none focus:border-cyan-500"
                  />
                ))}
              </div>

              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full py-3 bg-cyan-600 hover:bg-cyan-500 text-white font-bold rounded-xl text-xs flex items-center justify-center gap-2 transition shadow-md shadow-cyan-600/20"
              >
                {isSubmitting ? (
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                ) : (
                  <span>Xác Nhận Thay Đổi</span>
                )}
              </button>

              <div className="text-center pt-1">
                {countdown > 0 ? (
                  <span className="text-[11px] text-slate-400">Gửi lại mã OTP sau <b className="text-cyan-600">{countdown}s</b></span>
                ) : (
                  <button type="button" onClick={() => setCountdown(60)} className="text-[11px] text-cyan-600 font-bold hover:underline">
                    Gửi lại mã OTP SMS
                  </button>
                )}
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL ĐỔI MẬT KHẨU BẢO MẬT */}
      {showPasswordModal && (
        <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-2xl w-full max-w-md p-6 space-y-5">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
                <ShieldCheck className="w-5 h-5 text-cyan-600" /> Đổi Mật Khẩu (Xác Thực SMS)
              </h3>
              <button
                onClick={() => setShowPasswordModal(false)}
                className="text-slate-400 hover:text-slate-600 p-1 rounded-lg"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {modalError && (
              <div className="p-3 rounded-xl bg-red-50 border border-red-200 text-red-600 text-xs">
                {modalError}
              </div>
            )}

            {pwdStep === 1 && (
              <div className="space-y-4">
                <p className="text-xs text-slate-600 leading-relaxed">
                  Để thực hiện thay đổi, MEDIQ AI sẽ gửi mã SMS OTP gồm 6 chữ số đến số điện thoại: <b className="text-slate-900 font-bold">{formData.phone}</b>
                </p>
                <button
                  type="button"
                  onClick={handleRequestPasswordOtp}
                  disabled={isSubmitting}
                  className="w-full py-3 bg-cyan-600 hover:bg-cyan-500 text-white font-bold rounded-xl text-xs flex items-center justify-center gap-2 transition shadow-md shadow-cyan-600/20"
                >
                  {isSubmitting ? (
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                  ) : (
                    <>
                      <Send className="w-4 h-4" />
                      <span>Gửi Mã OTP Đến Số Điện Thoại</span>
                    </>
                  )}
                </button>
              </div>
            )}

            {pwdStep === 2 && (
              <form onSubmit={handleChangePasswordSubmit} className="space-y-4">
                <div>
                  <label className="block text-xs font-bold text-slate-500 uppercase mb-2">1. Nhập Mã OTP Gửi Qua SMS</label>
                  <div className="flex justify-between gap-2">
                    {pwdOtp.map((digit, index) => (
                      <input
                        key={index}
                        id={`pwd-otp-input-${index}`}
                        type="text"
                        maxLength="1"
                        value={digit}
                        onChange={(e) => handleOtpInputChange(e.target.value, index, pwdOtp, setPwdOtp)}
                        className="w-10 h-11 text-center text-base font-bold rounded-xl bg-slate-50 border border-slate-200 focus:outline-none focus:border-cyan-500"
                      />
                    ))}
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-500 uppercase mb-1">2. Mật Khẩu Mới</label>
                  <input
                    type="password"
                    required
                    value={passwordData.newPassword}
                    onChange={(e) => setPasswordData({ ...passwordData, newPassword: e.target.value })}
                    placeholder="••••••••"
                    className="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-xs focus:outline-none focus:border-cyan-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-500 uppercase mb-1">3. Xác Nhận Mật Khẩu Mới</label>
                  <input
                    type="password"
                    required
                    value={passwordData.confirmPassword}
                    onChange={(e) => setPasswordData({ ...passwordData, confirmPassword: e.target.value })}
                    placeholder="••••••••"
                    className="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-xs focus:outline-none focus:border-cyan-500"
                  />
                </div>

                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="w-full py-3 bg-cyan-600 hover:bg-cyan-500 text-white font-bold rounded-xl text-xs flex items-center justify-center gap-2 transition shadow-md shadow-cyan-600/20"
                >
                  {isSubmitting ? (
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                  ) : (
                    <span>Xác Nhận Đổi Mật Khẩu</span>
                  )}
                </button>

                <div className="text-center pt-1">
                  {countdown > 0 ? (
                    <span className="text-[11px] text-slate-400">Gửi lại mã SMS sau <b className="text-cyan-600">{countdown}s</b></span>
                  ) : (
                    <button type="button" onClick={() => setCountdown(60)} className="text-[11px] text-cyan-600 font-bold hover:underline">
                      Gửi lại mã OTP SMS
                    </button>
                  )}
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
};