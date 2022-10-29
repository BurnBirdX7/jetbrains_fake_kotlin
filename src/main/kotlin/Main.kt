
import java.nio.file.Path
import kotlin.io.path.notExists
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Not enough arguments!")
        println("fake <task> [tasks...]")
        exitProcess(1)
    }

    val path = Path.of("fake.yaml")
    if (path.notExists()) {
        println("There's no fake.yaml in this directory")
        exitProcess(1)
    }

    try {
        val res = Task.fromFile(path)
        val eqb = ExecutionQueueBuilder(res)

        args.forEach { eqb.build(it) }

        if (eqb.failed) {
            println(eqb)
            exitProcess(2)
        }
        exitProcess(eqb.execute())
    }
    catch (e : IncorrectYAML) {
        System.err.println("Error occurred when parsing YAML")
        System.err.println(e.message)
    }
}
