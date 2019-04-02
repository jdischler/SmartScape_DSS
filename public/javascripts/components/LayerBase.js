//-----------------------------------------------------
// DSS.components.LayerBase
//
//-----------------------------------------------------
Ext.define('DSS.components.LayerBase', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.layerbase',
    alternateClassName: [
        'DSS.Layers',
    ],
    
	collapsed: true,
	expandToolText: 'Find land by this attribute',//DSS.utils.tooltip('Find land by this attribute'),
	collapseToolText: 'Remove this attribute',
	allowCollapseAll: true,
	layout: 'fit',
	bodyPadding: '0 0 8 0',
	
	statics: {
		layers: [],
		processing: false,
		timeoutId: false,
		add: function(layer) {
			this.layers.push(layer);
			var cmp = Ext.getCmp('DSS_findLandByAttr');
			cmp.add(layer);
		},
		
		valueChanged: function() {
			var me = this;
			if (me.timeoutId) clearTimeout(me.timeoutId);
			Ext.getCmp('yes-dss-selection-container').setLoading('. . .').addCls('x-selection-working-mask')
			Ext.getCmp('dss-selection-loading').setVisible(true);
			if (!me.processing) {
				me.processing = true;
				me.getSelectionParms();
				return;
			}
			me.timeoutId = setTimeout(me.valueChanged.bind(me), 300)
		},
		
		getSelectionParms: function() {
			var me = this;
			var queryData = [];
			Ext.each(me.layers, function(layer) {
				var result = layer.configureSelection();
				if (result) queryData.push(result);
			})
			var obj = Ext.Ajax.request({
				url: location.href + '/createSelection',
				jsonData: {
					queryLayers: queryData,
					first: {
						queryLayers: [{
							"name":"dist_to_water","type":"continuous","lessThanTest":"<=","greaterThanTest":">=","lessThanValue":120
						}]
					},
					second: {
						queryLayers: queryData
					}
				},
				timeout: 25000, // in milliseconds
				
				success: function(response, opts) {
					var obj = JSON.parse(response.responseText);
					// TODO: server should pass this all back...
					//-10062652.65061, -9878152.65061, 5278060.469521415, 5415259.640662575
					obj['bounds']= [
						-10062652.65061, 5278060.469521415,
						-9878152.65061, 5415259.640662575
					]
					var area = (obj.selectedPixels * 30.0 * 30.0) / 1000000.0;
					// then convert from km sqr to acres (I know, wasted step, just go from 30x30 meters to acres)
					area *= 247.105;
			    
					var totalAreaPerc = (obj.selectedPixels / obj.totalPixels) * 100.0;
					
					var area = Ext.util.Format.number(area, '0,000.#'), 
						perc = Ext.util.Format.number(totalAreaPerc, '0.###');
						
					Ext.getCmp('yes-dss-selected-stats').setHtml(area + ' acres<br/>' + perc + '%');
					Ext.getCmp('yes-dss-selected-stats2').setHtml('---<br/>--');
					Ext.getCmp('yes-dss-selection-container').setLoading(false);
					me.validateImageOL(obj);			
				},
				
				failure: function(respose, opts) {
					alert("Query failed, request timed out?");
					me.processing = false;
					Ext.getCmp('yes-dss-selection-container').setLoading(false);
					Ext.getCmp('dss-selection-loading').setVisible(false);
				}
			});
		},

		showOcclusion: function(firstQueries, secondQuery) {
			var me = this;
			Ext.getCmp('dss-selection-loading').setVisible(true);
			var obj = Ext.Ajax.request({
				url: location.href + '/showOcclusion',
				jsonData: {
					first:  firstQueries,
					second: secondQuery
				},
				timeout: 25000, // in milliseconds
				
				success: function(response, opts) {
					var obj = JSON.parse(response.responseText);
					// TODO: server should pass this all back...
					obj['bounds']= [
						-10062652.65061, 5278060.469521415,
						-9878152.65061, 5415259.640662575
					]
					if (obj.selectedPixelsFirst) {
						var diff = obj.selectedPixelsSecond - obj.occludedSecondPixels;
						// convert from km sqr to acres (I know, wasted step, just go from 30x30 meters to acres)
						var area = ((diff * 30.0 * 30.0) / 1000000.0) * 247.105;
						var totalAreaPerc = (diff / obj.totalPixels) * 100.0;
						
						var occArea = ((obj.occludedSecondPixels * 30.0 * 30.0) / 1000000.0) * 247.105;
						var totalOccPerc = obj.occludedSecondPixels / obj.selectedPixelsSecond * 100.0;
						
						var area = Ext.util.Format.number(area, '0,000.#'), 
							perc = Ext.util.Format.number(totalAreaPerc, '0.###');
						Ext.getCmp('yes-dss-selected-stats').setHtml(area + ' acres<br/>' + perc + '%');
						
						area = Ext.util.Format.number(occArea, '0,000.#'), 
						perc = Ext.util.Format.number(totalOccPerc, '0.###');
						Ext.getCmp('yes-dss-selected-stats2').setHtml(area + ' acres<br/>' + perc + '%');
					}
					else {
						// convert from km sqr to acres (I know, wasted step, just go from 30x30 meters to acres)
						var area = ((obj.selectedPixels * 30.0 * 30.0) / 1000000.0) * 247.105;
						var totalAreaPerc = (obj.selectedPixels / obj.totalPixels) * 100.0;
						var area = Ext.util.Format.number(area, '0,000.#'), 
							perc = Ext.util.Format.number(totalAreaPerc, '0.###');
						Ext.getCmp('yes-dss-selected-stats').setHtml(area + ' acres<br/>' + perc + '%');
						Ext.getCmp('yes-dss-selected-stats2').setHtml('---<br/>--');
					}
					
					Ext.getCmp('yes-dss-selection-container').setLoading(false);
					me.validateImageOL(obj);			
				},
				
				failure: function(respose, opts) {
					alert("Query failed, request timed out?");
					me.processing = false;
					Ext.getCmp('yes-dss-selection-container').setLoading(false);
					Ext.getCmp('dss-selection-loading').setVisible(false);
				}
			});
		},
		
		validateImage: function(json) {
			var me = this;
			tryCount = (typeof tryCount !== 'undefined') ? tryCount : 0;
			
			Ext.defer(function() {
					
				var tester = new Image();
				tester.onload = function() {
					me.updateSelection(json);
				};
				tester.onerror = function() {
					tryCount++;
					if (tryCount < 20) {
						me.validateImage(json, tryCount);
					}
					else {
						//failed
						me.processing = false;
						Ext.getCmp('dss-selection-loading').setVisible(false);
					}
				};
				
				tester.src = json.url;
				
			}, 50 + tryCount * 50, me); //  
		},
		//---------------------------------------------------------------------------------
		updateSelection: function(json) {
			
			var me = this;
			selectionLayer.setSource(
				new ol.source.ImageStatic({
					url: json.url,
					crossOrigin: '',
					imageExtent: json.bounds
				})
			);
			me.processing = false;
			Ext.getCmp('dss-selection-loading').setVisible(false);
		},
		//---------------------------------------------------------------------------------
		validateImageOL: function(json) {
			var me = this;
			tryCount = (typeof tryCount !== 'undefined') ? tryCount : 0;
			
			Ext.defer(function() {
					
				var src = new ol.source.ImageStatic({
					url: json.url,
					crossOrigin: '',
					imageExtent: json.bounds
				});			
				src.on('imageloadend', function() { // IMAGELOADEND: 'imageloadend',

					selectionLayer.setSource(src);
					me.processing = false;
					Ext.getCmp('dss-selection-loading').setVisible(false);
				});
				src.on('imageloaderror', function() { // IMAGELOADERROR: 'imageloaderror'
					tryCount++;
					if (tryCount < 20) {
						me.validateImageOL(json, tryCount);
					}
					else {
						//failed
						me.processing = false;
						Ext.getCmp('dss-selection-loading').setVisible(false);
					}
				});
				src.M.load(); // EVIL internal diggings...M is the secret internal ol.Image
			}, 50 + tryCount * 50, me); //  
		},
		//---------------------------------------------------------------------------------
		cancelClickActionsForAllBut: function(layer) {
			var me = this;
			Ext.each(me.layers, function(test) {
				if (layer.getId() != test.getId()) {
					if (test.cancelClickSelection) {
						test.cancelClickSelection();
					}
				} else {
				}
			})
		}
	},
	
	listeners: {
		beforeexpand: function() {
			var me = this;
			me.getHeader().addCls('dss-no-header-border');
			if (me.expandInternal) me.expandInternal();
		},
		expand: function() {
			DSS.Layers.valueChanged();
		},
		beforecollapse: function() {
			var me = this;
			me.getHeader().removeCls('dss-no-header-border');
			if (me.collapseInternal) me.collapseInternal();
		},
		collapse: function() {
			DSS.Layers.valueChanged();
		}
	},
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
		});
		
		me.callParent(arguments);
	},

});
