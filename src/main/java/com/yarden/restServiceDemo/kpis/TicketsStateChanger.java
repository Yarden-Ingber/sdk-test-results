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
        } else if (currentState.equals(TicketStates.New) && newState.equals(TicketStates.Accepted)) {
            Logger.info("KPIs: Moving ticket to " + TicketStates.Accepted.name() + " state");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.AcceptedDate.value, Logger.getTimaStamp());
        } else if ((currentState.equals(TicketStates.Accepted) || currentState.equals(TicketStates.WaitingForFieldInput))
                && newState.equals(TicketStates.Reproduced)) {
            Logger.info("KPIs: Moving ticket to " + TicketStates.Reproduced.name() + " state");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.ReproducedDate.value, Logger.getTimaStamp());
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.TicketType.value, Enums.KPIsTicketTypes.Bug.value);
        } else if (currentState.equals(TicketStates.Accepted) && newState.equals(TicketStates.WaitingForFieldInput)) {
            Logger.info("KPIs: Moving ticket to " + TicketStates.WaitingForFieldInput.name() + " state");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.MovedToWaitingForFieldInput.value, Logger.getTimaStamp());
        } else if (currentState.equals(TicketStates.WaitingForFieldApproval) && newState.equals(TicketStates.Reproduced)) {
            Logger.info("KPIs: Moving ticket to " + TicketStates.Reproduced.name() + " state");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.ReopenedAfterMovedToApproval.value, Logger.getTimaStamp());
        } else if (currentState.equals(TicketStates.Reproduced) && newState.equals(TicketStates.WaitingForFieldApproval)) {
            Logger.info("KPIs: Moving ticket to " + TicketStates.WaitingForFieldApproval.name() + " state");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.MovedToWaitingForApprovalDate.value, Logger.getTimaStamp());
        } else if ((currentState.equals(TicketStates.New) || currentState.equals(TicketStates.WaitingForFieldInput) || currentState.equals(TicketStates.WaitingForFieldApproval))
                && newState.equals(TicketStates.Done)) {
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
            legalStateChanges.add(new StateChange(TicketStates.New, TicketStates.Accepted));
            legalStateChanges.add(new StateChange(TicketStates.New, TicketStates.Done));
            legalStateChanges.add(new StateChange(TicketStates.Accepted, TicketStates.Reproduced));
            legalStateChanges.add(new StateChange(TicketStates.Accepted, TicketStates.WaitingForFieldInput));
            legalStateChanges.add(new StateChange(TicketStates.WaitingForFieldInput, TicketStates.Reproduced));
            legalStateChanges.add(new StateChange(TicketStates.WaitingForFieldInput, TicketStates.Done));
            legalStateChanges.add(new StateChange(TicketStates.Reproduced, TicketStates.WaitingForFieldApproval));
            legalStateChanges.add(new StateChange(TicketStates.WaitingForFieldApproval, TicketStates.Done));
            legalStateChanges.add(new StateChange(TicketStates.WaitingForFieldApproval, TicketStates.Reproduced));
            legalStateChanges.add(new StateChange(TicketStates.Done, TicketStates.MissingQuality));
            legalStateChanges.add(new StateChange(TicketStates.MissingQuality, TicketStates.Done));
            legalStateChanges.add(new StateChange(TicketStates.New, TicketStates.New));
            legalStateChanges.add(new StateChange(TicketStates.Accepted, TicketStates.Accepted));
            legalStateChanges.add(new StateChange(TicketStates.MissingInformation, TicketStates.MissingInformation));
            legalStateChanges.add(new StateChange(TicketStates.WaitingForFieldInput, TicketStates.WaitingForFieldInput));
            legalStateChanges.add(new StateChange(TicketStates.Reproduced, TicketStates.Reproduced));
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
