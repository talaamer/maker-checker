package com.bibf.maker_checker;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MakerCheckerApplicationTests {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new ApprovalController()).build();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void pendingRequestsReturnsEmptyListInitially() throws Exception {
		mockMvc.perform(get("/approval/pending"))
				.andExpect(status().isOk())
				.andExpect(content().json("[]"));
	}

	@Test
	void createLoanRequestCreatesPendingRequest() throws Exception {
		mockMvc.perform(post("/approval/requests")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"customerName\":\"John\",\"amount\":15000,\"note\":\"Need quick review\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.loanId").exists())
				.andExpect(jsonPath("$.customerName").value("John"))
				.andExpect(jsonPath("$.amount").value(15000))
				.andExpect(jsonPath("$.status").value("PENDING"))
				.andExpect(jsonPath("$.note").value("Need quick review"));
	}

	@Test
	void approveLoanReturnsApprovedStatus() throws Exception {
		mockMvc.perform(post("/approval/1/approve"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.loanId").value(1))
				.andExpect(jsonPath("$.status").value("APPROVED"));
	}

	@Test
	void rejectLoanReturnsRejectedStatus() throws Exception {
		mockMvc.perform(post("/approval/2/reject"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.loanId").value(2))
				.andExpect(jsonPath("$.status").value("REJECTED"));
	}

	@Test
	void approveLoanAcceptsOptionalNote() throws Exception {
		mockMvc.perform(post("/approval/3/approve")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"note\":\"Customer verified\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.loanId").value(3))
				.andExpect(jsonPath("$.status").value("APPROVED"))
				.andExpect(jsonPath("$.note").value("Customer verified"));
	}

	@Test
	void rejectLoanAcceptsOptionalNote() throws Exception {
		mockMvc.perform(post("/approval/4/reject")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"note\":\"Insufficient documents\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.loanId").value(4))
				.andExpect(jsonPath("$.status").value("REJECTED"))
				.andExpect(jsonPath("$.note").value("Insufficient documents"));
	}
}
