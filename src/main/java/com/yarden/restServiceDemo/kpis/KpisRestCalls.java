package com.yarden.restServiceDemo.kpis;

import com.google.gson.Gson;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.RestCalls;
import com.yarden.restServiceDemo.reportService.WriteEntireSheetsPeriodically;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class KpisRestCalls {


    public void test() throws IOException {
        state_update("{\"team\":\"JS SDKs\",\"sub_project\":\"\",\"ticket_id\":\"ike58Acv\",\"ticket_title\":\"Storybook RFE | Add option to not fail the test suite when diffs are found\",\"created_by\":\"Rivka Beck\",\"ticket_url\":\"https://trello.com/c/ike58Acv\",\"state\":\"New\",\"current_trello_list\":\"New\",\"ticket_type\":\"\",\"workaround\":\"\",\"labels\":\"\"}");
    }

    @RequestMapping(method = RequestMethod.POST, path = "/state_update")
    public ResponseEntity state_update(@RequestBody String json) {
        synchronized (RestCalls.lock) {
            WriteEntireSheetsPeriodically.shouldStopSheetWritingTimer = false;
            WriteEntireSheetsPeriodically.start();
            newRequestPrint(json, "/state_update");
            TicketUpdateRequest ticketUpdateRequest = new Gson().fromJson(json, TicketUpdateRequest.class);
            new KpisMonitoringService(ticketUpdateRequest).updateStateChange();
            return new ResponseEntity(ticketUpdateRequest.toString(), HttpStatus.OK);
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/update_ticket_fields")
    public ResponseEntity update_ticket_fields(@RequestBody String json) {
        synchronized (RestCalls.lock) {
            WriteEntireSheetsPeriodically.shouldStopSheetWritingTimer = false;
            WriteEntireSheetsPeriodically.start();
            newRequestPrint(json, "/update_ticket_fields");
            TicketUpdateRequest ticketUpdateRequest = new Gson().fromJson(json, TicketUpdateRequest.class);
            new KpisMonitoringService(ticketUpdateRequest).updateTicketFields();
            return new ResponseEntity(ticketUpdateRequest.toString(), HttpStatus.OK);
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/archive_card")
    public ResponseEntity archive_card(@RequestBody String json) {
        synchronized (RestCalls.lock) {
            WriteEntireSheetsPeriodically.shouldStopSheetWritingTimer = false;
            WriteEntireSheetsPeriodically.start();
            newRequestPrint(json, "/archive_card");
            TicketUpdateRequest ticketUpdateRequest = new Gson().fromJson(json, TicketUpdateRequest.class);
            new KpisMonitoringService(ticketUpdateRequest).archiveCard();
            return new ResponseEntity(ticketUpdateRequest.toString(), HttpStatus.OK);
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/update_only_trello_list")
    public ResponseEntity update_only_trello_list(@RequestBody String json) {
        synchronized (RestCalls.lock) {
            WriteEntireSheetsPeriodically.shouldStopSheetWritingTimer = false;
            WriteEntireSheetsPeriodically.start();
            newRequestPrint(json, "/update_only_trello_list");
            TicketUpdateRequest ticketUpdateRequest = new Gson().fromJson(json, TicketUpdateRequest.class);
            new KpisMonitoringService(ticketUpdateRequest).updateOnlyTrelloList();
            return new ResponseEntity(ticketUpdateRequest.toString(), HttpStatus.OK);
        }
    }

    @GetMapping(value = "/get_create_ticket_page", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String get_create_ticket_page() throws IOException, UnirestException {
        return TrelloTicketCreator.getTicketCreationFormHtml();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/get_trello_ticket_url")
    public String get_trello_ticket_url(@RequestParam String requestID) {
        return TrelloTicketCreator.getTrelloTicketUrl(requestID);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/create_trello_ticket")
    public void create_trello_ticket(@RequestParam(name="account") String account,
                                     @RequestParam(name="boards") String board,
                                     @RequestParam(name="ticketTitle") String ticketTitle,
                                     @RequestParam(name="ticketDescription") String ticketDescription,
                                     @RequestParam(name="requestID") String requestID,
                                     @RequestParam(required=false,name="customerAppUrl") String customerAppUrl,
                                     @RequestParam(required=false,name="sdk") String sdk,
                                     @RequestParam(required=false,name="sdkVersion") String sdkVersion,
                                     @RequestParam(required=false,name="linkToTestResults") String linkToTestResults,
                                     @RequestParam(required=false,name="isAccessible") String isAppAccessible,
                                     @RequestParam(required=false,name="renderID") String renderID,
                                     @RequestParam(required=false,name="logFiles") MultipartFile[] logFiles,
                                     @RequestParam(required=false,name="reproducible") MultipartFile[] reproducibleFiles,
                                     ModelMap ticketFormFields) {
        ticketFormFields.addAttribute(TrelloTicketCreator.FormFields.requestID.name(), requestID);
        ticketFormFields.addAttribute(TrelloTicketCreator.FormFields.accountName.name(), account.split(",")[0]);
        ticketFormFields.addAttribute(TrelloTicketCreator.FormFields.accountID.name(), account.split(",")[1]);
        ticketFormFields.addAttribute(TrelloTicketCreator.FormFields.board.name(), board.split(",")[0]);
        ticketFormFields.addAttribute(TrelloTicketCreator.FormFields.listID.name(), board.split(",")[1]);
        ticketFormFields.addAttribute(TrelloTicketCreator.FormFields.ticketTitle.name(), ticketTitle);
        ticketFormFields.addAttribute(TrelloTicketCreator.FormFields.ticketDescription.name(), ticketDescription);
        ticketFormFields.addAttribute(TrelloTicketCreator.FormFields.customerAppUrl.name(), customerAppUrl == null ? "" : customerAppUrl);
        ticketFormFields.addAttribute(TrelloTicketCreator.FormFields.sdk.name(), sdk == null ? "" : sdk);
        ticketFormFields.addAttribute(TrelloTicketCreator.FormFields.sdkVersion.name(), sdkVersion == null ? "" : sdkVersion);
        ticketFormFields.addAttribute(TrelloTicketCreator.FormFields.linkToTestResults.name(), linkToTestResults == null ? "" : linkToTestResults);
        ticketFormFields.addAttribute(TrelloTicketCreator.FormFields.logFiles.name(), logFiles);
        ticketFormFields.addAttribute(TrelloTicketCreator.FormFields.reproducibleFiles.name(), reproducibleFiles);
        ticketFormFields.addAttribute(TrelloTicketCreator.FormFields.isAppAccessible.name(), isAppAccessible == null ? "" : isAppAccessible);
        ticketFormFields.addAttribute(TrelloTicketCreator.FormFields.renderID.name(), renderID == null ? "" : renderID);
        Logger.info("KpisRestCalls: Trello ticket creation request: " + ticketFormFields.toString());
        try {
            TrelloTicketCreator.createTicket(ticketFormFields);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    private void newRequestPrint(String json, String request){
        Logger.info("**********************************************************************************************");
        Logger.info("**********************************************************************************************");
        Logger.info("KPIs: New KPI request detected: " + request + " === payload: " + json);
    }
}
