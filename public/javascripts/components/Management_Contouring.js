//------------------------------------------------------------------------------
Ext.create('Ext.data.Store', {
	storeId: 'dss-contouring',
	fields: ['index', 'name'],
	data: [{ 
		index: 0, name: 'None', 	
	},{ 
		index: 1, name: 'On contour', 	
	},{ 
		index: 3, name: 'Strip crop',
	}]
});	

//------------------------------------------------------------------------------
Ext.define('DSS.components.Management_Contouring', {
	extend: 'Ext.container.Container',
	padding: '0 0 8 0',
	width: '100%',
	layout: 'fit',
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'combobox',
				itemId: 'dss-data',
				fieldLabel: 'Contouring',
				labelAlign: 'right',
				labelWidth: 80,
				displayField: 'name',
				forceSelection: true,
				store: 'dss-contouring',
				valueField: 'index',
				value: 0, // none
			}]
		});
		
		me.callParent(arguments);
		
	//	this.setFromTransform(this.DSS_Transform);
	},
	
	//--------------------------------------------------------------------------
	setFromTransform: function(transform) {
		
		if (transform && transform.Options && transform.Options.CoverCrop) {
			var coverCrop = this.getComponent('DSS_CoverCrop');
			coverCrop.setValue({'CoverCrop': !transform.Options.CoverCrop.CoverCrop});
		}
	},
	
	//--------------------------------------------------------------------------
	collectChanges: function(transform) {
		
		var obj = {
			CoverCrop: false,
			text: '<b>Cover Crop:</b> '
		};
		
		var tillageType = this.getComponent('DSS_CoverCrop');
		var value = tillageType.getValue()['CoverCrop'];
		console.log(value);
		
		if (value == 0) {
			obj.text += 'Yes';
			obj.CoverCrop = true;
		}
		else if (value == 1) {
			obj.text += 'None';
		}
		
		transform['CoverCrop'] = obj;
		
		return obj;
	}
	
});

