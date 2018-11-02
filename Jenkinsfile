#!groovy

pipeline {
    agent any
    stages {
        stage("Parallel Stage") {
            parallel "jdk8": {
                node("linux") {
                    stage("Build / Test - JDK8") {
                        timeout(time: 120, unit: 'MINUTES') {
                            checkout scm
                            mavenBuild("jdk8", "/", "-V -B -T6 -e -Dmaven.test.failure.ignore=true install -Djetty.testtracker.log=true -Pmongodb -Dunix.socket.tmp=" + env.JENKINS_HOME)
                            // Report failures in the jenkins UI
                            junit testResults: '**/target/surefire-reports/TEST-*.xml,**/target/failsafe-reports/TEST-*.xml'
                            consoleParsers = [[parserName: 'Maven'], [parserName: 'JavaC']]

                            // Collect up the jacoco execution results (only on main build)
                            def jacocoExcludes =
                                // build tools
                                "**/org/eclipse/jetty/ant/**" + ",**/org/eclipse/jetty/maven/**" +
                                    ",**/org/eclipse/jetty/jspc/**" +
                                    // example code / documentation
                                    ",**/org/eclipse/jetty/embedded/**" + ",**/org/eclipse/jetty/asyncrest/**" +
                                    ",**/org/eclipse/jetty/demo/**" +
                                    // special environments / late integrations
                                    ",**/org/eclipse/jetty/gcloud/**" + ",**/org/eclipse/jetty/infinispan/**" +
                                    ",**/org/eclipse/jetty/osgi/**" + ",**/org/eclipse/jetty/spring/**" +
                                    ",**/org/eclipse/jetty/http/spi/**" +
                                    // test classes
                                    ",**/org/eclipse/jetty/tests/**" + ",**/org/eclipse/jetty/test/**"
                            jacoco inclusionPattern: '**/org/eclipse/jetty/**/*.class',
                                exclusionPattern: jacocoExcludes,
                                execPattern: '**/target/jacoco.exec',
                                classPattern: '**/target/classes',
                                sourcePattern: '**/src/main/java'
                            step([$class         : 'MavenInvokerRecorder', reportsFilenamePattern: "**/target/invoker-reports/BUILD*.xml",
                                  invokerBuildDir: "**/target/its"])

                            // Report errors seen on console
                            step([$class: 'WarningsPublisher', consoleParsers: consoleParsers])
                        }
                    }
                }
            }, "jdk11": {
                node("linux") {
                    stage("Build / Test - JDK11") {
                        checkout scm
                        mavenBuild("jdk11", "/", "-V -B -T6 -e -Dmaven.test.failure.ignore=true install -Djetty.testtracker.log=true -Pmongodb -Dunix.socket.tmp=" + env.JENKINS_HOME)
                        junit testResults: '**/target/surefire-reports/TEST-*.xml,**/target/failsafe-reports/TEST-*.xml'
                        consoleParsers = [[parserName: 'Maven'], [parserName: 'JavaC']]
                        step([$class: 'WarningsPublisher', consoleParsers: consoleParsers])
                    }
                }
            }, "javadoc": {
                node("linux") {
                    stage("Build Javadoc") {
                        timeout(time: 30, unit: 'MINUTES') {
                            checkout scm
                            mavenBuild("jdk8", "/", "-V -B -T6 -e -Dmaven.test.failure.ignore=false javadoc:javadoc")
                            junit testResults: '**/target/surefire-reports/TEST-*.xml,**/target/failsafe-reports/TEST-*.xml'
                            consoleParsers = [[parserName: 'Maven'], [parserName: 'JavaDoc'], [parserName: 'JavaC']]
                            step([$class: 'WarningsPublisher', consoleParsers: consoleParsers])
                        }
                    }
                }
            }, "compact3": {
                node("linux") {
                    stage("Build Compact3") {
                        timeout(time: 30, unit: 'MINUTES') {
                            checkout scm
                            mavenBuild("jdk8", "/", "-V -B -e -Pcompact3 -Dmaven.test.failure.ignore=false package")
                            consoleParsers = [[parserName: 'JavaC']]
                            step([$class: 'WarningsPublisher', consoleParsers: consoleParsers])
                        }
                    }
                }
            }
        }
    }
}


def mavenBuild(jdk, path, cmdline) {
    def mvnName = 'maven3.5'
    def localRepo = "${env.JENKINS_HOME}/${env.EXECUTOR_NUMBER}" // ".repository" //
    def settingsName = 'oss-settings.xml'
    def mavenOpts = '-Xms1g -Xmx4g -Djava.awt.headless=true'

    withMaven(
        maven: mvnName,
        jdk: "$jdk",
        publisherStrategy: 'EXPLICIT',
        globalMavenSettingsConfig: settingsName,
        mavenOpts: mavenOpts,
        mavenLocalRepo: localRepo) {
        sh "mvn -f $path $cmdline"
    }
}

// vim: et:ts=2:sw=2:ft=groovy