package com.college.incubation.service;

import com.college.incubation.dto.CreateIdeaRequest;
import com.college.incubation.dto.IdeaResponse;
import com.college.incubation.dto.UpdateIdeaRequest;
import com.college.incubation.entity.Idea;
import com.college.incubation.entity.IdeaStatus;
import com.college.incubation.entity.User;
import com.college.incubation.repository.IdeaRepository;
import com.college.incubation.repository.UserRepository;
import com.college.incubation.util.EncryptionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdeaService {
    private final IdeaRepository ideaRepository;
    private final UserRepository userRepository;
    private final EncryptionUtils encryptionUtils;

    public IdeaResponse createIdea(CreateIdeaRequest request, String email) {
        User user = getUser(email);
        Idea idea = Idea.builder().user(user)
                .title(encryptionUtils.encrypt(request.getTitle()))
                .description(encryptionUtils.encrypt(request.getDescription()))
                .category(request.getCategory())
                .department(request.getDepartment())
                .problemStatement(request.getProblemStatement() == null ? null : encryptionUtils.encrypt(request.getProblemStatement()))
                .solution(request.getSolution() == null ? null : encryptionUtils.encrypt(request.getSolution()))
                .attachmentName(request.getAttachmentName())
                .attachmentType(request.getAttachmentType())
                .attachmentData(request.getAttachmentData())
                .status(IdeaStatus.SUBMITTED).build();
        return toResponse(ideaRepository.save(idea));
    }
    public List<IdeaResponse> getMyIdeas(String email) {
        User user = getUser(email);
        return ideaRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(this::toResponse).toList();
    }
    public IdeaResponse getMyIdea(UUID id, String email) {
        User user = getUser(email);
        Idea idea = ideaRepository.findByIdAndUserId(id, user.getId()).orElseThrow(() -> new AccessDeniedException("You cannot access this idea"));
        return toResponse(idea);
    }
    public IdeaResponse updateMyIdea(UUID id, UpdateIdeaRequest request, String email) {
        User user = getUser(email);
        Idea idea = ideaRepository.findByIdAndUserId(id, user.getId()).orElseThrow(() -> new AccessDeniedException("You cannot edit this idea"));
        if (idea.getStatus() != IdeaStatus.SUBMITTED && idea.getStatus() != IdeaStatus.UNDER_REVIEW) throw new IllegalStateException("This idea cannot be edited after a final decision");
        idea.setTitle(encryptionUtils.encrypt(request.getTitle()));
        idea.setDescription(encryptionUtils.encrypt(request.getDescription()));
        idea.setCategory(request.getCategory());
        idea.setDepartment(request.getDepartment());
        idea.setProblemStatement(request.getProblemStatement() == null ? null : encryptionUtils.encrypt(request.getProblemStatement()));
        idea.setSolution(request.getSolution() == null ? null : encryptionUtils.encrypt(request.getSolution()));
        idea.setAttachmentName(request.getAttachmentName());
        idea.setAttachmentType(request.getAttachmentType());
        idea.setAttachmentData(request.getAttachmentData());
        return toResponse(ideaRepository.save(idea));
    }
    public void deleteMyIdea(UUID id, String email) {
        User user = getUser(email);
        Idea idea = ideaRepository.findByIdAndUserId(id, user.getId()).orElseThrow(() -> new AccessDeniedException("You cannot delete this idea"));
        ideaRepository.delete(idea);
    }
    public IdeaResponse updateStatus(UUID id, IdeaStatus status) {
        Idea idea = ideaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Idea not found"));
        idea.setStatus(status); return toResponse(ideaRepository.save(idea));
    }
    public List<IdeaResponse> getAllIdeas() { return ideaRepository.findAll().stream().map(this::toResponse).toList(); }
    private User getUser(String email) { return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found")); }
    private IdeaResponse toResponse(Idea idea) {
        User user = idea.getUser();
        return IdeaResponse.builder().id(idea.getId()).userId(user.getId()).studentEmail(user.getEmail())
                .studentName(user.getName() == null || user.getName().isBlank() ? user.getEmail() : user.getName())
                .department(idea.getDepartment()).title(encryptionUtils.decrypt(idea.getTitle()))
                .description(encryptionUtils.decrypt(idea.getDescription()))
                .category(idea.getCategory())
                .problemStatement(idea.getProblemStatement() == null ? null : encryptionUtils.decrypt(idea.getProblemStatement()))
                .solution(idea.getSolution() == null ? null : encryptionUtils.decrypt(idea.getSolution()))
                .attachmentName(idea.getAttachmentName())
                .attachmentType(idea.getAttachmentType())
                .attachmentData(idea.getAttachmentData())
                .status(idea.getStatus())
                .createdAt(idea.getCreatedAt()).updatedAt(idea.getUpdatedAt()).build();
    }
}
