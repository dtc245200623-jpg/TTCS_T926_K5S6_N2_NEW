package com.example.auth.service;

import com.example.auth.dto.OfferDto;
import java.util.List;
import java.util.Map;

public interface OfferService {
    List<OfferDto> getPendingApprovalOffers();

    void approveOffer(Long id);

    void rejectOffer(Long id);

    Map<String, Object> validateOfferSalary(String jobTitleName, Double offeredSalary);
}