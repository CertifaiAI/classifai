pipeline {
  agent any
  stages {
    stage('Build') {
      parallel {
        stage('Build') {
          steps {
            sh './startClassifai.sh'
          }
        }

        stage('') {
          steps {
            powershell(script: 'startClassifai.bat', returnStatus: true, returnStdout: true)
          }
        }

      }
    }

  }
}