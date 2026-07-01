package com.financecontrol.service;

import com.financecontrol.dto.request.UserFeedbackRequest;
import com.financecontrol.dto.response.UserFeedbackResponse;
import com.financecontrol.entity.User;
import com.financecontrol.entity.UserFeedback;
import com.financecontrol.enums.FeedbackType;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.UserFeedbackRepository;
import com.financecontrol.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserFeedbackService {

    private final UserFeedbackRepository feedbackRepository;
    private final UserRepository         userRepository;
    private final EmailService           emailService;

    @Transactional
    public UserFeedbackResponse submit(@NonNull Long userId, UserFeedbackRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        UserFeedback feedback = new UserFeedback();
        feedback.setUser(user);
        feedback.setType(FeedbackType.valueOf(req.getType().toUpperCase()));
        feedback.setMessage(req.getMessage().trim());
        feedback.setNpsScore(req.getNpsScore());

        UserFeedback saved = feedbackRepository.save(feedback);

        userRepository.findAllByAdminTrueAndActiveTrue().forEach(admin ->
                emailService.sendFeedbackNotification(admin, user, saved));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<UserFeedbackResponse> findAll(int page, int size, String type) {
        Pageable pageable = PageRequest.of(page, size);
        if (type != null && !type.isBlank()) {
            return feedbackRepository
                    .findByTypeOrderByCreatedAtDesc(FeedbackType.valueOf(type.toUpperCase()), pageable)
                    .map(this::toResponse);
        }
        return feedbackRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    private UserFeedbackResponse toResponse(UserFeedback f) {
        return new UserFeedbackResponse(
                f.getId(),
                f.getUser().getUsername(),
                f.getUser().getEmail(),
                f.getType().name(),
                f.getMessage(),
                f.getNpsScore(),
                f.getCreatedAt()
        );
    }
}
