# sdk-test-results

### Report

https://docs.google.com/spreadsheets/d/1JZnUB5-nEHWouHJimwyJrTyr-TFsoC9RrKI6U66HJoY

### Production Endpoint

http://sdk-test-results.herokuapp.com

### Routes

Send a `POST` to `/result` with the JSON payload (below) - returns a `200` and a result JSON of how many records were deleted and created.

```
{  
  "sdk":"java",
  "id":"1234",
  "sandbox":true,
  "results":[  
    {  
      "test_name": "test7",
      "parameters":{
        "browser":"chrome"
      },
      "passed":true
    },
    {  
      "test_name": "test7",
      "parameters":{
        "browser":"firefox"
      },
      "passed":false
    }
  ]
}
```

### Optional Routes

<u>`id` - UUID<u>

If it matches the previous run ID for a given SDK, then it will add the currently provided results to the previous ones. Otherwise, all of the results for the target SDK will be overwritten.

<u>`sandbox` - Boolean<u>

If set to true, the ["sandbox"](https://docs.google.com/spreadsheets/d/1JZnUB5-nEHWouHJimwyJrTyr-TFsoC9RrKI6U66HJoY/edit#gid=741958923) worksheet will be written to. It's useful for verifying that your SDK reports correctly to the sheet.

Set it to `false`, or stop sending it in your request, to start using the shared worksheet for all SDKs.
