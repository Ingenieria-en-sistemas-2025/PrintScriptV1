import org.example.Output

//resultado de ejecutar un programa (outputs finales + final env)
data class RunResult(val outputs: List<String>, val finalEnv: Env)
