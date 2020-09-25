package com.yarden.restServiceDemo.kpis;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.reportService.SheetData;
import com.yarden.restServiceDemo.reportService.SheetTabIdentifier;
import javassist.NotFoundException;

public class KpisMonitoringService {

    SheetData sheetData = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.KPIS.value, Enums.KPIsSheetTabsNames.RawData.value));
    TicketUpdateRequest ticketUpdateRequest;
    TicketStates newState;

    public KpisMonitoringService(TicketUpdateRequest ticketUpdateRequest, TicketStates newState) {
        this.ticketUpdateRequest = ticketUpdateRequest;
        this.newState = newState;
    }

    public void updateStateChange() {
        try {
            JsonElement ticket = findSheetEntry();
            new TicketsStateChanger().updateState(ticket, newState);
        } catch (NotFoundException e) {
            if (!newState.equals(TicketStates.New)) {
                Logger.info("*#^#$%^#$^&@%^@$%&@#$^*$^&*#%^");
            }
            JsonElement newEntry = new JsonParser().parse("{\"" + Enums.KPIsSheetColumnNames.Team.value + "\":\"" + ticketUpdateRequest.getTeam() + "\"," +
                    "\"" + Enums.KPIsSheetColumnNames.SubProject.value + "\":\"" + ticketUpdateRequest.getSubProject() + "\"," +
                    "\"" + Enums.KPIsSheetColumnNames.TicketID.value + "\":\"" + ticketUpdateRequest.getTicketId() + "\"," +
                    "\"" + Enums.KPIsSheetColumnNames.TicketTitle.value + "\":\"" + ticketUpdateRequest.getTicketTitle() + "\"," +
                    "\"" + Enums.KPIsSheetColumnNames.TicketUrl.value + "\":\"" + ticketUpdateRequest.getTicketUrl() + "\"," +
                    "\"" + Enums.KPIsSheetColumnNames.CreationDate.value + "\":\"" + Logger.getTimaStamp() + "\"," +
                    "\"" + Enums.KPIsSheetColumnNames.CreatedBy.value + "\":\"" + ticketUpdateRequest.getCreatedBy() + "\"," +
                    "\"" + Enums.KPIsSheetColumnNames.CurrentFlowState.value + "\":\"" + newState.name() + "\"}");
            Logger.info("*#^#$%^#$^&@%^@$%&@#$^*$^&*#%^");
            sheetData.getSheetData().add(newEntry);
        }
    }

    public void updateTicketType() {
        try {
            Logger.info("*#^#$%^#$^&@%^@$%&@#$^*$^&*#%^");
            JsonElement ticket = findSheetEntry();
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.TicketType.value, ticketUpdateRequest.getTicketType());
        } catch (NotFoundException e) {
            Logger.info("*#^#$%^#$^&@%^@$%&@#$^*$^&*#%^");
        }
    }

    private JsonElement findSheetEntry() throws NotFoundException {
        for (JsonElement sheetEntry: sheetData.getSheetData()){
            if (sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.TicketID.value).getAsString().equals(ticketUpdateRequest.getTicketId())){
                return sheetEntry;
            }
        }
        throw new NotFoundException("");
    }

}
