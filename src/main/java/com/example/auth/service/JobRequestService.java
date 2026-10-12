package com.example.auth.service;

import com.example.auth.dto.JobRequestDto;
import java.util.List;

public interface JobRequestService {
    JobRequestDto createJobRequest(JobRequestDto dto, String username);
    List<JobRequestDto> getAllJobRequests();
    List<JobRequestDto> getJobRequestsByDepartment(Long departmentId);
    JobRequestDto getJobRequestById(Long id);
    JobRequestDto approveOrReject(Long id, boolean isApproved, String approverUsername);
    JobRequestDto approveOrRejectStep(Long id, boolean isApproved, String comments, String approverUsername);
    JobRequestDto assignRecruiter(Long id, String recruiterUsername);
}
