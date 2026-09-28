import React, { useState, useEffect } from "react";
import { useAuth } from "../../auth/AuthContext";
import { User, Mail, Phone, MapPin, Calendar, Edit3, Save, X, CheckCircle2, ShieldCheck } from "lucide-react";

export const PatientProfile = () => {
  const { user } = useAuth();

  // Trạng thái bật/tắt chế độ chỉnh sửa
  const [isEditing, setIsEditing] = useState(false);

  // Trạng thái lưu dữ liệu form
  const [formData, setFormData] = useState({
    name: "",
    phone: "",
    dob: "",
    address: ""
  });

  // Trạng thái thông báo cập nhật thành công
  const [showSuccessAlert, setShowSuccessAlert] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Khởi tạo dữ liệu người dùng ban đầu từ Context
  useEffect(() => {
    if (user) {
      setFormData({
        name: user.name || "Nguyễn Văn An",
        phone: user.phone || "0912 345 678",
        dob: user.dob || "1992-05-15",
        address: user.address || "Cầu Giấy, Hà Nội"
      });
    }
  }, [user]);

  // Lắng nghe thay đổi ô nhập liệu
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value
    }));
  };

  // Xử lý bấm Cập nhật
  const handleSubmit = (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    // Giả lập gửi request lên Patient Service backend
    setTimeout(() => {
      setIsSubmitting(false);
      setIsEditing(false);
      setShowSuccessAlert(true);

      // Tự động ẩn thông báo thành công sau 4 giây
      setTimeout(() => {
        setShowSuccessAlert(false);
      }, 4000);
    }, 600);
  };

  // Hủy chỉnh sửa & khôi phục dữ liệu ban đầu
  const handleCancel = () => {
    if (user) {
      setFormData({
        name: user.name || "Nguyễn Văn An",
        phone: user.phone || "0912 345 678",
        dob: user.dob || "1992-05-15",
        address: user.address || "Cầu Giấy, Hà Nội"
      });
    }
    setIsEditing(false);
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      {/* Thông báo cập nhật thành công */}
      {showSuccessAlert && (
        <div className="p-4 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm flex items-center justify-between shadow-sm animate-fade-in">
          <div className="flex items-center gap-3">
            <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0" />
            <span className="font-semibold">Cập nhật thông tin cá nhân thành công!</span>
          </div>
          <button
            onClick={() => setShowSuccessAlert(false)}
            className="text-emerald-600 hover:text-emerald-800 p-1 rounded-lg hover:bg-emerald-100 transition"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* Card Thông Tin Cá Nhân */}
      <div className="bg-white border border-slate-200 shadow-sm rounded-2xl p-6">
        <form onSubmit={handleSubmit}>
          {/* Header Thông tin & Nút Sửa / Cập nhật */}
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
                      className="px-3 py-1.5 rounded-lg bg-slate-50 border border-slate-300 text-slate-900 font-bold text-lg focus:outline-none focus:border-cyan-500 focus:bg-white"
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

            {/* Các Nút Thao Tác */}
            <div className="flex items-center gap-2">
              {!isEditing ? (
                <button
                  type="button"
                  onClick={() => setIsEditing(true)}
                  className="px-4 py-2.5 rounded-xl bg-cyan-600 hover:bg-cyan-500 text-white font-bold text-xs flex items-center gap-2 shadow-sm transition"
                >
                  <Edit3 className="w-4 h-4" />
                  <span>Chỉnh sửa</span>
                </button>
              ) : (
                <>
                  <button
                    type="button"
                    onClick={handleCancel}
                    disabled={isSubmitting}
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

          {/* Lưới hiển thị các trường dữ liệu */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mt-6 text-sm">
            {/* Email (Cố định, không chỉnh sửa) */}
            <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200">
              <span className="text-xs text-slate-500 block font-medium flex items-center gap-1.5 mb-1">
                <Mail className="w-3.5 h-3.5 text-slate-400" />
                Email tài khoản:
              </span>
              <span className="text-slate-800 font-semibold">{user?.email || "patient@mediq.ai"}</span>
            </div>

            {/* Số điện thoại */}
            <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200">
              <span className="text-xs text-slate-500 block font-medium flex items-center gap-1.5 mb-1">
                <Phone className="w-3.5 h-3.5 text-slate-400" />
                Số điện thoại:
              </span>
              {isEditing ? (
                <input
                  type="text"
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
                <Calendar className="w-3.5 h-3.5 text-slate-400" />
                Ngày sinh:
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
                <MapPin className="w-3.5 h-3.5 text-slate-400" />
                Địa chỉ cư trú:
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
    </div>
  );
};