
/*olCustomButton = function(opt_options) {

	var options = opt_options || {};
	
	var button = document.createElement('button');
	button.setAttribute('type', 'button');
	button.appendChild(document.createTextNode('\u21D5'));
	button.title = 'Toggle Map Options';
	
	var this_ = this;
	var toggleDetails = function() {
		var el = Ext.getCmp('footer'); 
		el.setVisible(!el.isVisible());
	};
	
	button.addEventListener('click', toggleDetails, false);
	button.addEventListener('touchstart', toggleDetails, false);
	
	var element = document.createElement('div');
	element.className = 'ol-custom-button ol-unselectable ol-control';
	element.appendChild(button);
	
	ol.control.Control.call(this, {
		element: element,
		target: options.target
	});
};

ol.inherits(olCustomButton, ol.control.Control);
*/      
var globalMap = new ol.Map({
	controls: ol.control.defaults({
		attributionOptions: {
			collapsible: true
		}
	}).extend([
		new ol.control.ScaleLine(),
//		new olCustomButton()
	]),
	layers: [
		new ol.layer.Tile({
			source: new ol.source.Stamen({
				layer: 'terrain'//-background' // terrain/ terrain-labels / terrain-lines
			})
		}),
	],
	view: new ol.View({
		center: ol.proj.fromLonLat([-89.565, 43.225]),
		zoom: 10
	})
});

var spotStyle = new ol.style.Style({
    stroke: new ol.style.Stroke({
        color: 'rgba(0, 0, 0, 0.9)',
        width: 2
    }),
    fill: new ol.style.Fill({
	    color: 'rgba(0, 32, 0, 0.8)'
	})
});

var maskLayer = new ol.layer.Vector({
	source: new ol.source.Vector(),
	style: spotStyle,
	opacity: 0.5,
	// these potentially reduce performance but looks better
	updateWhileAnimating: true, 
	updateWhileInteracting: true
});

var selectionLayer = new ol.layer.Image({
	opacity: 0.7
});



//-----------------------------------------------------
// DSS.components.MainMap
//
//-----------------------------------------------------
Ext.define('DSS.components.MainMap', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.mainmap',
 
    requires: [
	    'GeoExt.component.Map',
    ],
    
	header: {
		style: 'border-right: 1px solid #bcd; border-left: 1px solid #bcd;',
		//height: 8,
	},
	region: 'center',
	layout: 'fit',

	listeners: {
		afterrender: function(self) {
			Ext.defer(function() { 
				var rt = Ext.dom.Query.select('.ol-overlaycontainer-stopevent');
				console.log(rt);
				Ext.create('Ext.container.Container', {
					id: 'dss-selection-loading',
					hidden: true,
					style: 'top: 0; left: 2em; width: 5em; height: 5em; opacity:0.8; position: absolute; background-image: url(assets/images/spinner-icon-gif-24.gif); background-size: cover;',
					renderTo: rt
				});
			}, 500)
		}
	},
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'gx_map',
				map: globalMap,
				animate: false,
				style: 'background-color: #d9ddb7' // rgb(217,221,183)
			}]
		});
		
		me.callParent(arguments);
		me.addSelectionLayer();
		me.addMapMask();
	},
	
	//-------------------------------------------------------
	addMapMask: function() {
	    globalMap.addLayer(maskLayer);
	    
/*	    var bounds = [
			-10300000, 5200000,
			 -9670000, 5600000];*/
	    var bounds = [
	    	-10179328.176070, 5230121.354885,
	    	-9714738.267376, 5606000.198336];
	    // draw polygon
	    var spot = new ol.geom.Polygon([[
	        [bounds[0], bounds[1]],
	        [bounds[0], bounds[3]],
	        [bounds[2], bounds[3]],
	        [bounds[2], bounds[1]],
	        [bounds[0], bounds[1]]
	    ]]);
	    
	    // draw rectangle spot
		var c = [
			-10062652.7, 5278060.5,
			-9878152.7, 5415259.6
		];
	    
	    spot.appendLinearRing(new ol.geom.LinearRing([
	        [c[0], c[3]],
	        [c[0], c[1]],
	        [c[2], c[1]],
	        [c[2], c[3]],
	        [c[0], c[3]]
	    ]));
	    
	    maskLayer.getSource().addFeature(new ol.Feature(spot));
	},
	
	//--------------------------------------------------------------------------
	addSelectionLayer: function(selDef) {
		var me = this;
	//	selectionLayer = new ol.layer.Image({
//			opacity: 0.5
//		});
		globalMap.addLayer(selectionLayer);
	},

	//-----------------------------------------------------------------------
	addTips: function() {
		
		var el = Ext.dom.Query.select('.ol-custom-button')[0];
        Ext.tip.QuickTipManager.register({
            target: el,
            text: 'Toggle Map Controls'
        });
	}
	
});