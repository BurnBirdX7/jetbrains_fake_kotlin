
import java.nio.file.Path
import kotlin.system.exitProcess


fun main(args: Array<String>) {

    if (args.isEmpty()) {
        println("Not enough arguments!")
        exitProcess(-1)
    }

    val res = Task.fromFile(Path.of("fake.yaml"))

    if (res != null) {
        val eqb = ExecutionQueueBuilder(res)

        args.forEach { eqb.build(it) }

        if (eqb.failed) {
            System.err.println("EQB failed... List of errors:")
            eqb.errors.forEach { System.err.println("\t - $it") }

            if (eqb.dependencyStack.isNotEmpty()) {
                System.err.println("Dependency stack")
                eqb.errors.forEach { System.err.println("\t - $it") }
            }
            exitProcess(-2)
        }

        println("Queue:")
        eqb.executionQueue.forEach { task ->
            println(" > [${task.name}]: ${task.run}")
        }
    }
}