/*
 * File: app/view/ScenarioTools.js
 */

var DSS_DefaultScenarioSetup = {
	Active: true, 
	SelectionName: null,//'Double Click to Set Custom Name', 
	TransformText: null,//'Double Click to Set Crop',
	ManagementText: '',
	Transform: { LandUse: 1, Options: undefined },
	Query: {}
};

var occlusionExample = [{
	Active: true, SelectionName: 'Row crops near open water', TransformText: 'C3 Grass',
	Query: [{
		"name":"cdl_2012","type":"indexed","matchValues":[1,16,2]
	},{
		"name":"dist_to_water","type":"continuous","lessThanTest":"<=","greaterThanTest":">=","lessThanValue":160
	}]
},{
	Active: true, SelectionName: 'Row crops on marginal soils', TransformText: 'C4 Grass',
	Query: [{
		"name":"cdl_2012","type":"indexed","matchValues":[1,16,2]
	},{
		"name":"lcc","type":"indexed","matchValues":[4,5,6,7,8]
	}]
},{
	Active: true, SelectionName: 'Row crops on steeper slopes', TransformText: 'C3 Grass',
	Query: [{
		"name":"cdl_2012","type":"indexed","matchValues":[1,16,2]
	},{
		"name":"slope","type":"continuous","lessThanTest":"<=","greaterThanTest":">=","greaterThanValue":5
	}]
},{
	Active: true, SelectionName: 'Row crops near public lands', TransformText: 'High Diversity Native Prairie',
	Query: [{
		"name":"cdl_2012","type":"indexed","matchValues":[1,16,2]
	},{
		"name":"public_land","type":"continuous","lessThanTest":"<=","greaterThanTest":">=","lessThanValue":1000
	}]
},{
	Active: true, SelectionName: 'Row crops on low slopes', TransformText: 'Keep (increase nutrient spreading',
	Query: [{
		"name":"cdl_2012","type":"indexed","matchValues":[1,16,2]
	},{
		"name":"slope","type":"continuous","lessThanTest":"<=","greaterThanTest":">=","lessThanValue":3
	}]
},{
	Active: true, SelectionName: 'All remaining row crops', TransformText: 'Alfalfa',
	Query: [{
		"name":"cdl_2012","type":"indexed","matchValues":[1,16,2]
	}]
}];

var DSS_EmptySelectionName = 'Double Click to Name Selection', 
	DSS_EmptyTransformText = 'Click to Choose a New Landcover Type';

//------------------------------------------------------------------------------
Ext.create('Ext.data.Store', {
	
	storeId: 'dss-scenario-store',
    fields: ['Active', 'SelectionName', 'TransformText', 'ManagementText', 'Transform', 'Query'],
    data: {
        items: occlusionExample
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
 

// Scenario Summary....
//------------------------------------------------------------------------------
Ext.define('DSS.components.ScenarioGrid', {
		
    extend: 'Ext.grid.Panel',
    alias: 'widget.scenario_grid',

    requires: [
    ],
  
	autoScroll: true,
    height: 220,
  	resizable: {minHeight:120, maxHeight: 362},
	resizeHandles: 'n',
    
	title: 'Transform the Landscape',
	viewConfig: {
		stripeRows: true
	},
    bodyStyle: {'background-color': '#fafcff'},
	
    store: 'dss-scenario-store',
    enableColumnHide: false,
    enableColumnMove: false,
    sortableColumns: false,
    columnLines: true,
    
	plugins: [
		Ext.create('Ext.grid.plugin.CellEditing', {
			clicksToEdit: 2,
			listeners: {
				edit: {
					fn: function(editor, e) {
						// no real need for validation, but if we don't commit the changes,
						//	changed fields will show a red triangle in the corner...
						e.record.commit();
						var dssLeftPanel = Ext.getCmp('DSS_LeftPanel');
						dssLeftPanel.up().DSS_SetTitle(e.record.get('SelectionName'));
					}
				}
			}
		})
	],
	viewConfig: {
		getRowClass: function(record, index) {
			var c = record.get('Active')
			if (c == false) {
				return 'dss-greyed';
			}
		},
		plugins: {
			ptype: 'gridviewdragdrop',
			dragText: 'Drag and drop transforms to reorder them'
		}
	},
	
	listeners: {
		cellclick: function(me, td, cellIndex, record, tr, rowIndex, e, eOpts) {
			
		/*	if (cellIndex == 3) {
				record.set('Active', !record.get('Active')); // Toggle active field
				record.commit();
			}
			else*/ 
			if (cellIndex == 1) {
				var rectOfClicked = e.target.getBoundingClientRect();
				me.up().showTransformPopup(me, rowIndex, rectOfClicked);
			}
		},
		beforedeselect: function(me, record, index, eOpts) {
			/*var query = DSS_ViewSelectToolbar.buildQuery()
			record.set('Query', query);
			record.commit();*/
		},
		select: function(me, record, index, eOpts) {
			/*var query = record.get('Query');
			DSS_ViewSelectToolbar.setUpSelectionFromQuery(query);
			var dssLeftPanel = Ext.getCmp('DSS_LeftPanel');
			var panel = dssLeftPanel.up();
			panel.DSS_SetTitle(record.get('SelectionName'), panel.getCollapsed());*/
		},
		viewready: function(me, eOpts ) {
			/*var query = DSS_ViewSelectToolbar.buildQuery()
			var record = me.getStore().getAt(0);
			record.set('Query', query);
			record.commit();
			*/
			//me.getSelectionModel().select(0);
	        var view = me.getView();
	        var t = Ext.create('Ext.tip.ToolTip', {
	            // The overall target element.
	            target: view.el,
	            // Each grid row causes its own separate show and hide.
	            delegate: view.cellSelector, // view.itemSelector seems to be set to table.x-grid-item
	            trackMouse: false,
	            defaultAlign: 'b50-t50',
	            anchor: true,
	            hideDelay: 25,
	            dismissDelay: 0,
	            showDelay: 0,
	            layout: 'vbox',
	            bodyPadding: 8,
	            items: [{
	            	xtype: 'container',
	            	margin: 2,
	            	html: 'Landcover Transformation',
	            	style: 'font-weight: bold; text-decoration: underline',
	            },{
	            	xtype: 'container',
	            	margin: '4 16',
	            	itemId: 'msg',
	            },{
	            	xtype: 'container',
	            	margin: 2,
	            	html: 'Management Practices',
	            	style: 'font-weight: bold; text-decoration: underline',
	            },{
	            	xtype: 'container',
	            	margin: '-8 2 2 -16',
	            	html: '<ul><li>50% synthetic fertilizer</li><li>50% Manure (fall spread)</li><li>Cover crop: alfalfa</li></ul>',
	            }],
	            // Render immediately so that tip.body can be referenced prior to the first show.
	            renderTo: Ext.getBody(),
	            listeners: {
	                // Change content dynamically depending on which element triggered the show.
	                beforeshow: function updateTipBody(tip) {
	                	console.log(tip.triggerElement);
	                	if (!Ext.fly(tip.triggerElement).hasCls('dss-trx-col'))
	                		return false;
	                	var tt = tip.down('#msg');
	                	tt.update('To: "' + view.getRecord(tip.triggerElement).get('TransformText') + '"');
	                }
	            }
	        });  
	        console.log(view.itemSelector)
	        console.log(t);
			
		}
	},
	//--------------------------------------------------------------------------
	columns: {
		items:[{
			xtype: 'actioncolumn',
			width: 28,
			resizable: false,
			iconCls: 'dss-inspect-icon',
			tooltip: 'View the effective selection for this tranform',
			handler: function(grid, rowIndex, colIndex) {
				var record = grid.getStore().getAt(rowIndex);
				grid.getSelectionModel().select([record]); // make record selected to make things less confusing IMO
				
				if (rowIndex > 0) {
					var queries1 = [];
					for (var i = 0; i < rowIndex; i++) {
						queries1.push({
							queryLayers: grid.getStore().getAt(i).get('Query')
						});
					}
					var query2 = grid.getStore().getAt(rowIndex).get('Query');
					DSS.Layers.showOcclusion(queries1, {
						queryLayers: query2
					});
				}
				else {
					var query2 = grid.getStore().getAt(rowIndex).get('Query');
					DSS.Layers.showOcclusion(null, {
						queryLayers: query2
					});
				}
/*				var record = grid.getStore().getAt(rowIndex);
				grid.getSelectionModel().select([record]); // make record selected to make things less confusing IMO
				var query = record.get('Query');
				if (query) {
					DSS.Layers.showOcclusion(null, query);
				}*/
			}
		},{
			dataIndex: 'SelectionName',
			text: 'User-Named Selection',
			flex: 1, 
			maxWidth: 280,
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
		},{
			dataIndex: 'TransformText',
			text: 'Transforms & Managment',
			flex: 1, 
			maxWidth: 280,
			resizable: false,
			tdCls: 'dss-grey-scenario-grid dss-trx-col',
			renderer: function(value, meta, record) {
				if (!value) {
					meta.style = 'color: red';
					return DSS_EmptyTransformText;
				}
			//	meta.tdAttr = 'data-qtip=" wow stuff' + record.get("ManagementText") + '"';
				return value;
			}
		},{
			xtype: 'checkcolumn',
			dataIndex: 'Active',
			text: 'Active',
			width: 60,
			resizable: false,
			tdCls: 'dss-grey-scenario-grid'
		},{
			xtype: 'actioncolumn',
			width: 28,
			resizable: false,
			iconCls: 'dss-delete-icon',
			tooltip: 'Remove this transformation step',
			handler: function(grid, rowIndex, colIndex) {
				Ext.Msg.show({
					 title: 'Confirm Deleting this Transformation Step',
					 msg: 'Are you sure you want to delete this transformation step?',
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
	},

	//--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
        });

        me.callParent(arguments);
    },

	//--------------------------------------------------------------------------
	showTransformPopup: function(grid,rowIndex, rectOfClicked) {
		
		var record = grid.getStore().getAt(rowIndex);
		var transform = record.get('Transform');
		var window = Ext.create('MyApp.view.TransformPopup', {
			DSS_TransformIn: transform,
			listeners: {
				beforedestroy: {
					fn: function(win) {
						if (win.DSS_Transform) {
							record.set('Transform', win.DSS_Transform.Config);
							record.set('TransformText', win.DSS_Transform.Text);
							record.set('ManagementText', win.DSS_Transform.Management);
							record.commit();
						}
					}
				}
			}});
		window.show();
		window.setPosition(rectOfClicked.left,
							(rectOfClicked.top - window.getSize().height),
							false);
	},
	
	//--------------------------------------------------------------------------
	prepareModelRequest: function() {
	
		var scCombo1 = Ext.getCmp('DSS_ScenarioCompareCombo_1').getValue();	
		var haveQuery = false;
		var requestData = {
			clientID: 1234, //temp
			modelRequestCount: this.DSS_modelTypes.length,
			compare1ID: scCombo1,//-1, // default
			assumptions: DSS_AssumptionsAdjustable.Assumptions,
			transforms: []
		};
		
		var clientID_cookie = Ext.util.Cookies.get('DSS_clientID');
		if (clientID_cookie) {
			requestData.clientID = clientID_cookie;
		}
		else {
			requestData.clientID = 'BadID';
			console.log('WARNING: no client id cookie was found...');
		}

		var saveID_cookie = Ext.util.Cookies.get('DSS_nextSaveID');
		if (saveID_cookie) {
			requestData.saveID = saveID_cookie;
		}
		else {
			requestData.saveID = 0;
			console.log('WARNING: no save id cookie was found...');
		}

		DSS_currentModelRunID = requestData.saveID;
		var record = DSS_ScenarioComparisonStore.findRecord('Index', DSS_currentModelRunID);
		if (record) {
			DSS_ScenarioComparisonStore.remove(record);
		}
		
		// Add the new record and select it in the combo box....
		DSS_ScenarioComparisonStore.add({'Index': DSS_currentModelRunID, 'ScenarioName': 'Unstored Scenario Result'});
		DSS_ScenarioComparisonStore.commitChanges(); // FIXME: this necessary?
		Ext.getCmp('DSS_ScenarioCompareCombo_2').setValue(DSS_currentModelRunID);

		
		var st = this.getStore();
		for (var idx = 0; idx < st.getCount(); idx++) {
			var rec = st.getAt(idx);
			
			if (rec.get('Active')) {
				var query = rec.get('Query');		
				if (query == null) {
					break;
				}
				
				var trx = rec.get('Transform');
				if (trx == null) {
					trx = DSS_DefaultScenarioSetup.Transform; // blurf, set to corn....
				}
				
				var transform = {
					queryLayers: query.queryLayers,
					config: trx
				};
				requestData.transforms.push(transform);
				haveQuery = true;
			}
		}
		
//		console.log(requestData);
		if (haveQuery) {
			this.createScenario(requestData);
		}
		else {
			alert("No query built - nothing to query");
		}
	},
	
    //--------------------------------------------------------------------------
	createScenario: function(requestData) {
		
		var button = Ext.getCmp('DSS_runModelButton');
		button.setIcon('app/images/spinner_16a.gif');
		button.setDisabled(true);
		
		var self = this;
		var obj = Ext.Ajax.request({
			url: location.href + '/createScenario',
			jsonData: requestData,
			timeout: 10 * 60 * 1000, // minutes * seconds * (i.e. converted to) milliseconds
			
			success: function(response, opts) {
				
				try {
					var obj= JSON.parse(response.responseText);
//					console.log("success: ");
//					console.log(obj);
					var newRequest = requestData;
					newRequest.scenarioID = obj.scenarioID;
					self.submitModel(newRequest);
				}
				catch(err) {
					console.log(err);
				}
			},
			
			failure: function(respose, opts) {
				button.setIcon('app/images/go_icon.png');
				button.setDisabled(false);
				alert("Model run failed, request timed out?");
			}
		});
	},

    //--------------------------------------------------------------------------
    submitModel: function(queryJson) {
    	
    	var me = this;
//		console.log(queryJson);
		var button = Ext.getCmp('DSS_runModelButton');
		
		// NOTE: these strings MUST be synchronized with the server, or else the server will
		//	not know which models to run. FIXME: should maybe set this up in a more robust fashion?? How?
		
		var requestCount = me.DSS_modelTypes.length;
		var successCount = 0;
		
		Ext.getCmp('DSS_ReportDetail').setWaitFields();
		Ext.getCmp('DSS_SpiderGraphPanel').clearSpiderData(0);// set all fields to zero
		// Disable the save button until all models complete...
		Ext.getCmp('DSS_ScenarioSaveButton').setDisabled(true);

		for (var i = 0; i < me.DSS_modelTypes.length; i++) {
			var request = queryJson;
			request.modelType = me.DSS_modelTypes[i];
			
			var obj = Ext.Ajax.request({
				url: location.href + '/modelCluster',
				jsonData: request,
				timeout: 10 * 60 * 1000, // minutes * seconds * (i.e. converted to) milliseconds
				
				success: function(response, opts) {
					
					try {
						var obj= JSON.parse(response.responseText);
//						console.log("success: ");
//						console.log(obj);
						Ext.getCmp('DSS_ReportDetail').setData(obj);
					}
					catch(err) {
						console.log(err);
					}
					var reportPanel = Ext.getCmp('DSS_report_panel');
					if (reportPanel.getCollapsed() != false) {
						reportPanel.expand();
					}
					requestCount--;
					successCount++;
					if (requestCount <= 0) {
						button.setIcon('app/images/go_icon.png');
						button.setDisabled(false);
						
						// Only enable save button if all models succeed?
						if (successCount >= me.DSS_modelTypes.length) {
							Ext.getCmp('DSS_ScenarioSaveButton').setDisabled(false);
						}
					}
				},
				
				failure: function(respose, opts) {
					requestCount--;
					if (requestCount <=0) {
						button.setIcon('app/images/go_icon.png');
						button.setDisabled(false);
						alert("Model run failed for: '" + request.modelType 
								+ "', request timed out?");
					}
				}
			});
		}
	}
	
});

