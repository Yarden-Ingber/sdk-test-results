# sdk-test-results

### Instructions
This is a Rest API for the web service to post sdk test results to a unified report.
Below you can find a link to the google sheets report and all the endpoints of the web service. <br>
There are 2 main sheets in the report showing results: Coverage comparing, sandbox.
To toggle the results posting between the two sheets use the "sandbox" boolean field in the result json.
#### Please use the Coverage comparing sheet only for RELEASE test runs. For Dev, use sandbox sheet.

### Report

https://docs.google.com/spreadsheets/d/1JZnUB5-nEHWouHJimwyJrTyr-TFsoC9RrKI6U66HJoY

### Production Endpoint

http://sdk-test-results.herokuapp.com

### Routes

Send a `GET` to `/health` - returns a `200`

#### Post new test results
Send a `POST` to `/result` with the JSON payload (below) - returns a `200` and the requested JSON.

```
{  
  "sdk":"java",
  "id":"1234",
  "sandbox":true,
  "results":[  
    {  
      "test_name": "test7",
      "parameters":{
        "browser":"chrome",
        "stitching":"css"
      },
      "passed":true
    },
    {  
      "test_name": "test7",
      "parameters":{
        "browser":"firefox",
        "stitching":"scroll"
      },
      "passed":false
    }
  ]
}
```

#### Add any string data to a result in sandbox sheet
Send a `POST` to `/extra_test_data` with the JSON payload (below) - returns a `200` and the requested JSON.

```
{  
  "sdk":"java",
  "extra_data":[  
    {  
      "test_name": "test7",
      "data": "any string"
    },
    {  
      "test_name": "test8",
      "data": "any string"
    }
  ]
}
```

##### Send a `POST` to `/send_mail/sdks` with the JSON payload (below) - returns a `200`.

```
{  
  "sdk":"java",
  "version":"RELEASE_CANDIDATE-4.1.13",
  "changeLog":"### Fixed
                - Updated accessibility enums (experimental).",
  "testCoverageGap": "coverage gap"
}
```

##### Send a `POST` to `/send_mail/generic` with the JSON payload (below) - returns a `200`.

```
{  
  "mailTextPart":"Hello World!",
  "reportTitle":"Test Report for: Selenium IDE",
  "version":"RELEASE_CANDIDATE-4.1.13",
  "changeLog":"### Fixed
                - Updated accessibility enums (experimental).",
  "testCoverageGap": "coverage gap"
}
```

### Optional Routes

<u>`id` - UUID<u>

If it matches the previous run ID for a given SDK, then it will add the currently provided results to the previous ones. Otherwise, all of the results for the target SDK will be overwritten.
Use this to group together multiple results POST to a single report

<u>`sandbox` - Boolean<u>

If set to true, the ["sandbox"](https://docs.google.com/spreadsheets/d/1JZnUB5-nEHWouHJimwyJrTyr-TFsoC9RrKI6U66HJoY/edit#gid=741958923) worksheet will be written to. It's useful for verifying that your SDK reports correctly to the sheet.

Set it to `false`, or stop sending it in your request, to start using the shared worksheet for all SDKs.
