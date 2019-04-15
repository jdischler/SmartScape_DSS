
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
	DSS_duration: 250.0,
	DSS_containerPad: 5,
	DSS_nodePad: 20, // inner padding around each element
	DSS_nodeSpacing: 6,	//space between nodes
	
	DSS_elements: [{
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
			.attr("class", function(d) {
				return "d3-nav" + (d.active ? " d3-nav-active" : "")
			})
			.attr("transform", function(d) {
				const res = "translate("+ (d.t_x + cx) +","+me.DSS_containerPad+")";
				return res;
			})
		
		navs.append("rect")
			.data(me.DSS_elements)
			.attr("class", "d3-nav-rect")
			.attr("x", 0)
			.attr("y", 0)
			.attr("rx", 12)
			.attr("width", function(d) {
				return d.w + me.DSS_nodePad * 2;
			})
			.attr("height", "40");
			
		navs.append("text")
			.data(me.DSS_elements)
			.attr("x", me.DSS_nodePad)
			.attr("y", 20)
			.attr("dy", ".35em")
			.attr("class", "d3-nav-text")
			.text(function(d) {
				return d.active ? d.activeText : d.text
			});
			
		data_d.forEach(function(d) {
		//	d['curX'] = d['ox'] = d.tx;
		})
	      
		//me.doResized(true);
	},
	
	//--------------------------------------------------------------------------
	layoutNav: function() {
		
		const me = this;
		
		var atX = me.DSS_containerPad;
		me.DSS_elements.forEach(function(d) {
			d['t_x'] = atX;
			atX += d.w + me.DSS_nodePad * 2 + me.DSS_nodeSpacing;
		})
		
		return atX + me.DSS_containerPad - me.DSS_nodeSpacing;
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
			.each(function(d,i) {
				d['t_w'] = this.getComputedTextLength()
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
			atX += d.w + me.DSS_nodePad * 2 + me.DSS_nodeSpacing;
		})
		
		return atX + me.DSS_containerPad - me.DSS_nodeSpacing;

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
			t = Math.pow(t,0.4);
			if (t >= 1) {
				t = 1.0;
				me.DSS_timer.stop();
			}
			
			// compute & set real width
			svg.selectAll('.d3-nav')
				.selectAll(".d3-nav-rect")
				.attr("width", function(d) {
					d.w = d.s_w * (1.0 - t) + d.t_w * t;
					return d.w + me.DSS_nodePad * 2;
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
					atX += d.w + me.DSS_nodePad * 2 + me.DSS_nodeSpacing;
					return "translate("+ (d.t_x + cx) +"," + me.DSS_containerPad + ")"
				});
				
			
			svg.selectAll('.d3-nav-bg')
				.attr("transform", "translate("+cx+",1)")
				.attr("width", realWidth)
				
		}, 16/*1000.0 / 60.0*/);
	}
	
});

