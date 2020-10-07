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
import com.yarden.restServiceDemo.kpis.kpiCalculators.KpiCalculator;
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
        if (ticket.getAsJsonObject().get(Enums.KPIsSheetColumnNames.TimeUntilLeftNewForTheFirstTime.value).getAsString().isEmpty()) {
            Date currentDate = KpiCalculator.timestampToDate(timeStamp);
            Date creationDate = KpiCalculator.timestampToDate(ticket.getAsJsonObject().get(Enums.KPIsSheetColumnNames.CreationDate.value).getAsString());
            Long timeUntilLeftNew = TimeUnit.MILLISECONDS.toMinutes(currentDate.getTime() - creationDate.getTime());
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.TimeUntilLeftNewForTheFirstTime.value, timeUntilLeftNew);
        }
        Date endTime = KpiCalculator.timestampToDate(timeStamp);
        Date startTime = KpiCalculator.timestampToDate(ticket.getAsJsonObject().get(Enums.KPIsSheetColumnNames.EnterForTimeCalculationState.value + currentState.name()).getAsString());
        Long newCalculatedTime = TimeUnit.MILLISECONDS.toMinutes(endTime.getTime() - startTime.getTime());
        String currentCalculatedTimeString = ticket.getAsJsonObject().get(Enums.KPIsSheetColumnNames.CalculatedTimeInState.value + currentState.name()).getAsString();
        Long currentCalculatedTime = 0l;
        if (!currentCalculatedTimeString.isEmpty()) {
            currentCalculatedTime = Long.valueOf(currentCalculatedTimeString);
        }
        ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.CalculatedTimeInState.value + currentState.name(), currentCalculatedTime + newCalculatedTime);
        ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.EnterForTimeCalculationState.value + newState.name(), timeStamp);
        ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.CurrentState.value, newState.name());
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
