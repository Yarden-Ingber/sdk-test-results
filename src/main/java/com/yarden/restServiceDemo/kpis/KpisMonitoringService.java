package com.yarden.restServiceDemo.kpis;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.reportService.SheetData;
import com.yarden.restServiceDemo.reportService.SheetTabIdentifier;
import javassist.NotFoundException;

public class KpisMonitoringService {

    SheetData rawDataSheetData = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.KPIS.value, Enums.KPIsSheetTabsNames.RawData.value));
    SheetData eventLogSheetData = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.KPIS.value, Enums.KPIsSheetTabsNames.EventLog.value));
    TicketUpdateRequest ticketUpdateRequest;
    TicketStates newState;

    public KpisMonitoringService(TicketUpdateRequest ticketUpdateRequest, TicketStates newState) {
        this.ticketUpdateRequest = ticketUpdateRequest;
        this.newState = newState;
    }

    public static JsonArray getAllTickets(){
        return new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.KPIS.value, Enums.KPIsSheetTabsNames.RawData.value)).getSheetData();
    }

    public void updateStateChange() {
        try {
            addStateUpdateToLog();
            JsonElement ticket = findSheetEntry();
            new TicketsStateChanger().updateExistingTicketState(ticket, newState);
        } catch (NotFoundException e) {
            if (newState.equals(TicketStates.New)) {
                addNewTicketEntry();
            } else {
                Logger.info("KPIs: Ticket" + ticketUpdateRequest.getTicketId() + " sent an update but wasn't opened under field new column");
            }
        }
    }

    public void updateTicketType() {
        try {
            Logger.info("KPIs: Updating ticket type for ticket " + ticketUpdateRequest.getTicketId() + ": " + ticketUpdateRequest.getTicketType());
            JsonElement ticket = findSheetEntry();
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.TicketType.value, ticketUpdateRequest.getTicketType());
        } catch (NotFoundException e) {
            Logger.info("KPIs: Ticket " + ticketUpdateRequest.getTicketId() + " wasn't found in the sheet");
        }
    }

    private JsonElement findSheetEntry() throws NotFoundException {
        for (JsonElement sheetEntry: rawDataSheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.TicketID.value).getAsString().equals(ticketUpdateRequest.getTicketId())){
                return sheetEntry;
            }
        }
        throw new NotFoundException("");
    }

    private void addNewTicketEntry(){
        JsonElement newEntry = new JsonParser().parse("{\"" + Enums.KPIsSheetColumnNames.Team.value + "\":\"" + ticketUpdateRequest.getTeam() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.SubProject.value + "\":\"" + ticketUpdateRequest.getSubProject() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.TicketID.value + "\":\"" + ticketUpdateRequest.getTicketId() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.TicketTitle.value + "\":\"" + ticketUpdateRequest.getTicketTitle() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.TicketUrl.value + "\":\"" + ticketUpdateRequest.getTicketUrl() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.CreationDate.value + "\":\"" + Logger.getTimaStamp() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.CreatedBy.value + "\":\"" + ticketUpdateRequest.getCreatedBy() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.EnterForTimeCalculationState.value + TicketStates.New.name() + "\":\"" + Logger.getTimaStamp() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.CurrentState.value + "\":\"" + newState.name() + "\"}");
        Logger.info("KPIs: Adding a new ticket to the sheet: " + newEntry.toString());
        rawDataSheetData.getSheetData().add(newEntry);
    }

    private void addStateUpdateToLog(){
        JsonElement newEntry = new JsonParser().parse("{\"" + Enums.KPIsSheetColumnNames.Team.value + "\":\"" + ticketUpdateRequest.getTeam() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.SubProject.value + "\":\"" + ticketUpdateRequest.getSubProject() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.TicketID.value + "\":\"" + ticketUpdateRequest.getTicketId() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.TicketTitle.value + "\":\"" + ticketUpdateRequest.getTicketTitle() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.TicketUrl.value + "\":\"" + ticketUpdateRequest.getTicketUrl() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.Timestamp.value + "\":\"" + Logger.getTimaStamp() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.CurrentState.value + "\":\"" + newState.name() + "\"}");
        Logger.info("KPIs: Adding a new ticket to the sheet: " + newEntry.toString());
        eventLogSheetData.getSheetData().add(newEntry);
    }

}
