
//------------------------------------------------------------------------------
Ext.define('DSS.view.PortalViewport', {
//------------------------------------------------------------------------------
	extend: 'Ext.container.Viewport',
    renderTo: Ext.getBody(),
	
	requires: [
		'DSS.app_portal.Footer',
		'DSS.app_portal.AOI_Map',
		'DSS.app_portal.AOI',
		'DSS.app_portal.AOI_Refinement'
	],

	// most desktops/tablets support 1024x768 but Nexus 7 (2013) is a bit smaller so target that if at all possible
	minWidth: 750,
	minHeight: 720,
	style: "background: #F0F2F0;background: -webkit-linear-gradient(to top, #afbfaf, #ddc, #edefea) fixed; background: linear-gradient(to top, #afbfaf, #ddc,  #edefea) fixed;",

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
				width: 310, height: 70,
				margin: '16 0',
				html: '<a href="/assets/wip/landing_bs.html"><img src="assets/images/dss_logo.png" style="width:100%"></a>',
			},{
				xtype: 'container',
				layout: 'hbox',
				style: 'background: #fff; border-radius: 12px;',
				padding: 2,
				defaults: {
					xtype: 'container',
					height: 40,
					margin: 4,
					padding: '12 16',
					DSS_verbWidth: 74,
					width: 74,
					margin: '0 15',
			        listeners: {
			            element: 'el',
			            click: function(el) {
			            	var me = this;
			            	var up = me.up().component.items;
			            	Ext.suspendLayouts();
			            	Ext.each(up.items, function(els) {
			            		if (els == me.component) return true; // skip self
			            		els.removeCls('dss-breadcrumb-active');
			            		els.animate({
			            			dynamic: true,
			            			duration: 750,
			            			to: {
			            				width: els.DSS_verbWidth
			            			}
			            		});
			            		els.setHtml(els.DSS_verb);
								els.DSS_selectionChanged(false);
			            	})
			                me.component.addCls('dss-breadcrumb-active');
			            	me.component.stopAnimation().animate({
		            			dynamic: true,
		            			duration: 750,
			            		callback: function() {
					            	me.component.setHtml(me.component.DSS_fullText);
			            		},
		            			to: {
		            				width: me.component.DSS_fullWidth
		            			}
		            		});
			            	
			            	me.component.setHtml(me.component.DSS_fullText);
			            	me.component.DSS_selectionChanged(true);
							
			            	// awful...ensure the innerCt which holds the text just clips off any overflow...
			            	var downer = me.component.getEl().down('[data-ref="innerCt"]');
			            	downer.dom.style['overflow'] = 'hidden';
			            	Ext.resumeLayouts(true);
			            }
			        }
				},
				items: [{
					html: 'Select Area of Interest',
					id: 'dss-step-1',
					DSS_fullText: 'Select Area of Interest',
					DSS_verb: 'Select',
					width: 205,
					DSS_fullWidth: 205,
					margin: '0 15 0 0',
					cls: 'dss-breadcrumb-active dss-breadcrumb-point',
					style: 'border-bottom-left-radius: 12px; border-top-left-radius: 12px',
					DSS_selectionChanged: function(selected) {
						Ext.getCmp('dss-region-grid').updateState(selected);
						if (selected) {
							DSS_PortalMap.setMode('region');
							Ext.getCmp('dss-region-grid').animate({
								duration: 750, to: { left: 0 }
							});
							Ext.getCmp('dss-region-refinement').animate({
								duration: 750, to: { left: 380 }
							});
						}
					}
				},{
					html: 'Refine',
					DSS_fullText: 'Refine Area of Interest (Optional)',
					DSS_verb: 'Refine',
					DSS_fullWidth: 290,
					cls: 'dss-breadcrumb-point dss-breadcrumb-tail',
					DSS_selectionChanged: function(selected) {
						Ext.getCmp('dss-region-refinement').updateState(selected);
						if (selected) {
							DSS_PortalMap.setMode('refine');
							Ext.getCmp('dss-region-grid').animate({
								duration: 750, to: { left: -380 }
							});
							Ext.getCmp('dss-region-refinement').animate({
								duration: 750, to: { left: 0 }
							});
						}
					}
				},{
					html: 'Review',
					DSS_fullText: 'Review Assumptions (Optional)',
					DSS_verb: 'Review',
					DSS_fullWidth: 260,
					cls: 'dss-breadcrumb-point dss-breadcrumb-tail',
					DSS_selectionChanged: function(selected) {
					}
				},{
					html: 'Start',
					DSS_fullText: 'Start SmartScape',
					DSS_verb: 'Start',
					margin: '0 0 0 15',
					DSS_fullWidth: 170,
					cls: 'dss-breadcrumb-tail',
					style: 'border-top-right-radius: 12px; border-bottom-right-radius: 12px',
					DSS_selectionChanged: function(selected) {
					}
				}]
			},{
				flex: 20,
				margin: 8,
				maxHeight: 520,
				maxWidth: 760,
				width: '100%',
				layout: {
					type: 'hbox',
					pack: 'start',
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
							pack: 'start',
						},
						items: [{
							xtype: 'container',
							id: 'dss-action-holder',
						//	style: 'overflow: hidden',
							layout: 'absolute',
							height: 140,
							maxWidth: 380,
							items: [{
								id: 'dss-region-grid',
								xtype: 'aoi',
								maxWidth: 380,
								x: 0,
								y: 0
							},{
								id: 'dss-region-refinement',
								xtype: 'aoi_refinement',
								maxWidth: 380,
								x: 380,
								y: 0
							}]
						},{
							xtype: 'container',
							layout: {
								type: 'hbox',
								pack: 'end',
								align: 'middle'
							},
							items: [{
								xtype: 'checkbox',
								id: 'dss-portal-auto-zoom',
								margin: '4 16',
								boxLabel: 'Map Auto-Zoom + Auto-Pan' ,
								checked: true,
								/*stateful: true,
								stateId: 'dss-portal-auto-zoom',
								applyState: function(state) {
									console.log(this);
									var me = this;
									if (state && state['autoZoom']) {
										me.setValue(state['autoZoom']);
									}
								},
								getState: function() {
									console.log(this);
									var res = {};
									res['autoZoom'] = this.getValue();
									console.log(res);
									return res;
								}*/
							},{
								xtype: 'button',
								scale: 'small',
								disabled: true,
								width: 72,
								margin: '1 16',
								text: 'Next >'
							}]
						},{
							xtype: 'gx_map',
							id: 'dss-portal-map',
							flex: 1,
							map: globalMap,
							animate: false,
						},{
							xtype: 'aoi_map',
							flex: 1,
						}]
					}]
				},{
					xtype: 'container',
					flex: 1,
					layout: {
						type: 'vbox',
						align: 'stretch',
						pack: 'start'
					},
					items: [radarDef, pieDef]
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
		}).showBy(me, 'tr-tr', [-16,16]);
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

