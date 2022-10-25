import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.dataformat.yaml.*
import com.fasterxml.jackson.module.kotlin.*
import java.nio.file.Files
import java.nio.file.Path

data class YAMLTask (val run: String,
                     val target: String? = null,
                     val dependencies: List<String>? = null)
{

    companion object {
        fun fromFile(path: Path) : Map<String, YAMLTask>? {
            val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

            var res: HashMap<String, YAMLTask>? = null
            try {
                Files.newBufferedReader(path).use {
                    res = mapper.readValue(it)
                }
            }
            catch (e: MismatchedInputException) {
                val position = "line ${e.location.lineNr},  column ${e.location.columnNr}"

                val sb = StringBuilder()
                val it = e.path.iterator()
                while (it.hasNext()) {
                    sb.append(it.next().fieldName)
                    if (it.hasNext()) {
                        sb.append(" -> ")
                    }
                }

                System.err.println("Error occurred while parsing configuration file")
                System.err.println("Occurred on: $position")
                System.err.println("When processing: $sb")
            }

            return res
        }
    }
}