def call(Map config) {
    String image = "${config.registry}/${config.name}:${config.tag}"

    echo "Building ${image}"

    sh """
        docker build \
            --pull \
            -t '${image}' \
            -f '${config.context}/${config.dockerfile}' \
            '${config.context}'
    """

    return image
}
