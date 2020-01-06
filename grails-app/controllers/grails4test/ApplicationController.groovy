package grails4test

import software.amazon.awssdk.services.quicksight.QuickSightClient
import software.amazon.awssdk.services.quicksight.model.*

class ApplicationController {
    String roleName = "embed-dashboard"
    String namespace = "default"
    String awsAccountId
    String dashboardId
    QuickSightClient quickSightClient

    def index(String awsAccountId, String email, String dashboardId) {
        if (!awsAccountId || !email || !dashboardId) {
            render "'awsAccountId', 'email', and 'dashboardId' must be specified in the URL.<br/><br/>" +
                "Format: http://localhost:8080/?awsAccountId=123456789012&email=john.doe@company.com&dashboardId=1c1fe111-e2d2-3b30-44ef-a0e111111cde"
            return
        }

        this.awsAccountId = awsAccountId
        this.dashboardId = dashboardId
        quickSightClient = quickSightClient ?: QuickSightClient.create()

        log.info("Retrieving URL for AWS Account: $awsAccountId and user: $email")
        String url = fetchEmbedUrl(email)
        render "<a href='$url' target='_blank'>Dashboard</a>"
    }

    private String fetchEmbedUrl(String email) {
        // look up the user by email address
        // if we don't find the user register them
        User user = fetchUser(email) ?: registerUser(email)

        // get the dashboard URL
        String embedUrl = quickSightClient.getDashboardEmbedUrl(GetDashboardEmbedUrlRequest.builder()
            .awsAccountId(awsAccountId)
            .dashboardId(dashboardId)
            .userArn(user.arn)
            .identityType(IdentityType.QUICKSIGHT)
            .sessionLifetimeInMinutes(600L)
            .build()
        ).embedUrl

        log.info("URL:\n$embedUrl")

        return embedUrl
    }

    private User fetchUser(String email) {
        return quickSightClient.listUsers(ListUsersRequest.builder()
            .awsAccountId(awsAccountId)
            .namespace(namespace)
            .build()
        ).userList().find { it.email() == email }
    }

    private User registerUser(String email) {
        String roleArn = "arn:aws:iam::$awsAccountId:role/$roleName"
        return quickSightClient.registerUser(RegisterUserRequest.builder()
            .awsAccountId(awsAccountId)
            .namespace(namespace)
            .identityType(IdentityType.IAM)
            .iamArn(roleArn)
            .userRole("READER")
            .email(email)
            .sessionName(email)
            .build()
        ).user()
    }

}
