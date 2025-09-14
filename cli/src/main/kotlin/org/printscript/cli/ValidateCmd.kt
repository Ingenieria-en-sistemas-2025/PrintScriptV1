// package org.printscript.cli
//
// import com.github.ajalt.clikt.core.CliktCommand
// import com.github.ajalt.clikt.parameters.groups.provideDelegate
// import org.printscript.common.Version
// import org.printscript.runner.ProgramIo
// import org.printscript.runner.runners.ValidateRunner
//
// class ValidateCmd : CliktCommand(name = "validate", help = "Valida sintaxis y semántica") {
//    private val common by CommonOptions()
//
//    override fun run() {
//        val version: Version = CliSupport.resolveVersion(common.version)
//        val source = CliSupport.readSourceOrFail(common.file)
//
//        val spinner = ProgressSpinner("Parsing")
//        spinner.start()
//
//        val res = ValidateRunner.run(version, ProgramIo(source = source))
//    }
// }
