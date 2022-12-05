import java.io.File

class StaticBlogGenerator {

    private val BLOG_TITLE = "My Minimal Blog"
    private val BLOG_DESCRIPTION = "Saving bytes, one blog at a time."

    private val POSTS_INPUT_DIRECTORY = "__posts"

    private val BLOG_OUTPUT_DIRECTORY = "out/public/"
    private val POSTS_OUTPUT_DIRECTORY = "out/public/"

    fun generate() {
        File(BLOG_OUTPUT_DIRECTORY).deleteRecursively()
        File(POSTS_OUTPUT_DIRECTORY).mkdirs()
        generateHomePage()
        generatePostPages()
    }

    private fun generateHomePage(template: String = "__templates/home.html") {

    }

    private fun generatePostPages(template: String = "__templates/post.html") {

    }

}
