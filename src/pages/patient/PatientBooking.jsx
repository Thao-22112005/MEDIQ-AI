import React, { useState } from "react";
import { MOCK_SPECIALTIES } from "../../mocks/mockAccounts";
import { MOCK_DOCTORS } from "../../mocks/mockDoctors";
import { Calendar, Clock, CheckCircle } from "lucide-react";

export const PatientBooking = () => {
  const [selectedSpecialty, setSelectedSpecialty] = useState("Tim mạch");
  const [selectedDoctor, setSelectedDoctor] = useState(MOCK_DOCTORS[0]);
  const [selectedSlot, setSelectedSlot] = useState("09:30 AM");
  const [booked, setBooked] = useState(false);

  const filteredDoctors = MOCK_DOCTORS.filter((d) => d.specialty === selectedSpecialty);

  const handleBook = (e) => {
    e.preventDefault();
    setBooked(true);
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
        <h1 className="text-xl font-bold text-white mb-2">Đặt Lịch Khám Thông Minh</h1>
        <p className="text-xs text-slate-400">Chọn chuyên khoa, bác sĩ và khung giờ theo nhu cầu của bạn.</p>

        {booked ? (
          <div className="mt-8 p-6 bg-emerald-500/10 border border-emerald-500/20 rounded-2xl text-center space-y-3">
            <CheckCircle className="w-12 h-12 text-emerald-400 mx-auto" />
            <h3 className="text-lg font-bold text-white">Đặt Lịch Khám Thành Công!</h3>
            <p className="text-xs text-slate-300">
              Mã khám của bạn đã được ghi nhận vào Appointment Service. Bác sĩ <b>{selectedDoctor.name}</b> sẽ tiếp nhận lịch khám lúc <b>{selectedSlot}</b>.
            </p>
            <button
              onClick={() => setBooked(false)}
              className="mt-4 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-xs font-semibold rounded-xl text-slate-200"
            >
              Đặt thêm lịch mới
            </button>
          </div>
        ) : (
          <form onSubmit={handleBook} className="mt-6 space-y-6">
            {/* 1. Select Specialty */}
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase mb-3">1. Chọn Chuyên Khoa</label>
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
                        ? "bg-cyan-500/20 border-cyan-500 text-cyan-300"
                        : "bg-slate-950 border-slate-800 text-slate-400 hover:bg-slate-800"
                    }`}
                  >
                    <div>{sp.name}</div>
                    <div className="text-[10px] text-slate-500 font-normal mt-1">{sp.room}</div>
                  </button>
                ))}
              </div>
            </div>

            {/* 2. Select Doctor */}
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase mb-3">2. Chọn Bác Sĩ Chuyên Khoa</label>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {filteredDoctors.map((doc) => (
                  <div
                    key={doc.id}
                    onClick={() => setSelectedDoctor(doc)}
                    className={`p-4 rounded-xl border cursor-pointer flex items-center gap-3 transition ${
                      selectedDoctor?.id === doc.id
                        ? "bg-cyan-500/10 border-cyan-500 text-white"
                        : "bg-slate-950 border-slate-800 text-slate-400 hover:bg-slate-800"
                    }`}
                  >
                    <img src={doc.avatar} alt={doc.name} className="w-10 h-10 rounded-full object-cover" />
                    <div>
                      <p className="text-xs font-bold text-white">{doc.name}</p>
                      <p className="text-[11px] text-slate-400">Kinh nghiệm: {doc.experience} | Đánh giá: ⭐ {doc.rating}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* 3. Select Time */}
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase mb-3">3. Chọn Khung Giờ Khám</label>
              <div className="flex flex-wrap gap-3">
                {selectedDoctor?.availableSlots.map((slot) => (
                  <button
                    type="button"
                    key={slot}
                    onClick={() => setSelectedSlot(slot)}
                    className={`px-4 py-2 rounded-xl border text-xs font-mono font-medium transition ${
                      selectedSlot === slot
                        ? "bg-cyan-500 text-slate-950 border-cyan-500 font-bold"
                        : "bg-slate-950 border-slate-800 text-slate-300 hover:bg-slate-800"
                    }`}
                  >
                    {slot}
                  </button>
                ))}
              </div>
            </div>

            <button
              type="submit"
              className="w-full py-3.5 bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold rounded-xl transition shadow-lg shadow-cyan-500/20"
            >
              Xác Nhận Đặt Lịch ({selectedDoctor?.name} - {selectedSlot})
            </button>
          </form>
        )}
      </div>
    </div>
  );
};