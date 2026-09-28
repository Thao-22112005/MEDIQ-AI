import React, { useState } from "react";
import { MOCK_SPECIALTIES } from "../../mocks/mockAccounts";
import { MOCK_DOCTORS } from "../../mocks/mockDoctors";
import { Calendar as CalendarIcon, Clock, CheckCircle, Edit3 } from "lucide-react";

export const PatientBooking = () => {
  const [selectedSpecialty, setSelectedSpecialty] = useState("Tim mạch");
  const [selectedDoctor, setSelectedDoctor] = useState(MOCK_DOCTORS[0]);
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split("T")[0]);
  const [selectedSlot, setSelectedSlot] = useState("09:30");
  const [isCustomTime, setIsCustomTime] = useState(false);
  const [booked, setBooked] = useState(false);

  const filteredDoctors = MOCK_DOCTORS.filter((d) => d.specialty === selectedSpecialty);

  const handleBook = (e) => {
    e.preventDefault();
    setBooked(true);
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="bg-white border border-slate-200 shadow-sm rounded-2xl p-6">
        <h1 className="text-xl font-bold text-slate-900 mb-2">Đặt Lịch Khám Thông Minh</h1>
        <p className="text-xs text-slate-500 font-medium">Chọn chuyên khoa, ngày khám, bác sĩ và khung giờ theo nhu cầu của bạn.</p>

        {booked ? (
          <div className="mt-8 p-6 bg-emerald-50 border border-emerald-200 rounded-2xl text-center space-y-3">
            <CheckCircle className="w-12 h-12 text-emerald-600 mx-auto" />
            <h3 className="text-lg font-bold text-slate-900">Đặt Lịch Khám Thành Công!</h3>
            <p className="text-xs text-slate-600">
              Mã khám của bạn đã được ghi nhận vào Appointment Service. Bác sĩ <b>{selectedDoctor.name}</b> sẽ tiếp nhận lịch khám ngày <b>{selectedDate}</b> vào lúc <b>{selectedSlot}</b>.
            </p>
            <button
              onClick={() => setBooked(false)}
              className="mt-4 px-4 py-2 bg-slate-100 hover:bg-slate-200 text-xs font-semibold rounded-xl text-slate-700 transition"
            >
              Đặt thêm lịch mới
            </button>
          </div>
        ) : (
          <form onSubmit={handleBook} className="mt-6 space-y-6">
            {/* 1. Chọn Chuyên Khoa */}
            <div>
              <label className="block text-xs font-bold text-slate-500 uppercase mb-3">1. Chọn Chuyên Khoa</label>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                {MOCK_SPECIALTIES.map((sp) => (
                  <button
                    type="button"
                    key={sp.id}
                    onClick={() => {
                      setSelectedSpecialty(sp.name);
                      const docs = MOCK_DOCTORS.filter((d) => d.specialty === sp.name);
                      if (docs.length > 0) setSelectedDoctor(docs[0]);
                    }}
                    className={`p-3 rounded-xl border text-left text-xs font-semibold transition ${
                      selectedSpecialty === sp.name
                        ? "bg-cyan-50 border-cyan-500 text-cyan-800 shadow-xs"
                        : "bg-slate-50 border-slate-200 text-slate-600 hover:bg-slate-100"
                    }`}
                  >
                    <div>{sp.name}</div>
                    <div className="text-[10px] text-slate-400 font-normal mt-1">{sp.room}</div>
                  </button>
                ))}
              </div>
            </div>

            {/* 2. Chọn Ngày Khám */}
            <div>
              <label className="block text-xs font-bold text-slate-500 uppercase mb-3 flex items-center gap-1.5">
                <CalendarIcon className="w-4 h-4 text-cyan-600" />
                2. Chọn Ngày Dự Kiến Khám
              </label>
              <input
                type="date"
                required
                min={new Date().toISOString().split("T")[0]}
                value={selectedDate}
                onChange={(e) => setSelectedDate(e.target.value)}
                className="w-full sm:w-64 px-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-800 text-sm font-semibold focus:outline-none focus:border-cyan-500 focus:bg-white"
              />
            </div>

            {/* 3. Chọn Bác Sĩ */}
            <div>
              <label className="block text-xs font-bold text-slate-500 uppercase mb-3">3. Chọn Bác Sĩ Chuyên Khoa</label>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {filteredDoctors.map((doc) => (
                  <div
                    key={doc.id}
                    onClick={() => setSelectedDoctor(doc)}
                    className={`p-4 rounded-xl border cursor-pointer flex items-center gap-3 transition ${
                      selectedDoctor?.id === doc.id
                        ? "bg-cyan-50 border-cyan-500 text-slate-900 shadow-xs"
                        : "bg-slate-50 border-slate-200 text-slate-600 hover:bg-slate-100"
                    }`}
                  >
                    <img src={doc.avatar} alt={doc.name} className="w-10 h-10 rounded-full object-cover border border-slate-200" />
                    <div>
                      <p className="text-xs font-bold text-slate-800">{doc.name}</p>
                      <p className="text-[11px] text-slate-500">Kinh nghiệm: {doc.experience} | Đánh giá: ⭐ {doc.rating}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* 4. Chọn/Nhập Khung Giờ Khám Linh Hoạt */}
            <div>
              <label className="block text-xs font-bold text-slate-500 uppercase mb-3 flex items-center justify-between">
                <span className="flex items-center gap-1.5">
                  <Clock className="w-4 h-4 text-cyan-600" />
                  4. Chọn Hoặc Nhập Thời Gian Khám
                </span>
              </label>

              {/* Các nút bấm chọn nhanh */}
              <div className="flex flex-wrap gap-2 mb-3">
                {["08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "14:00", "14:30", "15:00", "15:30"].map((slot) => (
                  <button
                    type="button"
                    key={slot}
                    onClick={() => {
                      setSelectedSlot(slot);
                      setIsCustomTime(false);
                    }}
                    className={`px-3.5 py-2 rounded-xl border text-xs font-mono font-semibold transition ${
                      selectedSlot === slot && !isCustomTime
                        ? "bg-cyan-600 text-white border-cyan-600 shadow-xs"
                        : "bg-slate-50 border-slate-200 text-slate-700 hover:bg-slate-100"
                    }`}
                  >
                    {slot}
                  </button>
                ))}
              </div>

              {/* Ô chọn thời gian tùy chỉnh */}
              <div className="p-3.5 bg-slate-50 border border-slate-200 rounded-xl flex items-center gap-3 w-full sm:w-80">
                <Edit3 className="w-4 h-4 text-cyan-600 shrink-0" />
                <span className="text-xs font-medium text-slate-600 shrink-0">Giờ tùy chọn:</span>
                <input
                  type="time"
                  required
                  value={selectedSlot}
                  onChange={(e) => {
                    setSelectedSlot(e.target.value);
                    setIsCustomTime(true);
                  }}
                  className="flex-1 px-3 py-1.5 rounded-lg bg-white border border-slate-300 text-slate-900 font-mono font-bold text-sm focus:outline-none focus:border-cyan-500"
                />
              </div>
            </div>

            <button
              type="submit"
              className="w-full py-3.5 bg-cyan-600 hover:bg-cyan-500 text-white font-bold rounded-xl transition shadow-md shadow-cyan-600/20"
            >
              Xác Nhận Đặt Lịch ({selectedDoctor?.name} - {selectedDate} lúc {selectedSlot})
            </button>
          </form>
        )}
      </div>
    </div>
  );
};