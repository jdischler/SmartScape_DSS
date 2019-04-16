

// measure notes:
// shave 4-8px inner padding from the left side of all nodes

// first node chevron point is 13px further to the right

// second node chevron point is 16px further to the right

// third node chevron point is 27px further to the right

// 4th node rounded edge is 2px to short on the right

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
			self.createD3_Elements();
		},
		resize: function(self) {
			if (self.DSS_svg) {
			//	self.doResized();
			}
		}
	},
	DSS_time: false,
	DSS_timer: false,
	DSS_duration: 750.0,
	DSS_containerPad: 5,
	DSS_nodePad: 20, // inner padding around each element
	DSS_nodeSpacing: 6,	//space between nodes
	
	DSS_elements: [{
		text: 'Select',
		active: true,
		activeText: 'Select Area of Interest',
		tooltip: 'Select an area of interest'
	},{
		text: 'Refine',
		activeText: 'Refine Area of Interest (optional)',
		tooltip: 'Optionally refine the area of interest by choosing a county or a watershed'
	},{
		text: 'Review',
		activeText: 'Review Assumptions (optional)',
		tooltip: 'Review and adjust an assumptions if desired'
	},{
		text: 'Start',
		activeText: 'Start SmartScape',
	}],
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
		});
		
		me.callParent(arguments);
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
	createD3_Elements: function() {
		var me = this;
		
		me['DSS_svg'] = d3.select("#d3-nav")
			.append("svg")
				.attr("width", me.getWidth())
				.attr("height",me.getHeight());
		
		me.measureNodes();

		const width = me.layoutNav();
		const cx = (me.getWidth() - width) / 2;
		
		me.DSS_svg
			.append("g")
			.append("rect")
			.attr("transform", "translate(" + cx + ",1)")
			.attr("class", "d3-nav-bg")
			.attr("width", width)
			.attr("height", me.getHeight()-1) // ? why -1?
			.attr("rx", 18);
		
		me.DSS_svg.selectAll("clipPath")
			.data(me.DSS_elements)
			.enter()
			.append("clipPath")
			.attr("id", function(d,i) {
				return 'text-clip-' + i;
			})
			.append("use")
			.attr("xlink:href", function(d,i) {
				return "#path-" + i;
			})
		
		var navs = me.DSS_svg.selectAll('.d3-nav')
			.data(me.DSS_elements)
			.enter()
			.append("g")
			.on("click", function(d, i, nodes) {
				me.DSS_elements.forEach(function(dd) {
					dd.active = false;
				})
				d.active = true;
				me.DSS_svg.selectAll('text').text(function(dee) {
					return dee.active ? dee.activeText : dee.text;
				});
				me.updateNav();
			})
	        .on("mouseover", function(d) {
	        	if (d.tooltip && !d.active) {
		            me.DSS_tooltip.
		            	transition()
		            	.delay(function(d) {
		            		return me.DSS_tooltip.style("opacity") > 0 ? 0 : 600;
		            	})
		                .duration(100)		
		                .style("opacity", 1);		
		            me.DSS_tooltip.
		            	html(d.tooltip)	
		                .style("left", (d3.event.pageX) + "px")		
		                .style("left", (d3.event.target.parentNode.getBoundingClientRect().x - 64) + "px")
		            	.style("top", (d3.event.target.parentNode.getBoundingClientRect().y - 28) + "px")
		        }
	        	else {
	        		me.DSS_tooltip.transition()		
	                .duration(100)		
	                .style("opacity", 0);	
	        	}
	        })
	        .on("mouseout", function(d) {		
        		me.DSS_tooltip.transition()		
                .duration(100)		
                .style("opacity", 0);	
	        })			
			.attr("class", function(d) {
				return "d3-nav" + (d.active ? " d3-nav-active" : "")
			})
			.attr("transform", function(d) {
				const res = "translate("+ (d.t_x + cx) +"," + me.DSS_containerPad + ")";
				return res;
			})

		navs.append("path")
			.data(me.DSS_elements)
			.attr("id", function(d,i) {
				return "path-" + i;
			})
			.attr("class", "d3-nav-rect")
			.attr("d", function(d) { //width, height, r, roundLeft, roundRight
				var w = d.w + me.DSS_nodePad * 2 + (d.r_w > 0 ? 0 : me.DSS_nodePad);
				return me.roundedPointRect(w, 40, 16, (d.l_w <= 0), (d.r_w <= 0))
			})
			
		navs.append("text")
			.data(me.DSS_elements)
			.attr("clip-path", function(d,i) {
				return "url(#text-clip-" + i +")"
			})
			.attr("x", function(d) {
				return me.DSS_nodePad + d.l_w;
			})
			.attr("y", 18) // TODO: position automatically
			.attr("dy", ".35em")
			.attr("class", "d3-nav-text")
			.text(function(d) {
				return d.active ? d.activeText : d.text
			});
		
		me['DSS_tooltip'] = d3.select("body").append("div")	
		    .attr("class", "d3-nav-tooltip")				
		    .style("opacity", 0);	
	},
	
	//--------------------------------------------------------------------------
	layoutNav: function() {
		
		const me = this;
		
		var atX = me.DSS_containerPad;
		me.DSS_elements.forEach(function(d,i,a) {
			d['t_x'] = atX;
			atX += d.w + me.DSS_nodePad + me.DSS_nodeSpacing;
		})
		
		// nodePad compensates for hack in path drawing that adds an extra pad for the last item...
		return atX + me.DSS_nodePad + me.DSS_containerPad - me.DSS_nodeSpacing;
	},

	//--------------------------------------------------------------------------
	measureNodes: function() {
		
		const me = this;
		me.DSS_svg
			.selectAll('.dummyText')
			.append('g')
			.data(me.DSS_elements)
			.enter()
			.append("text")
			.attr("class", function(d) {
				return "dummyText d3-nav-text" + (d.active ? " d3-nav-active" : "")
			})
			.text(function(d) { 
				return d.active ? d.activeText : d.text
			})
			.each(function(d,i,a) {
				var w = this.getComputedTextLength();
				d['l_w'] = (i > 0) ? 20 : 0;
				d['r_w'] = (i < a.length-1) ? 20 : 0;
				d['t_w'] = w + d.l_w + d.r_w;
				d['w'] = d['w'] || d['t_w']; 
				d['s_w'] = d.w;
				this.remove() // remove them just after displaying them...
			});
	},
	
	//---------------------------------------------------------------------------------
	getNavWidth: function() {
		var me = this;
		var atX = me.DSS_containerPad;
		
		me.DSS_elements.forEach(function(d) {
			atX += d.w + me.DSS_nodePad + me.DSS_nodeSpacing;
		})
		
		// nodePad compensates for hack in path drawing that adds an extra pad for the last item...
		return atX + me.DSS_nodePad + me.DSS_containerPad - me.DSS_nodeSpacing;
	},
	
	//--------------------------------------------------------------------------
	updateNav: function() {
		
		var me = this;
		var svg = me.DSS_svg; 
		
		svg
			.attr("width", me.getWidth())
			.attr("height", me.getHeight());
		
		// set targets
		me.measureNodes();

		if (me.DSS_timer) {
			me.DSS_timer.stop();
		}
	
		me.DSS_timer = d3.timer(function(elapsed) {
			var t = elapsed / me.DSS_duration;
			if (t >= 1) {
				t = 1.0;
				me.DSS_timer.stop();
			}
			else {
				t = Ext.fx.Easing.ease(t);
			}
			
			// compute & set real width
			svg.selectAll('.d3-nav')
				.selectAll(".d3-nav-rect")
				.attr("d", function(d) { 
					var w = d.w = d.s_w * (1.0 - t) + d.t_w * t;
					w += me.DSS_nodePad * 2 + (d.r_w > 0 ? 0 : me.DSS_nodePad);
					return me.roundedPointRect(w, 40, 16, (d.l_w <= 0), (d.r_w <= 0))
				})

			var realWidth = me.getNavWidth();
			var cx = (me.getWidth() - realWidth) / 2;
			var atX = me.DSS_containerPad;
			svg.selectAll('.d3-nav')
				.attr("class", function(d) {
					return "d3-nav" + (d.active ? " d3-nav-active" : "")
				})
				.attr("transform", function(d) {
					d['t_x'] = atX;
					atX += d.w + me.DSS_nodePad + me.DSS_nodeSpacing;
					return "translate("+ (d.t_x + cx) +"," + me.DSS_containerPad + ")"
				});
				
			
			svg.selectAll('.d3-nav-bg')
				.attr("transform", "translate("+cx+",1)")
				.attr("width", realWidth)
				
		}, 16/*1000.0 / 60.0*/);
	}
	
});
