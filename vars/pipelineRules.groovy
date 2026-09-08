def isDevelopment() {
    return env.BRANCH_NAME == 'dev'
}

def isProduction() {
    return env.BRANCH_NAME == 'main'
}

def isReleaseTag() {
    return env.TAG_NAME && (env.TAG_NAME ==~ /^\d+\.\d+\.\d+$/)
}

def shouldBuild() {
    return isDevelopment() || isProduction() || isReleaseTag()
}

return this
