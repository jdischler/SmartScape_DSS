
const data_d = [{
	text: 'Select',
	active: true,
	activeText: 'Select Area of Interest',
},{
	text: 'Refine',
	activeText: 'Refine Area of Interest (optional)',
},{
	text: 'Review',
	activeText: 'Review Assumptions (optional)',
},{
	text: 'Start',
	activeText: 'Start SmartScape',
}];

//-----------------------------------------------------
// DSS.components.d3_sankey
//
//-----------------------------------------------------
Ext.define('DSS.app_portal.d3_nav', {
    extend: 'Ext.Component',
    alias: 'widget.d3_nav',

	id: 'd3-nav',
   
	listeners: {
		afterrender: function(self) {
			self.doD3();
		},
		resize: function(self) {
			if (self.DSS_svg) {
				self.doResized();
			}
		}
	},
	
	DSS_timer: false,
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
		});
		
		me.callParent(arguments);
	},
	
	//--------------------------------------------------------------------------
	doD3: function() {
		var me = this;
		me['DSS_svg'] = d3.select("#d3-nav")
			.append("svg")
				.attr("width", me.getWidth())
				.attr("height",me.getHeight());
//				.attr("class", "sankey-chart");
		
		var width = me.calculateTextWidths();
		var w = me.getWidth(), 
		h = me.getHeight(); 
		var cx = (w - width) / 2;
		const container_pad = 5;
		const pad = 20;
		
		me.DSS_svg//.selectAll('.d3-bg')
			//.data([{}])
			//.enter()
			.append("g")
		//	.append("path")
			.append("rect")//, me.roundedPointRect(width,h-1,18, true, true))
			.attr("transform", "translate("+cx+",1)")
			.attr("class", "d3-bg")
			.attr("width", width)
			.attr("height", h-1)
			.attr("rx", 18)
			.attr("stroke", "#ccc")
			.attr("stroke-width", 1)
			.attr("fill", "#fff")
		
		var navs = me.DSS_svg.selectAll('.d3-nav')
			.data(data_d)
			.enter()
			.append("g")
			.on("click", function(d, i, nodes) {
				data_d.forEach(function(dd) {
					dd.active = false;
					dd.ox = dd.tx;
				})
				d.active = true;
				me.DSS_svg.selectAll('text').text(function(dee) {
					return dee.active ? dee.activeText : dee.text;
				});
				me.doResized();
			})
			.attr("class", function(d) {
				return "d3-nav" + (d.active ? " d3-nav-active" : "")
			})
			.attr("transform", function(d) {
				const res = "translate("+ (d.tx + cx) +","+container_pad+")";
	//			x += d.width + pad*2 + itemSpace;// + (d.rl ? 0 : 20) + (d.rr ? 0 : 20);
				return res;
			})
		
		navs.append("rect")
			.data(data_d)
			.attr("x", 0)
			.attr("class", "d3-nav-rect")
			.attr("y", 0)
			.attr("rx", 12)
			.attr("width", function(d) {
				return d.w + pad * 2;
			})
			.attr("height", "40")
/*		navs.append("path")
			.attr("d", function(d) {
				return me.roundedPointRect(d.width + 70,40,16, d.rl, d.rr)
			})
		*/
		navs.append("text")
			.data(data_d)
		  .attr("x", pad)
		  .attr("y", 20)
	      .attr("dy", ".35em")
	      .attr("transform", null)
	      .attr("class", "d3-nav-text")
	      .text(function(d) {
	    	  return d.active ? d.activeText : d.text
	      })
			
		data_d.forEach(function(d) {
			d['curX'] = d['ox'] = d.tx;
		})
	      
		me.doResized(true);
	},
	
	//--------------------------------------------------------------------------
	roundedPointRect: function(width, height, r, roundLeft, roundRight) {
		if (roundLeft && roundRight) {
			const h = height - r * 2,
			w = width - r * 2;
			return "M0,"+r+" q0,-"+r+" "+r+",-"+r+" h"+w+" q"+r+",0 "+r+","+r+" v"+h+
				" q0,"+r+" -"+r+","+r+" h-"+w+" q-"+r+",0 -"+r+",-"+r+" Z";
		}
		else if (roundLeft) {
			const mh = height / 2,
				w = width - r - mh;
			return "M0,"+r+" q0,-"+r+" "+r+",-"+r+" h"+w+" l"+mh+","+mh+" l-"+mh+","+mh+
				" h-"+w+" q-"+r+",0 -"+r+",-"+r+" Z";
		}
		else if (roundRight) {
			const mh = height / 2,
				w = width - r - mh,
				h = height - r * 2;
			return "M"+mh+","+mh+" l-"+mh+",-"+mh+" h"+w+" q"+r+",0 "+r+","+r+" v"+h+
				" q0,"+r+" -"+r+","+r+" h-"+w+" Z";   
		}
		else {
			const mh = height / 2,
				w = width - mh;
			return "M"+mh+","+mh+" l-"+mh+",-"+mh+" h"+w+" l"+mh+","+mh+" l-"+mh+","+mh+
				" h-"+w+" Z";
		}
	},

	//--------------------------------------------------------------------------
	calculateTextWidths: function() {
		const me = this;
		me.DSS_svg
			.selectAll('.dummyText')
			.append('g')
			.data(data_d)
			.enter()
			.append("text")
			.attr("class", function(d) {
				return "dummyText d3-nav-text" + (d.active ? " d3-nav-active" : "")
			})
			.text(function(d) { return d.active ? d.activeText : d.text})
			.each(function(d,i) {
				d['w'] = this.getComputedTextLength()
				console.log(d.w)
				this.remove() // remove them just after displaying them...
			})//.remove(); //..and clean up temp g node
			
		const container_pad = 5;
		const pad = 20;
		const itemSpace = 6;
		var wx = container_pad;
		
		data_d.forEach(function(d) {
			d['ox'] = d['tx'];
			d['tx'] = wx;
			wx += d.w + pad * 2 + itemSpace;;
		})
		return wx + container_pad - itemSpace;
	},
	
	//--------------------------------------------------------------------------
	doResized: function(first) {
		var me = this;
		var t = d3.transition(750)
		var w = me.getWidth(), 
			h = me.getHeight();
		
		var svg = me.DSS_svg; 
		
		const container_pad = 5;
		var x = container_pad;
		const pad = 20;
		const itemSpace = 6;
		
		svg
			.attr("width", me.getWidth())
			.attr("height", me.getHeight());
		
		var width = me.calculateTextWidths();

		var ease = d3.easeLinear;
		var cx = (w - width) / 2;
		
		var navs = svg.selectAll('.d3-nav')
		.selectAll(".d3-nav-rect")
		.transition()
	//	.ease(ease)
		.attr("width", function(d) {
			return d.w + pad * 2;
		})
		svg.selectAll('.d3-bg')
			.transition()
			//.ease(ease)
			.attr("transform", "translate("+cx+",1)")
			.attr("width", width)

		function translateFn(d,i,a) {
			return d3.interpolateTransformSvg(
					"translate("+ (d.ox + cx) +","+container_pad+")",
					"translate("+ (d.tx + cx) +","+container_pad+")")
		}
		
		/*var*/ navs = svg.selectAll('.d3-nav')
			.transition()
			//.ease(ease)
			.attr("class", function(d) {
				return "d3-nav" + (d.active ? " d3-nav-active" : "")
			})
			.attrTween("transform", translateFn);
			
	}

	
});


