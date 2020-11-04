package com.yarden.restServiceDemo.kpis;

import com.google.gson.Gson;
import com.yarden.restServiceDemo.Logger;

public class TicketsNewStateResolver {

    private TicketUpdateRequest request;

    public TicketsNewStateResolver(TicketUpdateRequest request) {
        this.request = request;
    }

    private enum Boards {
        UltrafastGrid("Ultrafast Grid"), JSSDKs("JS SDKs"), AlgoBugs("Algo Bugs"), SDKs("SDKs"), EyesAppIssues("Eyes App - Issues");

        public String value;

        Boards(String value) {
            this.value = value;
        }
    }

    public TicketStates resolve() {
        if (request.getTeam().equals(Boards.UltrafastGrid.value)) {
            return resolveStateForUFG();
        } else if (request.getTeam().equals(Boards.JSSDKs.value)) {
            return resolveStateForJSSdks();
        } else if (request.getTeam().equals(Boards.AlgoBugs.value)) {
            return resolveStateForAlgoBugs();
        } else if(request.getTeam().equals(Boards.SDKs.value)) {
            return resolveStateForGeneralSdks();
        } else if(request.getTeam().equals(Boards.EyesAppIssues.value)) {
            return resolveStateForEyesIssues();
        } else {
            return noStateFound();
        }
    }

    private TicketStates resolveStateForUFG(){
        if (request.getCurrent_trello_list().equals("New")) {
            return TicketStates.New;
        } else if (request.getCurrent_trello_list().equals("Doing")) {
            return TicketStates.Doing;
        } else if (request.getCurrent_trello_list().equals("Waiting for field input")) {
            return TicketStates.WaitingForFieldInput;
        } else if (request.getCurrent_trello_list().equals("Done")) {
            return TicketStates.Done;
        } else if (request.getCurrent_trello_list().equals("Waiting for R&D")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().equals("Missing quality")) {
            return TicketStates.MissingQuality;
        } else if (request.getCurrent_trello_list().equals("Trying to reproduce")) {
            return TicketStates.TryingToReproduce;
        } else if (request.getCurrent_trello_list().equals("Waiting for field approval")) {
            return TicketStates.WaitingForFieldApproval;
        } else if (request.getCurrent_trello_list().equals("RFE")) {
            return TicketStates.RFE;
        } else if (request.getCurrent_trello_list().equals("Waiting for Product")) {
            return TicketStates.WaitingForProduct;
        } else  {
            return noStateFound();
        }
    }

    private TicketStates resolveStateForAlgoBugs() {
        if (request.getCurrent_trello_list().equals("Waiting for field input")) {
            return TicketStates.WaitingForFieldInput;
        } else if (request.getCurrent_trello_list().equals("New/Pending")) {
            return TicketStates.New;
        } else if (request.getCurrent_trello_list().equals("Layout")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().equals("RCA")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().equals("Contrast")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().equals("Waiting for Adam")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().equals("Automated maintenance")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().equals("Strict - Broken")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().equals("Hot ticket in work")) {
            return TicketStates.Doing;
        } else if (request.getCurrent_trello_list().equals("Solved/Waiting for publish")) {
            return TicketStates.Doing;
        } else if (request.getCurrent_trello_list().equals("Deployed to cloud")) {
            return TicketStates.WaitingForFieldApproval;
        } else if (request.getCurrent_trello_list().equals("Closed")) {
            return TicketStates.Done;
        } else if (request.getCurrent_trello_list().equals("No Algo Change Required")) {
            return TicketStates.Done;
        } else if (request.getCurrent_trello_list().equals("Known limitations (RFE)")) {
            return TicketStates.RFE;
        } else if (request.getCurrent_trello_list().equals("Waiting for other ticket")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().equals("Missing information")) {
            return TicketStates.WaitingForFieldInput;
        } else if (request.getCurrent_trello_list().equals("Waiting for product")) {
            return TicketStates.WaitingForProduct;
        } else {
            return noStateFound();
        }
    }

    private TicketStates resolveStateForJSSdks() {
        if (request.getCurrent_trello_list().equals("Doing")) {
            return TicketStates.Doing;
        } else if (request.getCurrent_trello_list().equals("New")) {
            return TicketStates.New;
        } else if (request.getCurrent_trello_list().equals("Waiting for field input")) {
            return TicketStates.WaitingForFieldInput;
        } else if (request.getCurrent_trello_list().equals("Done")) {
            return TicketStates.Done;
        } else if (request.getCurrent_trello_list().equals("Waiting for R&D")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().equals("Missing quality")) {
            return TicketStates.MissingQuality;
        } else if (request.getCurrent_trello_list().equals("Trying to reproduce")) {
            return TicketStates.TryingToReproduce;
        } else if (request.getCurrent_trello_list().equals("Waiting for field approval")) {
            return TicketStates.WaitingForFieldApproval;
        } else if (request.getCurrent_trello_list().equals("RFEs")) {
            return TicketStates.RFE;
        } else if (request.getCurrent_trello_list().equals("Waiting for Product")) {
            return TicketStates.WaitingForProduct;
        } else if (request.getCurrent_trello_list().equals("Next")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().equals("Bugs")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().equals("Fixed - need to release other SDK's")) {
            return TicketStates.Done;
        } else {
            return noStateFound();
        }
    }

    private TicketStates resolveStateForGeneralSdks() {
        if (request.getCurrent_trello_list().equals("New | Pending")) {
            return TicketStates.New;
        } else if (request.getCurrent_trello_list().equals("Trying to Reproduce")) {
            return TicketStates.TryingToReproduce;
        } else if (request.getCurrent_trello_list().equals("Doing")) {
            return TicketStates.Doing;
        } else if (request.getCurrent_trello_list().equals("Waiting For Release")) {
            return TicketStates.Doing;
        } else if (request.getCurrent_trello_list().equals("Waiting For Field's Input")) {
            return TicketStates.WaitingForFieldInput;
        } else if (request.getCurrent_trello_list().equals("Waiting for Field Approval")) {
            return TicketStates.WaitingForFieldApproval;
        } else if (request.getCurrent_trello_list().equals("Done")) {
            return TicketStates.Done;
        } else if (request.getCurrent_trello_list().equals("Refactor Tests to Generic")) {
            return TicketStates.Done;
        } else if (request.getCurrent_trello_list().equals("All SDKs: Implementation updates")) {
            return TicketStates.Done;
        } else if (request.getCurrent_trello_list().equals("On Hold")) {
            return TicketStates.Done;
        } else if (request.getCurrent_trello_list().equals("Known Limitations / Waiting for 3rd Party")) {
            return TicketStates.Done;
        } else if (request.getCurrent_trello_list().contains("RFEs")) {
            return TicketStates.RFE;
        } else if (request.getCurrent_trello_list().equals("Waiting for Product")) {
            return TicketStates.WaitingForProduct;
        } else if (request.getCurrent_trello_list().contains("Waiting for R&D investigation")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().contains("BUGs")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().equals("Next")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().equals("For Daniel's Review")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().equals("Integrations")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().equals("UFT")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().equals("Missing Quality Info")) {
            return TicketStates.MissingQuality;
        } else {
            return noStateFound();
        }
    }

    private TicketStates resolveStateForEyesIssues() {
        if (request.getCurrent_trello_list().equals("New")) {
            return TicketStates.New;
        } else if (request.getCurrent_trello_list().equals("Waiting for R&D")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().equals("Trying to reproduce")) {
            return TicketStates.TryingToReproduce;
        } else if (request.getCurrent_trello_list().equals("Doing")) {
            return TicketStates.Doing;
        } else if (request.getCurrent_trello_list().equals("On Hold / low priority")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().equals("Waiting for field input")) {
            return TicketStates.WaitingForFieldInput;
        } else if (request.getCurrent_trello_list().equals("Waiting for product")) {
            return TicketStates.WaitingForProduct;
        } else if (request.getCurrent_trello_list().equals("For Amit to Review")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().equals("To be Deployed next Hotfix")) {
            return TicketStates.WaitingForRD;
        } else if (request.getCurrent_trello_list().contains("Done")) {
            return TicketStates.Done;
        } else if (request.getCurrent_trello_list().equals("Waiting for field approval")) {
            return TicketStates.WaitingForFieldApproval;
        } else {
            return noStateFound();
        }
    }

    private TicketStates noStateFound(){
        Gson gson = new Gson();
        Logger.warn("KPIs: no state found for request " + gson.toJson(request));
        return TicketStates.NoState;
    }
}
