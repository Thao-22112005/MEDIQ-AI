package com.mediqai.ai.service;

import com.mediqai.ai.client.GeminiClient;
import com.mediqai.ai.dto.request.ChatRequest;
import com.mediqai.ai.dto.response.AiAnalysisResponse;
import com.mediqai.ai.dto.response.ChatResponse;
import com.mediqai.ai.dto.response.ConversationResponse;
import com.mediqai.ai.dto.response.MessageResponse;
import com.mediqai.ai.entity.Conversation;
import com.mediqai.ai.entity.Message;
import com.mediqai.ai.repository.ConversationRepository;
import com.mediqai.ai.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {

    private final GeminiClient geminiClient;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AiAnalysisParser aiAnalysisParser;

    @Transactional
    public ChatResponse chat(
            String patientEmail,
            ChatRequest request
    ) {

        Conversation conversation;

        // 1. TẠO CUỘC HỘI THOẠI MỚI
        if (request.getConversationId() == null) {

            conversation = Conversation.builder()
                    .patientEmail(patientEmail)
                    .build();

            conversation = conversationRepository.save(conversation);
        }

        // 2. LẤY CUỘC HỘI THOẠI CŨ
        else {

            conversation = conversationRepository
                    .findById(request.getConversationId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Không tìm thấy cuộc hội thoại"
                            )
                    );

            // 3. KIỂM TRA QUYỀN TRUY CẬP
            if (!conversation.getPatientEmail()
                    .equalsIgnoreCase(patientEmail)) {

                throw new RuntimeException(
                        "Bạn không có quyền truy cập cuộc hội thoại này"
                );
            }
        }

        // 4. LƯU TIN NHẮN BỆNH NHÂN
        Message patientMessage = Message.builder()
                .conversationId(conversation.getId())
                .sender(Message.Sender.PATIENT)
                .content(request.getMessage())
                .build();

        messageRepository.save(patientMessage);

        // 5. LẤY TOÀN BỘ LỊCH SỬ CHAT
        List<Message> messages =
                messageRepository
                        .findByConversationIdOrderByCreatedAtAsc(
                                conversation.getId()
                        );

        String conversationHistory = messages.stream()
                .map(message ->
                        message.getSender()
                                + ": "
                                + message.getContent()
                )
                .collect(Collectors.joining("\n"));

        // 6. TẠO PROMPT CHO GEMINI
        String prompt = """
        Bạn là trợ lý AI tư vấn sức khỏe của hệ thống phòng khám thông minh MEDIQ-AI.

        Người dùng hiện tại là bệnh nhân.

        NHIỆM VỤ:
        - Thu thập thông tin về triệu chứng thông qua hội thoại.
        - Không tự chẩn đoán bệnh.
        - Không tự kê đơn thuốc.
        - Khi có đủ thông tin, đánh giá sơ bộ mức độ cần đi khám và gợi ý chuyên khoa.
        - Nếu phát hiện dấu hiệu nguy hiểm, ưu tiên cảnh báo và khuyến nghị đi khám/cấp cứu.

        QUY TẮC HỎI THÊM:
                
        1. Mục tiêu là thu thập đủ thông tin để đánh giá sơ bộ mức độ khẩn cấp và gợi ý chuyên khoa.

        2. Nếu thông tin còn thiếu:
           - Chỉ hỏi tối đa 1-2 câu hỏi quan trọng nhất trong mỗi lượt.
           - Không hỏi lại thông tin bệnh nhân đã cung cấp.
           - Ưu tiên câu hỏi giúp phân biệt mức độ nguy hiểm.
           - Không hỏi máy móc tất cả các thông tin nếu không cần thiết.

        3. Với mỗi triệu chứng chính, ưu tiên thu thập:
           - Triệu chứng nằm ở đâu.
           - Bắt đầu từ khi nào.
           - Mức độ từ 0-10.
           - Tính chất hoặc diễn biến của triệu chứng.
           - Triệu chứng đi kèm quan trọng.

        4. Nếu triệu chứng có khả năng liên quan đến tình trạng cấp tính, ưu tiên kiểm tra các dấu hiệu cảnh báo trước.

        5. Không cần thu thập đầy đủ mọi thông tin nếu đã đủ cơ sở để đánh giá sơ bộ.

        6. Cố gắng hoàn thành việc thu thập thông tin trong khoảng 3-5 lượt hỏi.

        7. Khi đã có đủ thông tin để đánh giá sơ bộ:
           - status = READY
           - Không hỏi thêm.
           - Điền symptoms.
           - Điền triageLevel.
           - Điền specialty.
           - Điền summary.
           - reply phải giải thích ngắn gọn kết quả và hướng xử lý tiếp theo.

        8. Nếu phát hiện dấu hiệu nguy hiểm:
           - status = EMERGENCY
           - triageLevel = EMERGENCY
           - Không tiếp tục hỏi những câu hỏi không cần thiết.
           - reply phải ưu tiên cảnh báo và khuyến nghị đi cấp cứu hoặc đến cơ sở y tế gần nhất.

        QUY TẮC TRIAGE:

        LOW:
        - Triệu chứng nhẹ.
        - Không có dấu hiệu cảnh báo.
        - Có thể theo dõi và chăm sóc phù hợp.

        MEDIUM:
        - Triệu chứng cần được bác sĩ đánh giá nhưng chưa có dấu hiệu cấp cứu.
        - Có thể khuyến nghị đặt lịch khám.

        HIGH:
        - Triệu chứng đáng lo ngại hoặc có khả năng diễn tiến xấu.
        - Nên đi khám sớm.

        EMERGENCY:
        - Có dấu hiệu có thể đe dọa tính mạng hoặc cần cấp cứu.
        - Ưu tiên đến cơ sở y tế/cấp cứu ngay.

        LƯU Ý:
        - Đây chỉ là đánh giá sơ bộ, không phải chẩn đoán y khoa.
        - Không được khẳng định bệnh nhân mắc một bệnh cụ thể.
        - Không được tự kê đơn thuốc.
        
        QUY TẮC SPECIALTY:
        
        Chỉ được chọn MỘT trong các mã sau:
        
        CAP_CUU
        NOI_TONG_QUAT
        TIM_MACH
        HO_HAP
        TIEU_HOA
        THAN_KINH
        TAI_MUI_HONG
        MAT
        DA_LIEU
        CO_XUONG_KHOP
        NOI_TIET
        TIET_NIEU
        SAN_PHU_KHOA
        RANG_HAM_MAT
        KHAC
        CHUA_XAC_DINH
        
        Không được tự tạo mã chuyên khoa khác.
        
        Quy tắc:
        - Nếu phát hiện tình trạng cần cấp cứu: specialty = CAP_CUU.
        - Nếu đã đủ thông tin để xác định chuyên khoa phù hợp: chọn một mã phù hợp.
        - Nếu chưa đủ thông tin: specialty = CHUA_XAC_DINH.
        - Nếu triệu chứng không phù hợp với các chuyên khoa trên: specialty = KHAC.
        - Không trả về tên chuyên khoa bằng tiếng Việt.
        - Chỉ trả về mã enum.
                
        QUY ĐỊNH OUTPUT:
        Chỉ trả về JSON hợp lệ.
        Không được thêm markdown.
        Không được thêm ```json.
        Không được thêm bất kỳ nội dung nào bên ngoài JSON.

        JSON phải có đúng cấu trúc:

        {
          "status": "NEED_MORE_INFO hoặc READY hoặc EMERGENCY",
          "reply": "Câu trả lời tự nhiên dành cho bệnh nhân",
          "question": "Câu hỏi tiếp theo nếu cần, nếu không thì để chuỗi rỗng",
          "symptoms": ["triệu chứng 1", "triệu chứng 2"],
          "triageLevel": "LOW hoặc MEDIUM hoặc HIGH hoặc EMERGENCY",
          "specialty": "CAP_CUU hoặc NOI_TONG_QUAT hoặc TIM_MACH hoặc HO_HAP hoặc TIEU_HOA hoặc THAN_KINH hoặc TAI_MUI_HONG hoặc MAT hoặc DA_LIEU hoặc CO_XUONG_KHOP hoặc NOI_TIET hoặc TIET_NIEU hoặc SAN_PHU_KHOA hoặc RANG_HAM_MAT hoặc KHAC hoặc CHUA_XAC_DINH",
          "summary": "Tóm tắt tình trạng nếu đã đủ thông tin, nếu chưa đủ thì để chuỗi rỗng"
        }

        QUY TẮC STATUS:
        - NEED_MORE_INFO: chưa đủ thông tin và cần hỏi thêm.
        - READY: đã đủ thông tin để đưa ra đánh giá sơ bộ.
        - EMERGENCY: phát hiện dấu hiệu có thể cần cấp cứu.

        LỊCH SỬ CUỘC HỘI THOẠI:
        %s

        Hãy dựa trên toàn bộ lịch sử cuộc hội thoại để xử lý tin nhắn mới nhất.
        """.formatted(conversationHistory);

        // 7. GỌI GEMINI
//        String aiResponse =
//                geminiClient.generateContent(prompt);
//
//        // 8. LƯU CÂU TRẢ LỜI CỦA AI
//        Message aiMessage = Message.builder()
//                .conversationId(conversation.getId())
//                .sender(Message.Sender.AI)
//                .content(aiResponse)
//                .build();
//
//        messageRepository.save(aiMessage);
//
//        // 9. TRẢ RESPONSE
//        return ChatResponse.builder()
//                .conversationId(conversation.getId())
//                .message(aiResponse)
//                .build();
        String aiJson =
                geminiClient.generateContent(prompt);

        AiAnalysisResponse analysis =
                aiAnalysisParser.parse(aiJson);

        String aiResponse = analysis.getReply();

        Message aiMessage = Message.builder()
                .conversationId(conversation.getId())
                .sender(Message.Sender.AI)
                .content(aiResponse)
                .build();

        messageRepository.save(aiMessage);

        return ChatResponse.builder()
                .conversationId(conversation.getId())
                .message(aiResponse)
                .analysis(analysis)
                .build();
    }


    public List<ConversationResponse> getConversations(String patientEmail) {

        return conversationRepository
                .findByPatientEmailOrderByUpdatedAtDesc(patientEmail)
                .stream()
                .map(conversation -> ConversationResponse.builder()
                        .id(conversation.getId())
                        .createdAt(conversation.getCreatedAt())
                        .updatedAt(conversation.getUpdatedAt())
                        .build())
                .toList();
    }


    public List<MessageResponse> getMessages(
            String patientEmail,
            Long conversationId
    ) {

        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy cuộc hội thoại"
                        )
                );

        // Chỉ cho phép chủ sở hữu conversation xem lịch sử
        if (!conversation.getPatientEmail()
                .equalsIgnoreCase(patientEmail)) {

            throw new RuntimeException(
                    "Bạn không có quyền truy cập cuộc hội thoại này"
            );
        }

        return messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(message -> MessageResponse.builder()
                        .id(message.getId())
                        .sender(message.getSender())
                        .content(message.getContent())
                        .createdAt(message.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional
    public void deleteConversation(
            String patientEmail,
            Long conversationId
    ) {

        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy cuộc hội thoại"
                        )
                );

        // Kiểm tra conversation có thuộc bệnh nhân đang đăng nhập không
        if (!conversation.getPatientEmail()
                .equalsIgnoreCase(patientEmail)) {

            throw new RuntimeException(
                    "Bạn không có quyền xóa cuộc hội thoại này"
            );
        }

        // Xóa toàn bộ message trước
        messageRepository.deleteAll(
                messageRepository
                        .findByConversationIdOrderByCreatedAtAsc(
                                conversationId
                        )
        );

        // Sau đó xóa conversation
        conversationRepository.delete(conversation);
    }
}