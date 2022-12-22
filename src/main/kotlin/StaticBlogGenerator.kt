import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import java.io.File
import java.time.LocalDate
import java.util.Objects

class StaticBlogGenerator {

    private val BLOG_TITLE = "My Minimal Blog"
    private val BLOG_DESCRIPTION = "Saving bytes, one blog at a time."

    private val ASSETS_INPUT_DIRECTORY = "src/main/resources/assets"
    private val POSTS_INPUT_DIRECTORY = "src/main/resources/__posts"

    private val OUTPUT_DIRECTORY = "out"
    private val ASSETS_OUTPUT_DIRECTORY = "$OUTPUT_DIRECTORY/public/assets"
    private val HOME_OUTPUT_DIRECTORY = "$OUTPUT_DIRECTORY/public"
    private val POSTS_OUTPUT_DIRECTORY = "$OUTPUT_DIRECTORY/public"

    fun generate() {
        File(OUTPUT_DIRECTORY).deleteRecursively()
        File(ASSETS_OUTPUT_DIRECTORY).mkdirs()
        File(HOME_OUTPUT_DIRECTORY).mkdirs()
        File(POSTS_OUTPUT_DIRECTORY).mkdirs()
        File(ASSETS_INPUT_DIRECTORY).copyRecursively(File(ASSETS_OUTPUT_DIRECTORY))
        generateHomePage()
        log("Status: Done")
        log("See your blog locally by opening ${HOME_OUTPUT_DIRECTORY}/index.html")
    }

    private fun generateHomePage(template: String = "src/main/resources/__templates/home.html") {
        log("Generating blog home page...")
        val postsListHtml = StringBuilder("<ul class=\"posts\">")
        val files = File(POSTS_INPUT_DIRECTORY).listFiles()
        files.reverse()  // Put more recent posts at the top
        val blogPostPageTemplate = File("src/main/resources/__templates/post.html").readLines().joinToString("\n")
        for (file in files) {
            val metadata = parseBlogPostFile(file)
            if (isDateInFuture(metadata["date"])) continue // Don't link to future blog posts
            generatePostPage(metadata, blogPostPageTemplate)
            postsListHtml.append("<li><a href=\"${metadata["slug"]}\">${metadata["title"]}</a></li>")
        }
        postsListHtml.append("</ul>")
        val homePageHtml = File(template).readLines().joinToString("\n")
            .replace("{title}", BLOG_TITLE)
            .replace("{description}", BLOG_DESCRIPTION)
            .replace("{posts}", postsListHtml.toString())
        File("$HOME_OUTPUT_DIRECTORY/index.html").writeText(homePageHtml)
    }

    private fun generatePostPage(metadata: Map<String, String>, pageTemplate: String) {
        log("Generating blog post page ${metadata["slug"]}...")
        val blogPostDirectory = File(POSTS_OUTPUT_DIRECTORY + "/" + metadata["slug"])
        blogPostDirectory.mkdirs()
        val pageHtml = pageTemplate
            .replace("{description}", BLOG_DESCRIPTION)
            .replace("{blog_title}", BLOG_TITLE)
            .replace("{content}", metadata["content"].orEmpty())
            .replace("{date}", metadata["{date}"].orEmpty())
            .replace("{title}", metadata.getOrDefault("title", "Untitled"))
        File("$blogPostDirectory/index.html").writeText(pageHtml)
    }

    /**
     * The top-portion before '---' is considered metadata.
     */
    private fun parseBlogPostFile(file: File) : Map<String, String> {
        val values = HashMap<String, String>()
        values["date"] = file.name.substring(0, 10)
        LocalDate.parse(values["date"]) // Check if valid date (year-month-day)
        var isMetadata = true
        val content = StringBuilder()
        file.forEachLine { line ->
            if (line == "---") { isMetadata = false; return@forEachLine }
            if (isMetadata) {
                if (line.startsWith('#')) return@forEachLine  // Ignore comments
                val keyValue = line.split(": ", limit = 2)
                if (keyValue.size == 2) {
                    values[keyValue[0]] = keyValue[1]
                } else {
                    throw Exception("This syntax is not supported yet: $line")
                }
            } else {
                content.append(line).append("\n")
            }
        }
        val markdown = content.toString()
        val flavour = CommonMarkFlavourDescriptor()
        val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(markdown)
        values["content"] = HtmlGenerator(markdown, parsedTree, flavour).generateHtml()
        if (!values.contains("slug")) {
            // Remove '.md' and remove spaces
            values["slug"] = file.name.substring(0, file.name.length - 3).replace(' ', '-')
        }
        return values
    }

    private fun isDateInFuture(yearMonthDay: String?) : Boolean {
        Objects.requireNonNull(yearMonthDay)
        val date = LocalDate.parse(yearMonthDay)
        val dateNow = LocalDate.now()
        return date.isAfter(dateNow)
    }

    private fun log(message: String) {
        println(message)
    }

}
