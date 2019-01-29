
package views.html

import _root_.play.twirl.api.TwirlFeatureImports._
import _root_.play.twirl.api.TwirlHelperImports._
import _root_.play.twirl.api.Html
import _root_.play.twirl.api.JavaScript
import _root_.play.twirl.api.Txt
import _root_.play.twirl.api.Xml
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import java.lang._
import java.util._
import scala.collection.JavaConverters._
import play.core.j.PlayMagicForJava._
import play.mvc._
import play.api.data.Field
import play.mvc.Http.Context.Implicit._
import play.data._
import play.core.j.PlayFormsMagicForJava._

object index extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply():play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*1.1*/("""<!DOCTYPE html>
<html lang="en">
    <head>
        <title>SmartScape 2.0</title>
        <link rel="stylesheet" media="screen" href=""""),_display_(/*5.54*/routes/*5.60*/.Assets.versioned("stylesheets/ol.css")),format.raw/*5.99*/("""">
        <link rel="stylesheet" media="screen" href=""""),_display_(/*6.54*/routes/*6.60*/.Assets.versioned("stylesheets/ext-theme-crisp-all.css")),format.raw/*6.116*/("""">
        <link rel="shortcut icon" type="image/png" href=""""),_display_(/*7.59*/routes/*7.65*/.Assets.versioned("images/favicon.png")),format.raw/*7.104*/("""">
        <script src="/assets/javascripts/vendor/ol-debug.js"></script>
	    """),format.raw/*9.101*/("""
        """),format.raw/*10.9*/("""<script src="/assets/javascripts/vendor/ext-all.js"></script>
		<link rel="stylesheet" href="/assets/stylesheets/charts-all.css">
		<script type="text/javascript" src="/assets/javascripts/vendor/charts.js"></script>
        <link rel="stylesheet" media="screen" href=""""),_display_(/*13.54*/routes/*13.60*/.Assets.versioned("stylesheets/main.css")),format.raw/*13.101*/("""">
        
    </head>
    <body>
    <div id='description'>
    </div>    
        <script src="assets/javascripts/main.js" type="text/javascript"></script>
    </body>
</html>"""))
      }
    }
  }

  def render(): play.twirl.api.HtmlFormat.Appendable = apply()

  def f:(() => play.twirl.api.HtmlFormat.Appendable) = () => apply()

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  DATE: Fri Dec 28 16:34:52 CST 2018
                  SOURCE: /Users/jdischler/Projects/dss_version2/app/views/index.scala.html
                  HASH: f4d78cfe7b0ace7fbf2ca87b5da4b44a3db1d2c3
                  MATRIX: 1030->0|1191->135|1205->141|1264->180|1346->236|1360->242|1437->298|1524->359|1538->365|1598->404|1705->578|1741->587|2037->856|2052->862|2115->903
                  LINES: 33->1|37->5|37->5|37->5|38->6|38->6|38->6|39->7|39->7|39->7|41->9|42->10|45->13|45->13|45->13
                  -- GENERATED --
              */
          