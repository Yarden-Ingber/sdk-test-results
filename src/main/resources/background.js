document.querySelector('#myForm').addEventListener('submit', (e) => {
    const btn = document.querySelector('#btn-submit')
    btn.disabled = true
    btn.style.background = '#C1C1C1'
})

document.querySelector('#isLogFileAvailable').addEventListener('change', (e) => {
    const checkBox = document.querySelector('#isLogFileAvailable')
    if (checkBox.checked) {
        const logFileUpload = document.querySelector('#logFiles')
        logFileUpload.disabled = true
      } else {
        const logFileUpload = document.querySelector('#logFiles')
        logFileUpload.disabled = false
      }
})

document.querySelector('#isReproducableAvailable').addEventListener('change', (e) => {
    const checkBox = document.querySelector('#isReproducableAvailable')
    if (checkBox.checked) {
        const logFileUpload = document.querySelector('#reproducable')
        logFileUpload.disabled = true
      } else {
        const logFileUpload = document.querySelector('#reproducable')
        logFileUpload.disabled = false
      }
})

function httpGet(theUrl)
{
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "GET", theUrl, false ); // false for synchronous request
    xmlHttp.send( null );
    return xmlHttp.responseText;
}

var x = document.querySelector("#sdk");
var arrOfSdks = httpGet("https://sdk-test-results.herokuapp.com/get_list_of_sdks").split(",");

for (i = 0; i < arrOfSdks.length; i++) {
    var option = document.createElement("option");
    option.text = arrOfSdks[i];
    option.value = arrOfSdks[i];
    x.add(option);
}