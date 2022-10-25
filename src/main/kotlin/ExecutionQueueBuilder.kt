import java.nio.file.Path

class ExecutionQueueBuilder(val tasks: Map<String, Task>) {

    val processingQueue = ArrayDeque<Pair<String /* name */, Task /* parent/dependent */>>()
    val dependencyStack = ArrayDeque<Task>()

    val executionQueue = mutableListOf<Task>()
    val errors = mutableListOf<String>()

    val failed: Boolean
        get() = errors.isNotEmpty()

    fun build(taskName: String) {
        val rootTask = tasks[taskName]
        val rootDeps = rootTask?.dependencies

        if (rootTask == null) {
            errors.add("There's no task with name \"$taskName\"")
            return
        }

        if (rootDeps != null) {
            processingQueue.addAll(rootDeps.map { it to rootTask })
        }

        rootTask.processed = true
        dependencyStack.addFirst(rootTask)

        while (processingQueue.isNotEmpty()) {
            val (name, parent) = processingQueue.first()
            processingQueue.removeFirst()

            unfoldStack(parent)
            if (stackContains(name)) {
                addError(name, "Self-dependency")
                return
            }

            if (!addTask(name)) {
                if (!parent.evaluateFileDependency(Path.of(name))) {
                    addError(name, "Is not a task or a file")
                }
            }
        }

        unfoldStack()
    }

    private fun stackContains(name: String) : Boolean {
        return dependencyStack.find{ it.name == name } != null
    }

    private fun unfoldStack(upTo: Task? = null) {

        while(dependencyStack.isNotEmpty() && dependencyStack.first() !== upTo) {
            val current = dependencyStack.first()

            if (current.status == Task.Status.NEEDS_UPDATING) {
                executionQueue.add(current)
                current.enqueued = true
            }

            dependencyStack.removeFirst()
            if (dependencyStack.isNotEmpty())
                dependencyStack.first().evaluateTaskDependency(current)
        }

    }

    private fun addTask(taskName: String) : Boolean {
        val task = tasks[taskName] ?: return false
        dependencyStack.addFirst(task)

        if (task.processed)
            return true

        task.processed = true

        if (task.dependencies != null)
            processingQueue.addAll(0, task.dependencies.map{ it to task })

        return true
    }

    private fun addError(name: String, msg: String) {
        errors.add("\"$name\": $msg")
    }


}