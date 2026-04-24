package interview.coach.service;

import interview.coach.api.dto.CatalogDtos.CatalogItemRequest;
import interview.coach.api.dto.CatalogDtos.CatalogItemResponse;
import interview.coach.domain.entity.InterviewDirectionRef;
import interview.coach.domain.entity.InterviewLevelRef;
import interview.coach.exception.ApiException;
import interview.coach.repository.InterviewDirectionRepository;
import interview.coach.repository.InterviewLevelRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogReferenceService {

    private final InterviewDirectionRepository interviewDirectionRepository;
    private final InterviewLevelRepository interviewLevelRepository;

    public CatalogReferenceService(
            InterviewDirectionRepository interviewDirectionRepository,
            InterviewLevelRepository interviewLevelRepository
    ) {
        this.interviewDirectionRepository = interviewDirectionRepository;
        this.interviewLevelRepository = interviewLevelRepository;
    }

    public List<CatalogItemResponse> getDirections() {
        return interviewDirectionRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(InterviewDirectionRef::getCode))
                .map(direction -> new CatalogItemResponse(direction.getId(), direction.getCode(), direction.getName()))
                .toList();
    }

    public List<CatalogItemResponse> getLevels() {
        return interviewLevelRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(InterviewLevelRef::getCode))
                .map(level -> new CatalogItemResponse(level.getId(), level.getCode(), level.getName()))
                .toList();
    }

    @Transactional
    public CatalogItemResponse createDirection(CatalogItemRequest request) {
        String normalizedCode = normalizeCode(request.code());
        if (interviewDirectionRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new ApiException(HttpStatus.CONFLICT, "Direction with this code already exists");
        }
        InterviewDirectionRef direction = new InterviewDirectionRef();
        direction.setCode(normalizedCode);
        direction.setName(normalizeName(request.name()));
        direction.setCreatedAt(LocalDateTime.now());
        direction.setUpdatedAt(LocalDateTime.now());
        InterviewDirectionRef saved = interviewDirectionRepository.save(direction);
        return new CatalogItemResponse(saved.getId(), saved.getCode(), saved.getName());
    }

    @Transactional
    public CatalogItemResponse updateDirection(UUID directionId, CatalogItemRequest request) {
        InterviewDirectionRef direction = interviewDirectionRepository.findById(directionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Direction not found"));
        String normalizedCode = normalizeCode(request.code());
        if (!direction.getCode().equalsIgnoreCase(normalizedCode) && interviewDirectionRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new ApiException(HttpStatus.CONFLICT, "Direction with this code already exists");
        }
        direction.setCode(normalizedCode);
        direction.setName(normalizeName(request.name()));
        direction.setUpdatedAt(LocalDateTime.now());
        InterviewDirectionRef saved = interviewDirectionRepository.save(direction);
        return new CatalogItemResponse(saved.getId(), saved.getCode(), saved.getName());
    }

    @Transactional
    public void deleteDirection(UUID directionId) {
        InterviewDirectionRef direction = interviewDirectionRepository.findById(directionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Direction not found"));
        try {
            interviewDirectionRepository.delete(direction);
            interviewDirectionRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "Direction is used and cannot be deleted");
        }
    }

    @Transactional
    public CatalogItemResponse createLevel(CatalogItemRequest request) {
        String normalizedCode = normalizeCode(request.code());
        if (interviewLevelRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new ApiException(HttpStatus.CONFLICT, "Level with this code already exists");
        }
        InterviewLevelRef level = new InterviewLevelRef();
        level.setCode(normalizedCode);
        level.setName(normalizeName(request.name()));
        level.setCreatedAt(LocalDateTime.now());
        level.setUpdatedAt(LocalDateTime.now());
        InterviewLevelRef saved = interviewLevelRepository.save(level);
        return new CatalogItemResponse(saved.getId(), saved.getCode(), saved.getName());
    }

    @Transactional
    public CatalogItemResponse updateLevel(UUID levelId, CatalogItemRequest request) {
        InterviewLevelRef level = interviewLevelRepository.findById(levelId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Level not found"));
        String normalizedCode = normalizeCode(request.code());
        if (!level.getCode().equalsIgnoreCase(normalizedCode) && interviewLevelRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new ApiException(HttpStatus.CONFLICT, "Level with this code already exists");
        }
        level.setCode(normalizedCode);
        level.setName(normalizeName(request.name()));
        level.setUpdatedAt(LocalDateTime.now());
        InterviewLevelRef saved = interviewLevelRepository.save(level);
        return new CatalogItemResponse(saved.getId(), saved.getCode(), saved.getName());
    }

    @Transactional
    public void deleteLevel(UUID levelId) {
        InterviewLevelRef level = interviewLevelRepository.findById(levelId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Level not found"));
        try {
            interviewLevelRepository.delete(level);
            interviewLevelRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "Level is used and cannot be deleted");
        }
    }

    public InterviewDirectionRef requireDirection(String code) {
        if (code == null || code.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Direction is required");
        }
        return interviewDirectionRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Direction not found"));
    }

    public InterviewLevelRef requireLevel(String code) {
        if (code == null || code.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Level is required");
        }
        return interviewLevelRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Level not found"));
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Code is required");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        return name.trim();
    }
}
