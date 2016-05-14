var kPointsBookUrl = "https://docs.google.com/spreadsheets/d/1LUAH8ZzsfN7VQwS27pLzS-0ksiAoxfgWXTlRS30j9pQ/edit";

function doPost(req) {
  try {
    var inputPoints = JSON.parse(req.postData.contents);
    var pointsBook = SpreadsheetApp.openByUrl(kPointsBookUrl);
    var sheet = pointsBook.getSheetByName("PointsBook");
    var range = sheet.getRange("A2:B1000");
    range.clearContent();
    range = sheet.getRange(/* startRow */ 2, /* startCol */ 1, /* numRows */ inputPoints.data.length, /* numColumns */ 3);
    range.setValues(inputPoints.data);
    return ContentService.createTextOutput("Completed response");
  } catch (e) {
    return ContentService.createTextOutput("Something is borked " + e);
  }
}

function testDoPost() {
  doPost({
    "parameter":{},"contextPath":"","contentLength":53,"queryString":null,"parameters":{},"postData":{
      "length":53,
      "type":"application/json",
      "contents":"{\"data\":[[\"Alpha, Alice\",\"500\"],[\"Beta, Bob\",\"499\"]]}",
      "name":"postData"}
  });
}
