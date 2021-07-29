#!/bin/bash

readonly URL=https://api.stats.slingshotfoos.com/api/rating
#readonly URL=https://sandblower.net/cgi-bin/printenv

curl -o points-raw.json \
  -H "user-agent: okhttp/3.10.0" \
  -H "authkey: d2718a4e-b192-455e-b971-134b2771db16" \
  ${URL}

python3 -m json.tool points-raw.json > pretty.json
rm points-raw.json
