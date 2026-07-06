pipeline {
    agent any

    tools {
        nodejs 'nodejs-20'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        BACKEND_IMAGE = 'sophatel-backend:local'
        FRONTEND_IMAGE = 'sophatel-front-end:local'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backend tests') {
            steps {
                dir('backend') {
                    sh 'chmod +x mvnw'
                    sh 'java -version'
                    sh './mvnw -B clean test'
                }
            }
        }

        stage('Frontend build') {
            steps {
                dir('front-end') {
                    sh 'node --version'
                    sh 'npm --version'
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
        }

        stage('Docker build') {
            steps {
                sh 'docker --version'
                sh 'docker build -t "$BACKEND_IMAGE" backend'
                sh 'docker build -t "$FRONTEND_IMAGE" front-end'
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'backend/target/surefire-reports/*.xml'
        }

        success {
            echo 'CI pipeline completed successfully.'
        }

        failure {
            echo 'CI pipeline failed. Check the failed stage logs above.'
        }
    }
}
