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

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TicketsStateChanger {

    public void updateExistingTicketState(JsonElement ticket, TicketStates newState){
        TicketStates currentState = TicketStates.valueOf(ticket.getAsJsonObject().get(Enums.KPIsSheetColumnNames.CurrentState.value).getAsString());
        if (currentState.equals(newState)) {
            Logger.info("KPIs: The new state is equal to the current state");
            return;
        }
        try {
            executeUpdateState(ticket, currentState, newState);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void executeUpdateState(JsonElement ticket, TicketStates currentState, TicketStates newState) throws ParseException {
        String timeStamp = Logger.getTimaStamp();
        JsonElement timeUntilLeftNewJsonElement = ticket.getAsJsonObject().get(Enums.KPIsSheetColumnNames.TimeUntilLeftNewForTheFirstTime.value);
        if (timeUntilLeftNewJsonElement == null || timeUntilLeftNewJsonElement.isJsonNull() ||
                timeUntilLeftNewJsonElement.getAsString() == null || timeUntilLeftNewJsonElement.getAsString().isEmpty()) {
            setTimeUntilLeftNewForFirstTime(timeStamp, ticket);
        }
        if (!(currentState.equals(TicketStates.Done) || currentState.equals(TicketStates.NoState))) {
            addCalculatedTimeInPreviousState(timeStamp, ticket, currentState);
        }
        writeNewStateTimestamp(timeStamp, ticket, newState);
        ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.CurrentState.value, newState.name());
    }

    public void writeNewStateTimestamp(String timeStamp, JsonElement ticket, TicketStates newState) {
        String newStateColumnName;
        if (newState.equals(TicketStates.Done)) {
            newStateColumnName = Enums.KPIsSheetColumnNames.MovedToStateDone.value;
        } else {
            newStateColumnName = Enums.KPIsSheetColumnNames.EnterForTimeCalculationState.value + newState.name();
        }
        ticket.getAsJsonObject().addProperty(newStateColumnName, timeStamp);
    }

    public void addCalculatedTimeInPreviousState(String timeStamp, JsonElement ticket, TicketStates currentState) throws ParseException {
        Date endTime = Logger.timestampToDate(timeStamp);
        Date startTime = Logger.timestampToDate(ticket.getAsJsonObject().get(Enums.KPIsSheetColumnNames.EnterForTimeCalculationState.value + currentState.name()).getAsString());
        Long newCalculatedTime = TimeUnit.MILLISECONDS.toHours(endTime.getTime() - startTime.getTime());
        String currentCalculatedTimeString = "";
        JsonElement previousStateTimeFromSheet = ticket.getAsJsonObject().get(Enums.KPIsSheetColumnNames.CalculatedTimeInState.value + currentState.name());
        if (previousStateTimeFromSheet != null && !previousStateTimeFromSheet.isJsonNull()) {
            currentCalculatedTimeString = previousStateTimeFromSheet.getAsString();
        }
        Logger.info("Setting time in previous state: " + currentState.name() + " for ticket: " + ticket.getAsJsonObject().get(Enums.KPIsSheetColumnNames.TicketID.value).getAsString() +
                " enter previous state: " + startTime.toString() + " enter new state: " + endTime.toString() + " calculated time before change: " + currentCalculatedTimeString + " new calculated time: " + newCalculatedTime.toString());
        Long currentCalculatedTime = 0l;
        if (!currentCalculatedTimeString.isEmpty()) {
            currentCalculatedTime = Long.valueOf(currentCalculatedTimeString);
        }
        ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.CalculatedTimeInState.value + currentState.name(), currentCalculatedTime + newCalculatedTime);
    }

    private void setTimeUntilLeftNewForFirstTime(String timeStamp, JsonElement ticket) throws ParseException {
        Date currentDate = Logger.timestampToDate(timeStamp);
        Date creationDate = Logger.timestampToDate(ticket.getAsJsonObject().get(Enums.KPIsSheetColumnNames.CreationDate.value).getAsString());
        Long timeUntilLeftNew = TimeUnit.MILLISECONDS.toHours(currentDate.getTime() - creationDate.getTime());
        Logger.info("Setting time until left new for the first time for ticket: " + ticket.getAsJsonObject().get(Enums.KPIsSheetColumnNames.TicketID.value).getAsString() +
                " Createion date: " + creationDate.toString() + " current timestamp: " + currentDate.toString() + " total time: " + timeUntilLeftNew);
        ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.TimeUntilLeftNewForTheFirstTime.value, timeUntilLeftNew);
    }

    private void sendMailWarning(JsonElement ticket, TicketStates newState) {
        Logger.warn("KPIs: Sending email to notify an illegal state change");
        String ticketID = ticket.getAsJsonObject().get(Enums.KPIsSheetColumnNames.TicketID.value).getAsString();
        String currentState = ticket.getAsJsonObject().get(Enums.KPIsSheetColumnNames.CurrentState.value).getAsString();
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
