Ext.Loader.setConfig({
	enabled: true,
	paths: {
		'GeoExt': '/assets/javascripts/vendor/geo-ext'
	}
});

Ext.require([
    'GeoExt.component.Map',
    'Ext.panel.Panel',
    'Ext.Viewport'
]);

var DSS_EmptySelectionName = 'Descriptive Name', 
	DSS_EmptyTransformText = 'Click to Set Crop';

var DSS_tempColumns = {
	items:[{
		dataIndex: 'SelectionName',
		text: 'Transform Name',
		width: 150,
		resizable: false,
		editor: {
			xtype: 'textfield',
			allowBlank: false
		},
		renderer : function(value, meta, record) {
			if (value) {
				meta.style = "color: #000";
				return value;
			} else {
				meta.style = "color: RGBA(0,0,0,0.3)";
				return DSS_EmptySelectionName;
			}
		},
		tdCls: 'dss-grey-scenario-grid'
	},
	{
		dataIndex: 'TransformText',
		text: 'Transforms',
		width: 150,
		resizable: false,
		renderer: function(value, meta, record) {
			if (!value) {
				meta.style = 'color: red';
				return DSS_EmptyTransformText;
			}
			meta.tdAttr = 'data-qtip="' + record.get("ManagementText") + '"';
			return value;
		}
	},
	{
		xtype: 'checkcolumn',
		dataIndex: 'Active',
		text: 'Active',
		width: 64,
		resizable: false,
	},
	{
		xtype: 'actioncolumn',
		width: 23,
		resizable: false,
		tooltip: 'Remove this transform',
		handler: function(grid, rowIndex, colIndex) {
			Ext.Msg.show({
				 title: 'Confirm Transform Delete',
				 msg: 'Are you sure you want to delete this transform?',
				 buttons: Ext.Msg.YESNO,
				 icon: Ext.Msg.QUESTION,
				 fn: function(btn) {
					 if (btn == 'yes') {
						var record = grid.getStore().getAt(rowIndex);
						grid.getStore().remove(record);
						record.commit();
						var selModel = grid.getSelectionModel();
						if (selModel.selected.getCount() < 1) {
							selModel.select(0);
						}
					 }
				 }
			});
		}
	}]
};

//------------------------------------------------------------------------------
Ext.create('Ext.data.Store', {
	
	storeId: 'DSS_ScenarioStore',
    fields: ['Active', 'SelectionName', 'TransformText', 'ManagementText', 'Transform', 'Query'],
    data: {
        items: [{
			Active: true, 
			SelectionName: 'Row Crops',//'Double Click to Set Custom Name', 
			TransformText: 'Corn & Soy',//'Double Click to Set Crop',
			ManagementText: '',
			Transform: { LandUse: 1, Options: undefined },
			Query: {}
		},{
			Active: false, 
			SelectionName: null,//'Double Click to Set Custom Name', 
			TransformText: null,//'Double Click to Set Crop',
			ManagementText: '',
			Transform: { LandUse: 1, Options: undefined },
			Query: {}
		}]
    },
    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
            root: 'items'
        }
    },
    listeners: {
    	// blah, just force the commit to happen, no reason not to save it right away IMHO
    	update: function(store, record, operation, eOps) {
    		if (operation == Ext.data.Model.EDIT) {
    			store.commitChanges();
    		}
    	}
    }
});

Ext.define('Ext.layout.container.Accordion2', {
    extend: 'Ext.layout.container.Accordion',
    alias: 'layout.accordion2',
    type: 'accordion2',
 
    alternateClassName: 'Ext.layout.AccordionLayout2',
 
 	allowCollapseAll: true,

    updatePanelClasses: function(ownerContext) {
        var children = ownerContext.visibleItems,
            ln = children.length,
            siblingCollapsed = true,
            i, child, header;
 
        for (i = 0; i < ln; i++) {
            child = children[i];
            header = child.header;
            header.addCls(Ext.baseCSSPrefix + 'accordion-hd');
 
            if (siblingCollapsed) {
                header.removeCls(Ext.baseCSSPrefix + 'accordion-hd-sibling-expanded');
            } else {
                header.addCls(Ext.baseCSSPrefix + 'accordion-hd-sibling-expanded');
            }
 
            if (i + 1 === ln && child.collapsed) {
                header.addCls(Ext.baseCSSPrefix + 'accordion-hd-last-collapsed');
            } else {
                header.removeCls(Ext.baseCSSPrefix + 'accordion-hd-last-collapsed');
            }
 
            siblingCollapsed = child.collapsed;
        }
    },
    
    onBeforeComponentCollapse: function(comp) {
        var me = this,
            owner = me.owner,
            toExpand,
            expanded,
            previousValue;
 
        if (me.owner.items.getCount() === 1) {
            // do not allow collapse if there is only one item 
            return false;
        }
 
        if (!me.processing) {
            me.processing = true;
            previousValue = owner.deferLayouts;
            owner.deferLayouts = true;
            toExpand = comp.next() || comp.prev();
 
            // If we are allowing multi, and the "toCollapse" component is NOT the only expanded Component, 
            // then ask the box layout to collapse it to its header. 
            if (me.multi) {
                expanded = me.getExpanded();
 
                // If the collapsing Panel is the only expanded one, expand the following Component. 
                // All this is handling fill: true, so there must be at least one expanded, 
                if (expanded.length === 1 && !me.allowCollapseAll) {
                    toExpand.expand();
                }
 
            } else if (toExpand) {
                toExpand.expand();
            }
            owner.deferLayouts = previousValue;
            me.processing = false;
        }
    }
 
});

Ext.define('Ext.chart.theme.Custom', {
    extend: 'Ext.chart.theme.Base',
    singleton: true,
    alias: 'chart.theme.custom',
    config: {
		legend: {
			label: {
				fontSize: 13,
				fontWeight: 'default',
				fontFamily: 'default',
				fillStyle: 'black'
			}
		}
    }
});		

Ext.application({
	name: 'DSS',
	
	init: function() {
		Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
	},	
	launch: function() {
		var normalLogoHeight = 70;
		var olMap = new ol.Map({
			layers: [
				new ol.layer.Tile({
					source: new ol.source.Stamen({
						layer: 'terrain-background' // terrain/ terrain-labels
					})
				}),
				new ol.layer.Tile({
					source: new ol.source.Stamen({
						layer: 'terrain-lines'
					})
				})
			],
			view: new ol.View({
				center: ol.proj.fromLonLat([-89.4, 43.5]),
				zoom: 9
			})
		});
		
		// Sample Radar
		var radarDef = {
			xtype: 'polar',
			itemId: 'DSS-gurf',
			theme: 'custom',
			innerPadding: 20,
			insetPadding: {
				top: 0,
				left: 50,
				right: 50,
				bottom: 0
			},
			animation: {
				duration: 50
			},
			legend: {
				type: 'sprite', // 'sprite' is another possible value
				docked: 'bottom'
			},
			store: {
				fields: ['name', 'data1', 'data2','data3'],
				data: [{
					name: 'Net Income',data1: 90.1,data2: 30.8,data3: 100
				},{
					name: 'Gross Biofuel',data1: 80.5,data2: 20.1,data3: 75.1
				},{
					name: 'Emissions',data1: 90.7,data2: 30.3,data3: 80.1
				},{
					name: 'Soil Retention',data1: 30.1,data2: 98.2,data3: 68.0
				},{
					name: 'Soil Carbon',data1: 20.1,data2: 90.4,data3: 45.0
				},{
					name: 'Bird Habitat',data1: 10.9,data2: 80.2,data3: 96.0
				},{
					name: 'Pest Supression',data1: 15.6,data2: 70.8,data3: 100
				},{
					name: 'Pollinators',data1: 30.4,data2: 86.2,data3: 100
				}]
			},
			series: [{
				type: 'radar',
				title: 'Default',
				angleField: 'name',
				radiusField: 'data1',
				marker: {radius: 4, fillOpacity: 0.7},
				highlight: {fillStyle: '#FFF',strokeStyle: '#000'},
				tooltip: {
					trackMouse: false,
					renderer: function(toolTip, record, ctx) {
						toolTip.setHtml(record.get('name') + ': ' + record.get('data1'));
					}
				},			
				style: {fillOpacity: .3}				
			},{
				type: 'radar',
				title: 'Scenario1',
				angleField: 'name',
				radiusField: 'data2',
				marker: {radius: 4, fillOpacity: 0.7},
				highlight: {fillStyle: '#FFF',strokeStyle: '#000'},
				tooltip: {
					trackMouse: false,
					renderer: function(toolTip, record, ctx) {
						toolTip.setHtml(record.get('name') + ': ' + record.get('data2'));
					}
				},			
				style: {fillOpacity: .3}				
			}],
			axes: [{
				type: 'numeric',
				position: 'radial',
				fields: 'data1',
				style: {
					estStepSize: 10
				},
				minimum: 0,
				maximum: 100,
				grid: true
			}, {
				type: 'category',
				position: 'angular',
				fields: 'name',
				style: {
					estStepSize: 1,
					strokeStyle: 'rgba(0,0,0,0)'
				},
				grid: true
			}]
		};
		
		var landAttrs = [{
			title: 'Landcover Type'
		},{
			title: 'Slope'
		},{
			title: 'Distance to Water'
		},{
			title: 'Land Capability Class'
		},{
			title: 'Watershed'
		},{
			title: 'Subset of Land'
		}];
		
		// animate defs
		var closed = {to:{height:0}};		
		var open = {to:{height:normalLogoHeight}};
		Ext.create('Ext.Viewport', {
			layout: 'border',
			// minimum widest dimension I could find for tablets/desktop (nexus 7 - 2013)
			//	though most tablets/desktops are at least 1024
			minWidth: 960,
			// min shortest dimension for tablets/desktop (again, nexus 7)
			//	most support at least 768...
			minHeight: 600,
			autoScroll: true,
			items: [{
				id: 'DSS_logo',
				xtype: 'container',
				region: 'north',
				style: 'background-color: #C7CDBA',
				height: normalLogoHeight,
				layout: {
					type: 'hbox',
					pack: 'start',
					align: 'bottom'
				},
				defaults: {
					xtype: 'button',
					margin: 'auto 3',
					scale: 'large',
					toggleGroup: 'DSS-mode',
					allowDepress: false
				},
				items: [{
					xtype: 'container',
					margin: '0 0 0 36',
					width: 310,
					html: '<a href="/nav"><img id="ddd" src="assets/images/dss_logo.png" style="width:100%"></a>',
					listeners: {
						afterrender: function(self) {
							Ext.defer(function() {
								self.updateLayout();
							}, 10);
						}	
					}
				},{
					text: 'Explore Landscape',
					margin: '0 2 0 48',
					width: 140,
					pressed: true,
					handler: function(self) {
						Ext.suspendLayouts();
							Ext.getCmp('DSS_ScenarioCreator').setHidden(true);
							Ext.getCmp('DSS_findLandByAttr').setCollapsed(false);
							Ext.getCmp('DSS_resultsPanel').setCollapsed(true);
						Ext.resumeLayouts(true);
					}
				},{
					text: 'Create Scenarios',
					width: 140,
					handler: function(self) {
						Ext.suspendLayouts();
							Ext.getCmp('DSS_ScenarioCreator').setHidden(false);
							Ext.getCmp('DSS_findLandByAttr').setCollapsed(false);
							Ext.getCmp('DSS_resultsPanel').setCollapsed(true);
						Ext.resumeLayouts(true);
					}
				},{
					id: 'DSS_analyzeButton',
					text: 'Analyze Results',
					width: 140,
					toggleHandler: function(self, state) {
						if (state) {
							Ext.suspendLayouts();
								Ext.getCmp('DSS_ScenarioCreator').setHidden(true);
								Ext.getCmp('DSS_findLandByAttr').setCollapsed(true);
								Ext.getCmp('DSS_resultsPanel').setCollapsed(false);
								Ext.getCmp('DSS_selectedStatistics').setHidden(true);
							Ext.resumeLayouts(true);
						}
					}
				},{
					xtype: 'container',
					flex: 1,
				},{
					id: 'DSS_LoginButton',
					text: 'Login',
					minWidth: 96
				},{
					id: 'DSS_ExtraButton',
					text: 'Admin',
					hidden: true
				}]
			},{
				id: 'DSS_findLandByAttr',
				xtype: 'panel',
				title: 'Find Land By Attributes',
				collapsible: 'true',
				scrollable: 'vertical',
				stateful: true,
				stateId: 'DSS_findLandByAttr',
				layout: {
					type: 'accordion2',
					fill: false,
					multi: true
				},
				region: 'west',
				bodyStyle: 'background-color: #bbb',
				width: 380,
				defaults: {
					xtype: 'panel',
					collapsed: true,
					height: 90,
					expandToolText: 'Find land by this attribute',
					collapseToolText: 'Remove this attribute',
					listeners: {
						collapse: function() {
							Ext.getCmp('DSS_selectedStatistics').setHidden(true);
						},
						expand: function() {
							Ext.getCmp('DSS_selectedStatistics').setHidden(false);
						}
					}
				},
				items: landAttrs,
				dockedItems: [{
					id: 'DSS_ScenarioCreator',
					hidden: true,
					xtype: 'panel',
					title: 'Scenario Creator',
					stateful: true,
					stateID: 'DSS_ScenarioCreator',
					minHeight: 140,
					maxHeight: 320,
					resizeHandles: 'n',
					resizable: true,
					dock: 'bottom',
					bodyPadding: 2,
					layout: {
						type: 'vbox',
						align: 'stretch', 
						pack: 'start'
					},
					items: [{
						xtype: 'grid',
						store: 'DSS_ScenarioStore',
						enableColumnHide: false,
						enableColumnMove: false,
						sortableColumns: false,
						//hideHeaders: true,
						columnLines: true,
						padding: 2,
						columns: DSS_tempColumns
					}],
					bbar: [{
						xtype: 'tbfill' 
					},{
						xtype: 'button', text: 'Run', width: 80,
						handler: function() {
							Ext.defer(function() {
								Ext.getCmp('DSS_analyzeButton').toggle(true);
							}, 1000);
							
						}
					}]
				}]
			},{
				id: 'DSS_resultsPanel',
				xtype: 'panel',
				title: 'Results',
				collapsible: 'true',
				collapsed: true,
				stateful: true,
				stateId: 'DSS_resultsPanel',
				layout: {
					type:'vbox',
					pack: 'start',
					align: 'stretch'
				},
				region: 'east',
				bodyStyle: 'background-color: #bbb; border-left-width: 0;',
				minWidth: 300,
				maxWidth: 500,
				resizeHandles: 'w',
				resizable: true,
				items: [
					radarDef
				],
				listeners: {
					resize: function(self, width, height, oldWidth, oldHeight, eOpts) {
						self.down('#DSS-gurf').setHeight(width);
					}
				}
			},{
				xtype: 'panel',
				header: {
					style: 'border-right: 1px solid #bcd; border-left: 1px solid #bcd;'
				},
				region: 'center',
				layout: 'fit',
				items: [{
					xtype: 'gx_map',
					map: olMap
				}],
				dockedItems: [{
					id: 'DSS_selectedStatistics',
					xtype: 'panel',
					title: 'Selected Land',
					hidden: true,
					height: 70,
					dock: 'bottom',
					bodyPadding: 4,
					layout: {
						type: 'hbox',
						align: 'stretch', 
						pack: 'start'
					},
					items: [{
						xtype: 'container',
						layout: 'vbox',
						items: [{
							xtype: 'textfield',
							labelWidth: 60,
							width: 160,
							labelAlign: 'right',
							fieldLabel: 'Selected',
							value: '368.1 ha'
						}],
						flex: 1,
						maxWidth: 200
					},{
						xtype: 'container',
						layout: 'vbox',
						items: [{
							xtype: 'textfield',
							labelAlign: 'right',
							width: 160,
							fieldLabel: '% of Study Area',
							value: '13.2%'
						}],
						flex: 1,
						maxWidth: 200
					}]
				}]
			}]
		});
		
	}
	
});
