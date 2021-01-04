package com.yarden.restServiceDemo;

import com.google.gson.JsonSyntaxException;
import com.yarden.restServiceDemo.awsS3Service.AwsS3ResultsJsonsService;
import com.yarden.restServiceDemo.reportService.*;
import com.yarden.restServiceDemo.slackService.EyesSlackReporterSender;
import com.yarden.restServiceDemo.slackService.NonTestTableSlackReportSender;
import com.yarden.restServiceDemo.slackService.SdkSlackReportSender;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestCalls {

    public static final String lock = "LOCK";

    @RequestMapping(method = RequestMethod.POST, path = "/result")
    public ResponseEntity postResults(@RequestBody String json) {
        synchronized (lock) {
            WriteEntireSheetsPeriodically.shouldStopSheetWritingTimer = false;
            WriteEntireSheetsPeriodically.start();
            newRequestPrint(json, "/result");
            try {
                AwsS3ResultsJsonsService.addSdkRequestToS3File(json);
                new SdkReportService().postResults(json);
            } catch (InternalError e) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (JsonSyntaxException e) {
                String errorMessage = "Failed parsing the json: \n\n" + json + "\n\n" + e.getMessage();
                Logger.error(errorMessage);
                return new ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity(json, HttpStatus.OK);
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/delete_previous_results")
    public ResponseEntity deletePreviousResults(@RequestBody String json) {
        synchronized (lock) {
            WriteEntireSheetsPeriodically.shouldStopSheetWritingTimer = false;
            WriteEntireSheetsPeriodically.start();
            newRequestPrint(json, "/delete_previous_results");
            try {
                new SdkReportService().deleteAllColumnsForSdkInAllTabs(json);
            } catch (InternalError e) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (JsonSyntaxException e) {
                String errorMessage = "Failed parsing the json: \n\n" + json + "\n\n" + e.getMessage();
                Logger.error(errorMessage);
                return new ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity(json, HttpStatus.OK);
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/eyes_result")
    public ResponseEntity postEyesResults(@RequestBody String json) {
        synchronized (lock) {
            WriteEntireSheetsPeriodically.shouldStopSheetWritingTimer = false;
            WriteEntireSheetsPeriodically.start();
            newRequestPrint(json, "/eyes_result");
            try {
                AwsS3ResultsJsonsService.addEyesRequestToS3File(json);
                new EyesReportService().postResults(json);
            } catch (InternalError e) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (JsonSyntaxException e) {
                String errorMessage = "Failed parsing the json: \n\n" + json + "\n\n" + e.getMessage();
                Logger.error(errorMessage);
                return new ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity(json, HttpStatus.OK);
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/vg_status")
    public ResponseEntity postVisualGridStatus(@RequestBody String json) {
        synchronized (lock) {
            VisualGridStatusPageRequestTimer.isRequestReceived = true;
            WriteEntireSheetsPeriodically.shouldStopSheetWritingTimer = false;
            WriteEntireSheetsPeriodically.start();
            Logger.info("**********New VG status request detected**********");
            try {
                new VisualGridStatusPageService().postResults(json);
            } catch (Throwable e) {
                String errorMessage = "Request failed: \n\n" + json + "\n\n" + e.getMessage();
                Logger.error(errorMessage);
                return new ResponseEntity(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity(json, HttpStatus.OK);
        }
    }

    @RequestMapping(method = RequestMethod.GET, path = "/health")
    public ResponseEntity getHealth(){
        return new ResponseEntity("Up and running!", HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/extra_test_data")
    public ResponseEntity postExtraTestData(@RequestBody String json){
        synchronized (lock) {
            newRequestPrint(json, "/health");
            try {
                new SdkReportService().postExtraTestData(json);
            } catch (JsonSyntaxException e) {
                return new ResponseEntity("Failed parsing the json: " + json, HttpStatus.BAD_REQUEST);
            } catch (InternalError internalError) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity(json, HttpStatus.OK);
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/send_mail")
    public ResponseEntity sendSdkMailReport(@RequestBody String json){
        return sendSdkMailReportOverload(json);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/send_mail/sdks")
    public ResponseEntity sendSdkMailReportOverload(@RequestBody String json){
        synchronized (lock) {
            newRequestPrint(json, "/send_mail/sdks");
            try {
                SheetData.writeAllTabsToSheet();
                new SdkSlackReportSender().send(json);
            } catch (Throwable t) {
                t.printStackTrace();
                return new ResponseEntity("Failed sending email: " + t.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity("Mail sent", HttpStatus.OK);
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/send_full_regression/sdks")
    public ResponseEntity sendSdkFullRegressionReport(@RequestBody String json){
        synchronized (lock) {
            newRequestPrint(json, "/send_full_regression/sdks");
            Logger.info(Enums.EnvVariables.TurnOffFullRegressionEmail.value);
            if (Boolean.valueOf(Enums.EnvVariables.TurnOffFullRegressionEmail.value)) {
                return new ResponseEntity("Mail is turned off by server", HttpStatus.OK);
            }
            try {
                SheetData.writeAllTabsToSheet();
                new SdkSlackReportSender().sendFullRegression(json);
            } catch (Throwable t) {
                t.printStackTrace();
                return new ResponseEntity("Failed sending email: " + t.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity("Mail sent", HttpStatus.OK);
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/tests_end/eyes")
    public ResponseEntity sendEyesMailReport(@RequestBody String json){
        synchronized (lock) {
            newRequestPrint(json, "/tests_end/eyes");
            try {
                if (json == null) {
                    json = "{}";
                }
                new EyesSlackReporterSender().send(json);
            } catch (Throwable throwable) {
                return new ResponseEntity("Failed sending email: " + throwable.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity("Mail sent", HttpStatus.OK);
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/reset_eyes_report_data")
    public ResponseEntity deleteEntireEyesData(){
        synchronized (lock) {
            WriteEntireSheetsPeriodically.shouldStopSheetWritingTimer = false;
            WriteEntireSheetsPeriodically.start();
            newRequestPrint("", "/reset_eyes_report_data");
            try {
                new EyesReportService().deleteAllData();
                new EyesSlackReporterSender().resetEndTasksCounter();
            } catch (Throwable throwable) {
                return new ResponseEntity("Failed deleting eyes report data: " + throwable.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity("Data deleted", HttpStatus.OK);
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/send_mail/generic")
    public ResponseEntity sendGenericMailReport(@RequestBody String json){
        synchronized (lock) {
            newRequestPrint(json, "/send_mail/generic");
            try {
                new NonTestTableSlackReportSender().send(json);
            } catch (Throwable throwable) {
                return new ResponseEntity("Failed sending email: " + throwable.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity("Mail sent", HttpStatus.OK);
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/dump_results")
    public ResponseEntity dumpResults(){
        synchronized (lock) {
            try {
                SheetData.writeAllTabsToSheet();
            } catch (Throwable throwable) {
                return new ResponseEntity("Failed to dump data to sheet: " + throwable.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity("Sheet is updated", HttpStatus.OK);
        }
    }

    private void newRequestPrint(String json, String request){
        Logger.info("**********************************************************************************************");
        Logger.info("**********************************************************************************************");
        String jsonWithoutWhitespace = json.replace(" ", "").replace("\n", "");
        if (jsonWithoutWhitespace.contains("\"sandbox\":true")) {
            Logger.info("New sandbox request detected: " + request);
        } else {
            Logger.info("New request detected: " + request + " === payload: " + json.replace(" ", ""));
        }
    }

}
