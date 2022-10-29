import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.io.path.toPath

class ExecutionQueueBuilderTest {

    companion object {
        val pathConverter = { it: String -> this::class.java.getResource(it)!!.toURI().toPath() }
    }

    @Test
    fun basicBuild() {
        val tasks = Task.fromFile(this::class.java.getResource("test2.yaml")!!.toURI().toPath())
        val builder = ExecutionQueueBuilder(tasks)
        builder.pathConverter = pathConverter

        builder.build("exec")
        assertFalse(builder.failed, builder.toString())
        assertEquals(builder.executionQueue.size, 3)
    }

    @Test
    fun buildMultipleExecToCompile() {
        val tasks = Task.fromFile(this::class.java.getResource("test2.yaml")!!.toURI().toPath())
        val builder = ExecutionQueueBuilder(tasks)
        builder.pathConverter = pathConverter

        builder.build("exec")
        assertEquals(3, builder.executionQueue.size)
        builder.build("compile")
        assertEquals(3, builder.executionQueue.size)
    }

    @Test
    fun builderTasksCompileToExec() {
        val tasks = Task.fromFile(this::class.java.getResource("test2.yaml")!!.toURI().toPath())
        val builder = ExecutionQueueBuilder(tasks)
        builder.pathConverter = pathConverter

        println(tasks)

        builder.build("compile")
        assertEquals(1, builder.executionQueue.size)
        builder.build("exec")
        assertEquals(3, builder.executionQueue.size)
    }

    @Test
    fun buildDifferentBranches() {
        val tasks = Task.fromFile(this::class.java.getResource("test2.yaml")!!.toURI().toPath())
        val builder = ExecutionQueueBuilder(tasks)
        builder.pathConverter = pathConverter
        val queue = builder.executionQueue

        builder.build("leaf2")
        assertFalse(builder.failed)

        val n = queue.size
        builder.build("publish")
        assertFalse(builder.failed)
        assertEquals(queue.size, n)

        val leaf_n = queue.indexOfFirst { it.name == "leaf2" }
        val publ_n = queue.indexOfFirst { it.name == "publish" }
        assertNotEquals(-1, leaf_n)
        assertNotEquals(-1, publ_n)
        assertTrue(publ_n < leaf_n)

        builder.build("leaf1")
        assertFalse(builder.failed)
        assertEquals(n + 1, queue.size)

        builder.build("exec")
        assertEquals(queue.size, n + 4)
    }

    @Test
    fun executeOkTask() {
        val tasks = Task.fromFile(this::class.java.getResource("test2.yaml")!!.toURI().toPath())
        val builder = ExecutionQueueBuilder(tasks)
        builder.pathConverter = pathConverter

        builder.build("exec")
        assertEquals(3, builder.executionQueue.size)

        val ret = builder.execute()
        assertEquals(0, ret)
    }

    @Test
    fun executeFailingTask () {
        val tasks = Task.fromFile(this::class.java.getResource("test2.yaml")!!.toURI().toPath())
        val builder = ExecutionQueueBuilder(tasks)
        builder.pathConverter = pathConverter

        builder.build("failing_exec")
        assertEquals(3, builder.executionQueue.size)

        val ret = builder.execute()
        assertNotEquals(0, ret)
    }

    @Test
    fun buildFromLongFile() {
        val FILE = "super_long.yaml"
        val TASKS_GROUPS = 50
        val TASKS = TASKS_GROUPS * 3 + 1

        /*
         * In jetbrains-fake-cpp project long file is generated when test is launched
         * I don't want to rewrite file-generation, so I just took generated file from there
         */

        val tasks = Task.fromFile(this::class.java.getResource(FILE)!!.toURI().toPath())
        val builder = ExecutionQueueBuilder(tasks)
        builder.pathConverter = pathConverter

        builder.build("task_" + (TASKS_GROUPS / 2).toString())
        builder.build("task_$TASKS_GROUPS")

        assertFalse(builder.failed)
        assertEquals(builder.executionQueue.size, TASKS)
    }

}