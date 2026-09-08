def call(Map config) {
    withCredentials([
        usernamePassword(
            credentialsId: config.credentialsId,
            usernameVariable: 'HARBOR_USER',
            passwordVariable: 'HARBOR_PASSWORD'
        )
    ]) {
        sh """
            set +x

            echo "\$HARBOR_PASSWORD" | docker login '${config.registry}' \
                --username "\$HARBOR_USER" \
                --password-stdin

            docker push '${config.image}'

            docker tag \
                '${config.image}' \
                '${config.registry}/${config.name}:${config.channel}'

            docker push \
                '${config.registry}/${config.name}:${config.channel}'

            docker logout '${config.registry}'
        """
    }
}
