yamkins
=======

Jenkins plugin that posts notifications to Yammer groups written in Java

Requirements
------------
* A Yammer user account
* A Yammer group

Installation
------------




Configuration
-------------




Get API token (manual OAuth procedure)
--------------------------------------
# Go to [Yammer Client Applications](https://www.yammer.com/client_applications) to register your app
# Click on "Click here to register a new app"
# Enter details, along these lines:
	> *Application name:* Build Server Notifications
	> *Organization:* Engineering
	> *Support e-mail:* your e-mail adress or an engineering mailing list
	> *Website:* anything, maybe an internal wiki site
# Take note of the client ID and secret on the following application details page.
# Get the client token according to the steps on https://developer.yammer.com/authentication/ or listed here:
#- Log into Yammer with a user account that you like your Jenkins build notifications get posted with
#- Fill in client\_id and any webpage here:
	> https://www.yammer.com/dialog/oauth?client_id=[:client_id]&redirect_uri=[:redirect_uri]
#- Click "Allow"
#- You will be forwarded to an URL copy the code from it:
	> https://www.yammer.com/<URL>/?code=njNBNwmiJlYZheu917Dtw
#- Use this code in this URL, the client ID and secret you already have from step 4.
	> https://www.yammer.com/oauth2/access_token.json?client_id=[:client_id]&client_secret=[:client_secret]&code=[:code]
#- In the returned JSON, find access\_token.token property. This will be your single, long-lived API token to set-up in the Yamkins configuration.

Create Yammer network for testing
---------------------------------
# Go to https://www.yammer.com/signup
# Enter an e-mail address of an existing network you may join or a new one (depends on the hostname)
# Verify e-mal address
# Complete registration process

