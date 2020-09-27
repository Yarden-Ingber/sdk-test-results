package com.yarden.restServiceDemo.kpis;

import com.google.gson.JsonElement;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TicketsStateChanger {

    private static List<StateChange> legalStateChanges = null;

    public void updateExistingTicketState(JsonElement ticket, TicketStates newState){
        TicketStates currentState = TicketStates.valueOf(ticket.getAsJsonObject().get(Enums.KPIsSheetColumnNames.CurrentFlowState.value).getAsString());
        if (!isStateChangeLegal(currentState, newState)) {
            Logger.warn("KPIs: A ticket made an illegal state change");
            sendMailWarning(ticket, newState);
            return;
        }
        if (currentState.equals(newState)) {
            Logger.info("KPIs: The new state is equal to the current state");
            return;
        }
        executeUpdateState(ticket, currentState, newState);
    }

    private void executeUpdateState(JsonElement ticket, TicketStates currentState, TicketStates newState){
        if (newState.equals(TicketStates.New)) {
            Logger.warn("KPIs: Ticket moved from a non new state to new !!!!!!!!!!!!");
        } else if (newState.equals(TicketStates.StartedInvestigation)) {
            Logger.info("KPIs: Moving ticket to " + TicketStates.StartedInvestigation.name() + " state");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.StartedInvestigationDate.value, Logger.getTimaStamp());
        } else if (newState.equals(TicketStates.Reproduced)) {
            Logger.info("KPIs: Moving ticket to " + TicketStates.Reproduced.name() + " state");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.ReproducedDate.value, Logger.getTimaStamp());
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.TicketType.value, Enums.KPIsTicketTypes.Bug.value);
        } else if (newState.equals(TicketStates.MissingInformation)) {
            Logger.info("KPIs: Moving ticket to " + TicketStates.MissingInformation.name() + " state");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.MovedToMissingInformation.value, Logger.getTimaStamp());
        } else if (newState.equals(TicketStates.WaitingForFieldInput)) {
            Logger.info("KPIs: Moving ticket to " + TicketStates.WaitingForFieldInput.name() + " state");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.MovedToWaitingForFieldInput.value, Logger.getTimaStamp());
        } else if (newState.equals(TicketStates.WorkInProgress) && currentState.equals(TicketStates.Reproduced)) {
            Logger.info("KPIs: Moving ticket to " + TicketStates.WorkInProgress.name() + " state");
        } else if (newState.equals(TicketStates.WorkInProgress) && currentState.equals(TicketStates.WaitingForFieldApproval)) {
            Logger.info("KPIs: Moving ticket to " + TicketStates.WorkInProgress.name() + " state");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.ReopenedAfterMovedToApproval.value, Logger.getTimaStamp());
        } else if (newState.equals(TicketStates.WaitingForFieldApproval)) {
            Logger.info("KPIs: Moving ticket to " + TicketStates.WaitingForFieldApproval.name() + " state");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.MovedToWaitingForApprovalDate.value, Logger.getTimaStamp());
        } else if (newState.equals(TicketStates.Done)) {
            Logger.info("KPIs: Moving ticket to " + TicketStates.Done.name() + " state");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.MovedToDoneDate.value, Logger.getTimaStamp());
        } else if (newState.equals(TicketStates.MissingQuality)) {
            Logger.info("KPIs: Moving ticket to " + TicketStates.MissingQuality.name() + " state");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.MissingQuality.value, Logger.getTimaStamp());
        }
        ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.CurrentFlowState.value, newState.name());
    }

    private boolean isStateChangeLegal(TicketStates source, TicketStates destination){
        for (StateChange stateChange : legalStateChanges) {
            if (source.equals(stateChange.source) && destination.equals(stateChange.destination)) {
                return true;
            }
        }
        return false;
    }

    public TicketsStateChanger(){
        if (legalStateChanges == null) {
            legalStateChanges = new ArrayList<>();
            legalStateChanges.add(new StateChange(TicketStates.New, TicketStates.StartedInvestigation));
            legalStateChanges.add(new StateChange(TicketStates.StartedInvestigation, TicketStates.MissingInformation));
            legalStateChanges.add(new StateChange(TicketStates.StartedInvestigation, TicketStates.WaitingForFieldInput));
            legalStateChanges.add(new StateChange(TicketStates.StartedInvestigation, TicketStates.Reproduced));
            legalStateChanges.add(new StateChange(TicketStates.WaitingForFieldInput, TicketStates.Reproduced));
            legalStateChanges.add(new StateChange(TicketStates.MissingInformation, TicketStates.Reproduced));
            legalStateChanges.add(new StateChange(TicketStates.Reproduced, TicketStates.WorkInProgress));
            legalStateChanges.add(new StateChange(TicketStates.WorkInProgress, TicketStates.WaitingForFieldApproval));
            legalStateChanges.add(new StateChange(TicketStates.WaitingForFieldApproval, TicketStates.WorkInProgress));
            legalStateChanges.add(new StateChange(TicketStates.WaitingForFieldApproval, TicketStates.Done));
            legalStateChanges.add(new StateChange(TicketStates.StartedInvestigation, TicketStates.Done));
            legalStateChanges.add(new StateChange(TicketStates.Done, TicketStates.MissingQuality));
            legalStateChanges.add(new StateChange(TicketStates.MissingQuality, TicketStates.Done));
            legalStateChanges.add(new StateChange(TicketStates.New, TicketStates.New));
            legalStateChanges.add(new StateChange(TicketStates.StartedInvestigation, TicketStates.StartedInvestigation));
            legalStateChanges.add(new StateChange(TicketStates.MissingInformation, TicketStates.MissingInformation));
            legalStateChanges.add(new StateChange(TicketStates.WaitingForFieldInput, TicketStates.WaitingForFieldInput));
            legalStateChanges.add(new StateChange(TicketStates.Reproduced, TicketStates.Reproduced));
            legalStateChanges.add(new StateChange(TicketStates.WorkInProgress, TicketStates.WorkInProgress));
            legalStateChanges.add(new StateChange(TicketStates.WaitingForFieldApproval, TicketStates.WaitingForFieldApproval));
            legalStateChanges.add(new StateChange(TicketStates.Done, TicketStates.Done));
        }
    }

    private class StateChange {

        public TicketStates source;
        public TicketStates destination;

        public StateChange(TicketStates source, TicketStates destination) {
            this.source = source;
            this.destination = destination;
        }

    }

    private void sendMailWarning(JsonElement ticket, TicketStates newState) {
        Logger.warn("KPIs: Sending email to notify an illegal state change");
        String ticketID = ticket.getAsJsonObject().get(Enums.KPIsSheetColumnNames.TicketID.value).getAsString();
        String currentState = ticket.getAsJsonObject().get(Enums.KPIsSheetColumnNames.CurrentFlowState.value).getAsString();
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;
        client = new MailjetClient(Enums.EnvVariables.MailjetApiKeyPublic.value, Enums.EnvVariables.MailjetApiKeyPrivate.value, new ClientOptions("v3.1"));
        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", "yarden.ingber@applitools.com")
                                        .put("Name", "Yarden Ingber"))
                                .put(Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", "yarden.ingber@applitools.com")
                                                .put("Name", "Yarden Ingber"))
                                )
                                .put(Emailv31.Message.SUBJECT, "KPIs monitoring warning")
                                .put(Emailv31.Message.TEXTPART, "Ticket " + ticketID + " made an illegal state change from " + currentState + " to " + newState.name())
                                .put(Emailv31.Message.CUSTOMID, "TicketsStateChanger")));
        try {
            response = client.post(request);
            Logger.info("KPIs: " + response.getStatus());
            Logger.info("KPIs: " + response.getData().toString());
        } catch (MailjetException e) {
            e.printStackTrace();
        } catch (MailjetSocketTimeoutException e) {
            e.printStackTrace();
        }
    }

}
