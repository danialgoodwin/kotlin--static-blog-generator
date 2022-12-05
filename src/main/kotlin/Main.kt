fun main(args: Array<String>) {
    println("Program arguments: ${args.joinToString()}")
    StaticBlogGenerator().generate()
    println("Status: Done")
}
