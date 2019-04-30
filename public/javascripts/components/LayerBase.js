//-----------------------------------------------------
// DSS.components.LayerBase
//
//-----------------------------------------------------
Ext.define('DSS.components.LayerBase', {
    extend: 'Ext.container.Container',
    alias: 'widget.layerbase',
    alternateClassName: [
        'DSS.Layers',
    ],
    
	layout: {
		type: 'vbox',
		align: 'stretch'
	},
	padding: 4,
	margin: 2,
	style: 'background: #fff; border-radius: 8px; border: 1px solid #ccc',
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
		});
		
		me.callParent(arguments);
		me.insert(0, {xtype: 'container',
			layout: 'hbox',
			items: [{
				xtype: 'component',
				html: me.title,
				flex: 1,
				padding: '2 4',
				style: 'font-weight: bold; color: #2a6e9f; font-size: 1.1em'
			},{
				xtype: 'tool',
				type: 'minimize',
				margin: '0 16',
				callback: function(owner, tool) {
					me.setVisible(false);
					me.DSS_browser.layerHidden(me);
					if (me.cancelClickSelection) {
						me.cancelClickSelection();
					}
				}
				
			}]
		})
	},

});
