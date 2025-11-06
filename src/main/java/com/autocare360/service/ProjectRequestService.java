package com.autocare360.service;

import com.autocare360.dto.ProjectRequestCreateDTO;
import com.autocare360.dto.ProjectRequestResponseDTO;
import com.autocare360.dto.ProjectRequestUpdateDTO;
import com.autocare360.entity.Employee;
import com.autocare360.entity.ProjectRequest;
import com.autocare360.entity.User;
import com.autocare360.repo.EmployeeRepository;
import com.autocare360.repo.ProjectRequestRepository;
import com.autocare360.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectRequestService {
    
    private final ProjectRequestRepository projectRequestRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;
    
    public List<ProjectRequestResponseDTO> getAllProjectRequests() {
        return projectRequestRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public Page<ProjectRequestResponseDTO> getAllProjectRequestsPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        return projectRequestRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }
    
    public List<ProjectRequestResponseDTO> getProjectRequestsByCustomer(Long customerId) {
        return projectRequestRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public Page<ProjectRequestResponseDTO> getProjectRequestsByCustomerPaginated(Long customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return projectRequestRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId, pageable)
                .map(this::mapToResponseDTO);
    }
    
    public List<ProjectRequestResponseDTO> getProjectRequestsByStatus(String status) {
        return projectRequestRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public List<ProjectRequestResponseDTO> getProjectRequestsByEmployee(Long employeeId) {
        return projectRequestRepository.findByAssignedEmployee_IdOrderByCreatedAtDesc(employeeId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public List<ProjectRequestResponseDTO> searchProjectRequests(String searchTerm) {
        return projectRequestRepository.searchByNameOrDescription(searchTerm)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<ProjectRequestResponseDTO> getProjectRequestById(Long id) {
        return projectRequestRepository.findById(id)
                .map(this::mapToResponseDTO);
    }
    
    @Transactional
    public ProjectRequestResponseDTO createProjectRequest(ProjectRequestCreateDTO createDTO) {
        // Validate customer exists
        User customer = userRepository.findById(createDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setCustomer(customer);
        projectRequest.setProjectName(createDTO.getProjectName());
        projectRequest.setProjectType(createDTO.getProjectType());
        projectRequest.setVehicleDetails(createDTO.getVehicleDetails());
        projectRequest.setDescription(createDTO.getDescription());
        projectRequest.setPriority(createDTO.getPriority() != null ? createDTO.getPriority() : "MEDIUM");
        projectRequest.setEstimatedCost(createDTO.getEstimatedCost());
        projectRequest.setEstimatedDurationDays(createDTO.getEstimatedDurationDays());
        projectRequest.setAttachments(createDTO.getAttachments());
        
        ProjectRequest savedRequest = projectRequestRepository.save(projectRequest);
        
        // Send notification to admins about new project request
        notificationService.notifyProjectRequest(
                customer.getId(),
                savedRequest.getId(),
                savedRequest.getProjectName(),
                "SUBMITTED"
        );
        
        log.info("Created new project request with ID: {} for customer: {}", savedRequest.getId(), customer.getEmail());
        
        return mapToResponseDTO(savedRequest);
    }
    
    @Transactional
    public Optional<ProjectRequestResponseDTO> updateProjectRequest(Long id, ProjectRequestUpdateDTO updateDTO) {
        Optional<ProjectRequest> optionalProject = projectRequestRepository.findById(id);
        
        if (optionalProject.isEmpty()) {
            return Optional.empty();
        }
        
        ProjectRequest existing = optionalProject.get();
        String oldStatus = existing.getStatus();
        
        // Update fields if provided
        if (updateDTO.getProjectName() != null) {
            existing.setProjectName(updateDTO.getProjectName());
        }
        if (updateDTO.getProjectType() != null) {
            existing.setProjectType(updateDTO.getProjectType());
        }
        if (updateDTO.getVehicleDetails() != null) {
            existing.setVehicleDetails(updateDTO.getVehicleDetails());
        }
        if (updateDTO.getDescription() != null) {
            existing.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getPriority() != null) {
            existing.setPriority(updateDTO.getPriority());
        }
        if (updateDTO.getStatus() != null) {
            existing.setStatus(updateDTO.getStatus());
            if ("UNDER_REVIEW".equals(updateDTO.getStatus()) && existing.getReviewedAt() == null) {
                existing.setReviewedAt(LocalDateTime.now());
            }
            if ("IN_PROGRESS".equals(updateDTO.getStatus()) && existing.getStartDate() == null) {
                existing.setStartDate(LocalDateTime.now());
            }
            if ("COMPLETED".equals(updateDTO.getStatus()) && existing.getCompletionDate() == null) {
                existing.setCompletionDate(LocalDateTime.now());
            }
        }
        if (updateDTO.getEstimatedCost() != null) {
            existing.setEstimatedCost(updateDTO.getEstimatedCost());
        }
        if (updateDTO.getEstimatedDurationDays() != null) {
            existing.setEstimatedDurationDays(updateDTO.getEstimatedDurationDays());
        }
        if (updateDTO.getApprovedCost() != null) {
            existing.setApprovedCost(updateDTO.getApprovedCost());
        }
        if (updateDTO.getActualCost() != null) {
            existing.setActualCost(updateDTO.getActualCost());
        }
        if (updateDTO.getStartDate() != null) {
            existing.setStartDate(updateDTO.getStartDate());
        }
        if (updateDTO.getCompletionDate() != null) {
            existing.setCompletionDate(updateDTO.getCompletionDate());
        }
        if (updateDTO.getAdminNotes() != null) {
            existing.setAdminNotes(updateDTO.getAdminNotes());
        }
        if (updateDTO.getRejectionReason() != null) {
            existing.setRejectionReason(updateDTO.getRejectionReason());
        }
        if (updateDTO.getAttachments() != null) {
            existing.setAttachments(updateDTO.getAttachments());
        }
        if (updateDTO.getAssignedEmployeeId() != null) {
            Optional<Employee> employee = employeeRepository.findById(updateDTO.getAssignedEmployeeId());
            employee.ifPresent(existing::setAssignedEmployee);
        }
        
        ProjectRequest updated = projectRequestRepository.save(existing);
        
        // Send notification if status changed
        if (!oldStatus.equals(updated.getStatus())) {
            notificationService.notifyProjectRequest(
                    updated.getCustomerId(),
                    updated.getId(),
                    updated.getProjectName(),
                    updated.getStatus()
            );
            
            log.info("Project request {} status changed from {} to {}", 
                    updated.getId(), oldStatus, updated.getStatus());
        }
        
        return Optional.of(mapToResponseDTO(updated));
    }
    
    @Transactional
    public boolean deleteProjectRequest(Long id) {
        Optional<ProjectRequest> project = projectRequestRepository.findById(id);
        if (project.isPresent()) {
            projectRequestRepository.delete(project.get());
            log.info("Deleted project request with ID: {}", id);
            return true;
        }
        return false;
    }
    
    public Long getProjectRequestCountByStatus(String status) {
        return projectRequestRepository.countByStatus(status);
    }
    
    public Long getProjectRequestCountByCustomer(Long customerId) {
        return projectRequestRepository.countByCustomer_Id(customerId);
    }
    
    public List<ProjectRequestResponseDTO> getPendingProjectsOlderThanDays(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return projectRequestRepository.findPendingProjectsOlderThan(cutoffDate)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public Optional<ProjectRequestResponseDTO> approveProjectRequest(Long id, ProjectRequestUpdateDTO approvalData) {
        Optional<ProjectRequest> optionalProject = projectRequestRepository.findById(id);
        
        if (optionalProject.isEmpty()) {
            return Optional.empty();
        }
        
        ProjectRequest project = optionalProject.get();
        project.setStatus("APPROVED");
        project.setReviewedAt(LocalDateTime.now());
        
        if (approvalData.getApprovedCost() != null) {
            project.setApprovedCost(approvalData.getApprovedCost());
        }
        if (approvalData.getAdminNotes() != null) {
            project.setAdminNotes(approvalData.getAdminNotes());
        }
        if (approvalData.getAssignedEmployeeId() != null) {
            Optional<Employee> employee = employeeRepository.findById(approvalData.getAssignedEmployeeId());
            employee.ifPresent(project::setAssignedEmployee);
        }
        
        ProjectRequest updated = projectRequestRepository.save(project);
        
        // Send approval notification
        notificationService.notifyProjectRequest(
                updated.getCustomerId(),
                updated.getId(),
                updated.getProjectName(),
                "APPROVED"
        );
        
        log.info("Approved project request with ID: {}", updated.getId());
        
        return Optional.of(mapToResponseDTO(updated));
    }
    
    @Transactional
    public Optional<ProjectRequestResponseDTO> rejectProjectRequest(Long id, String rejectionReason) {
        Optional<ProjectRequest> optionalProject = projectRequestRepository.findById(id);
        
        if (optionalProject.isEmpty()) {
            return Optional.empty();
        }
        
        ProjectRequest project = optionalProject.get();
        project.setStatus("REJECTED");
        project.setReviewedAt(LocalDateTime.now());
        project.setRejectionReason(rejectionReason);
        
        ProjectRequest updated = projectRequestRepository.save(project);
        
        // Send rejection notification
        notificationService.notifyProjectRequest(
                updated.getCustomerId(),
                updated.getId(),
                updated.getProjectName(),
                "REJECTED"
        );
        
        log.info("Rejected project request with ID: {} - Reason: {}", updated.getId(), rejectionReason);
        
        return Optional.of(mapToResponseDTO(updated));
    }
    
    private ProjectRequestResponseDTO mapToResponseDTO(ProjectRequest project) {
        return ProjectRequestResponseDTO.builder()
                .id(project.getId())
                .customerId(project.getCustomerId())
                .customerName(project.getCustomerName())
                .customerEmail(project.getCustomerEmail())
                .projectName(project.getProjectName())
                .projectType(project.getProjectType())
                .vehicleDetails(project.getVehicleDetails())
                .description(project.getDescription())
                .priority(project.getPriority())
                .status(project.getStatus())
                .estimatedCost(project.getEstimatedCost())
                .estimatedDurationDays(project.getEstimatedDurationDays())
                .approvedCost(project.getApprovedCost())
                .actualCost(project.getActualCost())
                .startDate(project.getStartDate())
                .completionDate(project.getCompletionDate())
                .adminNotes(project.getAdminNotes())
                .rejectionReason(project.getRejectionReason())
                .attachments(project.getAttachments())
                .assignedEmployeeId(project.getAssignedEmployeeId())
                .assignedEmployeeName(project.getAssignedEmployeeName())
                .requestedAt(project.getRequestedAt())
                .reviewedAt(project.getReviewedAt())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}