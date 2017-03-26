var kPointsBookUrl = "https://docs.google.com/spreadsheets/d/1LUAH8ZzsfN7VQwS27pLzS-0ksiAoxfgWXTlRS30j9pQ/edit";
var kSheetName = "PointsBook";
var kPlayerRange = "A2:E1000";

function doGet(req) {
  var pointsBook = SpreadsheetApp.openByUrl(kPointsBookUrl);
  var sheet = pointsBook.getSheetByName(kSheetName);
  var range = sheet.getRange(kPlayerRange).getValues();
  var resp = {};
  resp.players = [];
  for (var row = 0; row < range.length; ++row) {
    if (range[row][1] === "") {
      break;
    }
    resp.players.push({
      "name": range[row][1],
      "points": emptyToZero(range[row][2]),
      "local": emptyToZero(range[row][3]),
      "ifpId": range[row][4]
    });
  }
  return ContentService.createTextOutput(JSON.stringify(resp));
}

function emptyToZero(number) {
  return number !== "" ? number : 0;
}

function doPost(req) {
  Logger.log("Running doPost " + req.postData.contents);
  try {
    var inputPoints = JSON.parse(req.postData.contents);
    var data = [];
    for (var i = 0; i < inputPoints.players.length; ++i) {
      var player = inputPoints.players[i];
      data.push([ i + 1, player.name, player.points, player.local, player.ifpId ]);
    }
    var pointsBook = SpreadsheetApp.openByUrl(kPointsBookUrl);
    var sheet = pointsBook.getSheetByName(kSheetName);
    sheet.getRange(kPlayerRange).clearContent();
    Logger.log("New data is " + data);
    var range = sheet.getRange(/* startRow */ 2, /* startCol */ 1, /* numRows */ data.length, /* numColumns */ 5);
    range.setValues(data);
    return ContentService.createTextOutput("Completed response");
  } catch (e) {
    return ContentService.createTextOutput("Something is borked " + e);
  }
}

function testDoGet() {
  var resp = doGet(null);
  Logger.log(resp.getContent());
}

function testDoPost() {
  var testData = {
    "players": [
      {
        "name": "Loffredo, Todd",
        "points": 7500,
        "local": 0,
        "ifpId": "TODD LOFFREDO (CA)",
      },
      {
        "name": "Spredeman, Tony",
        "points": 7000,
        "local": 1,
        "ifpId": "Tony SPREDEMAN (FL)"
      },
      {
        "name": "Player, Test",
        "points": 700,
        "local": 1,
        "ifpId": null
      }
     ]
  };
  var testDataStr = JSON.stringify(testData);
  var resp = doPost({
    "parameter":{},"contextPath":"","contentLength":testDataStr.length, "queryString":null,"parameters":{},"postData":{
      "length": testDataStr.length,
      "type":"application/json",
      "contents": testDataStr,
      "name":"postData"}
  });
  Logger.log(resp.getContent());
}
