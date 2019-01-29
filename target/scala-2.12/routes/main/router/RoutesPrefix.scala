// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/jdischler/Projects/dss_version2/conf/routes
// @DATE:Sat Dec 29 17:09:25 CST 2018


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
