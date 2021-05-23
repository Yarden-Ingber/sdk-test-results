package com.yarden.restServiceDemo.kpis;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.ui.ModelMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TrelloApi {

    private static final String trelloApiKey = Enums.EnvVariables.TrelloApiKey.value;
    private static final String trelloApiToken = Enums.EnvVariables.TrelloApiToken.value;

    public static JSONArray getTrelloAccountsArray() throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get("https://api.trello.com/1/organizations/applitools/members")
                .queryString("key", trelloApiKey)
                .queryString("token", trelloApiToken)
                .asJson();
        return response.getBody().getArray();
    }

    public static void uploadFilesToTicket(String ticketId, MultipartFile[] fileArray) throws UnirestException {
        for (MultipartFile multipartFile : fileArray) {
            File file = convertMultipartFileToFile(multipartFile, multipartFile.getOriginalFilename());
            HttpResponse<String> response2 = Unirest.post("https://api.trello.com/1/cards/" + ticketId + "/attachments")
                    .queryString("key", trelloApiKey)
                    .queryString("token", trelloApiToken)
                    .field("file", file).asString();
            file.delete();
        }
    }

    public static String createTicket(ModelMap ticketFormFields) throws UnirestException {
        HttpResponse<JsonNode> createTicketResponse = Unirest.post("https://api.trello.com/1/cards")
                .queryString("key", trelloApiKey)
                .queryString("token", trelloApiToken)
                .queryString("idList", ticketFormFields.get(TrelloTicketCreator.FormFields.listID.name()))
                .queryString("name", ticketFormFields.get(TrelloTicketCreator.FormFields.ticketTitle.name()))
                .queryString("desc", ticketFormFields.get(TrelloTicketCreator.FormFields.ticketDescription.name()))
                .asJson();
        JSONObject myObj = createTicketResponse.getBody().getObject();
        return myObj.getString("id");
    }

    public static void addMemberToTicket(String ticketId, ModelMap ticketFormFields) throws UnirestException {
        Unirest.post("https://api.trello.com/1/cards/" + ticketId + "/idMembers?key=" + trelloApiKey + "&token=" + trelloApiToken + "&value=" +  ticketFormFields.get(TrelloTicketCreator.FormFields.accountID.name()))
                .asString();
    }

    public static void updateCustomFieldValue(ModelMap ticketFormFields, String customFieldName, String ticketID, String fieldValue) {
        try {
            String customFieldID = getCustomFieldId(ticketFormFields, customFieldName);
            HttpResponse<JsonNode> response2 = Unirest.put("https://api.trello.com/1/cards/" + ticketID + "/customField/" + customFieldID + "/item?key=" + trelloApiKey + "&token=" + trelloApiToken)
                    .header("Content-Type", "application/json")
                    .body("{\"value\":{\"text\":\"" + fieldValue + "\"}}")
                    .asJson();
        } catch (Throwable t) {
            Logger.warn("Failed to set custom field value " + customFieldName + "=" + fieldValue);
        }

    }

    public static String getCustomFieldId(ModelMap ticketFormFields, String desiredField) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get("https://api.trello.com/1/boards/" + ticketFormFields.get(TrelloTicketCreator.FormFields.board.name()) + "/customFields?key=" + trelloApiKey + "&token=" + trelloApiToken).asJson();
        JSONArray customFieldsArray = response.getBody().getArray();
        for (Object customField : customFieldsArray) {
            String fieldName = (String)((JSONObject)customField).get("name");
            if (fieldName.equalsIgnoreCase(desiredField)) {
                return (String)((JSONObject)customField).get("id");
            }
        }
        return "not found";
    }

    private static File convertMultipartFileToFile(MultipartFile multipartFile, String fileName) {
        File file = new File(System.getProperty("java.io.tmpdir")+"/"+fileName);
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(multipartFile.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}