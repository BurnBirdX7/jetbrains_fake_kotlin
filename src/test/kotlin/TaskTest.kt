import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.RuntimeException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.notExists
import kotlin.io.path.toPath

internal class TaskTest {

    companion object {
        const val name1 = "my-task-1"
        const val name2 = "my-task-2"

        fun defaultTask() = Task(name1, "")
    }

    @Test
    fun basicTest() {
        val task = defaultTask()

        assertEquals(task.name, name1)
        assertEquals(task.run, "")
        assertFalse(task.targetExists())
        assertThrows(NullPointerException::class.java) { task.targetTime() }
    }

    @Test
    fun targetExistence() {
        val nonExistentTarget = "target1"
        val existingTarget = "target2"

        val existingPath = Path.of(existingTarget)
        if (existingPath.notExists())
            Files.createFile(existingPath)

        val taskWithNoTarget = Task(name1, YAMLTask("ps", nonExistentTarget))
        assertFalse(taskWithNoTarget.targetExists())
        assertThrows(java.lang.NullPointerException::class.java) { taskWithNoTarget.targetTime() }

        val taskWithTarget = Task(name1, YAMLTask("ls", existingTarget))
        assertTrue(taskWithTarget.targetExists())
        assertDoesNotThrow { taskWithTarget.targetTime() }

        val time = taskWithTarget.targetTime()
        println("Task with existing target file... File time $time")

        assertFalse(taskWithNoTarget < taskWithTarget)
        assertTrue(taskWithTarget < taskWithNoTarget)

        Files.delete(existingPath)
    }

    @Test
    fun taskFromYAML() {
        val pathToIncorrectYAML = this::class.java.getResource("incorrect.yaml")?.toURI()?.toPath()
        assertNotNull(pathToIncorrectYAML)
        assertThrows(IncorrectYAML::class.java) { Task.fromFile(pathToIncorrectYAML!!) }

        val path = this::class.java.getResource("/test1.yaml")?.toURI()?.toPath()
        assertNotNull(path)
        val tasks = Task.fromFile(path!!)

        val tasksCounts = listOf(
            "no_dependencies"     to 0,
            "one_dependency_list" to 1,
            "four_dependencies"   to 4
        )

        tasksCounts.forEach { (task, count) ->
            val deps = tasks[task]?.dependencies

            val depCount = when (deps) {
                null -> 0
                else -> deps.size
            }

            assertEquals(depCount, count)
        }
    }

    @Test
    fun noTargetStatus() {
        val path = this::class.java.getResource("/test1.yaml")?.toURI()?.toPath()
        assertNotNull(path)
        val tasks = Task.fromFile(path!!)
        val noTarget = tasks["no_target"]
        assertNotNull(noTarget)
        assertEquals(noTarget!!.status, Task.Status.NEEDS_UPDATING)
    }

    @Test
    fun noDependenciesStatusNoTarget() {
        Files.deleteIfExists(Path.of("no_dependencies.out"))

        val path = this::class.java.getResource("/test1.yaml")?.toURI()?.toPath()
        assertNotNull(path)
        val tasks = Task.fromFile(path!!)

        val noDep = tasks["no_dependencies"]
        assertNotNull(noDep)
        assertNull(noDep!!.dependencies)
        assertEquals(noDep.status, Task.Status.NEEDS_UPDATING)
    }

    @Test
    fun noDependenciesStatus() {
        val pathToOut = Files.createFile(Path.of("no_dependencies.out"))

        val path = this::class.java.getResource("/test1.yaml")?.toURI()?.toPath()
        assertNotNull(path)
        val tasks = Task.fromFile(path!!)

        val noDep = tasks["no_dependencies"]
        assertNotNull(noDep)
        assertNull(noDep!!.dependencies)
        assertEquals(noDep.status, Task.Status.UP_TO_DATE)

        Files.delete(pathToOut)

    }

    @Test
    fun enqueuedStatus() {
        val task = defaultTask()
        val status = task.status

        task.enqueued = true
        assertEquals(task.status, Task.Status.ENQUEUED)
        assertNotEquals(task.status, status)

        task.enqueued = false
        assertEquals(task.status, status)
    }

    fun depEval(t1: Task, t2: Task) {
        t1.evaluateFileDependency(Path.of("file.txt"))
        t1.evaluateTaskDependency(t2)
    }
    @Test
    fun dependencyEvaluation() {
        val path = this::class.java.getResource("/test1.yaml")?.toURI()?.toPath()
        assertNotNull(path)
        val tasks = Task.fromFile(path!!)


        val task = tasks["no_dependencies"]!!
        val taskD1 = tasks["one_dependency_list"]!!
        val taskD4 = tasks["four_dependencies"]!!

        assertDoesNotThrow { depEval(taskD1, task) }
        assertDoesNotThrow { depEval(taskD4, task) }
        assertThrows(RuntimeException::class.java) { depEval(task, taskD1) }
    }

    @Test
    fun targetTimeStatusAndComparison() {
        val path1 = Path.of("target1")
        val path2 = Path.of("target2")
        Files.deleteIfExists(path1)
        Files.deleteIfExists(path2)

        Files.createFile(path1)
        Thread.sleep(500)
        Files.createFile(path2)

        val task1 = Task(name1, YAMLTask("", "target1"))
        val task2 = Task(name2, YAMLTask("", "target2"))

        assertNotEquals(task1.status, Task.Status.NEEDS_UPDATING)
        assertNotEquals(task2.status, Task.Status.NEEDS_UPDATING)

        assertTrue(task1 < task2)
        assertFalse(task1 > task2)

        Files.delete(path1)
        Files.delete(path2)
    }

}