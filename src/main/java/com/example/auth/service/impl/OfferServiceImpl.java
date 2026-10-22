package com.example.auth.service.impl;

import com.example.auth.dto.OfferDto;
import com.example.auth.entity.JobTitle;
import com.example.auth.entity.Offer;
import com.example.auth.exception.AppException;
import com.example.auth.repository.JobTitleRepository;
import com.example.auth.repository.OfferRepository;
import com.example.auth.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {
    private final OfferRepository offerRepository;
    private final JobTitleRepository jobTitleRepository;

    @Override
    public List<OfferDto> getPendingApprovalOffers() {
        return offerRepository.findByStatus("PENDING_APPROVAL").stream().map(offer -> {
            OfferDto dto = new OfferDto();
            dto.setId(offer.getId());
            String jobTitle = "N/A";
            Double minSalary = null;
            Double maxSalary = null;

            if (offer.getApplication() != null) {
                if (offer.getApplication().getCandidateProfile() != null) {
                    dto.setCandidateName(offer.getApplication().getCandidateProfile().getFullName());
                }
                if (offer.getApplication().getJobRequest() != null) {
                    jobTitle = offer.getApplication().getJobRequest().getTitle();
                    dto.setJobTitle(jobTitle);
                    minSalary = offer.getApplication().getJobRequest().getMinSalary();
                    maxSalary = offer.getApplication().getJobRequest().getMaxSalary();
                }
            }

            // Tra cứu thêm dải lương cấu hình chuẩn từ danh mục Chức danh JobTitle
            if (jobTitle != null && !jobTitle.equals("N/A")) {
                Optional<JobTitle> jtOpt = jobTitleRepository.findByName(jobTitle);
                if (jtOpt.isEmpty()) {
                    jtOpt = jobTitleRepository.findByCode(jobTitle);
                }
                if (jtOpt.isPresent()) {
                    JobTitle jt = jtOpt.get();
                    if (jt.getMinSalary() != null) minSalary = jt.getMinSalary();
                    if (jt.getMaxSalary() != null) maxSalary = jt.getMaxSalary();
                }
            }

            dto.setMinSalary(minSalary);
            dto.setMaxSalary(maxSalary);
            dto.setOfferedSalary(offer.getOfferedSalary());
            dto.setStatus(offer.getStatus());

            // Đánh giá tính hợp lệ của mức offer so với dải lương chức danh
            Double offered = offer.getOfferedSalary();
            if (offered != null) {
                if (minSalary != null && offered < minSalary) {
                    dto.setSalaryStatus("BELOW_MIN");
                    dto.setSalaryMessage(String.format("Thấp hơn dải lương tối thiểu (%,.0fđ)", minSalary));
                } else if (maxSalary != null && offered > maxSalary) {
                    dto.setSalaryStatus("EXCEEDS_MAX");
                    dto.setSalaryMessage(String.format("Vượt ngoài dải lương tối đa (%,.0fđ)", maxSalary));
                } else {
                    dto.setSalaryStatus("VALID");
                    dto.setSalaryMessage("Hợp lệ theo dải lương chức danh");
                }
            } else {
                dto.setSalaryStatus("UNKNOWN");
                dto.setSalaryMessage("Chưa có mức offer");
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void approveOffer(Long id) {
        Offer offer = offerRepository.findById(id).orElseThrow(() -> new AppException("Không tìm thấy Offer với ID: " + id));

        // Áp dụng giới hạn duyệt offer theo dải lương của chức danh
        validateOfferSalaryRange(offer);

        offer.setStatus("APPROVED");
        offerRepository.save(offer);
    }

    @Override
    public void rejectOffer(Long id) {
        Offer offer = offerRepository.findById(id).orElseThrow(() -> new AppException("Không tìm thấy Offer với ID: " + id));
        offer.setStatus("DECLINED");
        offerRepository.save(offer);
    }

    @Override
    public Map<String, Object> validateOfferSalary(String jobTitleName, Double offeredSalary) {
        Map<String, Object> res = new HashMap<>();
        Double minSalary = null;
        Double maxSalary = null;

        if (jobTitleName != null) {
            Optional<JobTitle> jtOpt = jobTitleRepository.findByName(jobTitleName);
            if (jtOpt.isEmpty()) {
                jtOpt = jobTitleRepository.findByCode(jobTitleName);
            }
            if (jtOpt.isPresent()) {
                minSalary = jtOpt.get().getMinSalary();
                maxSalary = jtOpt.get().getMaxSalary();
            }
        }

        res.put("jobTitle", jobTitleName);
        res.put("offeredSalary", offeredSalary);
        res.put("minSalary", minSalary);
        res.put("maxSalary", maxSalary);

        if (offeredSalary == null || offeredSalary <= 0) {
            res.put("valid", false);
            res.put("message", "Mức offer phải lớn hơn 0 VNĐ");
            return res;
        }

        if (minSalary != null && offeredSalary < minSalary) {
            res.put("valid", false);
            res.put("message", String.format("Mức offer (%,.0f VNĐ) thấp hơn dải lương tối thiểu (%,.0f VNĐ) đã cấu hình cho chức danh '%s'.", offeredSalary, minSalary, jobTitleName));
            return res;
        }

        if (maxSalary != null && offeredSalary > maxSalary) {
            res.put("valid", false);
            res.put("message", String.format("Mức offer (%,.0f VNĐ) vượt ngoài dải lương tối đa (%,.0f VNĐ) đã cấu hình cho chức danh '%s'.", offeredSalary, maxSalary, jobTitleName));
            return res;
        }

        res.put("valid", true);
        res.put("message", "Mức lương offer nằm trong giới hạn cho phép của chức danh.");
        return res;
    }

    /**
     * Quy tắc nghiệp vụ cốt lõi: Mức offer không được vượt ngoài dải lương đã cấu hình cho chức danh tương ứng.
     */
    private void validateOfferSalaryRange(Offer offer) {
        Double offered = offer.getOfferedSalary();
        if (offered == null || offered <= 0) {
            throw new AppException("Mức lương offer không hợp lệ!");
        }

        String title = "Chức danh";
        Double minSalary = null;
        Double maxSalary = null;

        if (offer.getApplication() != null && offer.getApplication().getJobRequest() != null) {
            title = offer.getApplication().getJobRequest().getTitle();
            minSalary = offer.getApplication().getJobRequest().getMinSalary();
            maxSalary = offer.getApplication().getJobRequest().getMaxSalary();
        }

        // Ưu tiên tra cứu dải lương từ danh mục chức danh JobTitle
        if (title != null) {
            Optional<JobTitle> jtOpt = jobTitleRepository.findByName(title);
            if (jtOpt.isEmpty()) {
                jtOpt = jobTitleRepository.findByCode(title);
            }
            if (jtOpt.isPresent()) {
                if (jtOpt.get().getMinSalary() != null) minSalary = jtOpt.get().getMinSalary();
                if (jtOpt.get().getMaxSalary() != null) maxSalary = jtOpt.get().getMaxSalary();
            }
        }

        // Kiểm tra chặn nếu vượt ngoài dải lương
        if (minSalary != null && offered < minSalary) {
            throw new AppException(String.format(
                "Không thể phê duyệt Offer! Mức lương đề xuất (%,.0f VNĐ) thấp hơn dải lương tối thiểu (%,.0f VNĐ) đã cấu hình cho chức danh '%s'.",
                offered, minSalary, title
            ));
        }

        if (maxSalary != null && offered > maxSalary) {
            throw new AppException(String.format(
                "Không thể phê duyệt Offer! Mức lương đề xuất (%,.0f VNĐ) vượt ngoài dải lương tối đa (%,.0f VNĐ) đã cấu hình cho chức danh '%s'.",
                offered, maxSalary, title
            ));
        }
    }
}

