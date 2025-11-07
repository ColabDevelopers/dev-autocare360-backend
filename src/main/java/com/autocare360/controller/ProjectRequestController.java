package com.autocare360.controller;

import com.autocare360.dto.ProjectRequestCreateDTO;
import com.autocare360.dto.ProjectRequestResponseDTO;
import com.autocare360.dto.ProjectRequestUpdateDTO;
import com.autocare360.security.JwtService;
import com.autocare360.service.ProjectRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/project-requests")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "https://autocare360.vercel.app"})
public class ProjectRequestController {
    
    private final ProjectRequestService projectRequestService;
    private final JwtService jwtService;
    
    private Long getUserIdFromAuth(String authorizationHeader) {
        log.debug("🔍 getUserIdFromAuth called with header: {}", authorizationHeader != null ? "Present" : "Null");
        
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.debug("❌ Invalid authorization header format");
            return null;
        }
        
        String token = authorizationHeader.substring(7);
        log.debug("🎫 Token extracted (length: {})", token.length());
        
        if (!jwtService.isTokenValid(token)) {
            log.debug("❌ Token is invalid");
            return null;
        }
        
        try {
            String subject = jwtService.extractSubject(token);
            log.debug("✅ Subject extracted: {}", subject);
            Long userId = Long.parseLong(subject);
            log.debug("✅ User ID parsed: {}", userId);
            return userId;
        } catch (NumberFormatException e) {
            log.error("❌ Failed to parse user ID from token subject", e);
            return null;
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAllProjectRequests(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        
        try {
            boolean isAdmin = jwtService.hasRole(auth, "ADMIN");
            Long userId = getUserIdFromAuth(auth);
            
            if (!isAdmin && userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized access"));
            }
            
            // Handle search
            if (search != null && !search.trim().isEmpty()) {
                List<ProjectRequestResponseDTO> results = projectRequestService.searchProjectRequests(search);
                return ResponseEntity.ok(Map.of(
                        "items", results,
                        "total", results.size(),
                        "page", 0,
                        "size", results.size(),
                        "totalPages", 1
                ));
            }
            
            // Handle status filter
            if (status != null && !status.trim().isEmpty()) {
                List<ProjectRequestResponseDTO> results = projectRequestService.getProjectRequestsByStatus(status);
                // Filter by customer if not admin
                if (!isAdmin) {
                    results = results.stream()
                            .filter(pr -> pr.getCustomerId().equals(userId))
                            .toList();
                }
                return ResponseEntity.ok(Map.of(
                        "items", results,
                        "total", results.size(),
                        "page", 0,
                        "size", results.size(),
                        "totalPages", 1
                ));
            }
            
            // Paginated results
            if (isAdmin) {
                Page<ProjectRequestResponseDTO> pageResult = projectRequestService
                        .getAllProjectRequestsPaginated(page, size, sortBy, sortDirection);
                
                return ResponseEntity.ok(Map.of(
                        "items", pageResult.getContent(),
                        "total", pageResult.getTotalElements(),
                        "page", pageResult.getNumber(),
                        "size", pageResult.getSize(),
                        "totalPages", pageResult.getTotalPages()
                ));
            } else {
                // Customer can only see their own requests
                Page<ProjectRequestResponseDTO> pageResult = projectRequestService
                        .getProjectRequestsByCustomerPaginated(userId, page, size);
                
                return ResponseEntity.ok(Map.of(
                        "items", pageResult.getContent(),
                        "total", pageResult.getTotalElements(),
                        "page", pageResult.getNumber(),
                        "size", pageResult.getSize(),
                        "totalPages", pageResult.getTotalPages()
                ));
            }
            
        } catch (Exception e) {
            log.error("Error fetching project requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch project requests"));
        }
    }
    
    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyProjectRequests(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        
        try {
            Long userId = getUserIdFromAuth(auth);
            
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized access"));
            }
            
            // Handle status filter
            if (status != null && !status.trim().isEmpty()) {
                List<ProjectRequestResponseDTO> results = projectRequestService.getProjectRequestsByStatus(status);
                // Filter by customer
                results = results.stream()
                        .filter(pr -> pr.getCustomerId().equals(userId))
                        .toList();
                return ResponseEntity.ok(Map.of(
                        "items", results,
                        "total", results.size(),
                        "page", 0,
                        "size", results.size(),
                        "totalPages", 1
                ));
            }
            
            // Paginated results for current customer
            Page<ProjectRequestResponseDTO> pageResult = projectRequestService
                    .getProjectRequestsByCustomerPaginated(userId, page, size);
            
            return ResponseEntity.ok(Map.of(
                    "items", pageResult.getContent(),
                    "total", pageResult.getTotalElements(),
                    "page", pageResult.getNumber(),
                    "size", pageResult.getSize(),
                    "totalPages", pageResult.getTotalPages()
            ));
            
        } catch (Exception e) {
            log.error("Error fetching customer project requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch project requests"));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getProjectRequestById(@PathVariable Long id,
                                                   @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            boolean isAdmin = jwtService.hasRole(auth, "ADMIN");
            Long userId = getUserIdFromAuth(auth);
            
            if (!isAdmin && userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized access"));
            }
            
            Optional<ProjectRequestResponseDTO> projectRequest = projectRequestService.getProjectRequestById(id);
            
            if (projectRequest.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Project request not found"));
            }
            
            // Check if customer can access this request
            if (!isAdmin && !projectRequest.get().getCustomerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            return ResponseEntity.ok(projectRequest.get());
            
        } catch (Exception e) {
            log.error("Error fetching project request with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch project request"));
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createProjectRequest(@Valid @RequestBody ProjectRequestCreateDTO createDTO,
                                                  @RequestHeader(value = "Authorization", required = false) String auth) {
        log.info("🚀 POST /api/project-requests called");
        log.info("📝 Request body: {}", createDTO);
        log.info("🔐 Authorization header present: {}", auth != null ? "Yes" : "No");
        
        try {
            Long userId = getUserIdFromAuth(auth);
            log.info("👤 User ID extracted: {}", userId);
            
            if (userId == null) {
                log.warn("❌ No user ID found - returning unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized access"));
            }

            // Set customer ID from JWT token
            createDTO.setCustomerId(userId);
            log.info("✅ Customer ID set to: {}", userId);
            
            ProjectRequestResponseDTO createdRequest = projectRequestService.createProjectRequest(createDTO);
            log.info("🎉 Project request created successfully: {}", createdRequest.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
            
        } catch (RuntimeException e) {
            log.error("💥 Runtime error creating project request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("💥 Unexpected error creating project request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create project request"));
        }
    }    @PutMapping("/{id}")
    public ResponseEntity<?> updateProjectRequest(@PathVariable Long id,
                                                  @RequestBody ProjectRequestUpdateDTO updateDTO,
                                                  @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            boolean isAdmin = jwtService.hasRole(auth, "ADMIN");
            Long userId = getUserIdFromAuth(auth);
            
            if (!isAdmin && userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized access"));
            }
            
            // Check if project exists and user has access
            Optional<ProjectRequestResponseDTO> existingProject = projectRequestService.getProjectRequestById(id);
            if (existingProject.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Project request not found"));
            }
            
            // Only admin or project owner can update (customers can only update limited fields)
            if (!isAdmin && !existingProject.get().getCustomerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            // Customers can only update certain fields
            if (!isAdmin) {
                ProjectRequestUpdateDTO customerUpdateDTO = new ProjectRequestUpdateDTO();
                customerUpdateDTO.setProjectName(updateDTO.getProjectName());
                customerUpdateDTO.setVehicleDetails(updateDTO.getVehicleDetails());
                customerUpdateDTO.setDescription(updateDTO.getDescription());
                customerUpdateDTO.setAttachments(updateDTO.getAttachments());
                customerUpdateDTO.setRequestedAt(updateDTO.getRequestedAt()); // Allow customers to update request date
                updateDTO = customerUpdateDTO;
            }
            
            Optional<ProjectRequestResponseDTO> updatedRequest = projectRequestService.updateProjectRequest(id, updateDTO);
            
            if (updatedRequest.isPresent()) {
                return ResponseEntity.ok(updatedRequest.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Project request not found"));
            }
            
        } catch (Exception e) {
            log.error("Error updating project request with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update project request"));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProjectRequest(@PathVariable Long id,
                                                  @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            boolean isAdmin = jwtService.hasRole(auth, "ADMIN");
            Long userId = getUserIdFromAuth(auth);
            
            if (!isAdmin && userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication required"));
            }
            
            // Check if project exists and get project details
            Optional<ProjectRequestResponseDTO> existingProject = projectRequestService.getProjectRequestById(id);
            if (existingProject.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Project request not found"));
            }
            
            // Check ownership (customers can only delete their own projects)
            if (!isAdmin && !existingProject.get().getCustomerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only delete your own project requests"));
            }
            
            // Check project status - only allow deletion of PENDING projects
            String projectStatus = existingProject.get().getStatus();
            if (!"PENDING".equals(projectStatus)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Cannot delete project - it's already " + projectStatus.toLowerCase().replace("_", " ")));
            }
            
            boolean deleted = projectRequestService.deleteProjectRequest(id, userId, isAdmin);
            
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Project request deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to delete project request"));
            }
            
        } catch (Exception e) {
            log.error("Error deleting project request with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete project request"));
        }
    }
    
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveProjectRequest(@PathVariable Long id,
                                                   @RequestBody(required = false) ProjectRequestUpdateDTO approvalData,
                                                   @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            boolean isAdmin = jwtService.hasRole(auth, "ADMIN");
            
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only administrators can approve project requests"));
            }
            
            if (approvalData == null) {
                approvalData = new ProjectRequestUpdateDTO();
            }
            
            Optional<ProjectRequestResponseDTO> approvedRequest = projectRequestService.approveProjectRequest(id, approvalData);
            
            if (approvedRequest.isPresent()) {
                return ResponseEntity.ok(approvedRequest.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Project request not found"));
            }
            
        } catch (Exception e) {
            log.error("Error approving project request with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to approve project request"));
        }
    }
    
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectProjectRequest(@PathVariable Long id,
                                                  @RequestBody Map<String, String> rejectionData,
                                                  @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            boolean isAdmin = jwtService.hasRole(auth, "ADMIN");
            
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only administrators can reject project requests"));
            }
            
            String rejectionReason = rejectionData.get("rejectionReason");
            if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Rejection reason is required"));
            }
            
            Optional<ProjectRequestResponseDTO> rejectedRequest = projectRequestService.rejectProjectRequest(id, rejectionReason);
            
            if (rejectedRequest.isPresent()) {
                return ResponseEntity.ok(rejectedRequest.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Project request not found"));
            }
            
        } catch (Exception e) {
            log.error("Error rejecting project request with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to reject project request"));
        }
    }
    
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getProjectRequestsByEmployee(@PathVariable Long employeeId,
                                                         @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            boolean isAdmin = jwtService.hasRole(auth, "ADMIN");
            Long userId = getUserIdFromAuth(auth);
            
            if (!isAdmin && userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized access"));
            }
            
            List<ProjectRequestResponseDTO> requests = projectRequestService.getProjectRequestsByEmployee(employeeId);
            
            return ResponseEntity.ok(Map.of(
                    "items", requests,
                    "total", requests.size()
            ));
            
        } catch (Exception e) {
            log.error("Error fetching project requests for employee: {}", employeeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch project requests"));
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<?> getProjectRequestStats(@RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            boolean isAdmin = jwtService.hasRole(auth, "ADMIN");
            
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only administrators can view statistics"));
            }
            
            Map<String, Long> stats = Map.of(
                    "pending", projectRequestService.getProjectRequestCountByStatus("PENDING"),
                    "underReview", projectRequestService.getProjectRequestCountByStatus("UNDER_REVIEW"),
                    "approved", projectRequestService.getProjectRequestCountByStatus("APPROVED"),
                    "rejected", projectRequestService.getProjectRequestCountByStatus("REJECTED"),
                    "inProgress", projectRequestService.getProjectRequestCountByStatus("IN_PROGRESS"),
                    "completed", projectRequestService.getProjectRequestCountByStatus("COMPLETED")
            );
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error fetching project request statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch statistics"));
        }
    }
}