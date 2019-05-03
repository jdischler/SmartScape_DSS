var DSS_viewport = false;

//------------------------------------------------------------------------------
Ext.define('DSS.view.AppViewport', {
//------------------------------------------------------------------------------
	extend: 'Ext.container.Viewport',
	
	requires: [
	    'DSS.components.MainMap', // likes to be first...
	    'DSS.components.d3_nav', // this also likes to be here even if not used directly in this object
	    'DSS.components.LogoBar',
	    'DSS.components.AttributeBrowser',
	    'DSS.components.ScenarioManager',
	    'DSS.components.SelectionStatistics',
	    'DSS.components.AnalyzeResults',
	    'DSS.components.d3_gradedRadar',
	    'DSS.components.d3_pie',
	    'DSS.components.d3_portalStatistics'
	],

	minWidth: 640,
	minHeight: 480,
	style: 'background-color: rgb(198,208,168)',
	
	scrollable: false,
    renderTo: Ext.getBody(),
	layout: 'fit',

	//--------------------------------------------------------------------------
	enableNavBar: function() {
		var me = this;
		me['DSS-logo-bar'].enableNavBar()
	},
	//--------------------------------------------------------------------------
	updateLayerBrowser: function(query, selection_name) {
		var me = this;
		me['DSS-layer-browser'].configureFromQuery(query);
	},
	
	//--------------------------------------------------------------------------
	getLayerBrowserQuery: function() {
		var me = this;
		return me['DSS-layer-browser'].getCurrentQuery();
	},
	
	//--------------------------------------------------------------------------
	positionStatistics: function(andShow) {
		var me = this;
		var statisticsPane = me['DSS-selection-statistics'];
		
//		if (!statisticsPane || typeof statisticsPane.DSS_active === 'undefined') return;
		
		if (andShow) statisticsPane.DSS_active = true;
		if (!statisticsPane.DSS_active) return;
		
		var atY = statisticsPane.getY();
		var desiredY = me.getHeight() - statisticsPane.getHeight() - 8;
		
		if (me['DSS-scenario-manager'].DSS_active) {
			desiredY = me['DSS-scenario-manager'].getY() - statisticsPane.getHeight() - 8;
		}
		var atX = statisticsPane.getX();
		
		// if started offscreen, move it to the correct position to swoop in from the left
		if (atX < 0) {
			atX = -(statisticsPane.getWidth());
			atY = desiredY;
		}
		
		if (atX == 8 && Math.abs(atY - desiredY) < 0.1) return;
		
		statisticsPane.animate({
			duration: 500,
			from: {
				x: atX,
				y: atY
			},
			to: {
				y: desiredY,
				x: 8
			}
		})
	},
	
	//--------------------------------------------------------------------------
	updateScenarioManager: function(makeActive) {
		var me = this;
		var mgr = me['DSS-scenario-manager'];
		
		if (makeActive) {
			mgr.DSS_active = true;
			mgr.removeAnchor();
			me.positionStatistics();
			mgr.animate({
				duration: 500,
				to: {
					x: 8
				},
				callback: function() {
					mgr.anchorTo(DSS_viewport, 'bl-bl', [8,-8]);
				}
			})
		}
		else {
			mgr.DSS_active = false;
			mgr.removeAnchor();
			me.positionStatistics();
			mgr.animate({
				duration: 500,
				to: {
					x: -mgr.getWidth()
				},
				callback: function() {
					mgr.anchorTo(DSS_viewport, 'br-bl', [-8,-8]);
				}
			})
		}
	},

	//--------------------------------------------------------------------------
	positionAnalyzer: function(selected) {
		var me = this;
		var win = me['DSS-analyze-results'];
		
		if (selected) {
			win.removeAnchor();
			win.animate({
				duration: 500,
				from: {
					x: DSS_viewport.getWidth()
				},
				to: {
					x: (DSS_viewport.getWidth() - win.getWidth()) - 8
				},
				callback: function() {
				//	win.anchorTo(DSS_viewport, 'tr-tr'[-8,80]);
				}
			})
		}
		else {
			win.removeAnchor();
			win.animate({
				duration: 500,
				to: {
					x: DSS_viewport.getWidth()
				},
				callback: function() {
					win.anchorTo(DSS_viewport, 'tl-tr', [1024,80]);
				}
			})
		}
	},
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'mainmap'
			}]
		});
		
		me.callParent(arguments);
		DSS_viewport = me;
		
		me['DSS-logo-bar'] = Ext.create('DSS.components.LogoBar', {
			floating: true,
			shadow: false,
			x: 0, y: 0
		}).show();
		
		me['DSS-layer-browser'] = Ext.create('DSS.components.AttributeBrowser', {
			floating: true,
			shadow: false,
			x: 8, y: 80
		}).show();
		
		me['DSS-selection-statistics'] = Ext.create('DSS.components.SelectionStatistics', {
			floating: true,
			shadow: false,
			x: -388, y: 460
		}).show();
		
		me['DSS-scenario-manager'] = Ext.create('DSS.components.ScenarioManager', {
			floating: true,
			shadow: false,
		}).show().anchorTo(me, 'br-bl', [-8,-8]);
		
		me['DSS-analyze-results'] = Ext.create('DSS.components.AnalyzeResults', {
			floating: true,
			shadow: false,
		}).show().anchorTo(me, 'tl-tr', [1024,80]);
	},
		
});

