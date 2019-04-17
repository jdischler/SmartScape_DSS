
//------------------------------------------------------------------------------
Ext.define('DSS.view.PortalViewport', {
//------------------------------------------------------------------------------
	extend: 'Ext.container.Viewport',
    renderTo: Ext.getBody(),
	
	requires: [
		'DSS.app_portal.Footer',
		'DSS.app_portal.AOI_Map',
		'DSS.app_portal.AOI',
		'DSS.app_portal.AOI_Refinement',
		'DSS.app_portal.LaunchSummary',
		'DSS.app_portal.Assumptions',
		'DSS.components.d3_nav'
	],

	// most desktops/tablets support 1024x768 but Nexus 7 (2013) is a bit smaller so target that if at all possible
	minWidth: 750,
	minHeight: 720,
	style: "background: #F0F2F0;background: -webkit-linear-gradient(to top, #a1b3a1, #ddc, #edefea) fixed; background: linear-gradient(to top, #a1b3a1, #ddc,  #edefea) fixed;",

	autoScroll: true,
	layout: {
		type: 'vbox',
		align: 'middle'
	},
	defaults: {
		xtype: 'container',
	},
	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'component',
				width: 310, height: 70,
				margin: '16 0 8 0',
				html: '<a href="/assets/wip/landing_bs.html"><img src="assets/images/dss_logo.png" style="width:100%"></a>',
			},{
				xtype: 'd3_nav',
				itemId: 'dss-navigator',
				width: 720, height: 68,
				DSS_elements: [{
					text: 'Select',
					active: true,
					activeText: 'Select an Area of Interest',
					tooltip: 'Select an area of interest',
					DSS_selectionChanged: function(selected) {
						Ext.getCmp('dss-region-grid').updateState(selected);
						if (selected) {
							DSS_PortalMap.setMode('region');
							Ext.getCmp('dss-launch-summary').animate({duration: 750, to: { left: 760 }});
							Ext.getCmp('dss-region-refinement').animate({duration: 750, to: { left: 380 }});
							Ext.getCmp('dss-region-grid').animate({
								duration: 750, to: { left: 0 }
							});
						}
					}
				},{
					text: 'Refine',
					disabled: true,
					activeText: 'Refine Area of Interest (optional)',
					tooltip: 'Optionally refine the area of interest by choosing a county or a watershed',
					disabledTooltip: 'Select an Area of Interest to proceed',
					DSS_selectionChanged: function(selected) {
						Ext.getCmp('dss-region-refinement').updateState(selected);
						if (selected) {
							DSS_PortalMap.setMode('refine');
							Ext.getCmp('dss-launch-summary').animate({duration: 750, to: { left: 380 }});
							Ext.getCmp('dss-region-grid').animate({duration: 750, to: { left: -380 }});
							Ext.getCmp('dss-region-refinement').animate({
								duration: 750, to: { left: 0 }
							});
						}
					}
				},{
					text: 'Values',
					disabled: true,
					activeText: 'Set Personal Values (optional)',
					tooltip: 'Tell SmartScape about your values if desired',
					disabledTooltip: 'Select an Area of Interest to proceed',
					DSS_selectionChanged: function(selected) {
						var item = me['DSS_Assumptions'];
						if (!item) {
							item = me['DSS_Assumptions'] = Ext.create('DSS.app_portal.Assumptions');
						}
						if (selected) {
							Ext.defer(function() {
								item.setHeight(1);
								item.animate({
									duration: 750,
									dynamic: true,
									to: {
										height: 520
									}
								}).show().anchorTo(me.down('#dss-navigator'), 'tc-bc', [0,8])
							}, 50);
						}
						else {
							item.setHidden(true);
						}
					}
				},{
					text: 'Start',
					disabled: true,
					activeText: 'Start SmartScape',
					tooltip: 'Start the SmartScape application with the chosen Area of Interest',
					disabledTooltip: 'Select an Area of Interest to proceed',
					DSS_selectionChanged: function(selected) {
						if (selected) {
							DSS_PortalMap.setMode('refine');
							var cmp = Ext.getCmp('dss-region-grid');
							if (cmp.getX() != -380) {
								cmp.animate({duration: 750, to: { left: -760 }});
							}
							cmp = Ext.getCmp('dss-region-refinement');
							if (cmp.getX() != -380) {
								cmp.animate({duration: 750, to: { left: -380 }});
							}
							Ext.getCmp('dss-launch-summary').animate({
								duration: 750, to: { left: 0 }
							});
						}
					}
				}],
			},{
				flex: 20,
				margin: '0 8 8 8',
				maxHeight: 520,
				maxWidth: 760,
				width: '100%',
				layout: {
					type: 'hbox',
					pack: 'middle',
					align: 'stretch',
				},
				items: [{
					xtype: 'container',
					margin: '0 8 0 0',
					width: 380,
					layout: {
						type: 'vbox',
						pack: 'start',
						align: 'stretch'
					},
					items: [{
						xtype: 'container',
					//	style: 'border: 1px solid #ccc; border-radius: 2px;',// background-color: #fff',
						flex: 1,
						layout: {
							type: 'vbox',
							align: 'stretch',
							pack: 'middle',
						},
						items: [{
							xtype: 'container',
							margin: '0 0 4 0',
							id: 'dss-action-holder',
						//	style: 'overflow: hidden',
							layout: 'absolute',
							style: 'background: rgba(255,255,255,0.5); border-bottom-left-radius: 12px;border-bottom-right-radius: 12px',
							minHeight: 200, maxHeight: 200,
							maxWidth: 380,
							defaults: {
								bodyStyle: 'background: transparent',
								border: 0
							},
							items: [{
								id: 'dss-region-grid',
								xtype: 'aoi',
								onActivated: function() {
									me.down('#dss-stats').setVisible(true);
									me.down('#dss-navigator').enableAll();
								},
								maxWidth: 380,
								x: 0,
								y: 0
							},{
								id: 'dss-region-refinement',
								xtype: 'aoi_refinement',
								maxWidth: 380,
								x: 380,
								y: 0
							},{
								id: 'dss-launch-summary',
								xtype: 'launch_summary',
								maxWidth: 380,
								x: 760,
								y: 0
							}]
						},{
							xtype: 'checkbox',
							hidden: true,
							id: 'dss-portal-auto-zoom',
							margin: '4 16',
							boxLabel: 'Map Auto-Zoom + Auto-Pan' ,
							checked: true,
						},{
							xtype: 'aoi_map',
							id: 'dss-portal-map',
							flex: 1
						}]
					}]
				},{
					xtype: 'container',
					itemId: 'dss-stats',
					hidden: true,
					flex: 1,
					layout: {
						type: 'vbox',
						align: 'stretch',
						pack: 'start'
					},
					items: [pieDef, radarDef]
				}]
			},{
				flex: 1,
			},{
				xtype: 'footer'
			}]
		});
		
		me.callParent(arguments);
		me['dss-help-tool'] = Ext.create('Ext.panel.Tool', {
			type: 'help',
			floating: true,
			shadow: false,
			callback: function() {
				Ext.defer(me.doFakeArrow, 200);
			}
		}).show().anchorTo(me, 'tr-tr', [-16,16]);
	},
	
	doFakeArrow: function() {
		var img = Ext.create('Ext.Img', {
			src: 'assets/images/focus-arrow-icon.png',
			floating: true,
			shadow: false,
			style:'opacity:0',
			width: 32,
			height: 32,
		}).showBy(Ext.getCmp('dss-step-1'), 'r-tl', [16,18]); //dss-action-holder')
		
		img.animate({
			duration: 1000,
			from: {
				x: img.getX() - 32,
			},
			to: {
				x: img.getX(),
				opacity: 1
			}
		})
		
		Ext.defer(function() {
			img.animate({
				duration: 1000,
				to: {
					x: Ext.getCmp('dss-action-holder').getX() - 24,
					y: Ext.getCmp('dss-action-holder').getY()
				}
			});
			
		}, 1500);
		Ext.defer(function() {
			img.animate({
				duration: 1000,
				to: {
					y: Ext.getCmp('dss-action-holder').getY() + 110
				}
			});
			
		}, 3000);
		Ext.defer(function() {
			img.animate({
				duration: 1000,
				to: {
					opacity: 0,
					x: img.getX() - 32
				}
			});
			
		}, 5000);
	}
});

