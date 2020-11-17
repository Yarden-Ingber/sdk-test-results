package com.yarden.restServiceDemo.kpis;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.reportService.SheetData;
import com.yarden.restServiceDemo.splunkService.SplunkReporter;
import org.json.JSONObject;

public class KpiSplunkReporter {

    private SheetData rawDataSheetData;
    private TicketUpdateRequest ticketUpdateRequest;

    public KpiSplunkReporter(SheetData rawDataSheetData, TicketUpdateRequest ticketUpdateRequest) {
        this.rawDataSheetData = rawDataSheetData;
        this.ticketUpdateRequest = ticketUpdateRequest;
    }

    public void reportLatestState(JsonElement ticket) {
        rawDataSheetData.getColumnNames();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Started_at_state_new", "1");
        addColumnsToEvent(jsonObject, ticket);
        Logger.info("KPIs: reporting the latest state of ticket to Splunk: " + jsonObject.toString());
        new SplunkReporter().report(Enums.SplunkSourceTypes.RawKPILog, jsonObject.toString());
    }

    public void reportStandAloneEvent(TicketStates newState) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Started_at_state_new", "0");
        jsonObject.put(Enums.KPIsSheetColumnNames.Team.value.replace(" ", "_"), ticketUpdateRequest.getTeam());
        jsonObject.put(Enums.KPIsSheetColumnNames.SubProject.value.replace(" ", "_"), ticketUpdateRequest.getSubProject());
        jsonObject.put(Enums.KPIsSheetColumnNames.TicketID.value.replace(" ", "_"), ticketUpdateRequest.getTicketId());
        jsonObject.put(Enums.KPIsSheetColumnNames.TicketType.value.replace(" ", "_"), ticketUpdateRequest.getTicketType());
        jsonObject.put(Enums.KPIsSheetColumnNames.CreatedBy.value.replace(" ", "_"), ticketUpdateRequest.getCreatedBy());
        jsonObject.put(Enums.KPIsSheetColumnNames.Workaround.value.replace(" ", "_"), ticketUpdateRequest.getWorkaround());
        jsonObject.put(Enums.KPIsSheetColumnNames.TicketTitle.value.replace(" ", "_"), ticketUpdateRequest.getTicketTitle());
        jsonObject.put(Enums.KPIsSheetColumnNames.TicketUrl.value.replace(" ", "_"), ticketUpdateRequest.getTicketUrl());
        jsonObject.put(Enums.KPIsSheetColumnNames.Timestamp.value.replace(" ", "_"), Logger.getTimaStamp());
        jsonObject.put(Enums.KPIsSheetColumnNames.CurrentTrelloList.value.replace(" ", "_"), ticketUpdateRequest.getCurrent_trello_list());
        jsonObject.put(Enums.KPIsSheetColumnNames.Labels.value.replace(" ", "_"), ticketUpdateRequest.getLabels());
        jsonObject.put(Enums.KPIsSheetColumnNames.CurrentState.value.replace(" ", "_"), newState.name());
        Logger.info("KPIs: reporting a new ticket event to Splunk: " + jsonObject.toString());
        new SplunkReporter().report(Enums.SplunkSourceTypes.RawKPILog, jsonObject.toString());
    }

    private void addColumnsToEvent(JSONObject jsonObject, JsonElement ticket) {
        Logger.info("Adding columns to splunk event for ticket: " + ticket.toString());
        for (String column : rawDataSheetData.getColumnNames()) {
            if (column.equals(Enums.KPIsSheetColumnNames.Team.value)) {
                if (ticket.getAsJsonObject().get(column).getAsString().contains(KpisMonitoringService.TeamDelimiter)) {
                    jsonObject.put("Is_cross_boards", "1");
                } else {
                    jsonObject.put("Is_cross_boards", "0");
                }
                jsonObject.put(Enums.KPIsSheetColumnNames.Team.value.replace(" ", "_"), ticketUpdateRequest.getTeam());
            } else if (!column.contains(Enums.KPIsSheetColumnNames.EnterForTimeCalculationState.value)){
                JsonObject jo = ticket.getAsJsonObject();
                Logger.info(jo.toString());
                JsonElement je = jo.get(column);
                Logger.info(je.toString());
                if (je.isJsonNull()) {
                    jsonObject.put(column.replace(" ", "_"), "");
                } else {
                    jsonObject.put(column.replace(" ", "_"), ticket.getAsJsonObject().get(column).getAsString());
                }
            }
        }
    }

}
