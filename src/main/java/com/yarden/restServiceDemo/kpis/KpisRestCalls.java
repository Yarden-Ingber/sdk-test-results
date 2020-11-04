package com.yarden.restServiceDemo.kpis;

import com.google.gson.Gson;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.RestCalls;
import com.yarden.restServiceDemo.reportService.SheetData;
import com.yarden.restServiceDemo.reportService.WriteEntireSheetsPeriodically;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class KpisRestCalls {

    @Test
    public void test() throws IOException {
        state_change("{\"team\":\"sdk\",\"state\":\"Doing\",\"sub_project\":\"javascript\",\"ticket_id\":\"12\",\"ticket_title\":\"NAB: Business mega menu page rendering incorrectly on mobile devices\",\"created_by\":\"Nikhil Nigam\",\"ticket_url\":\"https://trello.com/c/nMNKaa4L\",\"current_trello_list\":\"test\"}");
        SheetData.writeAllTabsToSheet();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/state_update")
    public ResponseEntity state_change(@RequestBody String json) {
        synchronized (RestCalls.lock) {
            WriteEntireSheetsPeriodically.shouldStopSheetWritingTimer = false;
            WriteEntireSheetsPeriodically.start();
            newRequestPrint(json, "/state_change");
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

    private void newRequestPrint(String json, String request){
        Logger.info("**********************************************************************************************");
        Logger.info("**********************************************************************************************");
        Logger.info("KPIs: New KPI request detected: " + request + " === payload: " + json);
    }
}
