import React, { useState } from "react";
import { Bot, Send, User, Sparkles, ShieldAlert } from "lucide-react";
import { MOCK_DOCTORS } from "../../mocks/mockDoctors";

export const AiChatAndTriage = () => {
  const [messages, setMessages] = useState([
    { sender: "ai", text: "Xin chào! Tôi là MEDIQ AI. Bạn đang gặp phải các triệu chứng sức khỏe nào?" }
  ]);
  const [input, setInput] = useState("");
  const [triageResult, setTriageResult] = useState(null);

  const handleSend = (e) => {
    e.preventDefault();
    if (!input.trim()) return;

    const userText = input;
    setMessages((prev) => [...prev, { sender: "user", text: userText }]);
    setInput("");

    setTimeout(() => {
      setMessages((prev) => [
        ...prev,
        {
          sender: "ai",
          text: `Dựa trên triệu chứng "${userText}", AI đánh giá mức độ ưu tiên của bạn và gợi ý Chuyên Khoa Tim Mạch / Nội Khoa.`
        }
      ]);

      setTriageResult({
        level: "MEDIUM RISK",
        specialty: "Tim mạch",
        recommendation: "Nên đăng ký khám trực tiếp trong vòng 24h để thực hiện đo điện tâm đồ (ECG).",
        doctors: MOCK_DOCTORS.filter((d) => d.specialty === "Tim mạch")
      });
    }, 800);
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 h-[calc(100vh-8rem)]">
      {/* AI Chat Window */}
      <div className="lg:col-span-7 bg-white border border-slate-200 shadow-sm rounded-2xl flex flex-col h-full overflow-hidden">
        <div className="p-4 border-b border-slate-200 flex items-center justify-between bg-slate-50">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-cyan-100 rounded-xl text-cyan-600 border border-cyan-200">
              <Bot className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-slate-800">AI Medical Chatbot</h2>
              <p className="text-[11px] text-cyan-600 font-medium">Tự động phân loại triệu chứng (Triage AI)</p>
            </div>
          </div>
          <span className="text-xs px-2.5 py-1 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200 font-mono font-semibold">
            Active
          </span>
        </div>

        <div className="flex-1 p-4 overflow-y-auto space-y-4">
          {messages.map((m, idx) => (
            <div
              key={idx}
              className={`flex gap-3 max-w-[85%] ${m.sender === "user" ? "ml-auto flex-row-reverse" : ""}`}
            >
              <div
                className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 text-xs font-bold ${
                  m.sender === "user" ? "bg-cyan-600 text-white" : "bg-slate-100 text-cyan-700 border border-slate-200"
                }`}
              >
                {m.sender === "user" ? <User className="w-4 h-4" /> : <Bot className="w-4 h-4" />}
              </div>
              <div
                className={`p-3.5 rounded-2xl text-sm leading-relaxed ${
                  m.sender === "user"
                    ? "bg-cyan-600 text-white font-medium rounded-tr-none shadow-xs"
                    : "bg-slate-50 border border-slate-200 text-slate-800 rounded-tl-none"
                }`}
              >
                {m.text}
              </div>
            </div>
          ))}
        </div>

        <form onSubmit={handleSend} className="p-4 border-t border-slate-200 bg-slate-50 flex gap-2">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Mô tả triệu chứng của bạn (vd: đau đầu, tức ngực, sốt...)..."
            className="flex-1 bg-white border border-slate-200 rounded-xl px-4 py-2.5 text-sm text-slate-800 placeholder-slate-400 focus:outline-none focus:border-cyan-500"
          />
          <button
            type="submit"
            className="px-5 py-2.5 bg-cyan-600 hover:bg-cyan-500 text-white rounded-xl font-bold flex items-center gap-1 transition shadow-sm"
          >
            <Send className="w-4 h-4" />
          </button>
        </form>
      </div>

      {/* Triage & Smart Booking Result */}
      <div className="lg:col-span-5 space-y-6 overflow-y-auto">
        <div className="bg-white border border-slate-200 shadow-sm rounded-2xl p-5">
          <div className="flex items-center gap-2 text-cyan-700 font-bold mb-3">
            <Sparkles className="w-5 h-5 text-cyan-600" />
            <span>Kết quả Đánh giá Risk Triage</span>
          </div>

          {triageResult ? (
            <div className="space-y-4">
              <div className="p-3 bg-amber-50 border border-amber-200 rounded-xl flex items-center justify-between">
                <span className="text-xs text-amber-800 font-medium">Mức độ rủi ro:</span>
                <span className="text-xs font-bold px-2.5 py-1 rounded bg-amber-100 text-amber-800 font-mono">
                  {triageResult.level}
                </span>
              </div>

              <div>
                <p className="text-xs text-slate-500 font-medium">Chuyên khoa đề xuất:</p>
                <p className="text-sm font-bold text-slate-800 mt-0.5">{triageResult.specialty}</p>
              </div>

              <div>
                <p className="text-xs text-slate-500 font-medium">Lời khuyên AI:</p>
                <p className="text-xs text-slate-700 mt-1 leading-relaxed bg-slate-50 p-3 rounded-xl border border-slate-200">
                  {triageResult.recommendation}
                </p>
              </div>

              <div>
                <p className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Bác sĩ đề xuất:</p>
                {triageResult.doctors.map((doc) => (
                  <div key={doc.id} className="p-3 bg-slate-50 border border-slate-200 rounded-xl flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <img src={doc.avatar} alt={doc.name} className="w-9 h-9 rounded-full object-cover border border-slate-200" />
                      <div>
                        <p className="text-xs font-bold text-slate-800">{doc.name}</p>
                        <p className="text-[11px] text-slate-500">{doc.specialty} • {doc.experience}</p>
                      </div>
                    </div>
                    <button className="px-3 py-1.5 bg-cyan-600 hover:bg-cyan-500 text-white rounded-lg text-xs font-semibold shadow-xs transition">
                      Chọn Lịch
                    </button>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="text-center py-8 text-slate-400 text-xs font-medium">
              Nhập triệu chứng vào khung chat bên cạnh để AI tự động phân loại rủi ro và gợi ý bác sĩ.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};