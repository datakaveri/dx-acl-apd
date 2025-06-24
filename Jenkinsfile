pipeline {

  environment {
    devRegistry = 'ghcr.io/datakaveri/acl-apd-dev'
    deplRegistry = 'ghcr.io/datakaveri/acl-apd-depl'
    testRegistry = 'ghcr.io/datakaveri/acl-apd-test:latest'
    registryUri = 'https://ghcr.io'
    registryCredential = 'datakaveri-ghcr'
    GIT_HASH = GIT_COMMIT.take(7)
  }

  agent { 
    node {
      label 'slave1' 
    }
  }

  stages {

    stage('Trivy Code Scan (Dependencies)') {
      steps {
        script {
          sh '''
            trivy fs --scanners vuln,secret,misconfig --output trivy-fs-report.txt .
          '''
        }
      }
    }

    stage('Building images') {
      steps{
        script {
          echo 'Pulled - ' + env.GIT_BRANCH
          devImage = docker.build( devRegistry, "-f ./docker/dev.dockerfile .")
          deplImage = docker.build( deplRegistry, "-f ./docker/depl.dockerfile .")
          testImage = docker.build( testRegistry, "-f ./docker/test.dockerfile .")
        }
      }
    }

    stage('Trivy Docker Image Scan') {
      steps {
        script {
          sh "trivy image --output trivy-dev-image-report.txt ${devImage.imageName()}"
          sh "trivy image --output trivy-depl-image-report.txt ${deplImage.imageName()}"

        }
      }
    }
    stage('Archive Trivy Reports') {
      steps {
        archiveArtifacts artifacts: 'trivy-*.txt', allowEmptyArchive: true
        publishHTML(target: [
          allowMissing: true,
          keepAll: true,
          reportDir: '.',
          reportFiles: 'trivy-fs-report.txt, trivy-dev-image-report.txt, trivy-depl-image-report.txt',
          reportName: 'Trivy Reports'
        ])
      }
    }

    stage('Unit Tests and CodeCoverage Test'){
      steps{
        script{
          sh 'docker compose -f docker-compose.test.yml up test'
        }
        xunit (
          thresholds: [ skipped(failureThreshold: '0'), failed(failureThreshold: '0') ],
          tools: [ JUnit(pattern: 'target/surefire-reports/*.xml') ]
        )
        jacoco classPattern: 'target/classes', execPattern: 'target/jacoco.exec', sourcePattern: 'src/main/java', exclusionPattern: '**/*VertxEBProxy.class, **/*VertxProxyHandler.class, **/*Verticle.class, **/*Service.class, iudx/apd/acl/server/deploy/*, **/*Constants.class'
      }
      post{
        always {
          recordIssues(
            enabledForFailure: true,
            skipBlames: true,
            qualityGates: [[threshold:0, type: 'TOTAL', unstable: false]],
            tool: checkStyle(pattern: 'target/checkstyle-result.xml')
          )
          recordIssues(
            enabledForFailure: true,
            skipBlames: true,
            qualityGates: [[threshold:0, type: 'TOTAL', unstable: false]],
            tool: pmdParser(pattern: 'target/pmd.xml')
          )
        }
        failure{
          script{
            sh 'docker compose -f docker-compose.test.yml down --remove-orphans'
          }
          error "Test failure. Stopping pipeline execution!"
        }
        cleanup{
          script{
            sh 'sudo rm -rf target/'
          }
        }
      }
    }

    stage('Start acl-apd-Server for Integration Testing'){
      steps{
        script{
          sh 'scp src/test/resources/DX-ACL-APD-APIs.postman_collection.json jenkins@jenkins-master:/var/lib/jenkins/iudx/acl-apd/Newman/'
          sh 'mvn flyway:migrate -Dflyway.configFiles=/home/ubuntu/configs/5.6.0/acl-apd-flyway.conf'
          sh 'docker compose -f docker-compose.test.yml up -d integTest'
          sh 'sleep 45'
        }
      }
      post{
        failure{
          script{
            sh 'mvn flyway:clean -Dflyway.configFiles=/home/ubuntu/configs/5.6.0/acl-apd-flyway.conf'
            sh 'docker compose -f docker-compose.test.yml down --remove-orphans'
          }
          cleanWs deleteDirs: true, disableDeferredWipeout: true
        }
      }
    }

    stage('Integration Tests and OWASP ZAP pen test'){
      steps{
        node('built-in') {
          script{
            startZap ([host: 'localhost', port: 8090, zapHome: '/var/lib/jenkins/tools/com.cloudbees.jenkins.plugins.customtools.CustomTool/OWASP_ZAP/ZAP_2.11.0'])
            sh 'curl http://127.0.0.1:8090/JSON/pscan/action/disableScanners/?ids=10096'
            sh 'HTTP_PROXY=\'127.0.0.1:8090\' newman run /var/lib/jenkins/iudx/acl-apd/Newman/DX-ACL-APD-APIs.postman_collection.json -e /home/ubuntu/configs/5.6.0/acl-apd-postman-env.json --insecure -r htmlextra --reporter-htmlextra-export /var/lib/jenkins/iudx/acl-apd/Newman/report/report.html --reporter-htmlextra-skipSensitiveData'
            runZapAttack()
          }
        }
      }
      post{
        always{
          node('built-in') {
            script{
              publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: '/var/lib/jenkins/iudx/acl-apd/Newman/report/', reportFiles: 'report.html', reportTitles: '', reportName: 'Integration Test Report'])
              archiveZap failHighAlerts: 1, failMediumAlerts: 1, failLowAlerts: 1
            }
          }
        }
        failure{
          error "Test failure. Stopping pipeline execution!"
        }
        cleanup{
          script{
            sh 'mvn flyway:clean -Dflyway.configFiles=/home/ubuntu/configs/5.6.0/acl-apd-flyway.conf'
            sh 'docker compose -f docker-compose.test.yml down --remove-orphans'
          } 
        }
      }
    }
    
    stage('Push Images') {
      when {
        allOf {
          anyOf {
            changeset "docker/**"
            changeset "docs/**"
            changeset "pom.xml"
            changeset "src/main/**"
            triggeredBy cause: 'UserIdCause'
          }
          expression {
            return env.GIT_BRANCH == 'origin/1.1.0';
          }
        }
      }
      steps {
        script {
          docker.withRegistry( registryUri, registryCredential ) {
            devImage.push("1.1.0-${env.GIT_HASH}")
            deplImage.push("1.1.0-${env.GIT_HASH}")
          }
        }
      }
    }

  }
  post{
    failure{
      script{
        if (env.GIT_BRANCH == 'origin/1.1.0')
        emailext recipientProviders: [buildUser(), developers()], to: '$AAA_RECIPIENTS, $DEFAULT_RECIPIENTS', subject: '$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS!', body: '''$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS:
Check console output at $BUILD_URL to view the results.'''
      }
    }
  }

}