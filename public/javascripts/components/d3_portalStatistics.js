
function polarToCartesian(centerX, centerY, radius, angleInRadians) {

  return {
    x: centerX + (radius * Math.cos(angleInRadians)),
    y: centerY + (radius * Math.sin(angleInRadians))
  };
}

function describeArc(x, y, radius, startAngle, endAngle){

	startAngle -= (Math.PI / 2);
	endAngle -= (Math.PI / 2);
    var start = polarToCartesian(x, y, radius, endAngle);
    var end = polarToCartesian(x, y, radius, startAngle);

    var sweepFlag = endAngle > startAngle ? "0" : "1"
    var largeArcFlag = endAngle > startAngle 
    	? endAngle - startAngle <= Math.PI ? "0" : "1" 
    	: startAngle - endAngle <= Math.PI ? "0" : "1";
    
    var d = [
        "M", start.x, start.y, 
        "A", radius, radius, 0, largeArcFlag, sweepFlag, end.x, end.y
    ].join(" ");

    return d;       
}

//------------------------------------------------------------------------------
Ext.define('DSS.components.d3_portalStatistics', {
//------------------------------------------------------------------------------
    extend: 'Ext.Component',
	alias: 'widget.portal_statistics',
	
	id: 'd3-portal-stats',
	width: 364, // 260
	height: 364, // 260
	//style: 'background: #fff',
	listeners: {
		afterrender: function(self) {
			self.createD3_Radar();
			self.createD3_Pie();
		}
	},
	DSS_values: [
		{v:0.8, t:'Bird Habitat'},
		{v:0.76, t:'Pest Suppression'},
		{v:0.56, t:'N Retention'},
		{v:0.5, t:'Soil Retention'},
		{v:0.29, t:'P Retention'},
		{v:0.2, t:'Soil Carbon'},
		{v:0.25, t:'Emissions'},
		{v:0.92, t:'Pollinators'},
	],

	DSS_pieValues: [
		{v:2, t:'Developed', c:'#8c8895'},
		{v:54, t:'Row Crops', c:'#f2e75e'},
		{v:3, t:'Wetlands / Water', c:'#819fda'},
		{v:22, t:'Grasses',c:'#98bf63'},
		{v:19, t:'Woodland', c:'#dc8f50'},
	],

	DSS_valueWorst: '#d53e4f',
	DSS_valuePoor: '#dc8f50',
	DSS_valueAccetable: '#f6e851',
	DSS_valueBest: '#98bf63',
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
		});
		
		var tt = 0;
		me.callParent(arguments);
		setInterval(function() {
			var res = [];
			Ext.each(me.DSS_values, function(d,i) {
				res.push({
					v: Math.random() * 0.5 + (Math.cos(tt + i * 0.4) + 1) * 0.25,
					t: d.t
				})
			});
			me.updateRadarTo(res);
			tt += 0.5;
		}, 4000)
		
		setInterval(function() {
			var res = [];
			Ext.each(me.DSS_pieValues, function(d,i) {
				res.push({
					v: d.v + (Math.random() * 30),
					t: d.t,
					c: d.c
				})
			});
			me.updatePieTo(res);
		}, 5193)

	},

	//--------------------------------------------------------------------------
	createD3_Radar: function() {
		var me = this;
		var w = me.getWidth(), h = me.getHeight();
		const circlePow = 0.8;
		const count = me.DSS_values.length;
		
		me['DSS_svg'] = d3.select("#d3-portal-stats")
			.append("svg")
				.attr("width", w)
				.attr("height",h)

		var root = me.DSS_svg.append('g')
			.attr('transform','translate(' + (w * 0.5) + ',' +  (h * 0.5) + ')');

		const radar_size = 100;

		var c1 = me.DSS_valueWorst, c2 = me.DSS_valuePoor, c3 = me.DSS_valueAccetable, c4 = me.DSS_valueBest;
		var colorGrade = d3.scaleLinear()
			.domain([0,0.24, 0.26, 0.49, 0.51, 0.74, 0.76,1])
			.range([c1,c1, c2,c2, c3,c3, c4,c4])
			.interpolate(d3.interpolateHcl);
		
		var circularGrid = [];
		for (var i = 0; i <= 8; i++) {
			var g = {
				r: i * 0.125,
				o: (i == 0 || i == 8) ? 1 : 
					(i % 2) ? 0.1 : 0.5
			};
			circularGrid.push(g);
		}
		
		root.append("circle")
			.attr('r', radar_size + 10)
			.attr('fill', '#fff')
			
		const wedgeSize = Math.PI * 2 / count;
		var wedges = root.selectAll('.d3-wedge-container')
			.data(me.DSS_values)
			.enter()
			.append("g").attr('class','d3-wedge-container')
		
		var arcGenerator = d3.arc()
			.innerRadius(10)
			.padAngle(0.005)
			.cornerRadius(2)
			.padRadius(radar_size + 10);
		
		wedges
			.append("path")
			.attr("class", 'd3-wedge')
			.attr("d", function(d,i) {
				var sAngle = (i - 0.5) * wedgeSize,
					outer = Math.pow(d.v,circlePow) * radar_size + 10,
					inner = outer - 10;
		
				var path = arcGenerator
					//.innerRadius(inner > 10 ? inner : 10)
					.startAngle(sAngle)
					.endAngle(sAngle + wedgeSize)
					.outerRadius(outer);
				return path();
			})
			.attr("fill", function(d) {
				return colorGrade(d.v)
			})
			.attr("stroke", function(d) {
				const c = colorGrade(d.v)
				return d3.color(c).darker(1).hex()
			})
			.attr("stroke-width", 1)
			
		arcGenerator
			.innerRadius(10)
			.outerRadius(radar_size + 30);
			
		wedges
			.append("path")
			.attr("id", function(d,i) { 
				return "wedge-arc-" + i
			})
			.attr("class", 'd3-text-wedge')
			.attr("d", function(d,i) {
				var sAngle = (i - 0.5) * wedgeSize;
				var test = Math.cos((i/count) * Math.PI * 2)

				// flip start/end when we go around the bottom half. This will reorder the text placement
				var path = arcGenerator
					.startAngle(test < 0 ? sAngle + wedgeSize : sAngle)
					.endAngle(test < 0 ? sAngle : sAngle + wedgeSize);
				return path();
			})
			.attr("fill", 'rgba(0,0,0,0)')
	        .on("mouseover", function(d) {
	            me.DSS_tooltip
	            	.transition()
	            	.delay(function(d) {
	            		return me.DSS_tooltip.style("opacity") > 0 ? 0 : 600;
	            	})
	                .duration(100)		
	                .style("opacity", 1);		
	            me.DSS_tooltip
	            	.html("<b>" + d.t + ":</b> " + (d.v * 100).toFixed(1) + "%")	
	                .style("left", (d3.event.clientX + "px"))
	            	.style("top", (d3.event.clientY + "px"))
	        })
	        .on("mouseout", function(d) {		
        		me.DSS_tooltip
        		.transition()		
                .duration(100)		
                .style("opacity", 0);	
	        })			
;
		
		wedges.append("text")
			.attr("class","d3-wedge-text")
			.attr("opacity", 0.8)
			.style('font-size', '13px')
			.attr("dy", function(d,i) {
				var test = Math.cos((i/count) * Math.PI * 2);
				return test < 0 ? -5 : 13
			})
			.append("textPath")
			.attr("startOffset", '15%')
			.style("text-anchor", "middle")
			.attr("xlink:href", function(d,i) {
				return '#wedge-arc-' + i;
			})
			.text(function(d) {
				return d.t;
			})

		root.append("g")
			.attr("opacity", 0.25)
			.attr("stroke", '#000')
			.attr("stroke-width", 0.75)
		.selectAll('.d3-radial-grid')
			.data(me.DSS_values)
			.enter()
			.append("line")
			.attr("class", 'd3-radial-grid')
			.attr("x1", function(d,i) {
				i -= 0.5;
				var sAngle = ((i/count) * Math.PI * 2) - (Math.PI / 0.2);
				return Math.sin(sAngle) * 10;
			})
			.attr("y1", function(d,i) {
				i -= 0.5;
				var sAngle = ((i/count) * Math.PI * 2) - (Math.PI / 0.2);
				return Math.cos(sAngle) * 10;
			})
			.attr("x2", function(d,i) {
				i -= 0.5;
				var sAngle = ((i/count) * Math.PI * 2) - (Math.PI / 0.2);
				return Math.sin(sAngle) * (radar_size + 10);
			})
			.attr("y2", function(d,i) {
				i -= 0.5;
				var sAngle = ((i/count) * Math.PI * 2) - (Math.PI / 0.2);
				return Math.cos(sAngle) * (radar_size + 10);
			});
			
		root.append("g")
			.style("pointer-events","none")
			.attr("fill", "none")
			.attr("stroke", "#333")
		.selectAll('.d3-circular-grid')
			.data(circularGrid)
			.enter()
			.append("circle")
			.attr("class", "d3-circular-grid")
			.attr("r", function(d) {
				return Math.pow(d.r,circlePow) * radar_size + 10;
			})
			.attr("stroke-width", function(d) {
				return d.o * 0.5 + 0.6;
			})
			.attr("opacity", function(d) {
				return d.o;
			})
			
		// FIXME: acquires an existing one at a wonky delay
		Ext.defer(function() {
			me['DSS_tooltip'] = d3.select(".d3-nav-tooltip");
			console.log(me.DSS_tooltip)
		}, 250);
	},
	
	//-------------------------------------------------------------
	updateRadarTo: function(newData) {
		var me = this;
		const count = newData.length;
		const circlePow = 0.8;
		const radar_size = 100;//(w * 0.5) - 30,
		
		var c1 = me.DSS_valueWorst, c2 = me.DSS_valuePoor, c3 = me.DSS_valueAccetable, c4 = me.DSS_valueBest;
		
		var colorGrade = d3.scaleLinear()
			.domain([0,0.25, 0.26,0.5, 0.51,0.75, 0.76,1])
			.range([c1,c1, c2,c2, c3,c3, c4,c4])
		
		const wedgeSize = Math.PI * 2 / count;
		var arcGenerator = d3.arc()
			.cornerRadius(2)
			.innerRadius(10)
			.padAngle(0.005)
			.padRadius(radar_size + 10);
		
		// mouse tooltips are bound here so must be updated
		me.DSS_svg.selectAll('.d3-text-wedge')
			.data(newData);
		
		me.DSS_svg.selectAll('.d3-wedge')
		.data(newData)
		.transition()
		.duration(1000)
		.ease(d3.easeBounce)
		.attr("d", function(d,i) {
			var sAngle = (i - 0.5) * wedgeSize,
				outer = Math.pow(d.v,0.8) * radar_size + 10,
				inner = outer - 10;
	
			var path = arcGenerator
				//.innerRadius(inner > 10 ? inner : 10)
				.startAngle(sAngle)
				.endAngle(sAngle + wedgeSize)
				.outerRadius(outer);
			return path();
		})
		.attr("fill", function(d) {
			return colorGrade(d.v)
		})
		.attr("stroke", function(d) {
			const c = colorGrade(d.v)
			return d3.color(c).darker(1).hex()
		});
	},
	
	//--------------------------------------------------------------------------
	createD3_Pie: function() {
		var me = this;
		var w = me.getWidth(), h = me.getHeight();
		
		var pie = d3.pie()
			.padAngle(0.015)
			.sortValues(null)
			.value(function(d) {
				return d.v; 
			})(me.DSS_pieValues);
		
		console.log(pie);
		const total = d3.sum(me.DSS_pieValues, function(d) {return d.v});
		
		const padded_hw = (w * 0.5) - 30,
			padded_hh = (h * 0.5) - 30;
		
		var root = me.DSS_svg.append('g')
			.attr('transform','translate(' + (w * 0.5) + ',' +  (h * 0.5) + ')');
		
		var wedges = root.selectAll('.d3-pie-container')
			.data(pie)
			.enter()
			.append("g")
			.attr('class','d3-pie-container');
	
		root.selectAll('.d3-pie-container')
			.transition()
			.on("start",function repeat() {
				d3.active(this)
					.transition()
						.duration(50000)
						.ease(d3.easeLinear)
						.attr('transform', 'rotate(120)')
					.transition()
						.duration(50000)
						.ease(d3.easeLinear)
						.attr('transform', 'rotate(240)')
					.transition()
						.duration(50000)
						.ease(d3.easeLinear)
						.attr('transform', 'rotate(360)')
						.on("end", repeat)
			})
		
		var arcGenerator = d3.arc()
			.innerRadius(padded_hw - 14)
			.outerRadius(padded_hw+14)
			.cornerRadius(2)
		
		wedges
			.append("path")
			.attr("class", 'd3-pie')
			.attr("d", arcGenerator)
			.attr("fill", function(d) {
				return d.data.c;
			})
			.attr("stroke", function(d) {
				return d3.color(d.data.c).darker(1).hex()
			})
			.attr("stroke-width", 1)
			.each(function(d) {
				this._current = d
			})
			
		arcGenerator
			.innerRadius(padded_hw - 14)
			.outerRadius(padded_hw + 30)
			.padAngle(0)
			.cornerRadius(0)
		
		wedges
			.append("path")
			.attr("class", 'd3-text-pie')
			.attr("d", arcGenerator)
			.attr("fill", 'rgba(0,0,0,0)')
	        .on("mouseover", function(d) {
	            me.DSS_tooltip
	            	.transition()
	            	.delay(function(d) {
	            		return me.DSS_tooltip.style("opacity") > 0 ? 0 : 600;
	            	})
	                .duration(100)		
	                .style("opacity", 1);		
	            me.DSS_tooltip
	            	.html("<b>" + d.data.t + ":</b> " + d.data.v.toFixed(1) + "%")	
	                .style("left", (d3.event.clientX + "px"))
	            	.style("top", (d3.event.clientY + "px"))
	        })
	        .on("mouseout", function(d) {		
        		me.DSS_tooltip
        		.transition()		
                .duration(100)		
                .style("opacity", 0);	
	        })
			.each(function(d) {
				this._current = d
			})

	
	arcGenerator
		.innerRadius(padded_hw + 10)
		.outerRadius(padded_hw + 30);

	wedges
		.append("path")
		.attr("class", 'd3-pie-arc')
		.attr("id", function(d,i) { 
			return "pie-arc-" + i
		})
		.attr("d", function(d) {
			var testAng = (d.startAngle + d.endAngle) / 2 - Math.PI / 2;
			var test = 0;//Math.cos(testAng) // check center

			// flip start/end when we go around the bottom half. This will reorder the text placement
			var a1 = (test < 0) ? d.endAngle + 1 : d.startAngle - 1,
				a2 = (test < 0) ? d.startAngle - 1 : d.endAngle + 1;
			
			var path = describeArc(0,0,padded_hw + 10, a1, a2);
			return path
		})
		.attr("fill", "none")
		.each(function(d) {
			this._current = d
		})
		
	
		wedges.append("text")
			.attr("class","d3-pie-text")
			.attr("opacity", 0.8)
			.style('font-size', '14px')
			.attr("dy", function(d) {
				var testAng = (d.startAngle + d.endAngle) / 2 - Math.PI / 2;
				var test = 0;//Math.cos(testAng) // check center
				return test < 0 ? -8 : 16
			})
			.append("textPath")
			.attr("startOffset", '50%')
			.style("text-anchor", "middle")
			.attr("xlink:href", function(d,i) {
				return '#pie-arc-' + i;
			})
			.text(function(d) {
				return d.data.t;
			})
			.each(function(d) {
				this._current = d
			})
	},
	
	//-------------------------------------------------------------
	updatePieTo: function(newData) {
		var me = this;
		var w = me.getWidth(), h = me.getHeight();
		
		var pie = d3.pie()
			.padAngle(0.015)
			.sortValues(null)
			.value(function(d) {
				return d.v; 
			})(newData);
	
		const padded_hw = (w * 0.5) - 30,
			padded_hh = (h * 0.5) - 30;
		
		var arc = d3.arc()
			.innerRadius(padded_hw - 14)
			.outerRadius(padded_hw+14)
			.cornerRadius(2)
		function arcTween(a) {
			var iInterp =  d3.interpolate(this._current, a);
			this._current = iInterp(0);
			return function(t) {
				return arc(iInterp(t));
			}
		}
		
		var containerArc = d3.arc()
			.innerRadius(padded_hw - 14)
			.outerRadius(padded_hw+30);
		function containerArcTween(a) {
			var iInterp =  d3.interpolate(this._current, a);
			this._current = iInterp(0);
			return function(t) {
				return containerArc(iInterp(t));
			}
		}
		
		function pathTween(a) {
			var iInterp =  d3.interpolate(this._current, a);
			this._current = iInterp(0);
			return function(t) {
				var res = iInterp(t);
				var testAng = (res.startAngle + res.endAngle) / 2 - Math.PI / 2;
				var test = 0;//Math.cos(testAng) // check center

				// flip start/end when we go around the bottom half. This will reorder the text placement
				var a1 = (test < 0) ? res.endAngle + 1 : res.startAngle - 1,
					a2 = (test < 0) ? res.startAngle - 1 : res.endAngle + 1;
				return describeArc(0,0,padded_hw + 10, a1, a2);
			}
		}
		
		me.DSS_svg
			.selectAll('.d3-pie')
			.data(pie)
			.transition()
				.duration(1000)
				.ease(d3.easeBounce)
				.attrTween("d", arcTween);
	
		// mouse tooltips are bound here so must be updated
		me.DSS_svg
			.selectAll('.d3-text-pie')
			.data(pie)
			.transition()
				.duration(1000)
				.ease(d3.easeBounce)
				.attrTween("d", containerArcTween);

		me.DSS_svg
			.selectAll(".d3-pie-arc")
			.data(pie)
			.transition()
				.duration(1000)
				.ease(d3.easeBounce)
				.attrTween("d", pathTween);

		me.DSS_svg
			.selectAll("d3-pie-text")
			.data(pie)
			.transition()
				.duration(1000)
				.ease(d3.easeBounce)
				.attr("dy", function(d) {
					var testAng = (d.startAngle + d.endAngle) / 2 - Math.PI / 2;
					var test = 0;//Math.cos(testAng) // check center
					return test < 0 ? -8 : 16
				})
		
	},
	
});
