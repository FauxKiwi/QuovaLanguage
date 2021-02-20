package quova.internal

import java.io.File

val IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows")

fun system(commandWin: String, commandUnix: String? = null, workingDirectory: String? = null, redirectInput: Boolean = true, redirectOutput: Boolean = true, redirectError: Boolean = true): Int {
    val processBuilder = ProcessBuilder()

    if (IS_WINDOWS)
        processBuilder.command("cmd.exe", "/c", commandWin)
    else
        processBuilder.command("sh", "-c", commandUnix ?: commandWin)

    workingDirectory?.let {
        processBuilder.directory(File(workingDirectory))
    }

    if (redirectError) processBuilder.redirectErrorStream(true)
    if (redirectOutput) processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
    if (redirectInput) processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT)

    val process = processBuilder.start()

    process.waitFor()

    return process.exitValue()
}