
import java.nio.file.Path
import kotlin.io.path.notExists
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Not enough arguments!")
        exitProcess(1)
    }

    val path = Path.of("fake.yaml")
    if (path.notExists()) {
        println("There's no fake.yaml in this directory")
        exitProcess(1)
    }

    val res = Task.fromFile(path)

    if (res != null) {
        val eqb = ExecutionQueueBuilder(res)

        args.forEach { eqb.build(it) }

        if (eqb.failed) {
            exitProcess(2)
        }

        exitProcess(eqb.execute())
    }
}
