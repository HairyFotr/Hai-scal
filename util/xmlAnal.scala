object xmlAnal extends App {
    (scala.xml.XML.loadFile("lexanal.xml") \\ "terminal").foldLeft("")((line,tag) => {
        val currline = tag.attribute("line").get(0).toString
        if(line!=currline) {
            println()
            print(" "*tag.attribute("column").get(0).toString.toInt)
        }
        print(tag.attribute("token").get + " ")
        currline
    })
    println()  
}
