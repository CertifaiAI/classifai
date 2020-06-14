pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        bat(script: 'startClassifai.sh', returnStdout: true)
        bat(script: 'startClassifai.bat', returnStdout: true, returnStatus: true)
      }
    }

  }
}