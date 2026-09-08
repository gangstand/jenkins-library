def call(Map config) {
    withCredentials([
        usernamePassword(
            credentialsId: config.credentialsId,
            usernameVariable: 'HARBOR_USERNAME',
            passwordVariable: 'HARBOR_PASSWORD'
        )
    ]) {
        sh """
            set +x

            export DOCKER_CONFIG="\$WORKSPACE/.docker-auth"
            mkdir -p "\$DOCKER_CONFIG"

            echo "\$HARBOR_PASSWORD" | \
                docker login '${config.registry}' \
                --username "\$HARBOR_USERNAME" \
                --password-stdin

            docker push '${config.image}'

            docker tag \
                '${config.image}' \
                '${config.registry}/${config.name}:${config.channel}'

            docker push \
                '${config.registry}/${config.name}:${config.channel}'

            docker logout '${config.registry}' || true

            rm -rf "\$DOCKER_CONFIG"
        """
    }
}
