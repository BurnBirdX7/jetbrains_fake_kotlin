
import java.nio.file.Path
import kotlin.system.exitProcess


fun main(args: Array<String>) {

    if (args.isEmpty()) {
        println("Not enough arguments!")
        exitProcess(1)
    }

    val res = Task.fromFile(Path.of("fake.yaml"))

    if (res != null) {
        val eqb = ExecutionQueueBuilder(res)

        args.forEach { eqb.build(it) }

        if (eqb.failed) {
            exitProcess(2)
        }

        exitProcess(eqb.execute())
    }
}