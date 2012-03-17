object lexanal extends App {
    //TODO if error, xml is invalid
    var xml = scala.xml.XML.loadFile("lexanal.xml")
    var line = 0
    
    (xml \\ "terminal").foreach(tag=> {
        val currline = tag.attribute("line").get(0).toString.toInt
        val currcolumn = tag.attribute("column").get(0).toString.toInt
        if(line!=currline) {
            line = currline
            println()
            print(" "*currcolumn)
        }
        print(tag.attribute("token").get + " ")
    })
    println()    
}
