def branch = env.BRANCH_NAME
echo "🔀 Dispatcher: Current branch is '${branch}'"

node {
    checkout scm  // 강제 checkout

    sh 'ls -al'
    sh 'ls -al onprem || echo "onprem 디렉토리 없음"'

    if (branch == 'main') {
        load 'main/Jenkinsfile'
    } else if (branch == 'onprem') {
        load 'onprem/Jenkinsfile'
    } else {
        error "❌ No Jenkinsfile defined for branch: ${branch}"
    }
}
