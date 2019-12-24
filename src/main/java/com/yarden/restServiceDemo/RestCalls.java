package com.yarden.restServiceDemo;
import com.google.gson.JsonSyntaxException;
import com.yarden.restServiceDemo.mailService.NonTestTableMailSender;
import com.yarden.restServiceDemo.mailService.SdkMailSender;
import com.yarden.restServiceDemo.reportService.SdkReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestCalls {

    private static final String lock = "LOCK";

    @RequestMapping(method = RequestMethod.POST, path = "/result")
    public ResponseEntity postResults(@RequestBody String json) {
        synchronized (lock) {
            newRequestPrint(json);
            try {
                new SdkReportService().postResults(json);
            } catch (InternalError e) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (JsonSyntaxException e) {
                return new ResponseEntity("Failed parsing the json: \n\n" + json + "\n\n" + e.getMessage(), HttpStatus.BAD_REQUEST);
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
            newRequestPrint(json);
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
            newRequestPrint(json);
            try {
                new SdkMailSender().send(json);
            } catch (Throwable t) {
                t.printStackTrace();
                return new ResponseEntity("Failed sending email: " + t.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity("Mail sent", HttpStatus.OK);
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/send_mail/generic")
    public ResponseEntity sendGenericMailReport(@RequestBody String json){
        synchronized (lock) {
            newRequestPrint(json);
            try {
                new NonTestTableMailSender().send(json);
            } catch (Throwable throwable) {
                return new ResponseEntity("Failed sending email: " + throwable.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity("Mail sent", HttpStatus.OK);
        }
    }

    private void newRequestPrint(String json){
        Logger.info("**********************************************************************************************");
        Logger.info("**********************************************************************************************");
        Logger.info("New result request detected: " + json);
    }

}
