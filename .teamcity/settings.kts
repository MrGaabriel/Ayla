import jetbrains.buildServer.configs.kotlin.v2018_1.*
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2018_1.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_1.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2018.1"

project {

    buildType(Build)

    params {
        param("teamcity.activeBuildBranch.age.hours", "0")
        param("teamcity.activeVcsBranch.age.days", "1")
    }

    subProject(Development)
}

object Build : BuildType({
    name = "Build"

    artifactRules = """
        target/Ayla-1.0.0-SNAPSHOT.jar
        target/dependencies => target/dependencies
    """.trimIndent()

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            name = "Maven"
            goals = "install"
            mavenVersion = defaultProvidedVersion()
        }
        script {
            name = "Copy Artifacts"
            scriptContent = """
                #!/bin/bash
                cp -rf ./target/dependencies /root/ayla
                cp -rf ./target/Ayla-1.0.0-SNAPSHOT.jar /root/ayla
                
                screen -X -S ayla kill
                screen -dmS ayla
                screen -S ayla -X stuff ${'$'}'cd ~/ayla/ && sh start.sh\n'
            """.trimIndent()
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        feature {
            type = "teamcity.github.status"
            param("guthub_context", "TeamCity")
            param("guthub_owner", "MrGaabriel")
            param("guthub_authentication_type", "token")
            param("guthub_guest", "true")
            param("guthub_username", "MrGaabriel")
            param("guthub_repo", "Ayla")
            param("github_report_on", "on start and finish")
            param("secure:github_access_token", "credentialsJSON:e4ac208e-0198-4ead-8c6b-41f156ef44f8")
            param("secure:guthub_username", "credentialsJSON:efc9d0a7-f4dd-4882-a6f5-166b7e357d8b")
        }
    }
})


object Development : Project({
    name = "Development"

    vcsRoot(Development_HttpsGithubComMrGaabrielAylaRefsHeadsMaster)

    buildType(Development_Build)
})

object Development_Build : BuildType({
    name = "Build"

    vcs {
        root(Development_HttpsGithubComMrGaabrielAylaRefsHeadsMaster)
    }

    steps {
        maven {
            goals = "clean install"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
            mavenVersion = defaultProvidedVersion()
        }
        script {
            name = "Copy Artifacts"
            scriptContent = """
                #!/bin/bash
                cp -rf ./target/dependencies /root/ayla-canary
                cp -rf ./target/Ayla-1.0.0-SNAPSHOT.jar /root/ayla-canary
                
                screen -X -S ayla-canary kill
                screen -dmS ayla-canary
                screen -S ayla-canary -X stuff ${'$'}'cd ~/ayla-canary/ && sh start.sh\n'
            """.trimIndent()
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        feature {
            type = "teamcity.github.status"
            param("guthub_context", "TeamCity")
            param("guthub_owner", "MrGaabriel")
            param("guthub_authentication_type", "token")
            param("guthub_guest", "true")
            param("guthub_repo", "Ayla")
            param("github_report_on", "on start and finish")
            param("secure:github_access_token", "credentialsJSON:805210ae-e198-4f4f-8edd-e2bf8e1babfe")
        }
    }
})

object Development_HttpsGithubComMrGaabrielAylaRefsHeadsMaster : GitVcsRoot({
    name = "https://github.com/MrGaabriel/Ayla#refs/heads/master"
    url = "https://github.com/MrGaabriel/Ayla"
    branch = "refs/heads/development"
    authMethod = password {
        userName = "MrGaabriel"
        password = "credentialsJSON:fa1479da-d0b1-4ea9-8171-9ef6e041f9c3"
    }
})
