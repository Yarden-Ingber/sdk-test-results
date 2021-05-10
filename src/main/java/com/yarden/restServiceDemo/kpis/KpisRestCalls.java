package com.yarden.restServiceDemo.kpis;

import com.google.gson.Gson;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.RestCalls;
import com.yarden.restServiceDemo.reportService.WriteEntireSheetsPeriodically;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping(method = RequestMethod.POST, path = "/create_trello_ticket")
    public ResponseEntity create_trello_ticket(@RequestBody String json) {
        synchronized (RestCalls.lock) {
            Logger.info(json);
        }
    }

    private void newRequestPrint(String json, String request){
        Logger.info("**********************************************************************************************");
        Logger.info("**********************************************************************************************");
        Logger.info("KPIs: New KPI request detected: " + request + " === payload: " + json);
    }
}
