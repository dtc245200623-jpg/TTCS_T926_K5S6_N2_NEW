package com.example.auth.dto;

import lombok.Data;

@Data
public class OfferDto {
    private Long id;
    private String candidateName;
    private String jobTitle;
    private Double offeredSalary;
    private Double minSalary;
    private Double maxSalary;
    private String status;
    private String salaryStatus; // "VALID", "BELOW_MIN", "EXCEEDS_MAX"
    private String salaryMessage; // "Hợp lệ", "Thấp hơn dải lương tối thiểu", "Vượt ngoài dải lương tối đa"
}
