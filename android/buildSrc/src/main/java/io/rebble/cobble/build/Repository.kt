package io.rebble.cobble.build

object Repository {
    fun getShortCommitHash(): String {
        val process = Runtime.getRuntime().exec("git rev-parse --short HEAD")
        process.waitFor()
        return process.inputStream.bufferedReader().readText().trim()
    }
    fun getBranchName(): String {
        val process = Runtime.getRuntime().exec("git rev-parse --abbrev-ref HEAD")
        process.waitFor()
        return process.inputStream.bufferedReader().readText().trim()
    }
}