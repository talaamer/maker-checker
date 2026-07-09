package com.bibf.maker_checker;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;

import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlButton;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlTextArea;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class UiFlowTest {

    @Test
    void borrowerCanSubmitLoanRequestAndApproverCanSwitchView() throws IOException {
        int port = findFreePort();
        String[] args = {"--server.port=" + port};
        try (ConfigurableApplicationContext context = SpringApplication.run(MakerCheckerApplication.class, args);
             WebClient webClient = new WebClient()) {
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(false);

            HtmlPage page = webClient.getPage("http://localhost:" + port + "/");
            assertTrue(page.asNormalizedText().contains("FundR"));

            HtmlInput customerName = page.getFirstByXPath("//input[@id='customerName']");
            HtmlInput amount = page.getFirstByXPath("//input[@id='amount']");
            HtmlTextArea note = page.getFirstByXPath("//textarea[@id='note']");
            HtmlButton submit = page.getFirstByXPath("//button[@type='submit']");

            customerName.setValueAttribute("Jane Smith");
            amount.setValueAttribute("18000");
            note.setTextContent("Automated UI test");
            submit.click();

            HtmlPage updatedPage = (HtmlPage) webClient.getCurrentWindow().getEnclosedPage();
            assertTrue(updatedPage.asNormalizedText().contains("Your submitted requests") || updatedPage.asNormalizedText().contains("Request submitted successfully"));

            HtmlButton switchButton = updatedPage.getFirstByXPath("//button[@id='roleSwitch']");
            switchButton.click();

            HtmlPage approverPage = (HtmlPage) webClient.getCurrentWindow().getEnclosedPage();
            assertTrue(approverPage.asNormalizedText().contains("Approval queue") || approverPage.asNormalizedText().contains("Loan office mode"));
        }
    }

    private int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
