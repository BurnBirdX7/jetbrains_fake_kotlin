import java.nio.file.Path
import java.nio.file.attribute.FileTime
import kotlin.io.path.exists
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.notExists

class Task (val name: String,
            val run: String,
            val dependencies: List<String>? = null
        )
{
    var time: FileTime? = null
    var enqueued: Boolean = false
    var processed: Boolean = false
    var dependencyIsUpdated: Boolean = false

    constructor(name: String, task: YAMLTask) : this (name, task.run, task.dependencies) {
        if (task.target != null) {
            val path = Path.of(task.target)
            if (path.exists()) {
                time = path.getLastModifiedTime()
            }

        }
    }

    enum class Status {
        UP_TO_DATE, NEEDS_UPDATING, ENQUEUED
    }

    val status: Status
        get() {
            if (enqueued)
                return Status.ENQUEUED

            if (!targetExists() || dependencyIsUpdated)
                return Status.NEEDS_UPDATING

            return Status.UP_TO_DATE
        }

    fun targetExists() = time != null
    fun targetTime() = time!!
    fun hasDependencies() = dependencies?.isNotEmpty() == true

    fun evaluateFileDependency(path: Path) : Boolean {
        if (!hasDependencies()) {
            throw RuntimeException("This task doesn't have any dependencies...")
        }

        if (path.notExists()) {
            return false
        }

        if (targetExists() &&
            targetTime() < path.getLastModifiedTime())
        {
            dependencyIsUpdated = true
        }

        return true
    }

    fun evaluateTaskDependency(task: Task) {

    }

    companion object {
        fun fromFile(path: Path) : Map<String, Task>? {
            val res = YAMLTask.fromFile(path) ?: return null
            return res.mapValues { (name, yamlTask) -> Task(name, yamlTask) }
        }
    }

}
