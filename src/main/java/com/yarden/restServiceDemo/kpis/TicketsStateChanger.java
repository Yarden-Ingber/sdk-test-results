package com.yarden.restServiceDemo.kpis;

import com.google.gson.JsonElement;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;

import java.util.ArrayList;
import java.util.List;

public class TicketsStateChanger {

    private static List<StateChange> legalStateChanges = null;

    public void updateState(JsonElement ticket, TicketStates newState){
        TicketStates currentState = TicketStates.valueOf(ticket.getAsJsonObject().get(Enums.KPIsSheetColumnNames.CurrentFlowState.value).getAsString());
        if (!isStateChangeLegal(currentState, newState)) {
            Logger.info("*#^#$%^#$^&@%^@$%&@#$^*$^&*#%^");
            return;
        }
        if (currentState.equals(newState)) {
            Logger.info("*#^#$%^#$^&@%^@$%&@#$^*$^&*#%^");
            return;
        }
        if (newState.equals(TicketStates.New)) {
            Logger.info("*#^#$%^#$^&@%^@$%&@#$^*$^&*#%^");
        } else if (newState.equals(TicketStates.StartedInvestigation)) {
            Logger.info("*#^#$%^#$^&@%^@$%&@#$^*$^&*#%^");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.StartedInvestigationDate.value, Logger.getTimaStamp());
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.CurrentFlowState.value, newState.name());
        } else if (newState.equals(TicketStates.Reproduced)) {
            Logger.info("*#^#$%^#$^&@%^@$%&@#$^*$^&*#%^");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.ReproducedDate.value, Logger.getTimaStamp());
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.TicketType.value, Enums.KPIsTicketTypes.Bug.value);
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.CurrentFlowState.value, newState.name());
        } else if (newState.equals(TicketStates.MissingInformation)) {
            Logger.info("*#^#$%^#$^&@%^@$%&@#$^*$^&*#%^");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.MovedToMissingInformation.value, "true");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.CurrentFlowState.value, newState.name());
        } else if (newState.equals(TicketStates.WaitingForFieldInput)) {
            Logger.info("*#^#$%^#$^&@%^@$%&@#$^*$^&*#%^");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.MovedToWaitingForFieldInput.value, "true");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.CurrentFlowState.value, newState.name());
        } else if (newState.equals(TicketStates.WorkInProgress) && currentState.equals(TicketStates.Reproduced)) {
            Logger.info("*#^#$%^#$^&@%^@$%&@#$^*$^&*#%^");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.CurrentFlowState.value, newState.name());
        } else if (newState.equals(TicketStates.WorkInProgress) && currentState.equals(TicketStates.WaitingForFieldApproval)) {
            Logger.info("*#^#$%^#$^&@%^@$%&@#$^*$^&*#%^");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.ReopenedAfterMovedToApproval.value, "true");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.CurrentFlowState.value, newState.name());
        } else if (newState.equals(TicketStates.WaitingForFieldApproval)) {
            Logger.info("*#^#$%^#$^&@%^@$%&@#$^*$^&*#%^");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.MovedToWaitingForApprovalDate.value, Logger.getTimaStamp());
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.CurrentFlowState.value, newState.name());
        } else if (newState.equals(TicketStates.Done)) {
            Logger.info("*#^#$%^#$^&@%^@$%&@#$^*$^&*#%^");
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.MovedToDoneDate.value, Logger.getTimaStamp());
            ticket.getAsJsonObject().addProperty(Enums.KPIsSheetColumnNames.CurrentFlowState.value, newState.name());
        }
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

}
