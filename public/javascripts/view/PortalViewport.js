
//------------------------------------------------------------------------------
Ext.define('DSS.view.PortalViewport', {
//------------------------------------------------------------------------------
	extend: 'Ext.container.Viewport',
    renderTo: Ext.getBody(),
	
	requires: [
		'DSS.app_portal.Footer',
		'DSS.app_portal.AOI'
	],

	// most desktops/tablets support 1024x768 but Nexus 7 (2013) is a bit smaller so target that if at all possible
	minWidth: 760,
	minHeight: 600,
	style: "background: #F0F2F0;background: -webkit-linear-gradient(to top, #afbfaf, #ddc, #eee) fixed; background: linear-gradient(to top, #afbfaf, #ddc,  #eee) fixed;",

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
			/*},{
				html: 'Choose an Area of Interest to Explore',
				margin: '24 8 0 8',
				style: 'color: #333; font-size: 18px; font-weight: bold'
			*/
			},{
				flex: 20,
				margin: 8,
				maxHeight: 520,
				maxWidth: 960,
				width: '100%',
				layout: {
					type: 'hbox',
					pack: 'start',
					align: 'stretch',
				},
				items: [{
					xtype: 'container',
					margin: '0 8 0 0',
					width: 440,
					layout: {
						type: 'vbox',
						pack: 'start',
						align: 'stretch'
					},
					items: [{
						xtype: 'container',
						style: 'border: 1px solid #ccc; border-radius: 2px;',// background-color: #fff',
						flex: 1,
						layout: {
							type: 'vbox',
							align: 'middle'
						},
						items: [{
							xtype: 'aoi',
							id: 'dss-area-grid',
						},{
							xtype: 'gx_map',
							width: '100%',
							flex: 2,
							map: globalMap,
							animate: false,
							style: 'background-color: rgb(198,208,168)' // rgb(217,221,183)
						},{
							xtype: 'container',
							id: 'dss-description',
							hidden: true,
							padding: '8 16',
							width: '100%',
							height: 100,
							style: 'color: #777'
						},{
							xtype: 'panel',
							id: 'dss-refine-area',
							title: 'Refine Area of Interest (optional)',
							collapsible: true,
							collapsed: true,
							width: '100%',
							layout: {
								type: 'vbox',
								pack: 'center',
								align: 'stretch'
							},
							items: [{
								xtype: 'container',
								layout: 'hbox',
								padding: 4,
								items: [{
									xtype: 'container',
									html: 'Counties',
									width: 80,
									padding: '4 2',
									style: 'text-align: right'
								},{
									xtype: 'button',
									scale: 'small',
									text: 'Choose',
									width: 80,
									margin: '2 4'
								},{
									xtype: 'button',
									scale: 'small',
									text: 'Clear',
									width: 80,
									margin: '2 4'
								}]
							},{
								xtype: 'container',
								layout: 'hbox',
								padding: 4,
								items: [{
									xtype: 'container',
									html: 'Watersheds',
									width: 80,
									padding: '4 2',
									style: 'text-align: right'
								},{
									xtype: 'button',
									scale: 'small',
									text: 'Choose',
									width: 80,
									margin: '2 4'
								},{
									xtype: 'button',
									scale: 'small',
									text: 'Clear',
									width: 80,
									margin: '2 4'
								}]
							}]
						}]
					},{
						xtype: 'container',
						layout: {
							type: 'hbox',
							pack: 'end',
							align: 'stretch'
						},
						items: [{
							xtype: 'button',
							scale: 'medium',
							text: 'Back',
							margin: '4 6 0 0',
							width: 100
						},{
							xtype: 'button',
							scale: 'medium',
							text: 'Next',
							margin: '4 8 0 6',
							width: 100,
							handler: function() {
								location.href = '/app';
							}
							
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
		Ext.defer(me.doFakeArrow, 2000);
	},
	
	doFakeArrow: function() {
		var img = Ext.create('Ext.Img', {
			src: 'assets/images/focus-arrow-icon.png',
			floating: true,
			shadow: false,
			style:'opacity:0',
			width: 64,
			height: 64,
		}).showBy(Ext.getCmp('dss-area-grid'), 'r-tl', [16,18]);
		
		img.animate({
			from: {
				x: img.getX() - 32,
				//opacity: 0
			},
			to: {
				x: img.getX(),
				opacity: 1
			}
		})
		
		Ext.defer(function() {
			img.setStyle({opacity: 0});
			img.showBy(Ext.getCmp('dss-refine-area'), 'r-tl', [16,18]);
			img.animate({
				from: {
					x: img.getX() - 32,
					opacity: 0
				},
				to: {
					x: img.getX(),
					opacity: 1
				}
			})
		}, 6000);
	}
});

