# AWS Java SDK v2 Quicksight Dashboard Embedding Example
This sample project was built in Groovy on the Grails 4 framework, but porting it to pure 
Java is trivial.

## Purpose
The purpose of this project is to provide a working example of how to embed AWS Quicksight dashboards using the Java SDK.

## Prerequisites
 - Java 1.8 or higher
 
## Usage
 - Make sure that your properly authenticated. (see Authentication section below)
 - `./grailsw run-app`
 - In a browser, navigate to [http://localhost:8080/?awsAccountId=123456789012&email=john.doe%40company.com](http://localhost:8080/?awsAccountId=123456789012&email=john.doe%40company.com) 
 
## Authentication
 1. Create the following IAM policies
    1. [QuicksightGetDashboardEmbedUrl](QuicksightGetDashboardEmbedUrl.json)
    1. [QuicksightRegisterUser](QuicksightRegisterUser.json)
    1. [QuicksightListUsers](QuicksightListUsers.json)
    1. [QuicksightGetAuthCode](QuicksightGetAuthCode.json)
 1. Create a new IAM role
    1. When asked to 'Select type of trusted entity', I select 'Another AWS account' but specified the
    Account ID of the same account, which worked fine. 
    1. Grant it the policy `QuicksightGetDashboardEmbedUrl`
    1. Call the role `embed-dashboard`. (You can call the role whatever you want, but you will need
    to change the `roleName` property of `ApplicationController.groovy` to match.)
 1. Grant all of the above policies to a service account you will use to authenticate with the AWS
 API
 1. Authenticate with the AWS API using the same service account.
 
 ## Find / Register Quicksight User
 1. Check to see if your user exists; and if so, retrieve the user. See `ApplicationController.fetchUser(..)`
 1. If the user does not already exist, register the user.  
 The only way I was able to successfully register users was as `IAM` users using role we created in 
 step 2 of the Authentication section above. However, this doesn't actually create new IAM users, which 
 was good for me.  
 See `ApplicationController.registerUser(..)`
 
 ## Get the Dashboard's Embed URL
 While still authenticated as the service account and *without* assuming any other role, request the 
 dashboard URL for the user you just found/registered. 
 
 Make sure to: 
 1. Specify the ARN from the User object returned from the above section
 1. Specify `IdentityType.QUICKSIGHT` 
 
 See: `ApplicationController.fetchEmbedUrl(..)`
  
*Note:* As of Jan 2020, the AWS docs describe process in which your service account assumes the role of the 
end user for whom the dashboard URL is being requested and using the assumed role to retrieve the 
embed URL.  I was able to get that method to work via the AWS CLI.  However, I was never able to get 
it to work via the Java SDK.

