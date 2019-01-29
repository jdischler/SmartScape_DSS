
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

object nav extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply():play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*1.1*/("""<!DOCTYPE html>
<html lang="en">
    <head>
        <title>SmartScape 2.0</title>
        <link rel="stylesheet" media="screen" href=""""),_display_(/*5.54*/routes/*5.60*/.Assets.versioned("stylesheets/main.css")),format.raw/*5.101*/("""">
        <link rel="stylesheet" media="screen" href=""""),_display_(/*6.54*/routes/*6.60*/.Assets.versioned("stylesheets/ol.css")),format.raw/*6.99*/("""">
        <link rel="shortcut icon" type="image/png" href=""""),_display_(/*7.59*/routes/*7.65*/.Assets.versioned("images/favicon.png")),format.raw/*7.104*/("""">
        <script src="/assets/javascripts/vendor/ol-debug.js"></script>
        
	<link rel="stylesheet" href="/assets/stylesheets/theme-neptune-all.css">
	<script type="text/javascript" src="/assets/javascripts/vendor/ext-all.js"></script>
	<script type="text/javascript" src="/assets/stylesheets/theme-neptune.js"></script>
    <link rel="stylesheet" href="/assets/stylesheets/charts-all.css">
    <script type="text/javascript" src="/assets/javascripts/vendor/charts.js"></script>
    
    </head>
    <body>
        <script src="assets/javascripts/nav.js" type="text/javascript"></script>
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
                  DATE: Thu Dec 20 16:41:30 CST 2018
                  SOURCE: /Users/jdischler/Projects/dss_version2/app/views/nav.scala.html
                  HASH: abdf7c893ef0d0231d5a09648c4b67a98ffeac12
                  MATRIX: 1028->0|1189->135|1203->141|1265->182|1347->238|1361->244|1420->283|1507->344|1521->350|1581->389
                  LINES: 33->1|37->5|37->5|37->5|38->6|38->6|38->6|39->7|39->7|39->7
                  -- GENERATED --
              */
          