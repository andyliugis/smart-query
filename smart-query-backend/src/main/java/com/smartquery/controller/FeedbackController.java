package com.smartquery.controller;

import com.smartquery.model.QueryFeedback;
import com.smartquery.repository.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 反馈控制器 - 处理用户对查询结果的反馈
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private static final Logger log = LoggerFactory.getLogger(FeedbackController.class);

    private final FeedbackRepository feedbackRepository;

    public FeedbackController(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    /**
     * 提交反馈
     * POST /api/feedback
     */
    @PostMapping
    public ResponseEntity<?> submitFeedback(@RequestBody Map<String, Object> request) {
        try {
            Long sessionId = request.get("sessionId") != null ? Long.valueOf(request.get("sessionId").toString()) : null;
            Long messageId = request.get("messageId") != null ? Long.valueOf(request.get("messageId").toString()) : null;
            Long userId = request.get("userId") != null ? Long.valueOf(request.get("userId").toString()) : null;
            String feedbackType = (String) request.get("feedbackType");
            String content = (String) request.get("content");
            String question = (String) request.get("question");
            String sql = (String) request.get("sql");

            if (feedbackType == null || (!feedbackType.equals("like") && !feedbackType.equals("dislike"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "反馈类型必须是 like 或 dislike"));
            }

            QueryFeedback feedback;
            if ("like".equals(feedbackType)) {
                feedback = QueryFeedback.like(sessionId, messageId, userId, question, sql);
            } else {
                feedback = QueryFeedback.dislike(sessionId, messageId, userId, question, sql, content);
            }

            feedbackRepository.save(feedback);
            
            log.info("收到反馈: type={}, question={}", feedbackType, question);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "反馈已保存"));
        } catch (Exception e) {
            log.error("保存反馈失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "保存反馈失败: " + e.getMessage()));
        }
    }

    /**
     * 获取反馈统计
     * GET /api/feedback/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        try {
            int likeCount = feedbackRepository.findAllLikes().size();
            int dislikeCount = feedbackRepository.findAllDislikes().size();
            
            return ResponseEntity.ok(Map.of(
                    "likeCount", likeCount,
                    "dislikeCount", dislikeCount,
                    "total", likeCount + dislikeCount
            ));
        } catch (Exception e) {
            log.error("获取反馈统计失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "获取统计失败"));
        }
    }
}
