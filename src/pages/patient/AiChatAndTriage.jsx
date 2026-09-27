import React, { useState } from "react";
import { Bot, Send, ShieldAlert, Sparkles, User, Calendar } from "lucide-react";
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
      <div className="lg:col-span-7 bg-slate-900 border border-slate-800 rounded-2xl flex flex-col h-full overflow-hidden">
        <div className="p-4 border-b border-slate-800 flex items-center justify-between bg-slate-950/50">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-cyan-500/10 rounded-xl text-cyan-400 border border-cyan-500/20">
              <Bot className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-white">AI Medical Chatbot</h2>
              <p className="text-[11px] text-cyan-400">Tự động phân loại triệu chứng (Triage AI)</p>
            </div>
          </div>
          <span className="text-xs px-2.5 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-mono">
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
                  m.sender === "user" ? "bg-cyan-500 text-slate-950" : "bg-slate-800 text-cyan-400 border border-slate-700"
                }`}
              >
                {m.sender === "user" ? <User className="w-4 h-4" /> : <Bot className="w-4 h-4" />}
              </div>
              <div
                className={`p-3.5 rounded-2xl text-sm leading-relaxed ${
                  m.sender === "user"
                    ? "bg-cyan-500 text-slate-950 font-medium rounded-tr-none"
                    : "bg-slate-950 border border-slate-800 text-slate-200 rounded-tl-none"
                }`}
              >
                {m.text}
              </div>
            </div>
          ))}
        </div>

        <form onSubmit={handleSend} className="p-4 border-t border-slate-800 bg-slate-950/50 flex gap-2">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Mô tả triệu chứng của bạn (vd: đau đầu, tức ngực, sốt...)..."
            className="flex-1 bg-slate-900 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
          />
          <button
            type="submit"
            className="px-4 py-2.5 bg-cyan-500 hover:bg-cyan-400 text-slate-950 rounded-xl font-bold flex items-center gap-1 transition"
          >
            <Send className="w-4 h-4" />
          </button>
        </form>
      </div>

      {/* Triage & Smart Booking Result */}
      <div className="lg:col-span-5 space-y-6 overflow-y-auto">
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
          <div className="flex items-center gap-2 text-cyan-400 font-bold mb-3">
            <Sparkles className="w-5 h-5" />
            <span>Kết quả Đánh giá Risk Triage</span>
          </div>

          {triageResult ? (
            <div className="space-y-4">
              <div className="p-3 bg-amber-500/10 border border-amber-500/20 rounded-xl flex items-center justify-between">
                <span className="text-xs text-amber-300 font-medium">Mức độ rủi ro:</span>
                <span className="text-xs font-bold px-2.5 py-1 rounded bg-amber-500/20 text-amber-400 font-mono">
                  {triageResult.level}
                </span>
              </div>

              <div>
                <p className="text-xs text-slate-400">Chuyên khoa đề xuất:</p>
                <p className="text-sm font-bold text-white mt-0.5">{triageResult.specialty}</p>
              </div>

              <div>
                <p className="text-xs text-slate-400">Lời khuyên AI:</p>
                <p className="text-xs text-slate-300 mt-1 leading-relaxed bg-slate-950 p-3 rounded-xl border border-slate-800">
                  {triageResult.recommendation}
                </p>
              </div>

              <div>
                <p className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Bác sĩ đề xuất:</p>
                {triageResult.doctors.map((doc) => (
                  <div key={doc.id} className="p-3 bg-slate-950 border border-slate-800 rounded-xl flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <img src={doc.avatar} alt={doc.name} className="w-9 h-9 rounded-full object-cover" />
                      <div>
                        <p className="text-xs font-bold text-white">{doc.name}</p>
                        <p className="text-[11px] text-slate-400">{doc.specialty} • {doc.experience}</p>
                      </div>
                    </div>
                    <button className="px-3 py-1.5 bg-cyan-500/10 text-cyan-400 border border-cyan-500/30 rounded-lg text-xs font-semibold hover:bg-cyan-500/20">
                      Chọn Lịch
                    </button>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="text-center py-8 text-slate-500 text-xs">
              Nhập triệu chứng vào khung chat bên cạnh để AI tự động phân loại rủi ro và gợi ý bác sĩ.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};