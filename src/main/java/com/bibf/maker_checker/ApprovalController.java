package com.bibf.maker_checker;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/approval")
public class ApprovalController {

	private final Map<Long, ApprovalDecision> decisions = new ConcurrentHashMap<>();

	@GetMapping("/pending")
	public List<LoanRequestResponse> getPendingRequests() {
		return decisions.entrySet().stream()
				.filter(entry -> "PENDING".equals(entry.getValue().status()))
				.map(entry -> new LoanRequestResponse(entry.getKey(), entry.getValue().customerName(), entry.getValue().amount(), entry.getValue().status(), entry.getValue().note()))
				.toList();
	}

	@GetMapping("/requests")
	public List<LoanRequestResponse> getAllRequests() {
		return decisions.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(entry -> new LoanRequestResponse(entry.getKey(), entry.getValue().customerName(), entry.getValue().amount(), entry.getValue().status(), entry.getValue().note()))
				.toList();
	}

	@PostMapping("/requests")
	public ResponseEntity<LoanRequestResponse> createLoanRequest(@RequestBody(required = false) LoanRequest request) {
		Long loanId = (long) (decisions.size() + 1);
		String customerName = request != null && request.customerName() != null ? request.customerName().trim() : "Unknown";
		Double amount = request != null && request.amount() != null ? request.amount() : 0.0;
		String note = extractNote(request != null ? request.note() : null);
		decisions.put(loanId, new ApprovalDecision("PENDING", customerName, amount, note));
		return ResponseEntity.ok(new LoanRequestResponse(loanId, customerName, amount, "PENDING", note));
	}

	@PostMapping("/{loanId}/approve")
	public ResponseEntity<ApprovalResponse> approve(@PathVariable Long loanId,
			@RequestBody(required = false) ApprovalRequest request) {
		String note = extractNote(request);
		ApprovalDecision current = decisions.getOrDefault(loanId, new ApprovalDecision("PENDING", "Unknown", 0.0, null));
		decisions.put(loanId, new ApprovalDecision("APPROVED", current.customerName(), current.amount(), note));
		return ResponseEntity.ok(new ApprovalResponse(loanId, "APPROVED", note));
	}

	@PostMapping("/{loanId}/reject")
	public ResponseEntity<ApprovalResponse> reject(@PathVariable Long loanId,
			@RequestBody(required = false) ApprovalRequest request) {
		String note = extractNote(request);
		ApprovalDecision current = decisions.getOrDefault(loanId, new ApprovalDecision("PENDING", "Unknown", 0.0, null));
		decisions.put(loanId, new ApprovalDecision("REJECTED", current.customerName(), current.amount(), note));
		return ResponseEntity.ok(new ApprovalResponse(loanId, "REJECTED", note));
	}

	@PostMapping("/{loanId}/topup")
	public ResponseEntity<LoanRequestResponse> topUp(@PathVariable Long loanId,
			@RequestBody(required = false) TopUpRequest request) {
		ApprovalDecision current = decisions.getOrDefault(loanId, new ApprovalDecision("PENDING", "Unknown", 0.0, null));
		Double addedAmount = request != null && request.amount() != null ? request.amount() : 0.0;
		Double updatedAmount = current.amount() + addedAmount;
		String note = extractNote(request != null ? request.note() : null);
		decisions.put(loanId, new ApprovalDecision(current.status(), current.customerName(), updatedAmount, note != null ? note : current.note()));
		return ResponseEntity.ok(new LoanRequestResponse(loanId, current.customerName(), updatedAmount, current.status(), note != null ? note : current.note()));
	}

	private String extractNote(String note) {
		if (note == null || note.isBlank()) {
			return null;
		}
		return note.trim();
	}

	private String extractNote(ApprovalRequest request) {
		return extractNote(request != null ? request.note() : null);
	}

	public record ApprovalRequest(String note) {
	}

	public record LoanRequest(String customerName, Double amount, String note) {
	}

	public record TopUpRequest(Double amount, String note) {
	}

	public record LoanRequestResponse(Long loanId, String customerName, Double amount, String status, String note) {
	}

	public record ApprovalResponse(Long loanId, String status, String note) {
	}

	private record ApprovalDecision(String status, String customerName, Double amount, String note) {
	}
}
