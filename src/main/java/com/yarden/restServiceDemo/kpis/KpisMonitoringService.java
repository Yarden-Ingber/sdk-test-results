package com.yarden.restServiceDemo.kpis;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.reportService.SheetData;
import com.yarden.restServiceDemo.reportService.SheetTabIdentifier;
import com.yarden.restServiceDemo.splunkService.SplunkReporter;
import javassist.NotFoundException;

import java.util.ArrayList;
import java.util.List;

public class KpisMonitoringService {

    SheetData rawDataSheetData = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.KPIS.value, Enums.KPIsSheetTabsNames.RawData.value));
    TicketUpdateRequest ticketUpdateRequest;
    TicketStates newState;

    public KpisMonitoringService(TicketUpdateRequest ticketUpdateRequest) {
        this.ticketUpdateRequest = ticketUpdateRequest;
        this.newState = new TicketsNewStateResolver(ticketUpdateRequest).resolve();
    }

    public void updateStateChange() {
        try {
            reportEventToSplunk();
            JsonElement ticket = findSheetEntry();
            ticketUpdateRequest.setTeam(getTeamWithTrelloBoardsChange(ticket));
            new TicketsStateChanger().updateExistingTicketState(ticket, newState);
            updateTicketFields(ticket);
        } catch (NotFoundException e) {
            if (newState.equals(TicketStates.New)) {
                addNewTicketEntry();
            } else {
                Logger.info("KPIs: Ticket" + ticketUpdateRequest.getTicketId() + " sent an update but wasn't opened under field new column");
            }
        }
    }

    public void updateTicketFields() {
        try {
            reportEventToSplunk();
            JsonElement ticket = findSheetEntry();
            ticketUpdateRequest.setTeam(getTeamWithTrelloBoardsChange(ticket));
            updateTicketFields(ticket);
        } catch (NotFoundException e) {
            Logger.info("KPIs: Ticket " + ticketUpdateRequest.getTicketId() + " wasn't found in the sheet");
        }
    }

    public void updateOnlyTrelloList() {
        Logger.info("KPIs: Updating ticket trello list only for ticket " + ticketUpdateRequest.getTicketId() + ": " + ticketUpdateRequest.getCurrent_trello_list());
        reportEventToSplunk();
    }

    private String getTeamWithTrelloBoardsChange(JsonElement ticket){
        String teams = ticket.getAsJsonObject().get(Enums.KPIsSheetColumnNames.Team.value).getAsString();
        if (teams.equals(ticketUpdateRequest.getTeam())) {
            return ticketUpdateRequest.getTeam();
        } else {
            String delimiter = ",";
            if (teams.contains(delimiter)) {
                if (teams.contains(ticketUpdateRequest.getTeam())) {
                    return teams;
                } else {
                    return addTeamLexicographically(teams, delimiter);
                }
            } else {
                return addTeamLexicographically(teams, delimiter);
            }
        }
    }

    private String addTeamLexicographically(String teams, String delimiter){
        String[] teamsArray = teams.split(delimiter);
        List<String> teamsListResult = new ArrayList<>();
        boolean isTeamAdded = false;
        for (String team : teamsArray) {
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
            concatenatedList = concatenatedList + team + delimiter;
        }
        return concatenatedList.substring(0, concatenatedList.length() - 1);
    }

    private void updateTicketFields(JsonElement ticket) {
        addTypeToTicket(ticket);
        ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.Workaround.value, ticketUpdateRequest.getWorkaround());
        ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.Team.value, ticketUpdateRequest.getTeam());
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

    private void addNewTicketEntry(){
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
    }

    private void reportEventToSplunk() {
        JsonElement newEntry = new JsonParser().parse("{\"" + Enums.KPIsSheetColumnNames.Team.value.replace(" ", "_") + "\":\"" + ticketUpdateRequest.getTeam() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.SubProject.value.replace(" ", "_") + "\":\"" + ticketUpdateRequest.getSubProject() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.TicketID.value.replace(" ", "_") + "\":\"" + ticketUpdateRequest.getTicketId() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.TicketType.value.replace(" ", "_") + "\":\"" + ticketUpdateRequest.getTicketType() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.CreatedBy.value.replace(" ", "_") + "\":\"" + ticketUpdateRequest.getCreatedBy() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.Workaround.value.replace(" ", "_") + "\":\"" + ticketUpdateRequest.getWorkaround() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.TicketTitle.value.replace(" ", "_") + "\":\"" + ticketUpdateRequest.getTicketTitle() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.TicketUrl.value.replace(" ", "_") + "\":\"" + ticketUpdateRequest.getTicketUrl() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.Timestamp.value.replace(" ", "_") + "\":\"" + Logger.getTimaStamp() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.CurrentTrelloList.value.replace(" ", "_") + "\":\"" + ticketUpdateRequest.getCurrent_trello_list() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.Labels.value.replace(" ", "_") + "\":\"" + ticketUpdateRequest.getLabels() + "\"," +
                "\"" + Enums.KPIsSheetColumnNames.CurrentState.value.replace(" ", "_") + "\":\"" + newState.name() + "\"}");
        Logger.info("KPIs: reporting a new ticket event to Splunk: " + newEntry.toString());
        new SplunkReporter().report(Enums.SplunkSourceTypes.RawKPILog, newEntry.toString());
    }

}
