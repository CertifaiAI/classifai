pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        bat(script: 'startClassifai.bat', returnStdout: true, returnStatus: true)
        sh './startClassifai.sh'
      }
    }

  }
}