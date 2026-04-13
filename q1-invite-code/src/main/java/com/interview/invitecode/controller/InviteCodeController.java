package com.interview.invitecode.controller;

import com.interview.invitecode.dto.*;
import com.interview.invitecode.entity.*;
import com.interview.invitecode.service.InviteCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InviteCodeController {

    private final InviteCodeService inviteCodeService;

    public InviteCodeController(InviteCodeService inviteCodeService) {
        this.inviteCodeService = inviteCodeService;
    }

    @PostMapping("/campaigns")
    public ResponseEntity<Campaign> createCampaign(@RequestBody CreateCampaignRequest request) {
        Campaign campaign = inviteCodeService.createCampaign(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(campaign);
    }

    @PostMapping("/campaigns/{id}/codes/generate")
    public ResponseEntity<List<InviteCode>> generateCodes(
            @PathVariable Long id,
            @RequestBody GenerateCodesRequest request) {
        List<InviteCode> codes = inviteCodeService.generateCodes(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(codes);
    }

    @GetMapping("/codes/{code}")
    public ResponseEntity<InviteCode> getCode(@PathVariable String code) {
        InviteCode inviteCode = inviteCodeService.getCode(code);
        return ResponseEntity.ok(inviteCode);
    }

    @PostMapping("/codes/{code}/validate")
    public ResponseEntity<ValidationResult> validateCode(@PathVariable String code) {
        ValidationResult result = inviteCodeService.validate(code);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/codes/{code}/redeem")
    public ResponseEntity<RedeemResult> redeemCode(
            @PathVariable String code,
            @RequestBody RedeemRequest request) {
        RedeemResult result = inviteCodeService.redeem(code, request);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    @GetMapping("/codes/{code}/redemptions")
    public ResponseEntity<List<Redemption>> getRedemptions(@PathVariable String code) {
        List<Redemption> redemptions = inviteCodeService.getRedemptions(code);
        return ResponseEntity.ok(redemptions);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
