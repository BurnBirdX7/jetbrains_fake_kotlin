import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

internal class TaskTest {

    companion object {
        const val name1 = "my-task-1"
        // const val name2 = "my-task-2"

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


}