//-----------------------------------------------------
// DSS.components.LayerFarmInteraction
//
//-----------------------------------------------------
Ext.define('DSS.components.LayerFarmInteraction', {
    extend: 'DSS.components.LayerBase',
    alias: 'widget.layer_farm',
    
	title: 'Land Near Farms',
	
	DSS_serverLayer: '$farm', // TEST TEST:  considering whether it helps to have $ denotes custom handling
	DSS_stepSize: 50,
	DSS_greaterThanValue: 250,
	DSS_maxValue: 8000,
	DSS_layerUnit: ' (count)',
	
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'container',
				layout: {
					type: 'vbox',
					align: 'left'
				},
				padding: '0 8 6 0',
				items: [{ 
					// Count of Livestock selection
					xtype: 'container',
					padding: '2 0',
					layout: {
						type: 'hbox',
						align: 'stretch',
						pack: 'start'
					},
					items: [{
						xtype: 'component',
						style: 'text-align: right',
						html: 'Livestock',
						width: 72,
						padding: '3 4',
					},{
						xtype: 'button',
						itemId: 'greaterThanTest',
						width: 34, // would be nice to not have to set a width but changing the button text causes a resize
						text: '>=',
						tooltip: 'Change comparison function',
						handler: function(self) {
							if (self.getText() === '>=') {
								self.setText('>')
							}
							else {
								self.setText('>=')
							}
							Ext.getCmp('DSS_attributeFixMe').valueChanged();
						}
					},{
						xtype: 'numberfield',
						itemId: 'greaterThan',
						hideEmptyLabel: true,
						value: me.DSS_greaterThanValue,
						width: 70,
						step: me.DSS_stepSize,
						minValue: 0,
						maxValue: me.DSS_maxValue,
						listeners: {
							change: function(self, newVal, oldVal) {
								Ext.getCmp('DSS_attributeFixMe').valueChanged();
							}
						}
					},{
						xtype: 'button',
						iconCls: 'swap-icon',
						text: '',
						tooltip: 'Swap values',
						margin: '0 10',
						handler: function(self) {
							var gt = me.down('#greaterThan');
							var lt = me.down('#lessThan');
							var ltv = lt.getValue();
							lt.setRawValue(gt.getValue()); // skip change detection here
							gt.setValue(ltv);			// but do it here to auto trigger a refresh
						}
					},{
						xtype: 'button',
						itemId: 'lessThanTest',
						width: 34,
						text: '<=',
						tooltip: 'Change comparison function',
						handler: function(self) {
							if (self.getText() === '<=') {
								self.setText('<')
							}
							else {
								self.setText('<=')
							}
							Ext.getCmp('DSS_attributeFixMe').valueChanged();
						}
					},{
						xtype: 'numberfield',
						itemId: 'lessThan',
						hideEmptyLabel: true,
						width: 70,
						step: me.DSS_stepSize,
						value: me.DSS_lessThanValue,
						minValue: 0,
						maxValue: me.DSS_maxValue,
						listeners: {
							change: function(self, newVal, oldVal) {
								Ext.getCmp('DSS_attributeFixMe').valueChanged();
							}
						}
					}]
				},{
					xtype: 'component',
					itemId: 'dss-value-range',
					html: 'Value range: -- to -- degrees',
					style: 'color: #777; font-style: italic',
					padding: '2 4 8 80', // to auto center on inputs and not the label in front of those
				},{
					// Livestock types
					xtype: 'radiogroup',
					padding: '2 0',
					itemId: 'livestockType',
					labelAlign: 'right',
					fieldLabel: 'Type',
					labelSeparator: '',
					labelPad: 8,
					labelWidth: 68,
					width: 270,
					columns: 3,
					listeners: {
						change: function(self, newVal, oldVal) {
							Ext.getCmp('DSS_attributeFixMe').valueChanged();
						}
					},
					items: [{
						boxLabel: 'Any',
						inputValue: 'any',
						name: 'farm-livestock-type',
						checked: true
					},{
						boxLabel: 'Dairy',
						inputValue: 'dairy',
						name: 'farm-livestock-type',
					},{
						boxLabel: 'Beef',
						inputValue: 'beef',
						name: 'farm-livestock-type',
					}]
				},{	
					// Land area selection...
					xtype: 'container',
					padding: '2 0',
					layout: {
						type: 'hbox',
						align: 'stretch',
						pack: 'start'
					},
					items: [{
						xtype: 'component',
						style: 'text-align: right',
						html: 'Radius',
						width: 72,
						padding: '3 4',
					},{
						xtype: 'numberfield',
						itemId: 'selectRadius',
						hideEmptyLabel: true,
						value: 600,
						width: 104,
						step: 100,
						minValue: 30,
						maxValue: 5000,
						listeners: {
							change: function(self, newVal, oldVal) {
								Ext.getCmp('DSS_attributeFixMe').valueChanged();
							}
						}
					},{
						xtype: 'button',
						iconCls: 'circle-icon',
						itemId: 'selectShape',
						text: '',
						tooltip: 'Land selection shape (circle)',
						margin: '0 0 0 10',
						toggleGroup: 'dss-selection-shape',
						pressed: true,
						allowDepress: false,
						handler: function(self) {
							Ext.getCmp('DSS_attributeFixMe').valueChanged();
						}
					},{
						xtype: 'button',
						iconCls: 'square-icon',
						toggleGroup: 'dss-selection-shape',
						text: '',
						tooltip: 'Land selection shape (square)',
						allowDepress: false,
						margin: '0 1',
						handler: function(self) {
							Ext.getCmp('DSS_attributeFixMe').valueChanged();
						}
					}]
				}]
			}]
		});
		
		me.callParent(arguments);
		me.requestLayerRange();
	},
	
	//--------------------------------------------------------------------------
	configureSelection: function() {
		
		var me = this;
		if (me.isHidden()) return false;
		
		var selectionDef = { 
				name: me.DSS_serverLayer,
				type: 'continuous',
				lessThanTest: '<=',
				greaterThanTest: '>='
			};

		var gt = me.down('#greaterThan').getValue();
		if (gt) selectionDef['greaterThanValue'] = gt;
		
		var lt = me.down('#lessThan').getValue();
		if (lt) selectionDef['lessThanValue'] = lt;

		selectionDef['greaterThanTest'] = me.down('#greaterThanTest').getText();
		selectionDef['lessThanTest'] = me.down('#lessThanTest').getText();
		selectionDef['radius'] = me.down('#selectRadius').getValue();
		selectionDef['shapeCircle'] = me.down('#selectShape').pressed ? true : false;
		selectionDef['type'] = me.down('#livestockType').getValue()['farm-livestock-type'];
		
		console.log(selectionDef)
        return selectionDef;		
	},
	
	//--------------------------------------------------------------------------
    requestLayerRange: function() {

    	var me = this;
		var queryLayerRequest = { 
			name: me.DSS_serverLayer,
			type: 'layerRange',
		};
    	
		var obj = Ext.Ajax.request({
			url: location.href + '/layerParmRequest',
			jsonData: queryLayerRequest,
			timeout: 10000,
			scope: me,
			
			success: function(response, opts) {
				
				if (response.responseText != '') {
					var obj = JSON.parse(response.responseText);
					if (obj.length == 0 || obj.layerMin == null || obj.layerMax == null) {
						console.log("layer request object return was null?");
						return;
					}
					
					me.down('#greaterThan').setMinValue(Math.floor(obj.layerMin));
					me.down('#lessThan').setMinValue(Math.floor(obj.layerMin));
					
					me.down('#greaterThan').setMaxValue(Math.ceil(obj.layerMax));
					me.down('#lessThan').setMaxValue(Math.ceil(obj.layerMax));

					var rangeLabel = 'Value range: ' + 
								Ext.util.Format.number(obj.layerMin, '0,000.##') +
								' to ' + 
								Ext.util.Format.number(obj.layerMax, '0,000.##') + me.DSS_layerUnit;
	
					me.down('#dss-value-range').setHtml(rangeLabel);
				}
			},
			
			failure: function(response, opts) {
				me.down('#dss-value-range').setHtml('Value range: 5 to 8000 ' + me.DSS_layerUnit);
				console.log('layer request failed');
			}
		});
	},

	//--------------------------------------------------------------------------
	fromQuery: function(queryStep) {
		var me = this;

		me.down('#greaterThan').setValue(queryStep.greaterThanValue);
		me.down('#lessThan').setValue(queryStep.lessThanValue);
		me.down('#greaterThanTest').setText(queryStep.greaterThanTest);
		me.down('#lessThanTest').setText(queryStep.lessThanTest);

		// TODO: set these up
		/*
		selectionDef['radius'] = me.down('#selectRadius').getValue();
		selectionDef['shapeCircle'] = me.down('#selectShape').pressed ? true : false;
		selectionDef['type'] = me.down('#livestockType').getValue();
		*/
		
	}

});
