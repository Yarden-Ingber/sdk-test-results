package com.yarden.restServiceDemo.kpis;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.reportService.SheetData;
import com.yarden.restServiceDemo.reportService.SheetTabIdentifier;
import javassist.NotFoundException;

import java.util.ArrayList;
import java.util.List;

public class KpisMonitoringService {

    SheetData rawDataSheetData = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.KPIS.value, Enums.KPIsSheetTabsNames.RawData.value));
    TicketUpdateRequest ticketUpdateRequest;
    TicketStates newState;
    public static final String TeamDelimiter = ",";

    public KpisMonitoringService(TicketUpdateRequest ticketUpdateRequest) {
        this.ticketUpdateRequest = ticketUpdateRequest;
        this.newState = new TicketsNewStateResolver(ticketUpdateRequest).resolve();
    }

    public void updateStateChange() {
        try {
            JsonElement ticket = findSheetEntry();
            new TicketsStateChanger().updateExistingTicketState(ticket, newState);
            updateTicketFields(ticket);
            new KpiSplunkReporter(rawDataSheetData, ticketUpdateRequest).reportLatestState(ticket);
        } catch (NotFoundException e) {
            if (newState.equals(TicketStates.New)) {
                JsonElement ticket = addNewTicketEntry();
                new KpiSplunkReporter(rawDataSheetData, ticketUpdateRequest).reportLatestState(ticket);
            } else {
                Logger.info("KPIs: Ticket" + ticketUpdateRequest.getTicketId() + " sent an update but wasn't opened under field new column");
                new KpiSplunkReporter(rawDataSheetData, ticketUpdateRequest).reportStandAloneEvent(newState);
            }
        }
    }

    public void updateTicketFields() {
        try {
            JsonElement ticket = findSheetEntry();
            updateTicketFields(ticket);
            new KpiSplunkReporter(rawDataSheetData, ticketUpdateRequest).reportLatestState(ticket);
        } catch (NotFoundException e) {
            Logger.info("KPIs: Ticket " + ticketUpdateRequest.getTicketId() + " wasn't found in the sheet");
            new KpiSplunkReporter(rawDataSheetData, ticketUpdateRequest).reportStandAloneEvent(newState);
        }
    }

    public void updateOnlyTrelloList() {
        Logger.info("KPIs: Updating ticket trello list only for ticket " + ticketUpdateRequest.getTicketId() + ": " + ticketUpdateRequest.getCurrent_trello_list());
        new KpiSplunkReporter(rawDataSheetData, ticketUpdateRequest).reportStandAloneEvent(newState);
    }

    public void archiveCard() {
        try {
            ticketUpdateRequest.setTeam("archived");
            JsonElement ticket = findSheetEntry();
            updateTicketFields(ticket);
            new KpiSplunkReporter(rawDataSheetData, ticketUpdateRequest).reportLatestState(ticket);
        } catch (NotFoundException e) {
            Logger.info("KPIs: Ticket " + ticketUpdateRequest.getTicketId() + " wasn't found in the sheet");
        }
    }

    private String getTeamWithTrelloBoardsChange(JsonElement ticket){
        String teams = ticket.getAsJsonObject().get(Enums.KPIsSheetColumnNames.Team.value).getAsString();
        if (teams.equals(ticketUpdateRequest.getTeam())) {
            return ticketUpdateRequest.getTeam();
        } else {
            return addTeamLexicographically(teams);
        }
    }

    private String addTeamLexicographically(String teams){
        String[] teamsArray = teams.split(TeamDelimiter);
        List<String> teamsListResult = new ArrayList<>();
        boolean isTeamAdded = false;
        for (String team : teamsArray) {
            if (team.equals(ticketUpdateRequest.getTeam())) {
                return teams;
            }
            if (team.compareTo(ticketUpdateRequest.getTeam()) > 0) {
                teamsListResult.add(ticketUpdateRequest.getTeam());
                isTeamAdded = true;
            }
            teamsListResult.add(team);
        }
        if (!isTeamAdded) {
            teamsListResult.add(ticketUpdateRequest.getTeam());
        }
        String concatenatedList = "";
        for (String team : teamsListResult) {
            concatenatedList = concatenatedList + team + TeamDelimiter;
        }
        return concatenatedList.substring(0, concatenatedList.length() - 1);
    }

    private void updateTicketFields(JsonElement ticket) {
        addTypeToTicket(ticket);
        ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.Workaround.value, ticketUpdateRequest.getWorkaround());
        ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.Team.value, getTeamWithTrelloBoardsChange(ticket));
        ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.SubProject.value, ticketUpdateRequest.getSubProject());
        ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.TicketID.value, ticketUpdateRequest.getTicketId());
        ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.TicketTitle.value, ticketUpdateRequest.getTicketTitle());
        ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.TicketUrl.value, ticketUpdateRequest.getTicketUrl());
        ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.CreatedBy.value, ticketUpdateRequest.getCreatedBy());
        ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.CurrentTrelloList.value, ticketUpdateRequest.getCurrent_trello_list());
        ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.Labels.value, ticketUpdateRequest.getLabels());
    }

    private void addTypeToTicket(JsonElement ticket) {
        Logger.info("KPIs: Updating ticket type for ticket " + ticketUpdateRequest.getTicketId() + ": " + ticketUpdateRequest.getTicketType());
        String type = ticketUpdateRequest.getTicketType() == null ? "" : ticketUpdateRequest.getTicketType();
        ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.TicketType.value, type);
    }

    private JsonElement findSheetEntry() throws NotFoundException {
        for (JsonElement sheetEntry: rawDataSheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.TicketID.value).getAsString().equals(ticketUpdateRequest.getTicketId())){
                return sheetEntry;
            }
        }
        throw new NotFoundException("");
    }

    private JsonElement addNewTicketEntry(){
        JsonElement newEntry = new JsonParser().parse("{\"" + Enums.KPIsSheetColumnNames.Team.value + "\":\"" + ticketUpdateRequest.getTeam() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.SubProject.value + "\":\"" + ticketUpdateRequest.getSubProject() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.TicketID.value + "\":\"" + ticketUpdateRequest.getTicketId() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.TicketTitle.value + "\":\"" + ticketUpdateRequest.getTicketTitle() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.TicketUrl.value + "\":\"" + ticketUpdateRequest.getTicketUrl() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.CreationDate.value + "\":\"" + Logger.getTimaStamp() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.TicketType.value + "\":\"" + ticketUpdateRequest.getTicketType() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.CreatedBy.value + "\":\"" + ticketUpdateRequest.getCreatedBy() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.Workaround.value + "\":\"" + ticketUpdateRequest.getWorkaround() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.CurrentTrelloList.value + "\":\"" + ticketUpdateRequest.getCurrent_trello_list() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.Labels.value + "\":\"" + ticketUpdateRequest.getLabels() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.EnterForTimeCalculationState.value + TicketStates.New.name() + "\":\"" + Logger.getTimaStamp() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.CurrentState.value + "\":\"" + newState.name() + "\"}");
        Logger.info("KPIs: Adding a new ticket to the sheet: " + newEntry.toString());
        rawDataSheetData.getSheetData().add(newEntry);
        return newEntry;
    }

}
