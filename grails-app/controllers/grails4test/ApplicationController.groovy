package grails4test

import grails.core.GrailsApplication
import grails.plugins.GrailsPluginManager
import grails.plugins.PluginManagerAware
import software.amazon.awssdk.services.quicksight.QuickSightClient
import software.amazon.awssdk.services.quicksight.model.*

class ApplicationController implements PluginManagerAware {

    GrailsApplication grailsApplication
    GrailsPluginManager pluginManager

    def index(String awsAccountId, String email) {
        String url = fetchEmbedUrl(awsAccountId, email)
        render "<a href='$url' target='_blank'>Dashboard</a>"
    }

    def metadata() {
        [grailsApplication: grailsApplication, pluginManager: pluginManager]
    }

    private String fetchEmbedUrl(String awsAccountId, String email) {
        String namespace = "default"

        // make sure the user is registered
        QuickSightClient quickSightClient = QuickSightClient.create()
        User user = quickSightClient.listUsers(ListUsersRequest.builder()
            .awsAccountId(awsAccountId)
            .namespace(namespace)
            .build()
        ).userList().find { it.email() == email }

        if (!user) {
            user = registerUser(quickSightClient, awsAccountId, namespace, email)
        }

        String embedUrl = quickSightClient.getDashboardEmbedUrl(GetDashboardEmbedUrlRequest.builder()
            .awsAccountId(awsAccountId)
            .dashboardId("c390031c-07e1-4490-ae21-b55d46d6c4c0")
            .userArn(user.arn)
            .identityType(IdentityType.QUICKSIGHT)
            .sessionLifetimeInMinutes(600L)
            .build()
        ).embedUrl

        log.info("URL:\n$embedUrl")

        return embedUrl
    }

    private User registerUser(QuickSightClient quickSightClient, String awsAccountId, String namespace, String email) {
        String roleName = "cc-embedding_quicksight_dashboard_role"
        String roleArn = "arn:aws:iam::$awsAccountId:role/$roleName"
        quickSightClient.registerUser(RegisterUserRequest.builder()
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
