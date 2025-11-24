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
import java.time.LocalDate;
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
        // Set the request date (use provided date or current date)
        LocalDate requestDate = createDTO.getRequestedAt() != null ? createDTO.getRequestedAt() : LocalDate.now();
        projectRequest.setRequestedAt(requestDate);
        log.info("Setting requestedAt to: {}", requestDate);
        
        ProjectRequest savedRequest = projectRequestRepository.save(projectRequest);
        log.info("Saved project request - ID: {}, RequestedAt: {}", savedRequest.getId(), savedRequest.getRequestedAt());
        
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
        log.info("🔄 Updating project request with ID: {}", id);
        log.info("📋 Update DTO received: {}", updateDTO);
        log.info("🗓️ RequestedAt value in DTO: {}", updateDTO.getRequestedAt());
        
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
            log.info("👥 Assigning employee ID: {} to project {}", updateDTO.getAssignedEmployeeId(), id);
            Optional<User> userEmployee = userRepository.findById(updateDTO.getAssignedEmployeeId());
            if (userEmployee.isPresent()) {
                User user = userEmployee.get();
                log.info("✅ User found: {} - {}", user.getId(), user.getName());
                
                // Check if the user has employee fields
                if (user.getEmployeeNo() != null || user.getDepartment() != null) {
                    // Check if employee record already exists in employees table by ID or email
                    Optional<Employee> existingEmployee = employeeRepository.findById(user.getId());
                    if (!existingEmployee.isPresent() && user.getEmail() != null) {
                        // Also check by email in case there's already an employee with this email
                        Employee empByEmail = employeeRepository.findByEmail(user.getEmail());
                        existingEmployee = empByEmail != null ? Optional.of(empByEmail) : Optional.empty();
                    }
                    
                    Employee emp;
                    
                    if (existingEmployee.isPresent()) {
                        emp = existingEmployee.get();
                        log.info("🔍 Using existing employee record: {} - {}", emp.getId(), emp.getName());
                    } else {
                        // Create new Employee record in employees table
                        emp = new Employee();
                        // DON'T set ID - let Hibernate auto-generate it
                        emp.setName(user.getName());
                        emp.setEmail(user.getEmail());
                        emp.setPhoneNumber(user.getPhone());
                        emp.setStatus("ACTIVE");
                        emp.setHireDate(LocalDate.now());
                        
                        // Save to employees table first
                        emp = employeeRepository.save(emp);
                        log.info("📝 Created new employee record: {} - {}", emp.getId(), emp.getName());
                    }
                    
                    existing.setAssignedEmployee(emp);
                    log.info("🔄 Employee assigned to project. Current assigned employee: {}", 
                            existing.getAssignedEmployeeId());
                } else {
                    log.warn("❌ User with ID {} is not an employee (no employee_no or department)", updateDTO.getAssignedEmployeeId());
                }
            } else {
                log.warn("❌ User with ID {} not found", updateDTO.getAssignedEmployeeId());
            }
        } else {
            log.info("👥 No employee assignment in update DTO");
        }
        if (updateDTO.getRequestedAt() != null) {
            log.info("🗓️ Current requested date: {}", existing.getRequestedAt());
            log.info("🗓️ New requested date from DTO: {}", updateDTO.getRequestedAt());
            existing.setRequestedAt(updateDTO.getRequestedAt());
            log.info("🗓️ After setting - requested date: {}", existing.getRequestedAt());
        } else {
            log.info("⚠️ RequestedAt is null in update DTO");
        }
        
        log.info("🔄 Before saving - Employee ID: {}, Status: {}", existing.getAssignedEmployeeId(), existing.getStatus());
        log.info("🔄 Before saving - requested date: {}", existing.getRequestedAt());
        ProjectRequest updated = projectRequestRepository.save(existing);
        log.info("✅ After saving - Employee ID: {}, Status: {}", updated.getAssignedEmployeeId(), updated.getStatus());
        log.info("✅ After saving - requested date: {}", updated.getRequestedAt());
        log.info("🔍 Entity saved successfully - ID: {}", updated.getId());
        
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
    public boolean deleteProjectRequest(Long id, Long userId, boolean isAdmin) {
        log.info("🗑️ Delete request - Project ID: {}, User ID: {}, Is Admin: {}", id, userId, isAdmin);
        
        Optional<ProjectRequest> projectOpt = projectRequestRepository.findById(id);
        if (projectOpt.isEmpty()) {
            log.warn("❌ Project request with ID {} not found", id);
            return false;
        }
        
        ProjectRequest project = projectOpt.get();
        
        // Additional ownership validation (belt-and-suspenders approach)
        if (!isAdmin && !project.getCustomerId().equals(userId)) {
            log.warn("❌ User {} attempted to delete project {} owned by {}", userId, id, project.getCustomerId());
            throw new RuntimeException("You can only delete your own project requests");
        }
        
        // Additional status validation
        if (!"PENDING".equals(project.getStatus())) {
            log.warn("❌ Cannot delete project {} with status {}", id, project.getStatus());
            throw new RuntimeException("Cannot delete project - it's already " + project.getStatus().toLowerCase().replace("_", " "));
        }
        
        try {
            projectRequestRepository.delete(project);
            log.info("✅ Successfully deleted project request with ID: {} by user: {}", id, userId);
            return true;
        } catch (Exception e) {
            log.error("💥 Failed to delete project request with ID: {}", id, e);
            throw new RuntimeException("Failed to delete project request");
        }
    }
    
    // Keep the old method for backward compatibility if needed elsewhere
    @Transactional
    public boolean deleteProjectRequest(Long id) {
        return deleteProjectRequest(id, null, true); // Default to admin behavior
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