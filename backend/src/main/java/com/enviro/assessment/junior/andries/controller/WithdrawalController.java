package com.enviro.assessment.junior.andries.controller;

import com.enviro.assessment.junior.andries.dto.WithdrawalRequestDTO;
import com.enviro.assessment.junior.andries.dto.WithdrawalResponseDTO;
import com.enviro.assessment.junior.andries.service.WithdrawalService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/withdrawals")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    public WithdrawalController(WithdrawalService withdrawalService) {
        this.withdrawalService = withdrawalService;
    }

    @PostMapping
    public ResponseEntity<WithdrawalResponseDTO> createWithdrawal(@Valid @RequestBody WithdrawalRequestDTO request) {
        WithdrawalResponseDTO response = withdrawalService.createWithdrawal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/investor/{investorId}")
    public List<WithdrawalResponseDTO> getHistory(
            @PathVariable Long investorId,
            @RequestParam(required = false) Long productId) {
        return withdrawalService.getHistory(investorId, productId);
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportCsv(
            @RequestParam Long investorId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        String csv = withdrawalService.exportCsv(investorId, productId, startDate, endDate);
        String filename = "withdrawal-statement-investor-" + investorId + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
