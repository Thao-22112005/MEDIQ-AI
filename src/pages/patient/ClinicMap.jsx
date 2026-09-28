import React, { useState } from "react";
import { Search, MapPin, Building2, Compass, CheckCircle2, Clock, AlertTriangle } from "lucide-react";
import { MOCK_ROOMS } from "../../mocks/mockRooms";

export const ClinicMap = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedBuilding, setSelectedBuilding] = useState("ALL");
  const [selectedRoom, setSelectedRoom] = useState(null);

  // Lọc danh sách phòng theo từ khóa và khu vực
  const filteredRooms = MOCK_ROOMS.filter((room) => {
    const matchesSearch =
      room.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      room.department.toLowerCase().includes(searchTerm.toLowerCase()) ||
      room.doctor.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesBuilding = selectedBuilding === "ALL" || room.name.includes(selectedBuilding);

    return matchesSearch && matchesBuilding;
  });

  return (
    <div className="space-y-6">
      {/* Header Trang */}
      <div className="bg-white border border-slate-200 p-6 rounded-2xl shadow-sm flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
            <Compass className="w-6 h-6 text-cyan-600" />
            Sơ Đồ & Định Vị Phòng Khám
          </h1>
          <p className="text-xs text-slate-500 mt-1 font-medium">
            Tra cứu vị trí phòng khám, chuyên khoa và hướng dẫn di chuyển trong khuôn viên bệnh viện.
          </p>
        </div>
      </div>

      {/* Thanh Tìm Kiếm & Bô Lọc */}
      <div className="bg-white border border-slate-200 p-4 rounded-2xl shadow-sm flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Tìm theo tên phòng (P.201), chuyên khoa (Tim mạch), bác sĩ..."
            className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-sm text-slate-800 focus:outline-none focus:border-cyan-500 focus:bg-white"
          />
        </div>

        <div className="flex gap-2">
          {["ALL", "Phòng 1", "Phòng 2", "Phòng 3"].map((building) => (
            <button
              key={building}
              onClick={() => setSelectedBuilding(building)}
              className={`px-3.5 py-2 rounded-xl text-xs font-semibold border transition ${
                selectedBuilding === building
                  ? "bg-cyan-600 text-white border-cyan-600 shadow-xs"
                  : "bg-slate-50 border-slate-200 text-slate-600 hover:bg-slate-100"
              }`}
            >
              {building === "ALL" ? "Tất cả khu" : building}
            </button>
          ))}
        </div>
      </div>

      {/* Khu vực Bản Đồ Tương Tác & Chi Tiết */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Cột Trái: Sơ đồ Mặt Bằng Trực Quan (Floor Plan Grid) */}
        <div className="lg:col-span-8 bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-bold text-slate-800 uppercase tracking-wider flex items-center gap-2">
              <Building2 className="w-4 h-4 text-cyan-600" />
              Sơ Đồ Tổng Quan Tầng Khám
            </h3>
            <div className="flex items-center gap-3 text-[11px] font-semibold text-slate-500">
              <span className="flex items-center gap-1">
                <span className="w-2.5 h-2.5 rounded-full bg-emerald-500"></span> Đang hoạt động
              </span>
              <span className="flex items-center gap-1">
                <span className="w-2.5 h-2.5 rounded-full bg-cyan-500"></span> Sẵn sàng
              </span>
              <span className="flex items-center gap-1">
                <span className="w-2.5 h-2.5 rounded-full bg-amber-500"></span> Bảo trì
              </span>
            </div>
          </div>

          {/* Grid Minh Họa Sơ Đồ */}
          <div className="grid grid-cols-2 sm:grid-cols-2 gap-4 p-4 bg-slate-50 border border-slate-200 rounded-xl">
            {filteredRooms.map((room) => {
              const isSelected = selectedRoom?.id === room.id;
              return (
                <div
                  key={room.id}
                  onClick={() => setSelectedRoom(room)}
                  className={`p-4 rounded-xl border cursor-pointer transition flex flex-col justify-between h-32 ${
                    isSelected
                      ? "bg-cyan-50 border-cyan-500 ring-2 ring-cyan-500/20 shadow-md"
                      : "bg-white border-slate-200 hover:border-cyan-300 hover:shadow-xs"
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <span className="text-xs font-mono font-bold text-cyan-700">{room.name}</span>
                      <p className="text-sm font-bold text-slate-800 mt-0.5">{room.department}</p>
                    </div>
                    <span
                      className={`w-2.5 h-2.5 rounded-full ${
                        room.status === "Đang hoạt động"
                          ? "bg-emerald-500"
                          : room.status === "Sẵn sàng"
                          ? "bg-cyan-500"
                          : "bg-amber-500"
                      }`}
                    ></span>
                  </div>

                  <div className="text-[11px] text-slate-500 border-t border-slate-100 pt-2 flex items-center justify-between">
                    <span>{room.doctor}</span>
                    <MapPin className="w-3.5 h-3.5 text-cyan-600" />
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Cột Phải: Thông tin Chỉ Đường Chi Tiết */}
        <div className="lg:col-span-4 space-y-4">
          <div className="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm">
            <h3 className="text-sm font-bold text-slate-800 mb-3 flex items-center gap-2">
              <MapPin className="w-4 h-4 text-cyan-600" />
              Chi Tiết Vị Trí
            </h3>

            {selectedRoom ? (
              <div className="space-y-4">
                <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200">
                  <span className="text-xs text-slate-400 font-bold uppercase">Tên phòng & Chuyên khoa</span>
                  <p className="text-base font-bold text-slate-900 mt-0.5">{selectedRoom.name} - {selectedRoom.department}</p>
                  <p className="text-xs text-slate-500 mt-1">Bác sĩ phụ trách: <b>{selectedRoom.doctor}</b></p>
                </div>

                <div className="p-3.5 bg-cyan-50 rounded-xl border border-cyan-100 space-y-2">
                  <span className="text-xs font-bold text-cyan-800 uppercase flex items-center gap-1.5">
                    <Compass className="w-4 h-4 text-cyan-600" /> Hướng dẫn di chuyển:
                  </span>
                  <p className="text-xs text-slate-700 leading-relaxed font-medium">
                    Từ sảnh chính $\rightarrow$ Đi theo hành lang A $\rightarrow$ Lên thang máy tầng 2 $\rightarrow$ Phòng nằm phía bên tay phải.
                  </p>
                </div>

                <div className="flex items-center justify-between text-xs p-3 bg-slate-50 rounded-xl border border-slate-200">
                  <span className="text-slate-500 font-medium">Trạng thái phòng:</span>
                  <span className="font-bold text-emerald-700 bg-emerald-50 px-2.5 py-0.5 rounded-full border border-emerald-200">
                    {selectedRoom.status}
                  </span>
                </div>
              </div>
            ) : (
              <div className="text-center py-10 text-slate-400 text-xs font-medium">
                Vui lòng chọn một phòng trên sơ đồ để xem chỉ đường và thông tin chi tiết.
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};