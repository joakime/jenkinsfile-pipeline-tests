#!groovy

pipeline {
    agent any
    stages {
        stage("Parallel Stage") {
            parallel {
                stage("Build / Test - JDK8") {
                    agent { node { label 'linux' } }
                    options { timeout(time: 120, unit: 'MINUTES') }
                    steps {
                        mavenBuild("jdk8", "install -Djetty.testtracker.log=true -Pmongodb")
                        junit '**/target/surefire-reports/TEST-*.xml,**/target/failsafe-reports/TEST-*.xml'
                        // Collect up the jacoco execution results (only on main build)
                        jacoco inclusionPattern: '**/org/eclipse/jetty/**/*.class',
                            exclusionPattern: '' +
                                // build tools
                                '**/org/eclipse/jetty/ant/**' +
                                ',**/org/eclipse/jetty/maven/**' +
                                ',**/org/eclipse/jetty/jspc/**' +
                                // example code / documentation
                                ',**/org/eclipse/jetty/embedded/**' +
                                ',**/org/eclipse/jetty/asyncrest/**' +
                                ',**/org/eclipse/jetty/demo/**' +
                                // special environments / late integrations
                                ',**/org/eclipse/jetty/gcloud/**' +
                                ',**/org/eclipse/jetty/infinispan/**' +
                                ',**/org/eclipse/jetty/osgi/**' +
                                ',**/org/eclipse/jetty/spring/**' +
                                ',**/org/eclipse/jetty/http/spi/**' +
                                // test classes
                                ',**/org/eclipse/jetty/tests/**' +
                                ',**/org/eclipse/jetty/test/**',
                            execPattern: '**/target/jacoco.exec',
                            classPattern: '**/target/classes',
                            sourcePattern: '**/src/main/java'
                        warnings consoleParsers: [[parserName: 'Maven'], [parserName: 'Java']]

                        script {
                            step([$class         : 'MavenInvokerRecorder', reportsFilenamePattern: "**/target/invoker-reports/BUILD*.xml",
                                  invokerBuildDir: "**/target/its"])
                        }
                    }
                }

                stage("Build / Test - JDK11") {
                    agent { node { label 'linux' } }
                    options { timeout(time: 120, unit: 'MINUTES') }
                    steps {
                        mavenBuild("jdk11", "install -Djetty.testtracker.log=true -Pmongodb")
                        junit '**/target/surefire-reports/TEST-*.xml,**/target/failsafe-reports/TEST-*.xml'
                        warnings consoleParsers: [[parserName: 'Maven'], [parserName: 'Java']]
                    }
                }

                stage("Build Javadoc") {
                    agent { node { label 'linux' } }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        mavenBuild("jdk8", "javadoc:javadoc")
                        warnings consoleParsers: [[parserName: 'Maven'], [parserName: 'JavaDoc'], [parserName: 'Java']]
                    }
                }

                stage("Build Compact3") {
                    agent { node { label 'linux' } }
                    options { timeout(time: 120, unit: 'MINUTES') }
                    steps {
                        mavenBuild("jdk8", "-Pcompact3 package")
                        warnings consoleParsers: [[parserName: 'Maven'], [parserName: 'Java']]
                    }
                }
            }
        }
    }
}


def mavenBuild(jdk, cmdline) {
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
        // Some common Maven command line + provided command line
        sh "mvn -V -B -T6 -e -Dmaven.test.failure.ignore=true $cmdline -Dunix.socket.tmp=" + env.JENKINS_HOME
    }
}

// vim: et:ts=2:sw=2:ft=groovy